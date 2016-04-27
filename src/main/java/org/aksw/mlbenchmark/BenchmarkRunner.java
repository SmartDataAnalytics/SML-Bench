package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.exampleloader.ExampleLoaderBase;
import org.aksw.mlbenchmark.process.ProcessRunner;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Simon Bin on 16-4-13.
 */
public class BenchmarkRunner {
	static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
	private final String currentDir;
	private final URL sourceDir;
	private final String rootDir;
	private List<String> availableLearningSystems = new LinkedList<>();
	private final HierarchicalConfiguration<ImmutableNode> config;
	private final List<String> desiredSystems;
	private final Set<String> desiredLanguages = new HashSet<>();
	private final Map<String, String> systemLanguage = new HashMap<>();
	private Path tempDirectory;
	private long seed;
	private final int folds;


	public BenchmarkRunner(HierarchicalConfiguration<ImmutableNode> config) {
		currentDir = new File(".").getAbsolutePath();
		sourceDir = BenchmarkRunner.class.getProtectionDomain().getCodeSource().getLocation();
		logger.info("source dir = " + sourceDir);
		rootDir = new File(sourceDir.getFile()+"/../..").getAbsolutePath();
		logger.info("root = " + rootDir);
		for (File file: new File(rootDir+"/"+Constants.LEARNINGSYSTEMS).listFiles()) {
			if (file.isDirectory()) {
				availableLearningSystems.add(file.getName());
			}
		}
		logger.info("available learning systems:" + availableLearningSystems);
		this.config = config;
		desiredSystems = config.getList(String.class, "learningsystems");
		seed = config.getLong("framework.seed", new Random().nextLong());
		initLanguages();
		initTemp();
		folds = config.getInt("framework.crossValidationFolds", 1);
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
		for (final String sys: desiredSystems) {
			LearningSystemInfo learningSystemInfo = new LearningSystemInfo(this, sys);
			String language = learningSystemInfo.getConfig().getString("language");
			desiredLanguages.add(language);
			systemLanguage.put(sys, language);
			logger.info("language for "+sys+": " +language);
		}

	}

	public String getLearningSystemDir(String learningSystem) {
		return getLearningSystemsDir()+"/"+learningSystem;
	}
	public String getLearningSystemsDir() {
		return rootDir+"/"+ Constants.LEARNINGSYSTEMS;
	}
	public String getLearningTasksDir() {
		return rootDir+"/"+Constants.LEARNINGTASKS;
	}
	public String getLearningProblemsDir(String learningTask, String languageType) {
		return getLearningTasksDir()+"/"+learningTask+"/"+languageType+"/"+Constants.LEARNINGPROBLEMS;
	}
	public String getLearningProblemDir(String learningTask, String learningProblem, String languageType) {
		return getLearningProblemsDir(learningTask, languageType)+"/"+learningProblem;
	}

	public BenchmarkRunner(String configFilename) throws ConfigLoaderException {
		// load the properties file
		this(new ConfigLoader(configFilename).load().config());
	}

	public Configuration getConfig() {
		return config;
	}

	private List<String> expandScenarios(final Collection<String> scenarios) {
		LinkedList<String> ret = new LinkedList<>();
		for (String scn: scenarios) {
			if (scn.endsWith("/*")) { // need to expand it;
				LinkedHashSet<String> expansion = new LinkedHashSet<>();
				String[] split = scn.split("/");
				for (String lang: desiredLanguages) {
					for (File f : new File(getLearningProblemsDir(split[0], lang)).listFiles()) {
						if (f.isDirectory()) {
							expansion.add(split[0]+"/"+f.getName());
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

		//Testing testing = new Testing();
		//testing.conf(config);
	}

	private void runScenario(String scn) {
		logger.info("executing scenario "+scn);
		Configuration baseConf = new BaseConfiguration();
		final String[] split = scn.split("/");
		final String task = split[0];
		final String problem = split[1];

		Set<String> failedLang = new HashSet<>();

		baseConf.setProperty("framework.currentSeed", seed);
		Map<String, CrossValidation> languageFolds = new HashMap<>();

		for (final String lang : desiredLanguages) {
			String lpDir = getLearningProblemDir(task, problem, lang);
			try {
				ExampleLoaderBase elPos = ExampleLoader.forLanguage(lang);
				elPos.loadExamples(new File(lpDir+"/"+"pos"+LanguageInfo.forLanguage(lang).exampleExtension()));
				LinkedHashSet<String> posExamples = elPos.getExamples();

				ExampleLoaderBase elNeg = ExampleLoader.forLanguage(lang);
				elNeg.loadExamples(new File(lpDir+"/"+"neg"+LanguageInfo.forLanguage(lang).exampleExtension()));
				LinkedHashSet<String> negExamples = elNeg.getExamples();

				CrossValidation cv = new CrossValidation(posExamples, negExamples, folds, seed);

				languageFolds.put(lang, cv);
			} catch (IOException e) {
				logger.warn("could not load examples for "+lang+": " + e.getMessage());
				failedLang.add(lang);
			}
		}

		writeFolds(task, problem, languageFolds, failedLang);

		for (final String system: desiredSystems) {
			if (failedLang.contains(systemLanguage.get(system))) {
				logger.warn("skipping system "+system+" because examples are missing");
				continue;
			}

			logger.info("executing scenario "+scn+" with "+system);
			try {
				ProcessRunner processRunner = new ProcessRunner(getLearningSystemDir(system), "./run");
			} catch (IOException e) {
				logger.warn("learning system "+ system + " could not execute: " + e.getMessage() + "["+e.getClass()+"]");
			}
		}
	}

	private void writeFolds(String task, String problem, Map<String, CrossValidation> languageFolds, Set<String> failedLang) {
		File dir = new File(tempDirectory + "/" + task + "/" + problem);
		dir.mkdirs();
		for (final String lang: desiredLanguages) {
			if (failedLang.contains(lang)) { continue; }
			try {
				CrossValidation crossValidation = languageFolds.get(lang);
				for (Constants.ExType ex : Constants.ExType.values()) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir + "/" + "folds-" + lang + "-" + ex.name().toLowerCase() + LanguageInfo.forLanguage(lang).exampleExtension())));
					for (int i = 0; i < folds; ++i) {
						writer.write("; fold " + i); writer.newLine();
						LinkedHashSet<String> testingSet = crossValidation.getTestingSet(ex, i);
						for (String e: testingSet) {
							writer.write(e); writer.newLine();
						}
					}
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
