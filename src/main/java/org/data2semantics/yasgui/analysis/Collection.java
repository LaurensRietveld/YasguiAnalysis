package org.data2semantics.yasgui.analysis;

import java.io.IOException;
import java.text.ParseException;

import org.data2semantics.query.Query;
import org.data2semantics.query.QueryCollection;



public class Collection {
	public static String PATH_CSV_RESULTS = "output/";
	private String name;
	private QueryCollection<Query> queryCollection;
	private EndpointCollection endpointCollection = new EndpointCollection();
	public Collection(String name) throws IOException {
		queryCollection = new QueryCollection<Query>();
		this.name = name;
	}
	
	
	public QueryCollection<Query> getQueryCollection() {
		return queryCollection;
	}


	public void setQueryCollection(QueryCollection<Query> queryCollection) {
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
		endpointCollection.calcAggregatedStats(name, this);
		calcClosedEndpointStats();
	}
	
	/**
	 * for each of our endpoints, flag them as such:
	 * - when it is in ckan, flag ckan
	 * - when it is not in ckan, but accessible, flag as not_ckan_but_accessible
	 * - when it is not in ckan, not accessible, but only used once, flag as probably_incorrect_endpoint
	 * - when it is not in ckan, not accessible, used more than once, and only contains prefixes from prefix.cc, flag as private_endpoint_public_data
	 * - when it is not in ckan, not accessible, used more than once, and contains unknown prefies, flag as private_endpoint_private_data
	 */
	private void calcClosedEndpointStats() {
		for (String endpoint: endpointCollection.getEndpoints().keySet()) {
			
		}
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
}
