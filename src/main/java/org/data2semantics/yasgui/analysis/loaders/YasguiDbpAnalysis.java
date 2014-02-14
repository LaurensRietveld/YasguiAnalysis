package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;

import org.data2semantics.yasgui.analysis.filters.DbpediaFilter;



public class YasguiDbpAnalysis extends AnalysisSetup {
	
	public YasguiDbpAnalysis() throws IOException {
		super();
		this.name = "YASGUI_DBPedia";
		this.inputTypes = new Input[]{AnalysisSetup.Input.YASGUI_NEW};
		this.endpointFilters.add(new DbpediaFilter());
		this.loader = new CsvLoader(this);
		createDirs();
	}
 }
