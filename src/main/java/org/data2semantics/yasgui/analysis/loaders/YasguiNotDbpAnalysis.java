package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;

import org.data2semantics.yasgui.analysis.filters.NotDbpediaFilter;



public class YasguiNotDbpAnalysis extends AnalysisSetup {
	
	public YasguiNotDbpAnalysis() throws IOException {
		super();
		this.name = "YASGUI_NotDBPedia";
		this.inputTypes = new Input[]{AnalysisSetup.Input.YASGUI_NEW};
		this.endpointFilters.add(new NotDbpediaFilter());
		this.loader = new CsvLoader(this);
		createDirs();
	}
 }
