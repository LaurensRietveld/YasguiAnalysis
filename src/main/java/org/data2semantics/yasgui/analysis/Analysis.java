package org.data2semantics.yasgui.analysis;

import java.io.IOException;

import org.data2semantics.yasgui.analysis.helpers.AnalysisHelper;
import org.data2semantics.yasgui.analysis.loaders.AnalysisSetup;
import org.data2semantics.yasgui.analysis.loaders.DBPediaAnalysis;
import org.data2semantics.yasgui.analysis.loaders.YasguiAnalysis;
import org.data2semantics.yasgui.analysis.loaders.YasguiDbpAnalysis;
import org.data2semantics.yasgui.analysis.loaders.YasguiDbpSnorqlAnalysis;
import org.data2semantics.yasgui.analysis.loaders.YasguiSnorqlAnalysis;

public class Analysis {
	Collection collection;
	AnalysisHelper analysis;
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
//		runAnalysis(new YasguiAnalysis());
//		runAnalysis(new YasguiDbpAnalysis());
		runAnalysis(new YasguiDbpSnorqlAnalysis());
//		runAnalysis(new YasguiSnorqlAnalysis());
//		runAnalysis(new DBPediaSnorqlAnalysis());
//		runAnalysis(new DBPediaAnalysis());
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
