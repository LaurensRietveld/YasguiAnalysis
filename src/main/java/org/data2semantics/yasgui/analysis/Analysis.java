package org.data2semantics.yasgui.analysis;

import java.io.IOException;
import java.util.Arrays;

import org.data2semantics.yasgui.analysis.loaders.*;

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
//		runAnalysis(new YasguiAnalysis());
		runAnalysis(new YasguiDbpAnalysis());
//		runAnalysis(new YasguiLgdAnalysis());
//		runAnalysis(new Usewod2014DbpediaAnalysis());
	}
	

	
	public static void main(String[] args) throws Exception {
	    boolean bypassCache = false;
        boolean runOptionalOptimizationTest = true;
        boolean runCoverageAnalysis = true;
        
        Analysis analysis = new Analysis(bypassCache, runOptionalOptimizationTest, runCoverageAnalysis);
	    if (args.length > 0) {
	        if (args.length <= 1) {
	            System.err.println("Need arguments: a name, and a list of endpoints to filter for");
	            System.exit(1);
	        }
	        String name = args[0];
	        String[] endpointsFilter = Arrays.copyOfRange(args, 1, args.length - 1 );
	        System.out.println("Storing results in " + name + ", and filtering for endpoints " + Arrays.toString(endpointsFilter));
	        analysis.runAnalysis(new YasguiEndpointFilterAnalysis(name, endpointsFilter));
	    } else {
	        analysis.runAllAnalysis();
	    }
		
		
		
//		if (args.length > 0) {
//			System.out.println("expensive");
//			runOptionalOptimizationTest = true;
//		}
		
		
		analysis.runAllAnalysis();
	}
}
