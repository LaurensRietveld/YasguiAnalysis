package org.data2semantics.yasgui.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.data2semantics.yasgui.analysis.helpers.AccessibilityStats;
import org.data2semantics.yasgui.analysis.helpers.AccessibilityStats.EndpointAccessiblityAnalysis;

import au.com.bytecode.opencsv.CSVWriter;

public class EndpointCollection {
//	
	private HashMap<String, Integer> endpoints = new HashMap<String, Integer>();
	
//	private CkanStats ckanStats = new CkanStats(this);
	private AccessibilityStats accessibilityStats = new AccessibilityStats(this);
	
	public EndpointCollection() {
		
	}
	
	
	public void addEndpoint(String endpoint) {
		addEndpoint(endpoint, 1);
	}
	
	public void addEndpoint(String endpoint, int count) {
		if (endpoints.containsKey(endpoint)) count += endpoints.get(endpoint).intValue();
		endpoints.put(endpoint, count);
	}
	
	public HashMap<String, Integer> getEndpoints() {
		return this.endpoints;
	}
	
	public int getTotalCount() {
		int count = 0;
		for (Integer endpointCount: endpoints.values()) {
			count += endpointCount.intValue();
		}
		return count;
	}
	
	public int getEndpointCount(String endpoint) {
		int count = 0;
		if (this.endpoints.containsKey(endpoint)) {
			count = endpoints.get(endpoint).intValue();
		}
		return count;
	}
	public HashMap<String, Integer> getEndpoints(int minEndpointCount) {
		HashMap<String, Integer> newEndpointsList = new HashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : endpoints.entrySet()) {
		    if (entry.getValue() >= minEndpointCount) {
		    	newEndpointsList.put(entry.getKey(), entry.getValue());
		    }
		}
		return newEndpointsList;
	}
	public int getTotalCount(int minEndpointCount) {
		int count = 0;
		for (Integer num: getEndpoints(minEndpointCount).values()) {
			count += num;
		}
		return count;
	}
	public void calcAggregatedStats(String name, Collection collection) throws IOException, ParseException {
//		this.ckanStats.calc(name);
		this.accessibilityStats.calc(name, collection);
	}
	
	
	
	public String toString() {
		return "total endpoint count: " + getTotalCount() + " distinct endpoint count: " + getEndpoints().size();
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Integer> getOrderedEndpoints() {
		List list = new LinkedList(endpoints.entrySet());
		 
		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
                                       .compareTo(((Map.Entry) (o2)).getValue());
			}
		});
 
		// put sorted list into map again
                //LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
        return sortedMap;
	}

	public void toCsv(String name) throws IOException {
		toSimpleStatsCsv(name);
		toElaborateStatsCsv(name);
		
	}
	
	private void toElaborateStatsCsv(String name) throws IOException {
		File csvFile = new File(Collection.PATH_CSV_RESULTS + name + "/endpointStats.csv");
		
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{name});
		writer.writeNext(new String[]{"Endpoint", "#queries", "accessibilityStatus"});
		
		Map<String, Integer> endpoints = getOrderedEndpoints();
		for (Entry<String, Integer> entry : endpoints.entrySet()) {
			String endpoint = entry.getKey();
			writeElaborareStatsRow(writer, endpoint, entry.getValue(), accessibilityStats.isAccessible(endpoint));
		}
		
		writer.close();
	}
	
	private void writeElaborareStatsRow(CSVWriter writer, String endpoint, int count, EndpointAccessiblityAnalysis accessible) {
		writer.writeNext(new String[]{endpoint, Integer.toString(count), accessible.toString()});
	}
	
	private void toSimpleStatsCsv(String name) throws IOException {
		File csvFile = new File(Collection.PATH_CSV_RESULTS + name + "/endpointStatsSimple.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';');
		writer.writeNext(new String[]{name});
		writer.writeNext(new String[]{"Analysis", "Distinct endpoints", "Overall"});
		
		writeSimpleStatsRow(writer, "Number of endpoints", endpoints.size(), getTotalCount());
		writeSimpleStatsRow(writer, "Number of (>1 query) endpoints", getEndpoints(2).size(), getTotalCount(2));
		
		
		writeSimpleStatsRow(writer, "Number of accessible ckan endpoints", accessibilityStats.getNumEndpoints(EndpointAccessiblityAnalysis.CKAN_ACCESSIBLE), accessibilityStats.getTotalNumEndpoints(EndpointAccessiblityAnalysis.CKAN_ACCESSIBLE));
		writeSimpleStatsRow(writer, "Number of inaccessible endpoints", accessibilityStats.getNumEndpoints(EndpointAccessiblityAnalysis.CKAN_NOT_ACCESSIBLE), accessibilityStats.getTotalNumEndpoints(EndpointAccessiblityAnalysis.CKAN_NOT_ACCESSIBLE));
		writeSimpleStatsRow(writer, "Number of probably incorrect endpoints", accessibilityStats.getNumEndpoints(EndpointAccessiblityAnalysis.PROBABLY_INCORRECT), accessibilityStats.getTotalNumEndpoints(EndpointAccessiblityAnalysis.PROBABLY_INCORRECT));
		writeSimpleStatsRow(writer, "Number of private endpoints with public data", accessibilityStats.getNumEndpoints(EndpointAccessiblityAnalysis.PRIVATE_ENDPOINT_PUBLIC_DATA), accessibilityStats.getTotalNumEndpoints(EndpointAccessiblityAnalysis.PRIVATE_ENDPOINT_PUBLIC_DATA));
		writeSimpleStatsRow(writer, "Number of private endpoints with private data", accessibilityStats.getNumEndpoints(EndpointAccessiblityAnalysis.PRIVATE_ENDPOINT_PRIVATE_DATA), accessibilityStats.getTotalNumEndpoints(EndpointAccessiblityAnalysis.PRIVATE_ENDPOINT_PRIVATE_DATA));
		writer.close();
	}
	
	private void writeSimpleStatsRow(CSVWriter writer, String analysis, int distinctEndpoints, int overallCount) {
		writer.writeNext(new String[]{analysis, Integer.toString(distinctEndpoints), Integer.toString(overallCount)});
	}
	
	public static void main(String[] args) {
	}
}
