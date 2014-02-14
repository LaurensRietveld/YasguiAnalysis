package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;



public class YasguiAnalysis extends AnalysisSetup {
	
	public YasguiAnalysis() throws IOException {
		this.name = "YASGUI";
		
		this.inputTypes = new Input[]{AnalysisSetup.Input.YASGUI_NEW, AnalysisSetup.Input.YASGUI_OLD};
		this.loader = new CsvLoader(this);
		createDirs();
	}
	
 }
