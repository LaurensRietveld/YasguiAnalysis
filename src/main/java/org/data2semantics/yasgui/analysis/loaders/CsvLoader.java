package org.data2semantics.yasgui.analysis.loaders;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.validator.routines.UrlValidator;
import org.data2semantics.yasgui.analysis.Query;

import au.com.bytecode.opencsv.CSVReader;


public class CsvLoader extends Loader {
	private UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES + UrlValidator.ALLOW_2_SLASHES + UrlValidator.ALLOW_LOCAL_URLS) {
		private static final long serialVersionUID = -7572321851960984161L;
		/** allow missing scheme. */
        @Override
        public boolean isValid(String value) {
            return super.isValid(value) || value.contains("query") || value.contains("sparql");
        }
	};
	public CsvLoader(AnalysisSetup analysisSetup) throws IOException {
		super(analysisSetup);
	}

	
	public void load(AnalysisSetup.Input inputType) throws IOException {
		//pfffff. Oke: the only proper csv reader for java does not support using quotes to escape quotes. 
		//excel and libre office do not support settings the escape char to something different than a quote
		//therefore, load the csv in php first (which has -proper- loading of csvs), store these via php to file (which stores them with slashes instead of quotes as escape chars)
		//now, we can finally load them using the opencsv reader (with delimiter set to slash)
		CSVReader csvReader = new CSVReader(new FileReader(inputType.getInputPath()), ',', '"', '\\',0); 
		Reader in = new FileReader(inputType.getInputPath());
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		
		boolean useMetaRow = inputType.getEndpointCol() == inputType.getQueryCol();
		boolean lineRead = false;
		int errorCount = 0;
		int lineCount = 0;
		int totalQueryCount = 0;
		for (CSVRecord line : records) {
			lineCount++;
			try {
				lineRead = true;
				String endpoint = null;
				
				if (validColumn(line, inputType.getEndpointCol()) && (!useMetaRow || line.get(0).equals("endpoint"))) {
					if(!checkEndpointFilters(line.get(inputType.getEndpointCol()))) continue;
					
					if (urlValidator.isValid(line.get(inputType.getEndpointCol()))) {
						endpoint = line.get(inputType.getEndpointCol());
						if (inputType.getCountCol() >= 0) {
							collection.getEndpointCollection().addEndpoint(endpoint, Integer.parseInt(line.get(inputType.getCountCol()).replace(",","").trim()));
						} else {
							collection.getEndpointCollection().addEndpoint(endpoint);
						}
						
					}
				} 
				if (validColumn(line, inputType.getQueryCol()) && (!useMetaRow || line.get(0).equals("query"))) {
					if (line.get(inputType.getQueryCol()).toLowerCase().contains("insert")) {
						lineRead = true;
					}
					int count = Integer.parseInt(line.get(inputType.getCountCol()));
					totalQueryCount += count;
					Query query = getParsedAndFilteredQuery(line.get(inputType.getQueryCol()), count);
					
					if (query != null) {
						if (count <= 0) {
							throw new IOException("count cannot be zero");
						}
						query.setCount(count);
						if (endpoint != null) query.setEndpoints(endpoint);
						collection.getQueryCollection().addQuery(query);
					}
				}
			} catch (NumberFormatException e) {
				//hmm, opencsv sees part of the string as the count col, causing a number format exception
				errorCount++;
//				System.out.println("---");
//				System.out.println(line[inputType.getQueryCol()]);
//				System.out.println("---");
			}
		}
		System.out.println("walked through " + lineCount + " unique lines");
		System.out.println("opencsv could not parse " + errorCount + " rows");
//		System.out.println("jena could not parse " + invalidQueryCount + " queries (or " + collection.getQueryCollection().invalidQueries.getVal() + "?)");
//		System.out.println("queries skipped containing comments (incl dups) " + collection.getQueryCollection().queriesWithComment.getVal());
		System.out.println("valid unique queries: " + collection.getQueryCollection().getDistinctQueryCount());
		System.out.println();
		System.out.println("total numbers:");
		System.out.println("\tTotal: " + totalQueryCount);
		System.out.println("\tFiltered queries: " + collection.getQueryCollection().filteredQueries.getVal());
		System.out.println("\tValid: " + collection.getQueryCollection().getTotalQueryCount());
		System.out.println("\tInvalid: " + collection.getQueryCollection().invalidQueries.getVal());
		System.out.println("\tCommented: " + collection.getQueryCollection().queriesWithComment.getVal());
		csvReader.close();
		if (!lineRead) throw new IllegalStateException("could not read any file for ");
		if (collection.getQueryCollection().getQueries().size() == 0) throw new IllegalStateException("could read any queries from file");
	}
	
	

	
	
	public static void main(String[] args) throws IOException, ParseException, URISyntaxException {
//		Query query = Query.create("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
//				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
//				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + 
//				"\n" + 
//				"\n" + 
//				"INSERT { ?x rdfs:label ?lbl. ?x foaf:depiction ?dep. } \n" + 
//				"where { GRAPH <http://example.org/people> {<http://dbpedia.org/resource/University_of_Southern_California> ?y ?x. OPTIONAL{?x rdfs:label ?lbl.} OPTIONAL{?x foaf:depiction ?dep.}} }");
//		CsvLoader loader = new CsvLoader(new YasguiAnalysis());
//		loader.setQueryFilters(new SimpleBgpFilter());
//		loader.load(true, false);
//		System.out.println(loader.getCollection().getQueryCollection().toString());
//		System.out.println(loader.getCollection().getEndpointCollection().toString());
		
		
	}
}
