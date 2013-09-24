package org.data2semantics.yasgui.analysis;

import java.io.IOException;
import java.text.ParseException;

import org.data2semantics.query.QueryCollection;


public class Collection {
	public static String PATH_CSV_RESULTS = "output/";
	private String name;
	private QueryCollection queryCollection;
	private EndpointCollection endpointCollection = new EndpointCollection();
	public Collection(String name) throws IOException {
		queryCollection = new QueryCollection();
		this.name = name;
	}
	
	
	public QueryCollection getQueryCollection() {
		return queryCollection;
	}


	public void setQueryCollection(QueryCollection queryCollection) {
		this.queryCollection = queryCollection;
	}


	public EndpointCollection getEndpointCollection() {
		return endpointCollection;
	}


	public void setEndpointCollection(EndpointCollection endpointCollection) {
		this.endpointCollection = endpointCollection;
	}
	
	
	public void toCsv() throws IOException {
		queryCollection.toCsv(name);
		endpointCollection.toCsv(name);
	}
	public void calcAggregatedStats() throws IOException, ParseException {
		queryCollection.calcAggregatedStats();
		endpointCollection.calcAggregatedStats(name);
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
}
