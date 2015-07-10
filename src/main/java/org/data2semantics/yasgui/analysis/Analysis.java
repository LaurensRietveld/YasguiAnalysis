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
//	    args = new String[]{ "yasguiFinto", "http://api.dev.finto.fi/sparql"};//falls over
//	    args = new String[]{ "xdams", "http://lod.xdams.org/sparql"};//done
	    args = new String[]{ "kirjasampo", "http://sparql.vocab.at/kirjasampo/sparql"};
	    args = new String[]{ "disgenet", "http://rdf.disgenet.org/sparql/"};
	    args = new String[]{ "frdbpedia", "http://fr.dbpedia.org/sparql"};
	    args = new String[]{ "musicbrainz", "http://dbtune.org/musicbrainz/sparql"};
	    args = new String[]{ "saam", "http://edan.si.edu/saam/sparql"};
	    args = new String[]{ "risis", "http://risis.data2semantics.ops.few.vu.nl/sparql"};
	    args = new String[]{ "lgd", "http://linkedgeodata.org/sparql/", "http://linkedgeodata.org/sparql"};
	    args = new String[]{ "lmdb", "http://data.linkedmdb.org/sparql", "http://www.linkedmdb.org/sparql"};
	    args = new String[]{ "dbpedia", "http://live.dbpedia.org", "http://dbpedia.org/sparql"};
	    
	    boolean bypassCache = false;
        boolean runOptionalOptimizationTest = false;
        boolean runCoverageAnalysis = true;
        
        Analysis analysis = new Analysis(bypassCache, runOptionalOptimizationTest, runCoverageAnalysis);
	    if (args.length > 0) {
	        if (args.length <= 1) {
	            System.err.println("Need arguments: a name, and a list of endpoints to filter for");
	            System.exit(1);
	        }
	        String name = args[0];
	        String[] endpointsFilter = Arrays.copyOfRange(args, 1, args.length );
	        System.out.println("Storing results in " + name + ", and filtering for endpoints " + Arrays.toString(endpointsFilter));
	        analysis.runAnalysis(new YasguiEndpointFilterAnalysis(name, endpointsFilter));
	    } else {
	        analysis.runAllAnalysis();
	    }
		
		
		
//		if (args.length > 0) {
//			System.out.println("expensive");
//			runOptionalOptimizationTest = true;
//		}
		
		
//		analysis.runAllAnalysis();
	}
}
