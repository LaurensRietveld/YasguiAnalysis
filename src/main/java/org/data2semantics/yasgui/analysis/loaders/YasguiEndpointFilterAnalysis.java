package org.data2semantics.yasgui.analysis.loaders;

import java.io.IOException;

import org.data2semantics.yasgui.analysis.filters.EndpointFilter;



public class YasguiEndpointFilterAnalysis extends AnalysisSetup {
	
    private class DynamicEndpointFilter implements EndpointFilter {
        String[] filters;
        public DynamicEndpointFilter(String [] endpoints) {
            this.filters = endpoints;
        }
        
        public boolean filter(final String endpoint) {
            boolean match = false;
            for (String filter: filters) {
                if (!match) match = endpoint.equals(filter);
            }
            return match;
//            return !endpoint.equals("http://dbpedia.org/sparql") && !endpoint.equals("http://live.dbpedia.org/sparql");
        }
    }
    
    public YasguiEndpointFilterAnalysis(String name, String... endpointsToFilter) throws IOException {
        super(name, endpointsToFilter);
        this.name = name;
        this.inputTypes = new Input[]{AnalysisSetup.Input.YASGUI_NEW};
        
        

        this.endpointFilters.add(new DynamicEndpointFilter(endpointsToFilter));
        this.loader = new CsvLoader(this);
        createDirs();
    }
    
	public YasguiEndpointFilterAnalysis() throws IOException {
	    throw new UnsupportedOperationException("For this analysis type, provide a name and endpoint filters");
	}
 }
