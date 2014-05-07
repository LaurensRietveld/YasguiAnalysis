package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;



public class YasguiDbpAnalysis extends AnalysisSetup {
	
	public YasguiDbpAnalysis() throws IOException {
		super();
		this.name = "YASGUI_DBPedia";
		this.inputTypes = new Input[]{AnalysisSetup.Input.YASGUI_DBPEDIA};
//		this.endpointFilters.add(new DbpediaFilter());
		this.loader = new CsvLoader(this);
		createDirs();
	}
 }
