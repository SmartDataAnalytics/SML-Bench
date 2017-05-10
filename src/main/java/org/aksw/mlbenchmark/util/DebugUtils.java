package org.aksw.mlbenchmark.util;

import java.util.Iterator;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;

public class DebugUtils {
	public static void printConfig(Configuration conf) {
		System.out.println(configToString(conf));
	}
	
	public static String configToString(Configuration conf) {
		StringBuilder res = new StringBuilder();
		
		Iterator<String> it = conf.getKeys();
		
		while (it.hasNext()) {
			String k = it.next();
			res.append(k);
			res.append(" = ");
			String[] v = conf.getStringArray(k);
			res.append(StringUtils.join(v, ','));
			res.append('\n');
		}
		return res.toString();
	}
}
