package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.*;
import org.aksw.mlbenchmark.exampleloader.ExampleLoaderBase;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Methods common to system runners
 */
public abstract class AbstractSystemRunner implements SystemRunner {
	protected final BenchmarkRunner parent;
	protected final Configuration parentConf; // the partial scenario config from the parent
	protected final Scenario scn;

	public AbstractSystemRunner(BenchmarkRunner benchmarkRunner, Scenario scn, Configuration baseConf) {
		this.parent = benchmarkRunner;
		this.parentConf = baseConf;
		this.scn = scn;
	}

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

	public BenchmarkRunner getBenchmarkRunner() {
		return parent;
	}

	public Configuration getParentConfiguration() {
		return parentConf;
	}

	public Configuration getResultset() {
		return parent.getResultset();
	}
}
