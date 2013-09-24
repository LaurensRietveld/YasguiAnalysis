package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;
import org.data2semantics.query.filters.SnorqlFilter;
import org.data2semantics.yasgui.analysis.filters.DbpediaFilter;



public class YasguiDbpSnorqlAnalysis extends AnalysisSetup {
	
	public YasguiDbpSnorqlAnalysis() throws IOException {
		super();
		this.name = "YASGUI_DBPediaSnorql";
		this.inputTypes = new Input[]{AnalysisSetup.Input.NEW_QUERIES};
		this.endpointFilters.add(new DbpediaFilter());
		this.queryFilters.add(new SnorqlFilter());
		this.loader = new CsvLoader(this);
		createDirs();
	}
 }
