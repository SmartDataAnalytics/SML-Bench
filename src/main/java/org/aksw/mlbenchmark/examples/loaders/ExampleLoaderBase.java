package org.aksw.mlbenchmark.examples.loaders;

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by Simon Bin on 16-4-27.
 */
public class ExampleLoaderBase {
	LinkedHashSet<String> examples = new LinkedHashSet<>();

	public void loadExamples(File input) throws IOException {
		examples.clear();
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.matches("^\\s*$") || line.matches("^\\s*;")) {
				// skip, it is blank or comment
				continue;
			}
			examples.add(line);
		}
		reader.close();
	}

	public void loadExamples(String fileName) throws IOException {
		loadExamples(new File(fileName));
	}

	public void writeExamples(File output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		writeExamples(writer);
		writer.close();
	}

	public void writeExamples(BufferedWriter writer) throws IOException {
		for (String ex : examples) {
			writer.write(ex);
			writer.newLine();
		}
	}

	public void setExamples(Collection<String> newExamples) {
		examples.clear();
		examples.addAll(newExamples);
	}

	public LinkedHashSet<String> getExamples() {
		return examples;
	}
}
