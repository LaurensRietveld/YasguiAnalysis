package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;


public class YasguiSnorqlAnalysis extends AnalysisSetup {
	
	public YasguiSnorqlAnalysis() throws IOException {
		this.name = "YASGUI_Snorql";
		
		this.inputTypes = new Input[]{AnalysisSetup.Input.NEW_QUERIES};//only new queries: they are the only ones containing this feature. additionally we can link the queries to the dbpedia endpoint this way
//		this.queryFilters.add(new SnorqlFilter());
		
		
		//last
		this.loader = new CsvLoader(this);
		createDirs();
	}
	
 }
