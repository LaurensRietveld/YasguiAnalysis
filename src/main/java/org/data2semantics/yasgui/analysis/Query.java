package org.data2semantics.yasgui.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.web.HttpException;
import org.data2semantics.query.QueryCollection;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.sparql.syntax.Template;


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
	
	public Query getAsConstructQuery() throws IOException {
		Query constructQuery = Query.create(toString());
		constructQuery.setQueryConstructType();

		final BasicPattern constructBp = new BasicPattern();

		Element queryPattern = getQueryPattern();
		ElementWalker.walk(queryPattern, new ElementVisitorBase() {
			public void visit(ElementPathBlock el) {
				Iterator<TriplePath> triples = el.patternElts();
				while (triples.hasNext()) {
					constructBp.add(triples.next().asTriple());
				}
			}
		});
		constructQuery.setConstructTemplate(new Template(constructBp));
		return constructQuery;
	}

	public Set<String> getUsedTriplesFromConstruct(String endpoint) throws IOException {
		Set<String> triples = new HashSet<String>();
		try {
			Query query = getAsConstructQuery();
			QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
			queryExecution.setTimeout(5, TimeUnit.MINUTES);
			Iterator<Triple> constructResult = queryExecution.execConstructTriples();
			while (constructResult.hasNext()) {
				triples.add(constructResult.next().toString());
			}
		} catch (Exception e) {
			System.out.print("e");
//			System.out.println(e.getClass().getName());
//			System.out.println(e.getMessage());
		}
		return triples;
	}
	
	public static void main(String[] args) throws IOException {
		Query query = Query.create("SELECT * {?x ?yu ?j} LIMIT 10");
		System.out.println(query.getUsedTriplesFromConstruct("http://dbpedia.org/sparql").toString());
	}
}
