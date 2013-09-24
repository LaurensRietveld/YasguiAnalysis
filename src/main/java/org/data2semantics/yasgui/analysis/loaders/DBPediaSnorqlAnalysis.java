package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;
import org.data2semantics.query.filters.SnorqlFilter;


public class DBPediaSnorqlAnalysis extends AnalysisSetup {
	
	public DBPediaSnorqlAnalysis() throws IOException {
		this.name = "DBPedia_Snorql";
		this.inputTypes = new Input[]{AnalysisSetup.Input.DBP_SNORQL_QUERIES};
		this.queryFilters.add(new SnorqlFilter());
		//last
		this.loader = new DbpLoader(this);
		createDirs();
	}

 }
