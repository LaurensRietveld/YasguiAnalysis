package org.data2semantics.yasgui.analysis.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.data2semantics.yasgui.analysis.Collection;
import org.data2semantics.yasgui.analysis.EndpointCollection;
import org.data2semantics.yasgui.analysis.Query;

import au.com.bytecode.opencsv.CSVReader;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class AccessibilityStats  {
	public enum EndpointAccessiblityStatus{
		CKAN_ACCESSIBLE, 
		NOT_CKAN_BUT_ACCESSIBLE, 
		PROBABLY_INCORRECT, 
		PRIVATE_ENDPOINT_PUBLIC_DATA, 
		PRIVATE_ENDPOINT_PRIVATE_DATA
	};
	private Map<String, String> prefixesFromPrefixCc = new HashMap<String, String>();
	private CkanStats ckanStats;
	private HashMap<String, EndpointAccessiblityStatus> accessibilityInfo = new HashMap<String, EndpointAccessiblityStatus>();
	private static String CACHE_FILE_SEPARATOR = "#@#@";
	private File cacheFile;
	private FileWriter cacheFileWriter;
	private EndpointCollection endpointCollection;
	private Collection collection;
	public AccessibilityStats(EndpointCollection endpointCollection, Collection collection) {
		this.collection = collection;
		this.endpointCollection = endpointCollection;
		ckanStats = new CkanStats(endpointCollection);
		
	}
	
	
	
	public int getNumEndpoints(EndpointAccessiblityStatus getAccessible) {
		int count = 0;
		for (EndpointAccessiblityStatus accessible: accessibilityInfo.values()) {
			if (accessible == getAccessible) count++;
		}
		return count;
	}
	
	public int getNumEndpoints(EndpointAccessiblityStatus getAccessible, int minEndpointCount) {
		HashMap<String, Integer> endpoints = endpointCollection.getEndpoints();
		int count = 0;
		for (Map.Entry<String, EndpointAccessiblityStatus> entry : accessibilityInfo.entrySet()) {
			String endpoint = entry.getKey();
			EndpointAccessiblityStatus accessible = entry.getValue();
			int endpointCount = endpoints.get(endpoint);
			if (accessible == getAccessible && endpointCount >= minEndpointCount) {
				count++;
			}
		}
		return count;
	}
	
	
	public int getTotalNumEndpoints(EndpointAccessiblityStatus getAccessible) {
		return getTotalNumEndpoints(getAccessible, 0);
	}
	public int getTotalNumEndpoints(EndpointAccessiblityStatus getAccessible, int minEndpointCount) {
		HashMap<String, Integer> endpoints = endpointCollection.getEndpoints();
		int count = 0;
		for (Map.Entry<String, EndpointAccessiblityStatus> entry : accessibilityInfo.entrySet()) {
			String endpoint = entry.getKey();
			EndpointAccessiblityStatus accessible = entry.getValue();
			if (!endpoints.containsKey(endpoint)) {
				throw new IllegalStateException("Could not find endpoints in our accessiblity list. Probably a stale cache. Delete cache file");
			}
			int endpointCount = endpoints.get(endpoint);
			if (accessible == getAccessible && endpointCount >= minEndpointCount) count += endpointCount;
		}
		return count;
	}
	
	public void calc(String name) throws IOException, ParseException {
		this.ckanStats.calc(name);
		fetchPrefixCcPrefixes();
		
		cacheFile = new File("cache/endpoint_accessiblity_" + name + ".tmp");
		if (cacheFile.exists()) {
			loadCacheFile();
		} else {
			cacheFile.createNewFile();
			cacheFileWriter = new FileWriter(cacheFile);
			
			for (Entry<String, Integer> entry : endpointCollection.getEndpoints().entrySet()) {
			    String endpoint = entry.getKey();
			    Integer count = entry.getValue();
			    checkAccessibility(endpoint, count.intValue());
			}
			cacheFileWriter.close();
		}
		
	}
	
	private void fetchPrefixCcPrefixes() throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader("input/prefixcc.csv"), ',', '"',0); 
		String[] line;
		while ((line = csvReader.readNext()) != null) {
			if (line.length != 2) {
				csvReader.close();
				throw new IllegalStateException("could nog load a line from prefix.cc csv");
			}
			prefixesFromPrefixCc.put(line[0], line[1]);
			
		}
		csvReader.close();
	}



	private void loadCacheFile() throws IOException {
		System.out.println("loading endpoint accessibility stats from cache");
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(cacheFile));
		String line;
		while ((line = br.readLine()) != null) {
		   String[] exploded = line.split(CACHE_FILE_SEPARATOR);
		   if (exploded.length > 0) {
			   if (exploded.length != 3) {
				   throw new IOException("unable to read cache file with accessibility stats. affected line: " + line);
			   }
			   String endpoint = exploded[0];
			   boolean accessible = (exploded[1].equals("1")? true: false);
			   int count = Integer.parseInt(exploded[2]);
			   
			   
			   setAccessibility(endpoint, accessible, count);
		   }
		}
		br.close();
	}
	
	public EndpointAccessiblityStatus getAccessibleStatus(String endpoint) {
		if (!accessibilityInfo.containsKey(endpoint)) {
			throw new IllegalStateException("could not find accessibility info for endpoint " + endpoint);
		}
		return accessibilityInfo.get(endpoint);
	}
	
	public boolean checkAccessibility(String endpoint, int count) throws UnknownHostException, IOException {
		boolean accessible = false;
		try {
			com.hp.hpl.jena.query.Query query = QueryFactory.create("SELECT DISTINCT * { ?x ?y ?z} LIMIT 1");
			QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint, query);
			queryExecution.setTimeout(5000, 5000);
			queryExecution.execSelect();
			accessible = true;
		} catch(Exception e) {
			accessible = false;
		}
		setAccessibility(endpoint, accessible, count);
		cacheFileWriter.write(endpoint + CACHE_FILE_SEPARATOR + (accessible? "1": "0") + CACHE_FILE_SEPARATOR + count + "\n");
		return accessible;
	}

	/**
	 * for each of our endpoints, flag them as such:
	 * - when it is in ckan, flag ckan
	 * - when it is not in ckan, but accessible, flag as not_ckan_but_accessible
	 * - when it is not in ckan, not accessible, but only used once, flag as probably_incorrect_endpoint
	 * - when it is not in ckan, not accessible, used more than once, and only contains prefixes from prefix.cc, flag as private_endpoint_public_data
	 * - when it is not in ckan, not accessible, used more than once, and contains unknown prefies, flag as private_endpoint_private_data
	 */
	private void setAccessibility(String endpoint, boolean accessible, int count) {
		EndpointAccessiblityStatus accessibleStatus;
		if (accessible) {
			if (ckanStats.isInCkan(endpoint)) {
				accessibleStatus = EndpointAccessiblityStatus.CKAN_ACCESSIBLE;
			} else {
				accessibleStatus = EndpointAccessiblityStatus.NOT_CKAN_BUT_ACCESSIBLE;
			}
		} else {
			if (count <= 2) {
				accessibleStatus = EndpointAccessiblityStatus.PROBABLY_INCORRECT;
			} else {
				if (endpointQueriesContainCommonPrefixes(endpoint)) {
					accessibleStatus = EndpointAccessiblityStatus.PRIVATE_ENDPOINT_PUBLIC_DATA;
				} else {
					accessibleStatus = EndpointAccessiblityStatus.PRIVATE_ENDPOINT_PRIVATE_DATA;
				}
			}
		}
		accessibilityInfo.put(endpoint, accessibleStatus);
	}
	
	private boolean endpointQueriesContainCommonPrefixes(String endpoint) {
		Set<Query> queries = collection.getQueryCollection().getQueries(endpoint);
		boolean allCommonPrefixes = true;
		for (Query query: queries) {
			Map<String, String> prefixMapping = query.getPrefixMapping().getNsPrefixMap();
			for (String prefixUri: prefixMapping.values()) {
				if (prefixesFromPrefixCc.containsValue(prefixUri)) {
					//correct, it exists!
				} else {
					allCommonPrefixes = false;
					break;
				}
			}
		}
		
		return allCommonPrefixes;
	}

	
	
	public static void main(String[] args) throws IOException, ParseException {
//		Collection collection = new Collection("test");
//		collection.
		
//		AccessibilityStats ea = new AccessibilityStats(new EndpointCollection());
//		ea.fetchPrefixCcPrefixes();
	}

}
