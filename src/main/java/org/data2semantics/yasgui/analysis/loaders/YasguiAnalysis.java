package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;



public class YasguiAnalysis extends AnalysisSetup {
	
	public YasguiAnalysis() throws IOException {
		this.name = "YASGUI";
		
		this.inputTypes = new Input[]{AnalysisSetup.Input.NEW_QUERIES, AnalysisSetup.Input.OLD_ENDPOINTS, AnalysisSetup.Input.OLD_QUERIES};
		this.loader = new CsvLoader(this);
		createDirs();
	}
	
 }
