package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;


public class DBPediaAnalysis extends AnalysisSetup {
	
	public DBPediaAnalysis() throws IOException {
		this.name = "DBPedia";
		this.inputTypes = new Input[]{AnalysisSetup.Input.DBP_QUERIES};
		this.loader = new DbpLoader(this);
		createDirs();
	}
 }
