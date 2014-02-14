package org.data2semantics.yasgui.analysis;

import java.io.IOException;

import org.data2semantics.yasgui.analysis.loaders.AnalysisSetup;
import org.data2semantics.yasgui.analysis.loaders.YasguiAnalysis;

public class Analysis {
	Collection collection;
//	AnalysisHelper analysis;
	boolean bypassCache;
	public Analysis(boolean bypassCache) {
		this.bypassCache = bypassCache;
	}
	
	public void runAnalysis(AnalysisSetup analysisSetup) throws Exception {
		analysisSetup.getLoader().load(bypassCache);
		collection = analysisSetup.getLoader().getCollection();
		collection.toCsv();
		System.out.println(analysisSetup.getLoader().getCollection().getQueryCollection().toString());
		System.out.println(analysisSetup.getLoader().getCollection().getEndpointCollection().toString());
	}
	
	public void runAllAnalysis() throws IOException, Exception {
		runAnalysis(new YasguiAnalysis());
	}
	

	
	public static void main(String[] args) throws Exception {
		boolean bypassCache = false;
		if (args.length > 0) {
			System.out.println("bypassing cache");
			bypassCache = true;
		}
		Analysis analysis = new Analysis(bypassCache);
		
		analysis.runAllAnalysis();
	}
}
