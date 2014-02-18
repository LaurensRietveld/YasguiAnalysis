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

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

public class QueryCollection<E extends Query> extends org.data2semantics.query.QueryCollection<E> {
	private Collection collection;

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
	
	public void calcExpensiveStats() {
		calcOptionalOptimizationStats();
	}
	
	public String[] getDetailedInfoForQuery(E query) {
		String[] detailedInfo = super.getDetailedInfoForQuery(query);
		List<String> list = new LinkedList<String>(Arrays.asList(detailedInfo));
		String val = "-1";
		if (query.optionalNeeded != null) {
			val = query.optionalNeeded? "0": "1";
		}
		list.add(val);
		return list.toArray(new String[list.size()]);
	}
	public String[] getDetailedInfoHeader() {
		String[] detailedInfo = super.getDetailedInfoHeader();
		List<String> list = new LinkedList<String>(Arrays.asList(detailedInfo));
		list.add("uses not required optional (slow)");
		return list.toArray(new String[list.size()]);
	}
	
	
	private void calcOptionalOptimizationStats() {
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
				if (query.isSelectType() && query.hasOptionals()) {
					try {
						//execute the query, and see whether the number of bindings differ between query solutions. 
						//If they dont, then we wouldnt need the optionals!
						QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
						ResultSet result = queryExecution.execSelect();
						List<String> vars = result.getResultVars();
						Integer previousNumBindings = null;
						boolean optionalNeeded = false;
						while (result.hasNext()) {
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
								optionalNeeded = true;
								break;
							}
						}
						query.setOptionalNeeded(optionalNeeded);
					} catch (QueryExceptionHTTP e) {
						System.out.println(e.getClass().getName());
						//hmph. just ignore. can be cases where query execution takes a long time and stuff
					}
					
					
				}
			}
		}
	}
}
