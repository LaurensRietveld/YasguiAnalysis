package org.data2semantics.yasgui.analysis.helpers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Helper {
	public static int getTotalValueCount(HashMap<String, Integer> hm) {
		int count = 0;
		for (Integer intVal: hm.values()) {
			count += intVal.intValue();
		}
		return count;
	}
	
	public static int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
	public static ArrayList<Integer> percentLines(String filename) throws IOException {
	    int numLines = countLines(filename);
	    int percentGap = numLines / 100;
	    ArrayList<Integer> percentLines = new ArrayList<Integer>();
	    int percentGapAggregate = percentGap;
	    for (int i = 0; i <= 100; i++) {
	    	percentLines.add(i, percentGapAggregate);
	    	percentGapAggregate += percentGap;
	    }
	    return percentLines;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(Integer.toString(countLines("input/dbp.log")));
	}
}
