package org.aksw.mlbenchmark.config;

import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.container.ScenarioAttributes;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Convenience and constants for the benchmark root config
 */
public class BenchmarkConfig {
	private final HierarchicalConfiguration<ImmutableNode> config;

	/**
	 * create a new BenchmarkConfig which configures a SMLBench run
	 * @param config the apache commons config
	 */
	public BenchmarkConfig(HierarchicalConfiguration<ImmutableNode> config) {
		this.config = config;
	}

	/**
	 * @return the unterlying apache commons config object
	 */
	public HierarchicalConfiguration<ImmutableNode> getConfig() {
		return config;
	}

	/**
	 * @return the learning systems that should be trialed
	 */
	public List<String> getLearningSystems() {
		return config.getList(String.class, "learningsystems");
	}

	/**
	 * @return the random seed for deterministic fold generation
	 */
	public long getSeed() {
		return config.getLong("framework.seed", new Random().nextLong());
	}

	/**
	 * @return number of folds for n-fold cross validation
	 */
	public int getCrossValidationFolds() {
		return config.getInt("framework.crossValidationFolds", 1);
	}

	/**
	 * @return number of threads to use for parallelisation
	 */
	public int getThreadsCount() {
		return config.getInt("framework.threads", 1);
	}

	/**
	 * @param s a config key
	 * @return whether the config contains this key
	 */
/*
	public boolean containsKey(String s) {
		return config.containsKey(s);
	}
*/

	/**
	 * @return output file name for mex
	 */
	public String getMexOutputFile() {
		return config.getString("mex.outputFile", null);
	}

	/**
	 * @return list of scenarios to run
	 */
	public List<String> getScenarios() {
		return config.getList(String.class, "scenarios");
	}

	/**
	 * @return whether to delete the working directory after benchmark finishes
	 */
	public boolean isDeleteWorkDir() {
		return config.getBoolean("deleteWorkDir", false);
	}

	/**
	 * @param defaultValue default value if not configured
	 * @return the output file name for results
	 */
	public String getResultOutputFile(String defaultValue) {
		return config.getString("resultOutput", defaultValue);
	}

	public String getResultOutputFile() {
		return config.getString("resultOutput");
	}

	/**
	 * @param learningSystem the learning system
	 * @return an apache commons config subconfiguration for the learning system
	 */
	public Configuration getLearningSystemConfiguration(String learningSystem) {
		return config.subset("learningsystems." + learningSystem);
	}

	/**
	 * @param lsi learning system info
	 * @return an apache commons config subconfiguration for the learning system
	 */
	public Configuration getLearningSystemConfiguration(LearningSystemInfo lsi) {
		return getLearningSystemConfiguration(lsi.asString());
	}

	/**
	 * @param task the learning task name
	 * @return an apache commons config subconfiguration for the learning task
	 */
	public Configuration getLearningTaskConfiguration(String task) {
		return config.subset("learningtask." + task);
	}

	/**
	 * @param scn the scenario
	 * @return an apache commons config subconfiguration for the learning problem
	 */
	public Configuration getLearningProblemConfiguration(ScenarioAttributes scn) {
		return config.subset("learningproblem." + scn.getTask() + "." + scn.getProblem());
	}

	/**
	 * @return list of desired measures in validation
	 */
	public List<String> getMeasures() {
		return config.getList(String.class, "measures", Arrays.asList("pred_acc"));
	}

	/**
	 * @return the configures maximum execution time in seconds for each training step
	 */
	public long getMaxExecutionTime() {
		return config.getLong("framework.maxExecutionTime", Constants.DefaultMaxExecutionTime);
	}
}
