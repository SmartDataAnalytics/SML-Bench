package org.aksw.mlbenchmark.systemrunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.aksw.mlbenchmark.BenchmarkLog;
import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.ConfigLoaderException;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.MeasureMethod;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.examples.ExamplesSplit;
import org.aksw.mlbenchmark.process.ProcessRunner;
import org.aksw.mlbenchmark.resultloader.ResultLoaderBase;
import org.aksw.mlbenchmark.util.FileFinder;
import org.aksw.mlbenchmark.validation.measures.MeasureMethodTwoValued;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.exec.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actions shared between several types of steps (Cross Validation etc.). Each
 * step abstracts the execution of
 * - the training and validation run
 * - for one single learning system instance
 * - on one learning problem
 */
public abstract class CommonStep {
	final static Logger logger = LoggerFactory.getLogger(CommonStep.class);
	protected final LearningSystemInfo lsi;
	protected final Scenario scenario;
	protected final Configuration runtimeConfig;
	protected final FileFinder fileFinder;
	protected final ExamplesSplit examples;
	protected final BenchmarkLog log;
	protected Constants.State state;
	protected File trainingResultFile;
	/**
	 * The portion of time spent for initializing, loading data, etc. A system
	 * setup portion of 0.14 for example means that 14% of the overall runtime
	 * is spent for initialization and data loading and 86% is spent for actual
	 * learning. This estimation helps to ensure that learning systems like the
	 * DL-Learner really finish within the given execution time. For these
	 * systems *their* execution time starts *after* the initialization and
	 * data loading. With an overall execution time of 60 seconds and a system
	 * setup portion of 0.14 the execution time set for such learning systems
	 * would be 60*0.86 = 51.60 seconds. So hopefully the learning system will
	 * finish within the overall 60 seconds and does not have to be killed.
	 */
	protected final double systemSetupPortion = 0.14;
	
	public CommonStep(Scenario scenario, LearningSystemInfo lsi,
			ExamplesSplit examples, Configuration runtimeConfig,
			FileFinder fileFinder, BenchmarkLog log) {
		this.scenario = scenario;
		this.lsi = lsi;
		this.runtimeConfig = runtimeConfig;
		this.fileFinder = fileFinder;
		this.examples = examples;
		this.log = log;
	}
	
	/**
	 * Prepares and runs the training phase. All it has before invoking this is
	 * a directory to work on, the positive and negative training and
	 * validation examples and the knowledge about what scenario to solve with
	 * what learning system.
	 * Thus,
	 * 
	 * 1) the training examples have to be written to file
	 * 2) a configuration file for the learning system has to be prepared
	 * 3) the learning system has to be invoked
	 * 4) the results have to be collected
	 * @return A config holding the training results, i.e. the learned classifiers
	 */
	public Configuration train() {
		File trainDir = fileFinder.getTrainingDir();
		trainDir.mkdirs();

		// 1) write training examples to file ---------------------------------
		File posFile = fileFinder.getPositiveTrainingExamplesFile();
		File negFile = fileFinder.getNegativeTrainingExamplesFile();

		Set<String> trainingPos = getPositiveTrainingExamples();
		Set<String> trainingNeg = getNegativeTrainingExamples();
		Helper.writeExamplesFiles(lsi.getLanguage(),
				posFile.getAbsolutePath(), trainingPos,
				negFile.getAbsolutePath(), trainingNeg);

		// 2) prepare and write config for learning system ----------
		File configFile = new File(trainDir, "config." + lsi.getConfigFormat());
		Configuration cc = collectTrainingConfig(runtimeConfig);
		Helper.writeConfig(configFile.getAbsolutePath(), cc);
		saveLearningSystemConfig(configFile.getAbsolutePath());

		// 3) invoke learning system ------------------------------------------
		List<String> args = new LinkedList<>();
		args.add(configFile.getAbsolutePath());
		long maxExecutionTime = runtimeConfig.getLong(Constants.MAX_EXECUTION_TIME_KEY);
		
		final long now = System.nanoTime();
		this.trainingResultFile = new File(trainDir, "train.out");
		state = simpleProcessRunner("./run", args, cc, maxExecutionTime,
				trainingResultFile.getAbsolutePath(), "learning system");
		
		// 4) collect results -------------------------------------------------
		long duration = System.nanoTime() - now;
		
		Configuration result = new BaseConfiguration();
		result.setProperty(getResultKey() + "." + "duration",
				duration / 1000000000); // nanoseconds -> seconds

		if (state.equals(Constants.State.OK)) {
			ResultLoaderBase resultLoader = new ResultLoaderBase();
			try {
				resultLoader.loadResults(trainingResultFile);
				
			} catch (IOException e) {
				// training output is rubbish
				logger.warn("learning system " +
						lsi.asString() + " result cannot be read: " + e.getMessage());
				state = Constants.State.ERROR;
			}
	
			// result is empty
			if (resultLoader.isEmpty())
				state = Constants.State.NO_RESULT;

			result.setProperty(
					getResultKey() + "." + Constants.TRAINING_RES_RAW_KEY_PART,
					resultLoader.getResults());
		}
		
		result.setProperty(getResultKey() + "." + Constants.TRAIN_STATUS_KEY_PART,
				state.toString().toLowerCase());
		return result;
	}

	/**
	 * Prepares and runs the validation phase. All it has before invoking this
	 * method is a working directory which already holds the training results,
	 * the positive and negative training and validation examples and the
	 * knowledge about what scenario to solve with what learning system.
	 * Thus,
	 * 
	 * 1) the validation examples have to be written to file
	 * 2) a configuration file guiding the validation process has to be written
	 * 3) the validation executable has to be invoked
	 * 4) the validation results have to be gathered
	 * @return
	 */
	public Configuration validate() {
		File validateDir = fileFinder.getValidationDir();
		validateDir.mkdirs();
		
		// 1) write validation examples ---------------------------------------
		File posFilename = fileFinder.getPositiveValidationExamplesFile();
		File negFilename = fileFinder.getNegativeValidationExamplesFile();

		Set<String> testingPos = getPositiveValidationExamples();
		Set<String> testingNeg = getNegativeValidationExamples();
		
		Helper.writeExamplesFiles(lsi.getLanguage(),
				posFilename.getAbsolutePath(), testingPos,
				negFilename.getAbsolutePath(), testingNeg);

		// 2) prepare and write config file -----------------------------------
		File configFile = new File(validateDir, "config." + lsi.getConfigFormat());
		Configuration cc = collectValidationConfig(runtimeConfig);
		Helper.writeConfig(configFile.getAbsolutePath(), cc);

		// 3) invoke the validation executable --------------------------------
		List<String> args = new LinkedList<>();
		args.add(configFile.getAbsolutePath());
		File outputFile = new File(validateDir, "validateResult.prop");

		state = simpleProcessRunner("./validate", args, cc, 0,
				outputFile.getAbsolutePath(), "validation system");

		// 4) collect the validation results ----------------------------------
		String resultKey = getResultKey();
		HierarchicalConfiguration<ImmutableNode> rawValRes = null;
		if (state.equals(Constants.State.OK)) {
			try {
				rawValRes = ConfigLoader.load(outputFile.getAbsolutePath());
			} catch (ConfigLoaderException e) {
				logger.warn("could not load validation result: " + e.getMessage());
				state = Constants.State.ERROR;
			}
		}
		
		Configuration results = new BaseConfiguration();
		results.setProperty(resultKey + "." + "validationResult", state.toString().toLowerCase());

		if (!state.equals(Constants.State.OK)) {
			return results;
		}

		if (rawValRes != null) {
			Iterator<String> keys = rawValRes.getKeys();
			while (keys.hasNext()) {
				String key = keys.next();
				results.setProperty(resultKey + "." + "ValidationRaw" + "." +
						key, rawValRes.getProperty(key));
			}
		}
		
		List<String> measures = runtimeConfig.getList(String.class,
				Constants.MEASURES_KEY, Arrays.asList("pred_acc"));
		
		try {
			int tp = rawValRes.getInt("tp");
			int fn = rawValRes.getInt("fn");
			int fp = rawValRes.getInt("fp");
			int tn = rawValRes.getInt("tn");
			for (String m : measures) {
				MeasureMethodTwoValued method = MeasureMethod.create(m);
				double measure = method.getMeasure(tp, fn, fp, tn);
				results.setProperty(resultKey + "." + "measure" + "." + m, measure);
			}
		} catch (ConversionException | NoSuchElementException e) {
			logger.warn("invalid validation results: " + e.getMessage());
			state = Constants.State.ERROR;
			results.setProperty(resultKey + "." + "validationResult",
					state.toString().toLowerCase());
		}

		return results;
	}

	public Constants.State getState() {
		return state;
	}

	public boolean isStateOk() {
		return Constants.State.OK.equals(state);
	}
	
	protected abstract Set<String> getPositiveTrainingExamples();
	protected abstract Set<String> getNegativeTrainingExamples();
	protected abstract Set<String> getPositiveValidationExamples();
	protected abstract Set<String> getNegativeValidationExamples();
	protected abstract void saveLearningSystemConfig(String configFilePath);
	protected abstract String getResultKey();
	protected abstract void saveResultSet(Configuration result);

	/**
	 * Collects all the settings needed to run a learning system. These
	 * settings comprise
	 * 
	 * - file names like the working directory, positive/negative examples
	 *   files, the output file
	 * - the learning task and learning problem to run
	 * - learning system specific configuration
	 * 
	 * @return The learning system-specific training configuration
	 */
	protected Configuration collectTrainingConfig(Configuration runtimeConfig) {
		CombinedConfiguration trainConfig = new CombinedConfiguration();
		trainConfig.setNodeCombiner(new MergeCombiner());
		
		// file names
		trainConfig.setProperty(
				Constants.WORKDIR_KEY,
				fileFinder.getTrainingDir().getAbsolutePath());
		trainConfig.setProperty(
				Constants.POS_EXAMPLE_FILE_KEY,
				fileFinder.getPositiveTrainingExamplesFile().getAbsolutePath());
		trainConfig.setProperty(
				Constants.NEG_EXAMPLE_FILE_KEY,
				fileFinder.getNegativeTrainingExamplesFile().getAbsolutePath());
		trainConfig.setProperty(
				Constants.OUTPUT_FILE_KEY,
				fileFinder.getTrainingResultOutputFile().getAbsolutePath());
		
		// general settings
		trainConfig.addProperty(Constants.LEARNING_TASK_KEY, scenario.getTask());
		trainConfig.addProperty(Constants.LEARNING_PROBLEM_KEY, scenario.getProblem());
		trainConfig.addProperty(Constants.STEP_KEY, Constants.STEP_TRAIN);
		trainConfig.addProperty(Constants.LS_MAX_EXECUTION_TIME_KEY,
				runtimeConfig.getString(Constants.MAX_EXECUTION_TIME_KEY));
		
		// learning system settings
		// settings declared for all learning system instances of type lsi.getLearningSystem
		Iterator<String> keysIt = runtimeConfig.getKeys(
				Constants.LEARNING_SYSTEMS_KEY + "." + lsi.getLearningSystem());
		extractLearningSystemSettings(keysIt, runtimeConfig, trainConfig);

		if (lsi.getIdentifier() != null) {
			// settings declared for a specific instance with identifier lsi.getIdentifier()
			keysIt = runtimeConfig.getKeys(
					Constants.LEARNING_SYSTEMS_KEY + "." + lsi.asString());
			extractLearningSystemSettings(keysIt, runtimeConfig, trainConfig);
		}

		return trainConfig;
	}
	
	private void extractLearningSystemSettings(Iterator<String> keysIt,
			Configuration runtimeConfig, Configuration targetConfig) {
		
		String wholeKey, key;
		Object[] val;
		while (keysIt.hasNext()) {
			wholeKey = keysIt.next();
			
			// strip off the "learningsystems."
			key = wholeKey.split("\\.", 2)[1];
			
			/* strip off the actual learning system part of the key, i.e.
			 * "dllearner-1.algorithm.type" --> "algorithm.type" */
			key = key.split("\\.", 2)[1];
			
			val = (Object[]) runtimeConfig.getArray(Object.class, wholeKey);
			targetConfig.addProperty(Constants.LS_SPECIFIC_SETTINGS_KEY + "." + key, val);
		}
	}
	
	/**
	 * Collects all the settings to run the validation of a learning system's
	 * training output. These settings comprise
	 * 
	 * - file names like the working directory, positive/negative examples
	 *   files, input file, output file
	 * - the learning task and learning problem to validate the training
	 *   results for
	 * 
	 * @return The validation configuration
	 */
	protected Configuration collectValidationConfig(Configuration runtimeConfig) {
		Configuration validationConfig = new BaseConfiguration();
		
		// file names
		validationConfig.addProperty(
				Constants.WORKDIR_KEY,
				fileFinder.getValidationDir().getAbsolutePath());
		validationConfig.addProperty(
				Constants.POS_EXAMPLE_FILE_KEY,
				fileFinder.getPositiveValidationExamplesFile().getAbsolutePath());
		validationConfig.addProperty(
				Constants.NEG_EXAMPLE_FILE_KEY,
				fileFinder.getNegativeValidationExamplesFile().getAbsolutePath());
		validationConfig.addProperty(
				Constants.INPUT_FILE_KEY,
				fileFinder.getTrainingResultOutputFile().getAbsolutePath());
		validationConfig.addProperty(
				Constants.OUTPUT_FILE_KEY,
				fileFinder.getValidationResultOutputFile().getAbsolutePath());
		
		// general settings
		validationConfig.addProperty(Constants.LEARNING_TASK_KEY, scenario.getTask());
		validationConfig.addProperty(Constants.LEARNING_PROBLEM_KEY, scenario.getProblem());
		validationConfig.addProperty(Constants.STEP_KEY, Constants.STEP_VALIDATE);
		
		return validationConfig;
	}

	protected Constants.State simpleProcessRunner(String command, List<String> args,
			Configuration cc, long maxExecutionTime, String expectedOutput, String info) {
		
		Constants.State state = Constants.State.RUNNING;
		try {
			ProcessRunner processRunner = new ProcessRunner(
					lsi.getDir(), command, args, cc, maxExecutionTime);
			state = Constants.State.OK;
		
		} catch (ExecuteException e) {
			if (e.getExitValue() == 143) {
				logger.warn(info + " " + lsi.toString() +
						" was canceled due to timeout");
				state = Constants.State.TIMEOUT;
			
			} else {
				logger.warn(info + " " + lsi.toString() +
						" did not finish cleanly: " + e.getMessage());
				state = Constants.State.ERROR;
			}
		
		} catch (IOException e) {
			logger.warn(info + " " + lsi.toString() +
					" could not execute: " + e.getMessage() + "[" + e.getClass() + "]");
			state = Constants.State.ERROR;
		}

		if (expectedOutput != null) {
			File file = new File(expectedOutput);
			
			if (state.equals(Constants.State.OK) && !file.isFile()) {
				logger.warn(info + " " + lsi.toString() +
						" did not produce an output");
				state = Constants.State.ERROR;
			}
		}
		return state;
	}
}
