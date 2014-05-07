package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;


public class Usewod2014BioportalAnalysis extends AnalysisSetup {
	
	public Usewod2014BioportalAnalysis() throws IOException {
		this.name = "usewod2014Bioportal";
		this.inputTypes = new Input[]{AnalysisSetup.Input.USEWOD2014_BIOPORTAL};
		this.loader = new DbpLoader(this);
		createDirs();
	}
 }
