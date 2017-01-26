package org.aksw.mlbenchmark.config;

import java.util.Iterator;

import org.apache.commons.configuration2.Configuration;

public class DebugUtils {
	public static void printConfig(Configuration conf) {
		Iterator<String> it = conf.getKeys();
		
		while (it.hasNext()) {
			String k = it.next();
			System.out.println(k + " = " + conf.getString(k));
		}
	}
}
