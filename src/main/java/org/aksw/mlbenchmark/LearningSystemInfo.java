package org.aksw.mlbenchmark;

import java.util.List;

import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.config.LearningSystemConfig;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.configuration2.Configuration;

/**
 * Container holding all information about learning systems, configured under
 * the learningsystems namespace in the main configuration, e.g.
 * 
 *   learningsystems=aleph,dllearner-1,dllearner-2,progol
 *   learningsystems.dllearner-1.algorithm.maxClassExpressionTests = 5000
 *   learningsystems.dllearner-2.algorithm.maxExecutionTimeInSeconds = 60
 *   learningsystems.aleph.caching = true
 * 
 * There, a learning system is represented by name and an optional identifier.
 * The identifier is only use to distinguish multiple instances of the same
 * learning system, as in the case of the dllearner learning system.
 * 
 * Besides this a learning system supports a certain knowledge representation
 * language, e.g. Prolog or OWL, and holds further settings in a dedicated
 * configuration object.
 */
public class LearningSystemInfo {
	private final String learningSystem;
	private final String identifier;
	private LearningSystemConfig config;
	private final FileFinder fileFinder;

	/**
	 * Creates a new learning system information.
	 * 
	 * @param parent The benchmark runner in which context to create this info
	 * @param learningSystem The learning system for which to load the config
	 */
	public LearningSystemInfo(BenchmarkConfig runtimeConfig, String learningSystem,
			FileFinder fileFinder) {
		
		if (learningSystem.indexOf(Constants.LEARNINGSYSTEM_ID_SEPARATOR) == -1) {
			/* The learning system has no dedicated identifier, i.e. there is
			 * just one instance of it */
			this.learningSystem = learningSystem;
			this.identifier = "";
		
		} else {
			/* The learning system was configured with a specific identifier,
			 * i.e there are probably multiple instances of the learning system
			 */
			String[] parts = learningSystem.split(Constants.LEARNINGSYSTEM_ID_SEPARATOR);
			this.learningSystem = parts[0];
			this.identifier = parts[1];
		}
		this.fileFinder = fileFinder;
		this.config = new LearningSystemConfig(runtimeConfig, this);
	}

	/**
	 * Returns the full name of the learning system, i.e. the learning system
	 * name and the current instance's identifier, e.g. "dllearner-123", or
	 * "aleph-abc".
	 * 
	 * If there is no dedicated identifier, only the learning system name is
	 * returned, e.g. "dllearner" or "aleph".
	 * 
	 * @return Full name of the learning system, i.e. the learning system name
	 * and the current instance's identifier if configured.
	 */
	public String asString() {
		if (identifier.isEmpty())
			return learningSystem;
		else
			return learningSystem + Constants.LEARNINGSYSTEM_ID_SEPARATOR + identifier;
	}

	/**
	 * Returns the learning system name, e.g. "aleph", "dllearner", "funclog", ...
	 * 
	 * @return The learning system name, e.g. "aleph", "dllearner", "funclog", ...
	 */
	public String getLearningSystem() {
		return learningSystem;
	}
	
	/**
	 * Returns the learning system's identifier, if configured.
	 * 
	 * @return The learning system's identifier, if configured, otherwise null
	 */
	public String getIdentifier() {
		return !identifier.isEmpty() ? identifier : null;
	}

	/**
	 * @return The directory containing this learning system
	 */
	public String getDir() {
		return fileFinder.getLearningSystemDir(learningSystem).getAbsolutePath();
	}

	public Constants.LANGUAGES getLanguage() {
		return config.getLanguage();
	}

	/**
	 * @return The system specific configuration
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
		return learningSystem.equals(this.learningSystem);
	}
}
