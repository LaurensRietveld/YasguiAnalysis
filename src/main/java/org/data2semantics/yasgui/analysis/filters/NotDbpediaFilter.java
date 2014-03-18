package org.data2semantics.yasgui.analysis.filters;

public class NotDbpediaFilter implements EndpointFilter {
	public boolean filter(final String endpoint) {
		return endpoint.equals("http://dbpedia.org/sparql") || endpoint.equals("http://live.dbpedia.org/sparql");
	}
}
