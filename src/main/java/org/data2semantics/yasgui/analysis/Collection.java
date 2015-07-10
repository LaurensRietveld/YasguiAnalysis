package org.data2semantics.yasgui.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.data2semantics.yasgui.analysis.helpers.AccessibilityStats.EndpointAccessiblityStatus;

import au.com.bytecode.opencsv.CSVWriter;




public class Collection {
	public static String PATH_CSV_RESULTS = "output/";
	private String name;
	private QueryCollection<Query> queryCollection;
	private EndpointCollection endpointCollection = new EndpointCollection(this);
	public Collection(String name) throws IOException {
		queryCollection = new QueryCollection<Query>(this);
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
	
	
	public void toCsv() throws IOException, URISyntaxException {
		queryCollection.toCsv(name);
		endpointCollection.toCsv(name);
		generateLodPictureCsvLinkingNamespacesToEndpoints();
		generateLodPictureCsvGroupingNamespaces();
		generateLodPictureCsvLinkingNsAndEndpointsAsVertices();
		generateLodPictureCsvOnlyNamespaces();
	}
	
	
	
	public void calcAggregatedStats(boolean runOptionalOptimizationTest, boolean runCoverageAnalysis) throws IOException, ParseException {
		endpointCollection.calcAggregatedStats(name);
		if (runOptionalOptimizationTest)	queryCollection.calcOptionalOptimizationTest();
		if (runCoverageAnalysis) endpointCollection.runCoverageAnalysis(this);
		queryCollection.calcAggregatedStats();
	}
	
	



	private boolean includeEndpointInLodPicture(String endpoint) {
		int minNumQueries = 10;
		return (endpointCollection.getAccessibilityStats().getAccessibleStatus(endpoint) == EndpointAccessiblityStatus.CKAN_ACCESSIBLE
				|| endpointCollection.getAccessibilityStats().getAccessibleStatus(endpoint) == EndpointAccessiblityStatus.NOT_CKAN_BUT_ACCESSIBLE
				|| endpointCollection.getEndpoints().get(endpoint) >= minNumQueries);
	}
	
	/**
	 * detect domain name of prefixes, and detect host name of endpoints. If they match, draw a link between both
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void generateLodPictureCsvLinkingNamespacesToEndpoints() throws URISyntaxException, IOException {
		CSVWriter endpointWeightWriter = new CSVWriter(new FileWriter(new File(Collection.PATH_CSV_RESULTS + name + "/lod_endpointSizes.csv")), ';');
		CSVWriter endpointEdgeWriter = new CSVWriter(new FileWriter(new File(Collection.PATH_CSV_RESULTS + name + "/lod_edgelistMappingNsAsEndpoint.csv")), ';');
		HashMap<String, String> endpointEdgeList = new HashMap<String, String>();
		for (String endpoint: endpointCollection.getEndpoints().keySet()) {
			if (includeEndpointInLodPicture(endpoint)) {
				endpointWeightWriter.writeNext(new String[]{endpoint, Integer.toString(endpointCollection.getEndpointCount(endpoint))});
				Set<Query> queries = queryCollection.getQueries(endpoint);
				for (Query query: queries) {
					Map<String, String> prefixMapping = query.getPrefixMapping().getNsPrefixMap();
					for (String prefixUri: prefixMapping.values()) {
						URI uri = new URI(prefixUri);
					    String domain = uri.getHost();
					    if (domain == null) continue;
					    HashMap<String, Integer> endpointForDomain = endpointCollection.getEndpointForDomain(domain);
					    if (endpointForDomain != null) {
					    	//we have a link to another endpoint!
					    	String endpointForDomainInstance = endpointForDomain.keySet().iterator().next();
					    	if (!endpointForDomainInstance.equals(endpoint) && includeEndpointInLodPicture(endpointForDomainInstance)) {
					    		endpointEdgeList.put(endpoint, endpointForDomainInstance);
//					    		endpointEdgeWriter.writeNext(new String[]{endpoint, endpointForDomainInstance, Integer.toString(endpointCollection.getEndpointCount(endpoint))});
					    	}
					    }
					}
				}
			}
		}
		
		for (Entry<String, String> entry: endpointEdgeList.entrySet()) {
			endpointEdgeWriter.writeNext(new String[]{entry.getKey(), entry.getValue(), Integer.toString(endpointCollection.getEndpointCount(entry.getKey()))});
		}
		endpointEdgeWriter.close();
		endpointWeightWriter.close();
	}
	
	/**
	 * use endpoints AND namespaces as vertices. Draw an edge when: more than x percent of the queries of endpoint x use that namespace
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void generateLodPictureCsvLinkingNsAndEndpointsAsVertices() throws URISyntaxException, IOException {
		CSVWriter endpointEdgeWriter = new CSVWriter(new FileWriter(new File(Collection.PATH_CSV_RESULTS + name + "/lod_edgeListEndpoints2Ns.csv")), ';');
		double threshold = 0.0;
		
		for (String endpoint: endpointCollection.getEndpoints().keySet()) {
			if (includeEndpointInLodPicture(endpoint)) {
				Map<String, Integer> endpointPrefixes = new HashMap<String, Integer>();
				Set<Query> queries = queryCollection.getQueries(endpoint);
				for (Query query: queries) {
					for (String prefixUri: query.getPrefixMapping().getNsPrefixMap().values()) {
						if (!endpointPrefixes.containsKey(prefixUri)) endpointPrefixes.put(prefixUri, 0);
						endpointPrefixes.put(prefixUri, endpointPrefixes.get(prefixUri) + 1);
					}
				}
				
				int numQueries = queries.size();
				for (String prefixUri: endpointPrefixes.keySet()) {
					Integer prefixCount = endpointPrefixes.get(prefixUri);
					if (((double) prefixCount / (double) numQueries) > threshold) {
						endpointEdgeWriter.writeNext(new String[]{endpoint, prefixUri});
					}
				}
				
			}
		}
		endpointEdgeWriter.close();
	}
	
	/**
	 * for each endpoint, group the prefixes of all queries. if a prefix is defined in more than <threshold>% for endpoint 1, and the same for endpoint 2, then draw an edge 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void generateLodPictureCsvGroupingNamespaces() throws URISyntaxException, IOException {
		CSVWriter endpointEdgeWriter = new CSVWriter(new FileWriter(new File(Collection.PATH_CSV_RESULTS + name + "/lod_edgeListEndpointsGroupedByNs.csv")), ';');
//		HashMap<String, String> endpointEdgeList = new HashMap<String, String>();
		Map<String, HashMap<String, Double>> prefixDistPerEndpoint = new HashMap<String, HashMap<String, Double>>();
		for (String endpoint: endpointCollection.getEndpoints().keySet()) {
			if (includeEndpointInLodPicture(endpoint)) {
				prefixDistPerEndpoint.put(endpoint, queryCollection.getNormalizedNamespaceUsage(endpoint));
			}
		}
		
		//now create mappings between endpoints (undirected!)
		double threshold = 0.0;
		Map<String, String> edgeList = new HashMap<String, String>();
		for (Entry<String, HashMap<String, Double>> entry: prefixDistPerEndpoint.entrySet()) {
			String endpoint = entry.getKey();
			for (String prefixUri: getPrefixesForEndpointAboveThreshold(entry.getValue(), threshold)) {
				for (String targetEndpoint: getEndpointsWithPrefixesAboveThreshold(prefixDistPerEndpoint, prefixUri, threshold)) {
					edgeList.put(endpoint, targetEndpoint);
				}
			}
			
		}
		
		for (Entry<String, String> entry: edgeList.entrySet()) {
			if (entry.getKey().equals(entry.getValue())) continue; //same node
			if (entry.getKey().equals(edgeList.get(entry.getValue()))) continue;//same as inverse. ignore
			endpointEdgeWriter.writeNext(new String[]{entry.getKey(), entry.getValue()});
		}
		endpointEdgeWriter.close();
	}
	/**
	 * only draw links between namespaces, and ignore endpoint dimension. For each query, make all possible combinations of namespaces. Over all queries, calculate the frequency of each combination. (i.e. the edge weight)
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void generateLodPictureCsvOnlyNamespaces() throws URISyntaxException, IOException {
		CSVWriter nsEdgeWriter = new CSVWriter(new FileWriter(new File(Collection.PATH_CSV_RESULTS + name + "/lod_edgeListNamespacesUsedInCombination.csv")), ';');
		CSVWriter nsFrequencies = new CSVWriter(new FileWriter(new File(Collection.PATH_CSV_RESULTS + name + "/nsFrequencies.csv")), ';');
		
		
		Set<Query> queries= new HashSet<Query>(queryCollection.getQueries());
		Map<String, Integer> totalPrefixOccurances = new HashMap<String, Integer>();
		HashMap<String, Integer> prefixCombinationCounts = new HashMap<String, Integer>();
		for (Query query: queries) {
			List<String> prefixUris = new ArrayList<String>(query.getPrefixMapping().getNsPrefixMap().values());
			Collections.sort(prefixUris);
			/**
			 * calc uri combinations
			 */
			for (int lhsIt = 0; lhsIt < prefixUris.size(); lhsIt++) {
				String lhsUri = prefixUris.get(lhsIt);
				for (int rhsIt = lhsIt + 1; rhsIt < prefixUris.size(); rhsIt++) {
					String rhsUri = prefixUris.get(rhsIt);
					if (lhsUri.equals(rhsUri)) continue;
					String prefixCombiString = lhsUri + "@#@#@#" + rhsUri;
					if (!prefixCombinationCounts.containsKey(prefixCombiString)) prefixCombinationCounts.put(prefixCombiString, 0);
					prefixCombinationCounts.put(prefixCombiString, prefixCombinationCounts.get(prefixCombiString) + 1);
				}
			}
			/**
			 * add to total uri count
			 */
			for (String prefixUri: query.getPrefixMapping().getNsPrefixMap().values()) {
				if (!totalPrefixOccurances.containsKey(prefixUri)) totalPrefixOccurances.put(prefixUri, 0);
				//don't use distinct queries here!
				totalPrefixOccurances.put(prefixUri, totalPrefixOccurances.get(prefixUri) + query.getCount());
			}
		}
		
		/**
		 * write uri combinations
		 */
		for (Entry<String, Integer> entry: prefixCombinationCounts.entrySet()) {
			String[] splittedUris = entry.getKey().split("@#@#@#");
			nsEdgeWriter.writeNext(new String[]{splittedUris[0], splittedUris[1], Integer.toString(entry.getValue())});
		}
		
		/**
		 * write total uri combinations
		 */
		for (Entry<String, Integer> entry: totalPrefixOccurances.entrySet()) {
			nsFrequencies.writeNext(new String[]{entry.getKey(), Integer.toString(entry.getValue())});
		}
		nsEdgeWriter.close();
		nsFrequencies.close();
	}
	
	private Set<String> getPrefixesForEndpointAboveThreshold(HashMap<String, Double> prefixDist, double threshold) {
		Set<String> prefixUris = new HashSet<String>();
		for (String prefix: prefixDist.keySet()) {
			if (prefixDist.get(prefix) >= threshold) {
				prefixUris.add(prefix);
			}
		}
		return prefixUris;
	}
	private Set<String> getEndpointsWithPrefixesAboveThreshold(Map<String, HashMap<String, Double>> prefixDistPerEndpoint, String prefixToTest, double threshold) {
		Set<String> endpoints = new HashSet<String>();
		for (Entry<String, HashMap<String, Double>> entry: prefixDistPerEndpoint.entrySet()) {
			String endpoint = entry.getKey();
			if (entry.getValue().containsKey(prefixToTest) && entry.getValue().get(prefixToTest) > threshold) {
				endpoints.add(endpoint);
			}
		}
		return endpoints;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
	
//	public static void main(String[] args) throws IOException {
//		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
//				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
//				"PREFIX abc: <http://www.metadata.net/harmony/ABCSchemaV5Commented.rdf#>\n" + 
//				"PREFIX acl: <http://www.w3.org/ns/auth/acl#>\n" + 
//				"\n" + 
//				"SELECT * WHERE {\n" + 
//				"  ?sub ?pred ?obj\n" + 
//				"} LIMIT 10";
//		
//		Query query = Query.create(queryString);
//		System.out.println(Sets.powerSet(new HashSet<String>(query.getPrefixMapping().getNsPrefixMap().values())));				
//	}
}
