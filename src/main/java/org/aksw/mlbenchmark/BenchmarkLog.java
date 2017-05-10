package org.aksw.mlbenchmark;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.mlbenchmark.Constants.State;
import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoader;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoaderBase;
import org.aksw.mlbenchmark.systemrunner.AccuracyRunner;
import org.aksw.mlbenchmark.systemrunner.CrossValidationRunner;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import com.google.common.collect.Sets;

public class BenchmarkLog {
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

	public void saveLearningSystemInfo(LearningSystemInfo lsi) {
		lsis.put(lsi.asString(), lsi);
	}

	public void saveLearningSystemConfig(ScenarioSystem scenarioSystem, int fold, String configFilePath) {

		learningSystemConfigs.addConfig(scenarioSystem, fold, configFilePath);
	}

	public void saveAbsoluteLearningSystemConfig(ScenarioSystem scenarioSystem, String configFilePath) {

		learningSystemConfigs.addConfig(scenarioSystem, 0, configFilePath);
	}

	public void saveResultSet(ScenarioSystem scenarioSystem, int fold, Configuration res) {
		results.addResult(scenarioSystem, fold, res);
	}

	public void saveAbsoluteResultSet(ScenarioSystem ss, Configuration resultset) {
		saveResultSet(ss, 0, resultset);
	}

	public Configuration getValidationResults(ScenarioSystem scenarioSystem, int fold) {

		Configuration resConfig = results.getResult(scenarioSystem, fold);

		String prefix = CrossValidationRunner.getResultKey(scenarioSystem, fold)
				+ ".ValidationRaw";
		return resConfig.subset(prefix);
	}
	
	public Configuration getSingleFoldValidationResults(ScenarioSystem scenarioSystem) {
		Configuration resConfig = results.getResult(scenarioSystem, 0);
		String prefix = AccuracyRunner.getResultKey(scenarioSystem) + ".ValidationRaw";

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

	public String getLearningTaskPath(ScenarioSystem scenarioSystem) {
		Constants.LANGUAGES lang = scenarioSystem.getLanguage();

		return learningTasksDir + File.separator + scenarioSystem.getTask() + File.separator + lang.asString();
	}

	public LearningSystemInfo getLearningSystemInfo(String learningSystem) {
		return lsis.get(learningSystem);
	}

	public Set<String> getLearningProblems(String learningTask) {
		if (learningProblemsCache == null) {
			learningProblemsCache = new HashMap<String,Set<String>>();

			for (String scenario : benchmarkConfig.getScenarios()) {
				Scenario scn = Scenario.fromString(scenario);

				if (!learningProblemsCache.containsKey(scn.getTask())) {
					learningProblemsCache.put(scn.getTask(), new HashSet<String>());
				}

				learningProblemsCache.get(scn.getTask()).add(scn.getProblem());
			}
		}

		return learningProblemsCache.get(learningTask);
	}

	public Set<String> getPosExamples(ScenarioSystem scenarioSystem, int fold) throws IOException {

		return getExamples(scenarioSystem, fold, Constants.ExType.POS);
	}

	public Set<String> getNegExamples(ScenarioSystem scenarioSystem, int fold) throws IOException {

		return getExamples(scenarioSystem, fold, Constants.ExType.NEG);
	}

	public int getNumFolds() {
		return benchmarkConfig.getCrossValidationFolds();
	}

	public Set<String> getExamples(ScenarioSystem scenarioSystem, int fold, Constants.ExType posNeg) throws IOException {

		Constants.LANGUAGES lang = scenarioSystem.getLearningSystemInfo().getLanguage();
		ExampleLoaderBase exLoader = ExampleLoader.forLanguage(lang);

		String posNegFileName = scenarioSystem.getLearningSystemInfo().getFilename(posNeg);

		String path = tempDir + File.separator + CrossValidationRunner.getResultDir(scenarioSystem, fold)
				+ File.separator + "train" + File.separator + posNegFileName;

		exLoader.loadExamples(path);

		return exLoader.getExamples();
	}

	public Configuration getLearningSystemConfig(ScenarioSystem scenarioSystem, int fold) {

		try {
			return learningSystemConfigs.getConfig(scenarioSystem, fold).subset("main.");

		} catch (ConfigLoaderException e) {
			e.printStackTrace();
			return new BaseConfiguration();
		}
	}
	
	public Constants.State getSingleFoldTrainingState(ScenarioSystem ss) {
		Configuration res = results.getResult(ss, 0);
		String stateStr = res.getString(
				AccuracyRunner.getResultKey(ss) + "." + Constants.TRAIN_STATUS_KEY_PART);
		
		return Constants.State.getStateFor(stateStr);
	}
	
	public Constants.State getTrainingState(ScenarioSystem ss, int fold) {
		Configuration res = results.getResult(ss, fold);
		String stateStr = res.getString(
				AccuracyRunner.getResultKey(ss) + "." + Constants.TRAIN_STATUS_KEY_PART);
		
		return Constants.State.getStateFor(stateStr);
	}
	
	/**
	 * Returns an overall training state for a n-fold cross validation
	 * experiment. In case the status was the same for all folds, the state to
	 * return is obvious. For all other cases the best achieved status is
	 * returned, where 'best' is determined by this order:
	 * 
	 *   OK > TIMEOUT > NO_RESULT > ERROR
	 * 
	 * RUNNING is ignored here, since it is assumed that the experiment
	 * finished when calling this method.
	 * 
	 * @param ss Scenario + system describing which experiment we refer to
	 * @return The overall training state
	 */
	public Constants.State getTrainingState(ScenarioSystem ss) {
		Map<Constants.State, Integer> states = new HashMap<Constants.State, Integer>();
		
		for (int i=0; i<getNumFolds(); i++) {
			Configuration res = results.getResult(ss, i);
			String stateStr = res.getString(
					CrossValidationRunner.getResultKey(ss, i) + "."
							+ Constants.TRAIN_STATUS_KEY_PART);
			Constants.State s = Constants.State.getStateFor(stateStr);
			
			if (!states.containsKey(s))
				states.put(s, 0);
			int tmp = states.get(s);
			states.put(s, ++tmp);
		}
		
		boolean containsOK = false;
		boolean containsTimeout = false;
		boolean containsNoResult = false;
		for (State state : states.keySet()) {
			// e.g. if there were errors or timeouts in all folds
			if (states.get(state) == getNumFolds())
				return state;
			
			if (state.equals(Constants.State.OK))
				containsOK = true;
			if (state.equals(Constants.State.TIMEOUT))
				containsTimeout = true;
			if (state.equals(Constants.State.NO_RESULT))
				containsNoResult = true;
		}
		
		if (containsOK) return Constants.State.OK;
		if (containsTimeout) return Constants.State.TIMEOUT;
		if (containsNoResult) return Constants.State.NO_RESULT;
		return Constants.State.ERROR;
	}

	// ------------------------------------------------------------------------

	class ConfigStore {
		Map<String, Map<String, Map<String, Map<Integer, String>>>> configPaths;
		public ConfigStore() {
			configPaths = new HashMap<>();
		}

		void addConfig(ScenarioSystem scenarioSystem, int fold, String configFilePath) {

			if (!configPaths.containsKey(scenarioSystem.getTask())) {
				configPaths.put(
						scenarioSystem.getTask(),
						new HashMap<String, Map<String, Map<Integer, String>>>());
			}

			if (!configPaths.get(scenarioSystem.getTask()).containsKey(scenarioSystem.getProblem())) {
				configPaths.get(scenarioSystem.getTask()).put(
						scenarioSystem.getProblem(), new HashMap<String, Map<Integer, String>>());
			}

			if (!configPaths.get(scenarioSystem.getTask()).get(scenarioSystem.getProblem()).containsKey(scenarioSystem.getLearningSystem())) {
				configPaths.get(scenarioSystem.getTask()).get(scenarioSystem.getProblem()).put(
						scenarioSystem.getLearningSystem(), new HashMap<Integer, String>());
			}

			configPaths.get(scenarioSystem.getTask()).get(scenarioSystem.getProblem()).get(
					scenarioSystem.getLearningSystem()).put(fold, configFilePath);
		}

		Configuration getConfig(ScenarioSystem scenarioSystem, int fold) throws ConfigLoaderException {
			String configFilePath = configPaths.get(scenarioSystem.getTask()).get(
					scenarioSystem.getProblem()).get(scenarioSystem.getLearningSystem()).get(fold);

			return ConfigLoader.load(configFilePath);
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

		void addResult(ScenarioSystem scenarioSystem, int fold, Configuration config) {

			if (!res.containsKey(scenarioSystem.getLearningSystem())) {
				res.put(scenarioSystem.getLearningSystem(), new HashMap<String, Map<String, Map<Integer, Configuration>>>());
			}

			if (!res.get(scenarioSystem.getLearningSystem()).containsKey(scenarioSystem.getTask())) {
				res.get(scenarioSystem.getLearningSystem()).put(scenarioSystem.getTask(),
						new HashMap<String, Map<Integer, Configuration>>());
			}

			if (!res.get(scenarioSystem.getLearningSystem()).get(scenarioSystem.getTask()).containsKey(scenarioSystem.getProblem())) {
				res.get(scenarioSystem.getLearningSystem()).get(scenarioSystem.getTask()).put(scenarioSystem.getProblem(),
						new HashMap<Integer, Configuration>());
			}

			res.get(scenarioSystem.getLearningSystem()).get(scenarioSystem.getTask()).get(scenarioSystem.getProblem()).put(fold, config);
		}

		Configuration getResult(ScenarioSystem scenarioSystem, int fold) {
			Configuration result;
			try {
				result = res.get(scenarioSystem.getLearningSystem()).get(scenarioSystem.getTask()).get(scenarioSystem.getProblem()).get(fold);
			} catch (NullPointerException e) {
				result = new BaseConfiguration();
			}
			return result;
		}
	}

}
