package org.data2semantics.yasgui.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.Template;
import org.data2semantics.query.QueryCollection;


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
	     //set base uri to something. If no base uri is set, and there are relative uris, query throws exception (which I cannot catch.. (separate thread)).
        if ( constructQuery.getBaseURI() == null ) constructQuery.setBaseURI("http://blaaaaaaat");
		return constructQuery;
	}

	public Set<String> getUsedTriplesFromConstruct(String endpoint) throws IOException {
		Set<String> triples = new HashSet<String>();
		try {
			Query query = getAsConstructQuery();
//			query.setBaseURI("http://bla");
//			System.out.println(query.toString());
			QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
//			queryExecution.
			queryExecution.setTimeout(3, TimeUnit.MINUTES, 3, TimeUnit.MINUTES);
			Iterator<Triple> constructResult = queryExecution.execConstructTriples();
			while (constructResult.hasNext()) {
				triples.add(constructResult.next().toString());
			}
		} catch (Throwable e) {
			System.out.print("e");
//			System.out.println(e.getClass().getName());
//			System.out.println(e.getMessage());
		}
		return triples;
	}
	
	public static void main(String[] args) throws IOException {
		Query query = Query.create("PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
		        "PREFIX  dbpedia: <http://dbpedia.org/resource/>\n" + 
		        "PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" + 
		        "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
		        "\n" + 
		        "SELECT DISTINCT  ?area ?ans\n" + 
		        "WHERE\n" + 
		        "  { ?area ?prop ?ans .\n" + 
		        "    ?area rdf:type owl:Class\n" + 
		        "  }\n" + 
		        "LIMIT   100");
		System.out.println(query.getUsedTriplesFromConstruct("http://dbpedia.org/sparql").toString());
	}
}
