package org.aksw.mlbenchmark.config;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.List;
import java.util.Random;

/**
 * Convenience and constants for the benchmark root config
 */
public class BenchmarkConfig {
	private final HierarchicalConfiguration<ImmutableNode> config;

	public BenchmarkConfig(HierarchicalConfiguration<ImmutableNode> config) {
		this.config = config;
	}

	public HierarchicalConfiguration<ImmutableNode> getConfig() {
		return config;
	}

	public List<String> getLearningSystems() {
		return config.getList(String.class, "learningsystems");
	}

	public long getSeed() {
		return config.getLong("framework.seed", new Random().nextLong());
	}

	public int getCrossValidationFolds() {
		return config.getInt("framework.crossValidationFolds", 1);
	}

	public int getThreadsCount() {
		return config.getInt("framework.threads", 1);
	}

	public boolean containsKey(String s) {
		return config.containsKey(s);
	}

	public String getMexOutputFile() {
		return config.getString("mex.outputFile");
	}

	public List<String> getScenarios() {
		return config.getList(String.class, "scenarios");
	}

	public boolean isDeleteWorkDir() {
		return config.getBoolean("deleteWorkDir", false);
	}

	public String getResultOutputFile(String defaultValue) {
		return config.getString("resultOutput", defaultValue);
	}

	public String getResultOutputFile() {
		return config.getString("resultOutput");
	}
}
