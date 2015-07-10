package org.data2semantics.yasgui.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.data2semantics.yasgui.analysis.helpers.AccessibilityStats.EndpointAccessiblityStatus;
import org.data2semantics.yasgui.query.helpers.Counter;

import au.com.bytecode.opencsv.CSVWriter;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class QueryCollection<E extends Query> extends org.data2semantics.query.QueryCollection<E> {
	private Collection collection;
	private boolean queryOptimizationTestExecuted = false;
	private Counter optionalNeededCount = new Counter();
	private Counter optionalNeededCountDistinct = new Counter();
	public QueryCollection(Collection collection) throws IOException {
		super();
		this.collection = collection;
	}
	
	public HashMap<String, Double> getNormalizedNamespaceUsage(String endpoint) {
		HashMap<String,Double> normalizedNamespaces = new HashMap<String, Double>();
		List<String> prefixes = new ArrayList<String>();
		Set<E> queries = getQueries(endpoint);
		for (Query query: queries) {
			prefixes.addAll(query.getPrefixMapping().getNsPrefixMap().values());
		}
		Iterator<String> prefixesIt = prefixes.iterator();
		//remove most common prefixes (i.e. rdf and rdfs)
		while (prefixesIt.hasNext()) {
		   String prefix = prefixesIt.next(); 
		   if (prefix.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#") || prefix.equals("http://www.w3.org/2000/01/rdf-schema#")) {
			   prefixesIt.remove();
		   }
		}
		
		Map<String, Integer> counts = new HashMap<String, Integer>();
		for (String temp : prefixes) {
			Integer count = counts.get(temp);
			counts.put(temp, (count == null) ? 1 : count + 1);
		}
		
		//we have the counts, now normalize them
		int numQueries = queries.size();
		for (Entry<String, Integer> prefixCount: counts.entrySet()) {
			normalizedNamespaces.put(prefixCount.getKey(), (double)prefixCount.getValue() / (double)numQueries);
		}
		
		
		return normalizedNamespaces;
	}
	
	public void calcOptionalOptimizationTest() throws IOException {
		queryOptimizationTestExecuted  = true;
		calcOptionalOptimizationStats();
	}
	
	public String[] getDetailedInfoForQuery(E query) {
		String[] detailedInfo = super.getDetailedInfoForQuery(query);
		
		if (queryOptimizationTestExecuted) {
			List<String> list = new LinkedList<String>(Arrays.asList(detailedInfo));
			String val = "-1";
			if (query.optionalNeeded != null) {
				val = query.optionalNeeded? "1": "0";
			}
			list.add(val);
			detailedInfo = list.toArray(new String[list.size()]);
		}
		return detailedInfo;
	}
	public String[] getDetailedInfoHeader() {
		String[] detailedInfo = super.getDetailedInfoHeader();
		if (queryOptimizationTestExecuted) {
			List<String> list = new LinkedList<String>(Arrays.asList(detailedInfo));
			list.add("uses required optional (slow)");
			detailedInfo = list.toArray(new String[list.size()]);
		}
		return detailedInfo;
	}
	
	public void calcAggregatedStats() throws IOException {
		System.out.println("calc aggregated query stats");
		super.calcAggregatedStats();
		if (queryOptimizationTestExecuted) {
			for (E query: queries.values()) {
				if (query.optionalNeeded != null && query.optionalNeeded == true) {
					optionalNeededCountDistinct.increase();
					optionalNeededCount.add(query.getCount());
				}
			}
		}
	}
	
	public int getQueryCountWithOptionalNeededAnalysis(boolean distinct) {
		System.out.println("get optional needed q count");
		int count = 0;
		for (E query: queries.values()) {
			if (query.optionalNeeded != null) {
				if (distinct) {
					count++;
				} else {
					count += query.getCount();
				}
			}
		}
		return count;
	}
	
	public void writeSummaryCsvRows(CSVWriter writer) {
		super.writeSummaryCsvRows(writer);
		if (queryOptimizationTestExecuted) {
			writeCsvRow(writer,"uses required optional (relative to other queries with optionals)", optionalNeededCountDistinct, getQueryCountWithOptionalNeededAnalysis(true), optionalNeededCount, getQueryCountWithOptionalNeededAnalysis(false));
		}
	}
	private void calcOptionalOptimizationStats() throws IOException {
		System.out.println("calculating optional optimization stats");
		int needOptionalCount = 0;
		int checkOptionalCount = 0;
		for (E query: queries.values()) {
			
			//we only want to test queries for endpoints we can reach
			String endpoint = null;
			for (String queryEndpoint: query.getEndpoints()) {
				EndpointAccessiblityStatus status = collection.getEndpointCollection().getAccessibilityStats().getAccessibleStatus(queryEndpoint);
				if (status == EndpointAccessiblityStatus.CKAN_ACCESSIBLE || status == EndpointAccessiblityStatus.NOT_CKAN_BUT_ACCESSIBLE) {
					endpoint = queryEndpoint;
					break;
				}
			}
			
			
			if (endpoint != null) {
//			    System.out.println(endpoint);
//			    System.exit(1);
				if (query.isSelectType() && query.hasOptionals()) {
					try {
						//execute the query, and see whether the number of bindings differ between query solutions. 
						//If they dont, then we wouldnt need the optionals!
						
//						endpoint = "http://ops.few.vu.nl:8890/sparql";
						
//						QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query.getQueryWithFromClause("http://dbpedia"));
						QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
						ResultSet result = queryExecution.execSelect();
						List<String> vars = result.getResultVars();
						Integer previousNumBindings = null;
						boolean optionalNeeded = false;
						boolean firstResults = true;
						while (result.hasNext()) {
							if (firstResults) {
								checkOptionalCount++;
								firstResults = false;
							}
							int numBindingsInQs = 0;
							QuerySolution solution = result.next();
							for (String var: vars) {
								if (solution.contains(var)) numBindingsInQs++;
							}
							if (previousNumBindings == null) {
								//first time
								previousNumBindings = numBindingsInQs;
							} else if (previousNumBindings != numBindingsInQs) {
								//different number of bindings. We need the optional
								needOptionalCount++;
								optionalNeeded = true;
								break;
							}
						}
						query.setOptionalNeeded(optionalNeeded);
					} catch (QueryExceptionHTTP e) {
//						System.out.println(e.getClass().getName());
						System.out.println(e.getMessage());
						//hmph. just ignore. can be cases where query execution takes a long time and stuff
					}
					
					
				}
			}
		}
		System.out.println("" + needOptionalCount);
		System.out.println("" + checkOptionalCount);
	}
}
