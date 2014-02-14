package org.data2semantics.yasgui.analysis.loaders;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.validator.routines.UrlValidator;
import org.data2semantics.query.Query;
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
		CSVReader csvReader = new CSVReader(new FileReader(inputType.getInputPath()), ',', '"',0); 
		String[] line;
		boolean useMetaRow = inputType.getEndpointCol() == inputType.getQueryCol();
		boolean lineRead = false;
		int errorCount = 0;
		while ((line = csvReader.readNext()) != null) {
			try {
				lineRead = true;
				String endpoint = null;
				
				if (validColumn(line, inputType.getEndpointCol()) && (!useMetaRow || line[0].equals("endpoint"))) {
					if(!checkEndpointFilters(line[inputType.getEndpointCol()])) continue;
					
					if (urlValidator.isValid(line[inputType.getEndpointCol()])) {
						endpoint = line[inputType.getEndpointCol()];
						if (inputType.getCountCol() >= 0) {
							collection.getEndpointCollection().addEndpoint(endpoint, Integer.parseInt(line[inputType.getCountCol()].replace(",","").trim()));
						} else {
							collection.getEndpointCollection().addEndpoint(endpoint);
						}
						
					}
				} 
				if (validColumn(line, inputType.getQueryCol()) && (!useMetaRow || line[0].equals("query"))) {
					Query query = getParsedAndFilteredQuery(line[inputType.getQueryCol()]);
					
					if (query != null) {
						int count = Integer.parseInt(line[inputType.getCountCol()]);
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
			}
		}
		System.out.println("opencsv could not parse " + errorCount + " rows");
		csvReader.close();
		if (!lineRead) throw new IllegalStateException("could not read any file for ");
	}
	
	

	
	
	public static void main(String[] args) throws IOException, ParseException {
		CsvLoader loader = new CsvLoader(new YasguiAnalysis());
//		loader.setQueryFilters(new SimpleBgpFilter());
		loader.load(true);
		System.out.println(loader.getCollection().getQueryCollection().toString());
		System.out.println(loader.getCollection().getEndpointCollection().toString());
		
		
	}
}
