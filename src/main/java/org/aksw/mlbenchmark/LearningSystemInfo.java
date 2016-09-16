package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.config.LearningSystemConfig;
import org.apache.commons.configuration2.Configuration;

import java.util.List;

/**
 * All useful information about a LearningSystem in the context of a BenchmarkRunner
 * - name
 * - language
 * - families
 * - filenames for file generation
 */
public class LearningSystemInfo {
	final String learningSystem;
	final BenchmarkRunner br;
	private LearningSystemConfig config;

	/**
	 * create a new learning system information
	 * @param parent the benchmark runner in which context to create this info
	 * @param learningSystem the learning system for which to load the config
	 */
	public LearningSystemInfo(BenchmarkRunner parent, String learningSystem) {
		this.learningSystem = learningSystem.toLowerCase();
		this.br = parent;
		this.config = new LearningSystemConfig(br, this);
	}

	public String asString() {
		return learningSystem;
	}

	/**
	 * @return the directory containing this learning system
	 */
	public String getDir() {
		return br.getLearningSystemDir(learningSystem);
	}

	public Constants.LANGUAGES getLanguage() {
		return config.getLanguage();
	}

	/**
	 * @return the system specific configuration
	 */
	public LearningSystemConfig getConfig() {
		return config;
	}

	/**
	 * @return the filename to use when generating examples folds for this learning system
	 */
	public String getFilename(Constants.ExType type) {
		return config.getFilename(type);
	}

	/**
	 * @return the filename to use when generating knowledge definition files for this learning system
	 */
	public String getBaseFilename() {
		return config.getBaseFilename();
	}

	public List<String> getFamilies() {
		return config.getFamilies();
	}

	public Configuration getCommonsConfig() {
		return config.getConfig();
	}

	public String getConfigFormat() {
		return config.getConfigFormat();
	}

	public boolean hasType(String learningSystem) {
		// TODO: query config.parent
		return learningSystem.equals(asString());
	}
}
