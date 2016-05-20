package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.*;
import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.container.ScenarioAttributes;
import org.aksw.mlbenchmark.container.ScenarioLang;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.aksw.mlbenchmark.exampleloader.ExampleLoaderBase;
import org.aksw.mlbenchmark.process.ProcessRunner;
import org.aksw.mlbenchmark.resultloader.ResultLoaderBase;
import org.aksw.mlbenchmark.validation.measures.MeasureMethodTwoValued;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.exec.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Execute Cross Validation scenario
 */
public class CrossValidationRunner {
	final static Logger logger = LoggerFactory.getLogger(CrossValidationRunner.class);
	private final BenchmarkRunner parent;
	private final Set<Constants.LANGUAGES> failedLang = new HashSet<>();
	private final Map<Constants.LANGUAGES, CrossValidation> languageFolds = new HashMap<>();
	private final Configuration parentConf; // the partial scenario config from the parent
	private final Scenario scn;

	public CrossValidationRunner(BenchmarkRunner benchmarkRunner, Scenario scn, Configuration baseConf) {
		this.parent = benchmarkRunner;
		this.scn = scn;
		this.parentConf = baseConf;
		for (final Constants.LANGUAGES lang : parent.getDesiredLanguages()) {
			ScenarioLang sl = scn.addLanguage(lang);

			try {
				ExampleLoaderBase elPos = ExampleLoader.forLanguage(lang);
				elPos.loadExamples(sl.getExamplesFile(parent, Constants.ExType.POS));
				LinkedHashSet<String> posExamples = elPos.getExamples();

				ExampleLoaderBase elNeg = ExampleLoader.forLanguage(lang);
				elNeg.loadExamples(sl.getExamplesFile(parent, Constants.ExType.NEG));
				LinkedHashSet<String> negExamples = elNeg.getExamples();

				CrossValidation cv = new CrossValidation(posExamples, negExamples, parent.getFolds(), parent.getSeed());

				languageFolds.put(lang, cv);
			} catch (IOException e) {
				logger.warn("could not load examples for "+lang+": " + e.getMessage());
				failedLang.add(lang);
			}
		}

		writeAllFolds();

	}

	private void writeAllFolds() {
		File dir = new File(parent.getTempDirectory() + "/" + scn.getTask() + "/" + scn.getProblem());
		dir.mkdirs();
		for (final Constants.LANGUAGES lang: parent.getDesiredLanguages()) {
			if (failedLang.contains(lang)) { continue; }
			try {
				CrossValidation crossValidation = languageFolds.get(lang);
				for (Constants.ExType ex : Constants.ExType.values()) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir + "/" + "folds-" + lang + "-" + ex.asString() + lang.getInfo().exampleExtension())));
					for (int i = 0; i < parent.getFolds(); ++i) {
						writer.write("; fold " + i); writer.newLine();
						LinkedHashSet<String> testingSet = crossValidation.getTestingSet(ex, i);
						ExampleLoaderBase elf = ExampleLoader.forLanguage(lang);
						elf.setExamples(testingSet);
						elf.writeExamples(writer);
					}
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		for (final String system: parent.getDesiredSystems()) {
			final LearningSystemInfo lsi = parent.getSystemInfo(system);
			final Constants.LANGUAGES lang = lsi.getLanguage();
			if (failedLang.contains(lang)) {
				logger.warn("skipping system " + system + " because examples are missing");
				continue;
			}

			/*			parent.getExecutorService().submit(
					new Runnable() {
						@Override
						public void run() { */

			ScenarioSystem ss = scn.addSystem(lsi);
			ConfigLoader learningProblemConfigLoader = ConfigLoader.findConfig(parent.getLearningProblemDir(ss) + "/" + system);

			parent.getBenchmarkLog().saveLearningSystemInfo(lsi);

			for (int fold = 0; fold < parent.getFolds(); ++fold) {
				logger.info("executing scenario " + ss.getTask() + "/" + ss.getProblem() + " with " + ss.getLearningSystem() + ", fold " + fold);

				Step step = new Step(ss, learningProblemConfigLoader, fold);
				step.train();

				if (State.OK.equals(step.getState())) {
					step.validate();
				}

			}

			/*						}
					}); */
		}
	}

	/**
	 * @param scenarioSystem the Scenario and LearningSystem
	 * @param fold the fold
	 * @return the key in the resultset configuration output for a single cross validation fold
	 */
	public static String getResultKey(ScenarioSystem scenarioSystem, int fold) {
		return scenarioSystem.getTask() + "." + scenarioSystem.getProblem() + "." + "fold-" + fold + "." + scenarioSystem.getLearningSystem();
	}

	/**
	 * @param ss the Scenario and LearningSystem
	 * @param fold the fold
	 * @return the relative subdirectory in the working directory for a single cross validation fold
	 */
	public static String getResultDir(ScenarioSystem ss, int fold) {
		return ss.getTask() + "/" + ss.getProblem() + "/" + "fold-" + fold + "/" + ss.getLearningSystem();
	}

	enum State { RUNNING, OK, TIMEOUT, FAILURE, ERROR };

	private Configuration getResultset() {
		return parent.getResultset();
	}

	private static void writeConfig(String configFile, Configuration cc) {
		try {
			ConfigLoader.write(cc, new File(configFile));
		} catch (IOException | ConfigLoaderException | ConfigurationException e) {
			throw new RuntimeException("Writing scenario config failed", e);
		}
	}

	private static void writeFolds(Constants.LANGUAGES lang, String posFilename, LinkedHashSet<String> testingPos, String negFilename, LinkedHashSet<String> testingNeg) {
		try {
			ExampleLoaderBase posWriter = ExampleLoader.forLanguage(lang);
			posWriter.setExamples(testingPos);
			posWriter.writeExamples(new File(posFilename));

			ExampleLoaderBase negWriter = ExampleLoader.forLanguage(lang);
			negWriter.setExamples(testingNeg);
			negWriter.writeExamples(new File(negFilename));
		} catch (IOException e) {
			throw new RuntimeException("Could not write fold file", e);
		}
	}

	private static BaseConfiguration getBaseConfiguration(ScenarioAttributes scn, int fold, File dir, String posFilename, String negFilename, String outputFile) {
		BaseConfiguration baseConfig = new BaseConfiguration();
		baseConfig.setProperty("data.workdir", dir.getAbsolutePath());
		baseConfig.setProperty("framework.currentFold", fold);
		baseConfig.setProperty("learningtask", scn.getTask());
		baseConfig.setProperty("learningproblem", scn.getProblem());
		baseConfig.setProperty("step", "train");

		baseConfig.setProperty("filename.pos", posFilename);
		baseConfig.setProperty("filename.neg", negFilename);
		baseConfig.setProperty("output", outputFile);
		return baseConfig;
	}

	private static BaseConfiguration getValidateConfiguration(ScenarioAttributes scn, File trainingResultFile, int fold, File dir, String posFilename, String negFilename, String outputFile) {
		BaseConfiguration baseConfig = getBaseConfiguration(scn, fold, dir, posFilename, negFilename, outputFile);
		baseConfig.setProperty("step", "validate");
		baseConfig.setProperty("input", trainingResultFile.getAbsolutePath());
		return baseConfig;
	}

	private class Step {
		private final ScenarioSystem ss;
		private final ConfigLoader learningProblemConfigLoader;
		private final int fold;
		private final LearningSystemInfo lsi;
		private final Constants.LANGUAGES lang;
		private final String system;
		private String trainingResultFile;
		private State state;

		public Step(ScenarioSystem ss, ConfigLoader learningProblemConfigLoader, int fold) {
			this.ss = ss;
			this.learningProblemConfigLoader = learningProblemConfigLoader;
			this.fold = fold;
			this.lsi = ss.getLearningSystemInfo();
			this.system = ss.getLearningSystem();
			this.lang = lsi.getLanguage();
		}

		private Configuration collectConfig(BaseConfiguration baseConfig) {
			BenchmarkConfig runtimeConfig = parent.getConfig();
			Configuration scnRuntimeConfig = runtimeConfig.getLearningTaskConfiguration(ss.getTask());
			Configuration lpRuntimeConfig = runtimeConfig.getLearningProblemConfiguration(ss);

			CombinedConfiguration cc = new CombinedConfiguration();
			cc.setNodeCombiner(new MergeCombiner());
			cc.addConfiguration(baseConfig);
			cc.addConfiguration(parentConf);
			cc.addConfiguration(lpRuntimeConfig);
			if (learningProblemConfigLoader != null) {
				cc.addConfiguration(learningProblemConfigLoader.config());
			}
			List<String> families = lsi.getFamilies();
			if (families != null) {
				for (String family : families) {
					ConfigLoader famLpCL = ConfigLoader.findConfig(parent.getLearningProblemDir(ss) + "/" + family);
					if (famLpCL != null) {
						cc.addConfiguration(famLpCL.config());
					}
				}
			}
			cc.addConfiguration(scnRuntimeConfig);
			cc.addConfiguration(lsi.getCommonsConfig());
			cc.addConfiguration(parent.getCommonsConfig());
			BaseConfiguration defaultConfig = new BaseConfiguration();
			defaultConfig.setProperty("maxExecutionTime", (long)(new BenchmarkConfig(cc).getMaxExecutionTime()*0.86));
			cc.addConfiguration(defaultConfig);
			return cc;
		}

		public String getThisResultDir() {
			return getResultDir(ss, fold);
		}

		public String getThisResultKey() {
			return getResultKey(ss, fold);
		}

		public void train() {
			File dir = new File(parent.getTempDirectory() + "/" + getThisResultDir() + "/" + "train");
			dir.mkdirs();

			String posFilename = dir + "/" + lsi.getFilename(Constants.ExType.POS);
			String negFilename = dir + "/" + lsi.getFilename(Constants.ExType.NEG);
			this.trainingResultFile = dir + "/" + "train.out";
			String configFile = dir + "/" + "config." + lsi.getConfigFormat();

			LinkedHashSet<String> trainingPos = languageFolds.get(lang).getTrainingSet(Constants.ExType.POS, fold);
			LinkedHashSet<String> trainingNeg = languageFolds.get(lang).getTrainingSet(Constants.ExType.NEG, fold);

			BaseConfiguration baseConfig = getBaseConfiguration(ss, fold, dir, posFilename, negFilename, trainingResultFile);

			Configuration cc = collectConfig(baseConfig);
			writeConfig(configFile, cc);
			writeFolds(lang, posFilename, trainingPos, negFilename, trainingNeg);

			List<String> args = new LinkedList<>();
			args.add(configFile);
			parent.getBenchmarkLog().saveLearningSystemConfig(ss, fold, configFile);

			final long now = System.nanoTime();
			state = State.RUNNING;
			try {
				ProcessRunner processRunner = new ProcessRunner(lsi.getDir(), "./run", args, cc, parent.getConfig().getMaxExecutionTime());
				state = State.OK;
			} catch (ExecuteException e) {
				if (e.getExitValue() == 143) {
					logger.warn("learning system " + system + " was canceled due to timeout");
					state = State.TIMEOUT;
				} else {
					logger.warn("learning system " + system + " did not finish cleanly: " + e.getMessage());
					state = State.FAILURE;
				}
			} catch (IOException e) {
				logger.warn("learning system " + system + " could not execute: " + e.getMessage() + "[" + e.getClass() + "]");
				state = State.ERROR;
			}
			long duration = System.nanoTime() - now;
			File outputFileFile =  new File(trainingResultFile);
			if (state.equals(State.OK) && !outputFileFile.isFile()) {
				logger.warn("learning system " + system + " did not produce an output");
				state = State.FAILURE;
			}
			String resultKey = getThisResultKey();
			getResultset().setProperty(resultKey + "." + "duration", duration / 1000000000); // nanoseconds -> seconds

			ResultLoaderBase resultLoader = new ResultLoaderBase();
			try {
				resultLoader.loadResults(outputFileFile);
				getResultset().setProperty(resultKey + "." + "trainingRaw", resultLoader.getResults());
			} catch (IOException e) {
				logger.warn("learning system " + system + " result cannot be read: " + e.getMessage());
				state = state.ERROR;
			}

			getResultset().setProperty(resultKey + "." + "trainingResult", state.toString().toLowerCase());
		}

		public State getState() {
			return state;
		}

		public void validate() {
			File dir = new File(parent.getTempDirectory() + "/" + getThisResultDir() + "/" + "validate");
			dir.mkdirs();

			String posFilename = dir + "/" + lsi.getFilename(Constants.ExType.POS);
			String negFilename = dir + "/" + lsi.getFilename(Constants.ExType.NEG);
			String outputFile = dir + "/" + "validateResult.prop";
			String configFile = dir + "/" + "config." + lsi.getConfigFormat();

			LinkedHashSet<String> testingPos = languageFolds.get(lang).getTestingSet(Constants.ExType.POS, fold);
			LinkedHashSet<String> testingNeg = languageFolds.get(lang).getTestingSet(Constants.ExType.NEG, fold);

			BaseConfiguration baseConfig = getValidateConfiguration(ss, new File(trainingResultFile), fold, dir, posFilename, negFilename, outputFile);

			Configuration cc = collectConfig(baseConfig);
			writeConfig(configFile, cc);
			writeFolds(lang, posFilename, testingPos, negFilename, testingNeg);

			List<String> args = new LinkedList<>();
			args.add(configFile);

			State state = State.RUNNING;
			try {
				ProcessRunner processRunner = new ProcessRunner(lsi.getDir(), "./validate", args, cc, 0);
				state = State.OK;
			} catch (ExecuteException e) {
				logger.warn("validation system " + system + " did not finish cleanly: " + e.getMessage());
				state = State.FAILURE;
			} catch (IOException e) {
				logger.warn("validation system " + system + " could not execute: " + e.getMessage() + "[" + e.getClass() + "]");
				state = State.ERROR;
			}
			File outputFileFile = new File(outputFile);
			if (state.equals(State.OK) && !outputFileFile.isFile()) {
				logger.warn("validation system " + system + " did not produce an output");
				state = State.FAILURE;
			}
			String resultKey = getThisResultKey();
			HierarchicalConfiguration<ImmutableNode> result = null;
			if (state.equals(State.OK)) {
				try {
					result = new ConfigLoader(outputFile).load().config();
				} catch (ConfigLoaderException e) {
					logger.warn("could not load validation result: " + e.getMessage());
					state = State.FAILURE;
				}
			}
			getResultset().setProperty(resultKey + "." + "validationResult", state.toString().toLowerCase());
			if (!state.equals(State.OK)) {
				parent.getBenchmarkLog().saveResultSet(ss, fold, getResultset());
				return;
			}

			if (result != null) {
				Iterator<String> keys = result.getKeys();
				while (keys.hasNext()) {
					String key = keys.next();
					getResultset().setProperty(resultKey + "." + "ValidationRaw" + "." + key, result.getProperty(key));
				}
			}
			List<String> measures = parent.getConfig().getMeasures();
			try {
				int tp = result.getInt("tp");
				int fn = result.getInt("fn");
				int fp = result.getInt("fp");
				int tn = result.getInt("tn");
				for (String m : measures) {
					MeasureMethodTwoValued method = MeasureMethod.create(m);
					double measure = method.getMeasure(tp, fn, fp, tn);
					getResultset().setProperty(resultKey + "." + "measure" + "." + m, measure);
				}
			} catch (ConversionException | NoSuchElementException e) {
				logger.warn("invalid validation results: " + e.getMessage());
				state = State.ERROR;
				getResultset().setProperty(resultKey + "." + "validationResult", state.toString().toLowerCase());
			}

			parent.getBenchmarkLog().saveResultSet(ss, fold, getResultset());
		}
	}

}
