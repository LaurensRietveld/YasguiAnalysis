package org.data2semantics.yasgui.analysis.helpers;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.data2semantics.yasgui.analysis.EndpointCollection;

public class CkanStats  {
	
	private HashMap<String, Integer> ckanEndpoints;
	
	
	private EndpointCollection collection;
	public CkanStats(EndpointCollection collection) {
		this.collection = collection;
	}
	
	public int getEndpointsSize() {
		return ckanEndpoints.size();
	}
	public int getTotalEndpointsSize() {
		int totalSize = 0;
		for (Integer count: ckanEndpoints.values()) {
			totalSize += count;
		}
		return totalSize;
	}
	public int getTotalEndpointsSize(int minEndpointCount) {
		HashMap<String, Integer> endpoints = getEndpoints(minEndpointCount);
		int totalSize = 0;
		for (Integer count: endpoints.values()) {
			totalSize += count;
		}
		return totalSize;
	}
	
	
	public HashMap<String, Integer> getEndpoints(int minEndpointCount) {
		HashMap<String, Integer> newEndpointsList = new HashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : ckanEndpoints.entrySet()) {
		    if (entry.getValue() >= minEndpointCount) {
		    	newEndpointsList.put(entry.getKey(), entry.getValue());
		    }
		}
		return newEndpointsList;
	}
	public int getEndpointsSize(int minEndpointCount) {
		return getEndpoints(minEndpointCount).size();
	}
	
	public HashMap<String, Integer> getEndpoints() {
		return this.ckanEndpoints;
	}

	public void calc(String name) throws IOException, ParseException {
		getCkanStats();
	}
	
	private void getCkanStats() {
		ckanEndpoints = getCkanEndpoints();
		
	}
	
	public boolean isInCkan(String endpoint) {
		return ckanEndpoints.containsKey(endpoint);
	}
	
	/**
	 * get list of endpoints from ckan, which are in our endpoint selection as well.
	 * @return
	 */
	private HashMap<String, Integer> getCkanEndpoints() {
		org.apache.jena.query.Query query = QueryFactory.create(getCkanQuery());
		QueryEngineHTTP queryExecution = new QueryEngineHTTP("http://semantic.ckan.net/sparql", query);
		HashMap<String, Integer> endpoints = new HashMap<String, Integer>();
		ResultSet resultSet = queryExecution.execSelect();
		while (resultSet.hasNext()) {
			String endpoint = resultSet.next().get("endpoint").toString();
			if (collection.getEndpoints().containsKey(endpoint)) {
				endpoints.put(endpoint, collection.getEndpointCount(endpoint));
			}
		}
		return endpoints;
	}
	private String getCkanQuery() {
		return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" 
				+ "		PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "		PREFIX dcat: <http://www.w3.org/ns/dcat#>\n" 
				+ "		PREFIX dcterms: <http://purl.org/dc/terms/>\n" + "\n"
				+ "		SELECT DISTINCT ?endpoint  {\n"
				+ "		  ?dataset dcat:distribution ?distribution.\n"
				+ "		?distribution dcterms:format ?format.\n"
				+ "		?format rdf:value 'api/sparql'.\n"
				+ "		?distribution dcat:accessURL ?endpoint.\n"
				+ "		}";
	}
	
	public String toString() {
		return "#distinct ckan endpoints: " + ckanEndpoints.size() 
				
				+ " ";
	}
	
	public static void main(String[] args) throws IOException, ParseException {
//		CkanStats ea = new CkanStats(new EndpointCollection());
//		ea.calc("test");
	}
}
