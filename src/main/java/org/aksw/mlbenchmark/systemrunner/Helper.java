package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.ConfigLoaderException;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoader;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoaderBase;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Static helper functions for System Runners.
 */
public class Helper {
	public static void writeConfig(String configFile, Configuration cc) {
		try {
			ConfigLoader.write(cc, new File(configFile));
		} catch (IOException | ConfigLoaderException | ConfigurationException e) {
			throw new RuntimeException("Writing scenario config failed", e);
		}
	}

	public static void writeExamplesFiles(Constants.LANGUAGES lang, String posFilename, Set<String> testingPos, String negFilename, Set<String> testingNeg) {
		try {
			ExampleLoaderBase posWriter = ExampleLoader.forLanguage(lang);
			posWriter.setExamples(testingPos);
			posWriter.writeExamples(new File(posFilename));

			ExampleLoaderBase negWriter = ExampleLoader.forLanguage(lang);
			negWriter.setExamples(testingNeg);
			negWriter.writeExamples(new File(negFilename));
		} catch (IOException e) {
			throw new RuntimeException("Could not write examples file", e);
		}
	}
}
