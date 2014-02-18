package org.data2semantics.yasgui.analysis.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;

import org.data2semantics.yasgui.analysis.Query;
import org.data2semantics.yasgui.analysis.helpers.Helper;

public class DbpLoader extends Loader {
	
	public DbpLoader(AnalysisSetup analysisSetup) throws IOException {
		super(analysisSetup);
	}

	private boolean addQueryToList(String queryString) {
		Query query = getParsedAndFilteredQuery(queryString);
		if (query != null) {
			query.setEndpoints("http://dbpedia.org/sparql");
			query.setCount(1);
			collection.getQueryCollection().addQuery(query);
			collection.getEndpointCollection().addEndpoint("http://dbpedia.org/sparql");
		}
		return query != null;
	}
	

	@Override
	public void load(AnalysisSetup.Input inputType) throws IOException {
		File cacheFile = new File(inputType.getCachePath());
		if (!bypassCache && cacheFile.exists()) {
			System.out.println("fetching dbpedia stuff from cache!");
			ArrayList<Integer> linePercentages = Helper.percentLines(inputType.getCachePath());
			int lineNumber = 0;
			System.out.println("0%");
			BufferedReader br = new BufferedReader(new FileReader(cacheFile));
			String line;
			while ((line = br.readLine()) != null) {
				if (linePercentages.contains(lineNumber)) {
					System.out.println(Integer.toString(linePercentages.indexOf(lineNumber) + 1) + "%");
				}
				lineNumber++;
				String queryString = URLDecoder.decode(line, "UTF-8");
				addQueryToList(queryString);
			}
			br.close();
		} else {
			if (cacheFile.exists()) cacheFile.delete();
			FileWriter cacheWriter = new FileWriter(inputType.getCachePath());
			BufferedReader br = new BufferedReader(new FileReader(inputType.getInputPath()));
			ArrayList<Integer> linePercentages = Helper.percentLines(inputType.getInputPath());
			String line;
			int lineNumber = 0;
			System.out.println("0%");
			while ((line = br.readLine()) != null) {
				if (linePercentages.contains(lineNumber)) {
					System.out.println(Integer.toString(linePercentages.indexOf(lineNumber) + 1) + "%");
				}
				lineNumber++;
				String matchSubString = "/sparql?query=";
				if (line.contains(matchSubString)) {
					
					int startIndex = line.indexOf(matchSubString);
					startIndex += matchSubString.length();
					String firstString = line.substring(startIndex);
					String encodedUrlQuery = firstString.split(" ")[0];
					// remove other args
					String encodedSparqlQuery = encodedUrlQuery.split("&")[0];
					try {
						String queryString = URLDecoder.decode(encodedSparqlQuery, "UTF-8");
						boolean success = addQueryToList(queryString);
						if (success) { //i.e. it was added
							cacheWriter.write(URLEncoder.encode(queryString, "UTF-8") + "\n");
						}
					} catch (IllegalArgumentException e) {
						//in very rare cases, the query might contain illegal hex chars in escape pattern
						//causing decode to go wrong
						continue;
					}
					
				}
			}
			br.close();
			cacheWriter.close();
		}
		System.out.println("done");
	}
	

	
	public static void main(String[] args) throws IOException, ParseException, URISyntaxException {
		DbpLoader loader = new DbpLoader(new DBPediaAnalysis());
//		loader.setQueryFilters(new SimpleBgpFilter());
		loader.load(true, false);
		System.out.println(loader.getCollection().getQueryCollection().toString());
		System.out.println(loader.getCollection().getEndpointCollection().toString());
		
	}


}
