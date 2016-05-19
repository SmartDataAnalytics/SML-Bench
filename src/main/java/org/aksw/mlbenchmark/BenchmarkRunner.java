package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.mex.MEXWriter;
import org.aksw.mlbenchmark.systemrunner.CrossValidationRunner;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FileUtils;
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
 * The main runner for the SMLBench framework, will execute one benchmark configuration (without saving of results)
 */
public class BenchmarkRunner {
	static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
	private final String currentDir;
	private final URL sourceDir;
	private final String rootDir;
	private final BenchmarkConfig config;
	private final List<String> desiredSystems;
	private final int threads;
	private final Set<String> desiredLanguages = new HashSet<>();
	private final Map<String, String> systemLanguage = new HashMap<>();
	private final int folds;
	private List<String> availableLearningSystems = new LinkedList<>();
	private Map<String, LearningSystemInfo> systemInfos = new HashMap<>();
	private ExecutorService executorService;
	private Path tempDirectory;
	private long seed;
	private PropertyListConfiguration resultset = new PropertyListConfiguration();
	private BenchmarkLog benchmarkLog;
	private String mexOutputFilePath = null;

	public BenchmarkRunner(HierarchicalConfiguration<ImmutableNode> config) {
		this(new BenchmarkConfig(config));
	}

	public BenchmarkRunner(BenchmarkConfig config) {
		benchmarkLog = new BenchmarkLog();
		benchmarkLog.saveBenchmarkConfig(config);

		File file1 = new File(new File(".").getAbsolutePath());
		currentDir = (file1.getName().equals(".") ? file1.getParentFile() : file1).getAbsolutePath();
		sourceDir = BenchmarkRunner.class.getProtectionDomain().getCodeSource().getLocation();
		logger.info("source dir = " + sourceDir);
		rootDir = new File(sourceDir.getPath()).getParentFile().getParentFile().getAbsolutePath();
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
		desiredSystems = config.getLearningSystems();
		seed = config.getSeed();
		initLanguages();
		initTemp();
		folds = config.getCrossValidationFolds();
		threads = config.getThreadsCount();
		if (config.containsKey("mex.outputFile")) {
			mexOutputFilePath = config.getMexOutputFile();
		}

		if (threads == 1) {
			executorService = Executors.newSingleThreadExecutor();
		} else {
			executorService = Executors.newFixedThreadPool(threads);
		}

		benchmarkLog.saveDirs(rootDir, getLearningTasksDir(),
				getLearningSystemsDir(), tempDirectory.toAbsolutePath().toString());
	}

	public BenchmarkRunner(String configFilename) throws ConfigLoaderException {
		// load the properties file
		this(new ConfigLoader(configFilename).loadWithInfo().config());
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

	public BenchmarkConfig getConfig() {
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

		List<String> scenarios = expandScenarios(config.getScenarios());

		logger.info("requested scenarios: " + scenarios);
		for (final String scn : scenarios) {
			runScenario(scn);
		}

		finalizeExecutor();

		if (mexOutputFilePath != null) {
			MEXWriter mexWriter = new MEXWriter();

			try {
				mexWriter.write(benchmarkLog, mexOutputFilePath);
				logger.info("wrote MEX file to " + mexOutputFilePath + ".ttl");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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
		if (!systemInfos.containsKey(system)) {
			LearningSystemInfo lsi = new LearningSystemInfo(this, system);
			systemInfos.put(system, lsi);
			return lsi;
		}
		return systemInfos.get(system);
	}

	public Configuration getResultset() {
		return resultset;
	}

	public void cleanTemp() {
	if (config.isDeleteWorkDir()) {
			Path tempDirectory = getTempDirectory();
			if (tempDirectory != null) {
				logger.debug("deleting working directory: " + tempDirectory);
				try {
					FileUtils.forceDeleteOnExit(tempDirectory.toFile());
				} catch (IOException e) {
					logger.debug("could not remove working directory: " + e.getMessage());
				}
			}
		}
	}

	public BenchmarkLog getBenchmarkLog() {
		return benchmarkLog;
	}
}
