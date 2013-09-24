package org.data2semantics.yasgui.analysis.loaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.data2semantics.query.filters.QueryFilter;
import org.data2semantics.yasgui.analysis.filters.EndpointFilter;


public abstract class AnalysisSetup {
	public static enum Input {
		OLD_QUERIES("input/old_queries.csv", null, -1, 0, 1 ),//column IDs (-1 if column id for this data does not exist)
		NEW_QUERIES("input/new_logs.csv", null, 0, 1, 2),
		OLD_ENDPOINTS("input/old_endpoints.csv", null, 0, -1, 1),
		DBP_QUERIES("input/dbp.log", "cache/dbp.cache", -1, -1, -1),
		DBP_SNORQL_QUERIES("input/dbp.log", "cache/dbpSnorql.cache", -1, -1, -1);
		
		private int endpointCol = 0;
		private int queryCol = 0;
		private int countCol = 0;
		private String inputPath = null;
		private String cachePath = null;
	    private Input(String inputPath, String cachePath, int endpointCol, int queryCol, int countCol) {
	        this.endpointCol = endpointCol;
	        this.queryCol = queryCol;
	        this.countCol = countCol;
	        this.cachePath = cachePath;
	        this.inputPath = inputPath;
	    }
	   
	    public int getQueryCol() {return queryCol;}
	    public int getEndpointCol() {return endpointCol;}
	    public int getCountCol() {return countCol;}
	    public String getInputPath() {return inputPath;}
	    public String getCachePath() {return cachePath;}
	};
	protected Loader loader;
	protected Input[] inputTypes;
	protected ArrayList<QueryFilter> queryFilters = new ArrayList<QueryFilter>();
	protected ArrayList<EndpointFilter> endpointFilters = new ArrayList<EndpointFilter>();
	protected String name;
	
	public AnalysisSetup() throws IOException {
	}
	public ArrayList<QueryFilter> getQueryFilters() {
		return this.queryFilters;
	}
	public ArrayList<EndpointFilter> getEndpointFilters() {
		return this.endpointFilters;
	}
	public String getName() {
		return this.name;
	}
	public Input[] getInputTypes() {
		return this.inputTypes;
	}
	public Loader getLoader() {
		return this.loader;
	}
	
	protected void createDirs() {
		File outputDir = new File("output/"+name);
		if (!outputDir.exists()) {
			outputDir.mkdir();
		}
	}
}
