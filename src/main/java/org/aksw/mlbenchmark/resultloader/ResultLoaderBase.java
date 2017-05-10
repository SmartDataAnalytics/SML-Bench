package org.aksw.mlbenchmark.resultloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Simon Bin on 16-4-28.
 */
public class ResultLoaderBase {
	public List<String> getResults() {
		return results;
	}

	List<String> results = new LinkedList<>();
	public void loadResults(File input) throws IOException {
		results.clear();
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.matches("^\\s*$") || line.matches("^\\s*;")) {
				// skip, it is blank or comment
				continue;
			}
			results.add(line);
		}
		reader.close();
	}

	public boolean isEmpty() {
		return results.isEmpty();
	}
}
