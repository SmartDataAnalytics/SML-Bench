package org.aksw.mlbenchmark;

import com.google.common.collect.Sets;
import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.exampleloader.ExampleLoaderBase;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BenchmarkLog {
	public static final String tp = "tp";
	public static final String fp = "fp";
	public static final String tn = "tn";
	public static final String fn = "fn";

	/**
	 * Contains
	 * - learning systems to run ["learningsystems"]
	 * - seed ["framework.seed"]
	 * - number of cross validation folds ["framework.crossValidationFolds"]
	 * - number of threads ["framework.threads"]
	 * - learning scenarios (e.g. suramin/1) ["scenarios"]
	 * - max. execution time ["framework.maxExecutionTime"]
	 */
	private BenchmarkConfig benchmarkConfig;
	/**
	 * Each learning system info object contains the following informations:
	 * - learning system ID (String)
	 * - config
	 *   - language ["language"]
	 *   - config format (either "prop" or "conf") ["configFormat"]
	 *
	 * - optional learning system settings
	 * - runtime config of the learning system
	 *   - filename positive examples ('data.f')
	 *   - filename negative examples ('data.n')
	 *   - filename base ('data.b')
	 */
	private Map<String, LearningSystemInfo> lsis;
	private ResultsStore results;
	private String rootDir;
	private String learningTasksDir;
	private String learningSystemsDir;
	private String tempDir;
	private Map<String,Set<String>> learningProblemsCache = null;
	private ConfigStore learningSystemConfigs;

	public BenchmarkLog() {
		lsis = new HashMap<String, LearningSystemInfo>();
		learningSystemConfigs = new ConfigStore();
		results = new ResultsStore();
	}

	public void saveBenchmarkConfig(BenchmarkConfig config) {
		benchmarkConfig = config;
	}

	public void saveDirs(String rootDir, String learningTasksDir,
			String learningSystemsDir, String tempDir) {
		this.rootDir = rootDir;
		this.learningTasksDir = learningTasksDir;
		this.learningSystemsDir = learningSystemsDir;
		this.tempDir = tempDir;
	}

	public void saveLearningSystemInfo(String learningSystem, LearningSystemInfo lsi) {
		lsis.put(learningSystem, lsi);
	}

	public void saveLearningSystemConfig(String learningSystem, String learningTask,
			String learningProblem, int fold, String configFilePath) {

		learningSystemConfigs.addConfig(learningSystem, learningTask,
				learningProblem, fold, configFilePath);
	}

	public void saveResultSet(String learningSystem, String learningTask,
			String learningProblem, int fold, Configuration res) {
		results.addResult(learningSystem, learningTask, learningProblem, fold, res);
	}

	public Configuration getValidationResults(String learningSystem,
			String learningTask, String learningProblem, int fold) {

		Configuration resConfig = results.getResult(learningSystem, learningTask,
				learningProblem, fold);

		String prefix = learningTask + "." + learningProblem + ".fold-" +
				fold + "." + learningSystem + ".ValidationRaw.";
		return resConfig.subset(prefix);
	}

	public Set<String> getLearningTasks() {
		Set<String> learningTasks = new HashSet<String>();

		List<String> scenarios = benchmarkConfig.getScenarios();

		for (String scenario : scenarios) {
			String learningTask = scenario.split("/")[0];
			learningTasks.add(learningTask);
		}

		return learningTasks;
	}

	public Set<String> getLearningSystems() {
		return Sets.newHashSet(benchmarkConfig.getLearningSystems());
	}

	public String getLearningTaskPath(String learningTask, String learningSystem) {
		LearningSystemInfo lsi = lsis.get(learningSystem);
		String lang = lsi.getConfig().getString("language");

		return learningTasksDir + File.separator + learningTask + File.separator + lang;
	}

	public Set<String> getLearningProblems(String learningTask) {
		if (learningProblemsCache == null) {
			learningProblemsCache = new HashMap<String,Set<String>>();

			for (String scenario : benchmarkConfig.getScenarios()) {
				String[] tmpParts = scenario.split("/");

				String task = tmpParts[0];
				String problem = tmpParts[1];

				if (!learningProblemsCache.containsKey(task)) {
					learningProblemsCache.put(task, new HashSet<String>());
				}

				learningProblemsCache.get(task).add(problem);
			}
		}

		return learningProblemsCache.get(learningTask);
	}

	public Set<String> getPosExamples(String learningTask, String learningProblem,
			String learningSystem, int fold) throws IOException {

		return getExamples(learningTask, learningProblem, learningSystem, fold, "pos");
	}

	public Set<String> getNegExamples(String learningTask, String learningProblem,
			String learningSystem, int fold) throws IOException {

		return getExamples(learningTask, learningProblem, learningSystem, fold, "neg");
	}

	public int getNumFolds() {
		return benchmarkConfig.getCrossValidationFolds();
	}

	public Set<String> getExamples(String learningTask, String learningProblem,
			String learningSystem, int fold, String posNeg) throws IOException {

		String lang = lsis.get(learningSystem).getConfig().getString("language");
		ExampleLoaderBase exLoader = ExampleLoader.forLanguage(lang);

		String posNegFileName = "";

		if (posNeg.equals("pos")) posNegFileName = lsis.get(learningSystem).getPosFilename();
		else posNegFileName = lsis.get(learningSystem).getNegFilename();

		String path = tempDir + File.separator + learningTask +
				File.separator + learningProblem + File.separator + "fold-" +
				fold + File.separator + learningSystem + File.separator +
				"train" + File.separator + posNegFileName;

		exLoader.loadExamples(new File(path));

		return exLoader.getExamples();
	}

	public Configuration getLearningSystemConfig(String learningSystem,
			String learningTask, String learningProblem, int fold) {

		try {
			return learningSystemConfigs.getConfig(learningTask, learningProblem,
					learningSystem, fold).subset("main.");

		} catch (ConfigLoaderException e) {
			e.printStackTrace();
			return new BaseConfiguration();
		}
	}

	// ------------------------------------------------------------------------

	class ConfigStore {
		Map<String, Map<String, Map<String, Map<Integer, String>>>> configPaths;
		public ConfigStore() {
			configPaths = new HashMap<>();
		}

		void addConfig(String learningSystem, String learningTask,
				String learningProblem, int fold, String configFilePath) {

			if (!configPaths.containsKey(learningTask)) {
				configPaths.put(
						learningTask,
						new HashMap<String, Map<String, Map<Integer, String>>>());
			}

			if (!configPaths.get(learningTask).containsKey(learningProblem)) {
				configPaths.get(learningTask).put(
						learningProblem, new HashMap<String, Map<Integer, String>>());
			}

			if (!configPaths.get(learningTask).get(learningProblem).containsKey(learningSystem)) {
				configPaths.get(learningTask).get(learningProblem).put(
						learningSystem, new HashMap<Integer, String>());
			}

			configPaths.get(learningTask).get(learningProblem).get(
					learningSystem).put(fold, configFilePath);
		}

		Configuration getConfig(String learningTask, String learningProblem,
				String learningSystem, int fold) throws ConfigLoaderException {
			String configFilePath = configPaths.get(learningTask).get(
					learningProblem).get(learningSystem).get(fold);

			return new ConfigLoader(configFilePath).loadWithInfo().config();
		}
	}

	/*
	 * suramin.1.fold-0.aleph.duration
	 * suramin.1.fold-0.aleph.trainingRaw
	 * suramin.1.fold-0.aleph.trainingResult
	 * suramin.1.fold-0.aleph.validationResult
	 * suramin.1.fold-0.aleph.ValidationRaw.tp
	 * suramin.1.fold-0.aleph.ValidationRaw.fp
	 * suramin.1.fold-0.aleph.ValidationRaw.tn
	 * suramin.1.fold-0.aleph.ValidationRaw.fn
	 * suramin.1.fold-0.aleph.measure.pred_acc
	 */
	class ResultsStore {

		Map<String, Map<String, Map<String, Map<Integer, Configuration>>>> res;

		public ResultsStore() {
			res = new HashMap<>();
		}

		void addResult(String learningSystem, String learningTask,
				String learningProblem, int fold, Configuration config) {

			if (!res.containsKey(learningSystem)) {
				res.put(learningSystem, new HashMap<String, Map<String, Map<Integer, Configuration>>>());
			}

			if (!res.get(learningSystem).containsKey(learningTask)) {
				res.get(learningSystem).put(learningTask,
						new HashMap<String, Map<Integer, Configuration>>());
			}

			if (!res.get(learningSystem).get(learningTask).containsKey(learningProblem)) {
				res.get(learningSystem).get(learningTask).put(learningProblem,
						new HashMap<Integer, Configuration>());
			}

			res.get(learningSystem).get(learningTask).get(learningProblem).put(fold, config);
		}

		Configuration getResult(String learningSystem, String learningTask,
				String learningProblem, int fold) {
			Configuration result;
			try {
				result = res.get(learningSystem).get(learningTask).get(learningProblem).get(fold);
			} catch (NullPointerException e) {
				result = new BaseConfiguration();
			}
			return result;
		}
	}

}
