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

import org.data2semantics.yasgui.analysis.EndpointCollection;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class AccessibilityStats extends AnalysisHelper {
	
	private HashMap<String, Boolean> accessibilityInfo = new HashMap<String, Boolean>();
//	public int numAccessibleEndpoints = 0;
//	public int numInaccessibleEndpoints = 0;
//	public int numTotalAccessibleEndpoints = 0;
//	public int numTotalInaccessibleEndpoints = 0;
	private static String CACHE_FILE_SEPARATOR = "#@#@";
	private File cacheFile;
	private FileWriter cacheFileWriter;
	public AccessibilityStats(EndpointCollection collection) {
		super(collection);
	}
	
	public int getNumEndpoints(boolean getAccessible) {
		int count = 0;
		for (Boolean accessible: accessibilityInfo.values()) {
			if (accessible == getAccessible) count++;
		}
		return count;
	}
	
	public int getNumEndpoints(boolean getAccessible, int minEndpointCount) {
		HashMap<String, Integer> endpoints = collection.getEndpoints();
		int count = 0;
		for (Map.Entry<String, Boolean> entry : accessibilityInfo.entrySet()) {
			String endpoint = entry.getKey();
			Boolean accessible = entry.getValue();
			int endpointCount = endpoints.get(endpoint);
			if (accessible == getAccessible && endpointCount >= minEndpointCount) {
				count++;
			}
		}
		return count;
	}
	
	
	public int getTotalNumEndpoints(boolean getAccessible) {
		return getTotalNumEndpoints(getAccessible, 0);
	}
	public int getTotalNumEndpoints(boolean getAccessible, int minEndpointCount) {
		HashMap<String, Integer> endpoints = collection.getEndpoints();
		int count = 0;
		for (Map.Entry<String, Boolean> entry : accessibilityInfo.entrySet()) {
			String endpoint = entry.getKey();
			Boolean accessible = entry.getValue();
			if (!endpoints.containsKey(endpoint)) {
				throw new IllegalStateException("Could not find endpoints in our accessiblity list. Probably a stale cache. Delete cache file");
			}
			int endpointCount = endpoints.get(endpoint);
			if (accessible == getAccessible && endpointCount >= minEndpointCount) count += endpointCount;
		}
		return count;
	}
	
	public void calc(String name) throws IOException, ParseException {
		EndpointCollection endpointCollection = collection;
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
	
	public boolean isAccessible(String endpoint) {
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
	private void setAccessibility(String endpoint, boolean accessible, int count) {
		accessibilityInfo.put(endpoint, accessible);
		
	}
	
	
	
	public static void main(String[] args) throws IOException, ParseException {
		AccessibilityStats ea = new AccessibilityStats(new EndpointCollection());
		ea.checkAccessibility("http://mac311.few.vu.nl:8000/openrdf-sesame/nci", 2);
	}
}
