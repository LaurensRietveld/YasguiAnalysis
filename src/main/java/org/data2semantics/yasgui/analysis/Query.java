package org.data2semantics.yasgui.analysis;

import java.io.IOException;

import org.data2semantics.query.QueryCollection;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;


public class Query extends org.data2semantics.query.Query {
	public Boolean optionalNeeded = null;
	public static Query create(String queryString, QueryCollection<? extends Query> queryCollection) {
		Query query = new Query();
		query = (Query)(QueryFactory.parse(query, queryString, null, Syntax.defaultQuerySyntax));
		query.setQueryCollection(queryCollection);
		query.generateQueryStats();
		return query;
	}
	
	public static Query create(String queryString) throws IOException {
		
		return create(queryString, new QueryCollection<Query>());
	}
	public void setOptionalNeeded(boolean optionalNeeded) {
		this.optionalNeeded = optionalNeeded;
	}
}
