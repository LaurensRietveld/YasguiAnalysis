package org.data2semantics.yasgui.analysis;

import java.io.IOException;

import org.data2semantics.yasgui.analysis.loaders.AnalysisSetup;
import org.data2semantics.yasgui.analysis.loaders.DBPediaAnalysis;
import org.data2semantics.yasgui.analysis.loaders.YasguiAnalysis;
import org.data2semantics.yasgui.analysis.loaders.YasguiDbpAnalysis;
import org.data2semantics.yasgui.analysis.loaders.YasguiNotDbpAnalysis;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

public class Analysis {
	Collection collection;
//	AnalysisHelper analysis;
	boolean bypassCache;
	private boolean runOptionalOptimizationTest;
	private boolean runCoverageAnalysis;
	public Analysis(boolean bypassCache, boolean runOptionalOptimizationTest, boolean runCoverageAnalysis) {
		this.bypassCache = bypassCache;
		this.runOptionalOptimizationTest = runOptionalOptimizationTest;
		this.runCoverageAnalysis = runCoverageAnalysis;
	}
	
	public void runAnalysis(AnalysisSetup analysisSetup) throws Exception {
		analysisSetup.getLoader().load(bypassCache, runOptionalOptimizationTest, runCoverageAnalysis);
		collection = analysisSetup.getLoader().getCollection();
		collection.toCsv();
		System.out.println(analysisSetup.getLoader().getCollection().getQueryCollection().toString());
		System.out.println(analysisSetup.getLoader().getCollection().getEndpointCollection().toString());
	}
	
	public void runAllAnalysis() throws IOException, Exception {
		runAnalysis(new YasguiAnalysis());
//		runAnalysis(new YasguiDbpAnalysis());
//		runAnalysis(new DBPediaAnalysis());
	}
	

	
	public static void main(String[] args) throws Exception {
		boolean bypassCache = false;
		boolean runOptionalOptimizationTest = false;
		boolean runCoverageAnalysis = true;
		
		
//		if (args.length > 0) {
//			System.out.println("expensive");
//			runOptionalOptimizationTest = true;
//		}
		Analysis analysis = new Analysis(bypassCache, runOptionalOptimizationTest, runCoverageAnalysis);
		
		analysis.runAllAnalysis();
	}
}
