package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;


public class Usewod2014LgdAnalysis extends AnalysisSetup {
	
	public Usewod2014LgdAnalysis() throws IOException {
		this.name = "usewod2014Lgd";
		this.inputTypes = new Input[]{AnalysisSetup.Input.USEWOD2014_LGD};
		this.loader = new DbpLoader(this);
		createDirs();
	}
 }
