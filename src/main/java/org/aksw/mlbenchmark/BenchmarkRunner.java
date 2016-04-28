package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.systemrunner.CrossValidationRunner;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Simon Bin on 16-4-13.
 */
public class BenchmarkRunner {
	static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
	private final String currentDir;
	private final URL sourceDir;
	private final String rootDir;
	private final HierarchicalConfiguration<ImmutableNode> config;
	private final List<String> desiredSystems;
	private final int threads;
	private final Set<String> desiredLanguages = new HashSet<>();
	private final Map<String, String> systemLanguage = new HashMap<>();
	private final int folds;
	private List<String> availableLearningSystems = new LinkedList<>();
	private ExecutorService executorService;
	private Path tempDirectory;
	private long seed;

	public BenchmarkRunner(HierarchicalConfiguration<ImmutableNode> config) {
		currentDir = new File(".").getAbsolutePath();
		sourceDir = BenchmarkRunner.class.getProtectionDomain().getCodeSource().getLocation();
		logger.info("source dir = " + sourceDir);
		rootDir = new File(sourceDir.getFile() + "/../..").getAbsolutePath();
		logger.info("root = " + rootDir);
		File[] files = new File(getLearningSystemsDir()).listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					availableLearningSystems.add(file.getName());
				}
			}
		}
		logger.info("available learning systems:" + availableLearningSystems);
		this.config = config;
		desiredSystems = config.getList(String.class, "learningsystems");
		seed = config.getLong("framework.seed", new Random().nextLong());
		initLanguages();
		initTemp();
		folds = config.getInt("framework.crossValidationFolds", 1);
		threads = config.getInt("framework.threads", 1);

		if (threads == 1) {
			executorService = Executors.newSingleThreadExecutor();
		} else {
			executorService = Executors.newFixedThreadPool(threads);
		}
	}

	public BenchmarkRunner(String configFilename) throws ConfigLoaderException {
		// load the properties file
		this(new ConfigLoader(configFilename).load().config());
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public Set<String> getDesiredLanguages() {
		return desiredLanguages;
	}

	public Path getTempDirectory() {
		return tempDirectory;
	}

	public List<String> getDesiredSystems() {
		return desiredSystems;
	}

	public long getSeed() {
		return seed;
	}

	public int getFolds() {
		return folds;
	}

	private void initTemp() {
		try {
			tempDirectory = Files.createTempDirectory(new File(currentDir).toPath(), "sml-temp");
			logger.info("using working directory: " + tempDirectory);
			tempDirectory.toFile().deleteOnExit();
		} catch (IOException e) {
			throw new RuntimeException("Cannot create temporary folder, terminating.", e);
		}
	}

	private void initLanguages() {
		for (final String sys : desiredSystems) {
			LearningSystemInfo learningSystemInfo = new LearningSystemInfo(this, sys);
			String language = learningSystemInfo.getConfig().getString("language");
			desiredLanguages.add(language);
			systemLanguage.put(sys, language);
			logger.info("language for " + sys + ": " + language);
		}

	}

	public String getLearningSystemDir(String learningSystem) {
		return getLearningSystemsDir() + "/" + learningSystem;
	}

	public String getLearningSystemsDir() {
		return rootDir + "/" + Constants.LEARNINGSYSTEMS;
	}

	public String getLearningTasksDir() {
		return rootDir + "/" + Constants.LEARNINGTASKS;
	}

	public String getLearningProblemsDir(String learningTask, String languageType) {
		return getLearningTasksDir() + "/" + learningTask + "/" + languageType + "/" + Constants.LEARNINGPROBLEMS;
	}

	public String getLearningProblemDir(String learningTask, String learningProblem, String languageType) {
		return getLearningProblemsDir(learningTask, languageType) + "/" + learningProblem;
	}

	public Configuration getConfig() {
		return config;
	}

	private List<String> expandScenarios(final Collection<String> scenarios) {
		LinkedList<String> ret = new LinkedList<>();
		for (String scn : scenarios) {
			if (scn.endsWith("/*")) { // need to expand it;
				LinkedHashSet<String> expansion = new LinkedHashSet<>();
				String[] split = scn.split("/");
				for (String lang : desiredLanguages) {
					File[] files = new File(getLearningProblemsDir(split[0], lang)).listFiles();
					if (files != null) {
						for (File f : files) {
							if (f.isDirectory()) {
								expansion.add(split[0] + "/" + f.getName());
							}
						}
					}
				}
				ret.addAll(expansion);
			} else {
				ret.add(scn);
			}
		}
		return ret;
	}

	public void run() {
		logger.info("benchmarking systems: " + desiredSystems);

		List<String> scenarios = expandScenarios(config.getList(String.class, "scenarios"));

		logger.info("requested scenarios: " + scenarios);
		for (final String scn : scenarios) {
			runScenario(scn);
		}

		finalizeExecutor();
		//Testing testing = new Testing();
		//testing.conf(config);
	}

	private void finalizeExecutor() {
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Threads could not finish", e);
		}
	}

	private void runScenario(String scn) {
		logger.info("executing scenario " + scn);
		Configuration baseConf = new BaseConfiguration();
		final String[] split = scn.split("/");
		final String task = split[0];
		final String problem = split[1];

		baseConf.setProperty("framework.currentSeed", seed);

		if (folds > 1) {
			CrossValidationRunner crossValidationRunner = new CrossValidationRunner(this, task, problem, baseConf);
			crossValidationRunner.run();
		} else {
			throw new NotImplementedException("Absolute measure not yet implemented");
		}

	}

	public String getSystemLanguage(String lang) {
		return systemLanguage.get(lang);
	}

	public LearningSystemInfo getSystemInfo(String system) {
		return new LearningSystemInfo(this, system);
	}
}
