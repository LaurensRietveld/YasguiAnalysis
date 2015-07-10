package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.csv.CSVRecord;
import org.data2semantics.yasgui.analysis.Collection;
import org.data2semantics.yasgui.analysis.Query;
import org.data2semantics.query.filters.QueryFilter;
import org.data2semantics.yasgui.analysis.filters.EndpointFilter;

import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.expr.ExprException;


public abstract class Loader {
	
	protected Collection collection;
	protected AnalysisSetup analysisSetup;
	protected int invalidQueries = 0;
	protected int validQueries = 0;
	protected int filteredQueries = 0;
	protected ArrayList<QueryFilter> queryFilters;
	protected ArrayList<EndpointFilter> endpointFilters;
	protected boolean bypassCache;
	public Loader(AnalysisSetup analysisSetup) throws IOException {
		this.analysisSetup = analysisSetup;
		collection = new Collection(analysisSetup.getName());
		this.queryFilters = analysisSetup.getQueryFilters();
		this.endpointFilters = analysisSetup.getEndpointFilters();
	}
	
	public abstract void load(AnalysisSetup.Input inputType) throws IOException;
	public void load(boolean bypassCache, boolean runOptionalOptimizationTest, boolean runCoverageAnalysis) throws IOException, ParseException, URISyntaxException {
		this.bypassCache = bypassCache;
		for (AnalysisSetup.Input inputType: analysisSetup.getInputTypes()) {
			load(inputType);
		}
		collection.calcAggregatedStats(runOptionalOptimizationTest, runCoverageAnalysis);
	}
	public Collection getCollection() {
		return this.collection;
	}
	protected boolean checkQueryFilters(Query query, int queryCount) {
		boolean passed = true;
		if (queryFilters != null) {
			try {
				for (QueryFilter filter : queryFilters) {
					if (filter.filter(query)) {
						passed = false;
						collection.getQueryCollection().filteredQueries.add(queryCount);
						break;
					}
				}
			} catch (Exception e) {
				System.out.println(query.toString());
				e.printStackTrace();
				System.exit(1);
			}
		}
		return passed;
	}
	
	protected boolean checkEndpointFilters(String endpoint) {
		boolean passed = true;
		if (endpointFilters != null) {
			for (EndpointFilter filter : endpointFilters) {
				if (filter.filter(endpoint)) {
					passed = false;
					break;
				}
			}
		}
		return passed;
	}
	protected Query getParsedAndFilteredQuery(String queryString) {
	    return getParsedAndFilteredQuery(queryString, 1);
	}
	
	protected Query getParsedAndFilteredQuery(String queryString, int queryCount) {
		Query query = null;
		try {
			query = Query.create(queryString, collection.getQueryCollection());
		} catch (QueryParseException e){
			//unable to parse query. invalid!
			
//			String testString = queryString.replace("#>", "");
			if (queryString.contains("#")) {
			    collection.getQueryCollection().queriesWithComment.add(queryCount);
			} else {
			    collection.getQueryCollection().invalidQueries.add(queryCount);
			}
		} catch (QueryBuildException e) {
			//e.g. 'duplicate variable in result projection'
			collection.getQueryCollection().invalidQueries.add(queryCount);
		} catch (ExprException e){
			//unable to parse regex in query. invalid!
			collection.getQueryCollection().invalidQueries.add(queryCount);
		} catch (PatternSyntaxException e){
		    //unable to parse regex in query. invalid!
		    collection.getQueryCollection().invalidQueries.add(queryCount);
		} catch (QueryException e){
		    collection.getQueryCollection().invalidQueries.add(queryCount);
		} catch (Exception e) {
			System.out.println("failed to parse query string: " + queryString);
			e.printStackTrace();
			System.exit(1);
		}
		if (query != null && !checkQueryFilters(query, queryCount)) query = null;
		return query;
	}
	
	protected boolean validColumn(CSVRecord line, int column) {
		return (column >= 0 && column < line.size());
	}
//	protected boolean validColumn(String[] line, int column) {
//		return (column >= 0 && column < line.length);
//	}

}
