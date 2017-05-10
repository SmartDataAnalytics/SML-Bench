package org.aksw.mlbenchmark.config;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.container.ScenarioAttributes;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * This class is a wrapper for the benchmark root configuration. It provides
 * convenience methods and constants.
 */
public class BenchmarkConfig {
	private final HierarchicalConfiguration<ImmutableNode> config;

	/**
	 * create a new BenchmarkConfig which configures a SMLBench run
	 * @param config the apache commons configuration
	 */
	public BenchmarkConfig(HierarchicalConfiguration<ImmutableNode> config) {
		this.config = config;
	}

	/**
	 * @return the underlying apache commons configuration object
	 */
	public HierarchicalConfiguration<ImmutableNode> getConfig() {
		return config;
	}

	/**
	 * Returns the learning systems that should be executed. These are those
	 * declared by the `learningsystems` parameter. But since the current
	 * configuration approach also allows to provide different configurations
	 * for a single tool, a bit more care has to be taken here. Given the
	 * following example
	 * 
	 *   learningsystems=aleph,dllearner-1,dllearner-2
	 * 
	 *   learningsystems.dllearner-1.type = dllearner
	 *   learningsystems.dllearner-2.alg.type = celoe
	 * 
	 *   learningsystems.dllearner-2.type = dllearner
	 *   learningsystems.dllearner-2.alg.type = ocel
	 * 
	 * the learning systems to return aren't 'aleph', 'dllearner-1' and
	 * 'dllearner-2' but just 'aleph' and 'dllearner' since the '-1' and '-2'
	 * suffixes just refer to the two different configurations of the dllearner
	 * learning system.
	 * 
	 * @return the learning systems that should be executed
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
	 * @return whether to use leave 1 out cross validation
	 */
	public boolean isLeaveOneOut() {
		return config.getBoolean("framework.leaveOneOut", false);
	}

	/**
	 * @return number of threads to use for parallelisation
	 */
	public int getThreadsCount() {
		return config.getInt("framework.threads", 1);
	}

	/**
	 * @return output file name for mex
	 */
	public String getMexOutputFile() {
		return config.getString("outputfile.mex", null);
	}

	public String getCSVOutputFile() {
		return config.getString("outputfile.csv", null);
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
	private Configuration getLearningSystemConfiguration(String learningSystem) {
		return config.subset("learningsystems." + learningSystem);
	}

	/**
	 * Extract a sub-configuration from the configuration passed by the user.
	 * The extracted sub-configuration should contain information about
	 * learning system
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
