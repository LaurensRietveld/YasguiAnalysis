package org.data2semantics.yasgui.analysis.helpers;

import java.util.HashSet;
import java.util.Set;
import org.data2semantics.query.QueryCollection;
import org.data2semantics.query.Query;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

public class Filter {
	int maxdepth=0;
	int numvars=0;
	int numexpr=0;

	Set<String> constants = new HashSet<String>();
	
	public int getMaxdepth() {
		return maxdepth;
	}

	public int getNumvars() {
		return numvars;
	}

	public int getNumconstants() {
		return constants.size();
	}

	public int getNumexpr() {
		return numexpr;
	}

	QueryCollection queryCollection;
	Query query;
	
	public Filter(QueryCollection queryCollection, Query query) {
		this.queryCollection = queryCollection;
		this.query = query;
	}
	
	void clear() {
		maxdepth=numvars=numexpr=0;
		constants.clear();
	}
	
	void analyze(Expr ex, int depth) {
//		System.out.println("\t"+ex.toString());
		if(depth>maxdepth){
			maxdepth = depth;
		}
		
		if(ex.isFunction()) {
			numexpr++;
			ExprFunction ef = ex.getFunction();
			String fun = ef.getFunctionSymbol().getSymbol();
		
			if(fun.equals("function")) {
				fun = ef.getFunctionIRI();
			}
			
			if(fun.toLowerCase().contains("regex")){
				//System.out.println(ex.toString());
//				queryCollection.addString("REGEX", ex.toString());
			}
			
			// System.out.println("\t Function: "+fun);
//			queryCollection.addString("FILTER FUNCTION USAGE",fun);
			
			for(Expr child : ef.getArgs()) {
				this.analyze(child, depth+1);
			}
		} else if(ex.isVariable()) {
			//System.out.println("Variable: "+e.getVarName());
		} else if(ex.isConstant()) {
			//System.out.println("Constant: "+e.getConstant().toString());
			constants.add(ex.getConstant().toString());
		}
	}
	
	public void analyze(ElementFilter el) {
		numvars = el.getExpr().getVarsMentioned().size();
		analyze(el.getExpr(), 1);
		queryCollection.addHisto("FILTER: NUM VARIABLES", getNumvars());
		queryCollection.addHisto("FILTER: DEPTH", getMaxdepth());
		queryCollection.addHisto("FILTER: EXPR", getNumexpr());
		queryCollection.addHisto("FILTER: CONST", getNumconstants());
		
		
	}
}
