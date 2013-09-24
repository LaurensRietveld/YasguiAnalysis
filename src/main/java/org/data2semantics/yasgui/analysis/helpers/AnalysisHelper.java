package org.data2semantics.yasgui.analysis.helpers;
import org.data2semantics.yasgui.analysis.EndpointCollection;

public abstract class AnalysisHelper {
	
	protected EndpointCollection collection;
	
	public AnalysisHelper(EndpointCollection collection) {
		this.collection = collection;
	}
	public abstract void calc(String name) throws Exception;
}
