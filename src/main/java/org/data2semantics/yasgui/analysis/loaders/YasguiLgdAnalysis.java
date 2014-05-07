package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;

import org.data2semantics.yasgui.analysis.filters.DbpediaFilter;



public class YasguiLgdAnalysis extends AnalysisSetup {
	
	public YasguiLgdAnalysis() throws IOException {
		super();
		this.name = "YASGUI_LGD";
		this.inputTypes = new Input[]{AnalysisSetup.Input.YASGUI_LGD};
//		this.endpointFilters.add(new DbpediaFilter());
		this.loader = new CsvLoader(this);
		createDirs();
	}
 }
