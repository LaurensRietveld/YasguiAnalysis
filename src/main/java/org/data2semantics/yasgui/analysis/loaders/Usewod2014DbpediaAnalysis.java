package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;


public class Usewod2014DbpediaAnalysis extends AnalysisSetup {
	
	public Usewod2014DbpediaAnalysis() throws IOException {
		this.name = "usewod2014DBpedia";
		this.inputTypes = new Input[]{AnalysisSetup.Input.USEWOD2014_DBP};
		this.loader = new DbpLoader(this);
		createDirs();
	}
 }
