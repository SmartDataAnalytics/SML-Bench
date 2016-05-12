package org.aksw.mlbenchmark.systemrunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.aksw.mlbenchmark.BenchmarkRunner;
import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.ConfigLoaderException;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.CrossValidation;
import org.aksw.mlbenchmark.ExampleLoader;
import org.aksw.mlbenchmark.LanguageInfo;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.MeasureMethod;
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

/**
 * Execute Cross Validation scenario
 */
public class CrossValidationRunner {
	final static Logger logger = LoggerFactory.getLogger(CrossValidationRunner.class);
	private final BenchmarkRunner parent;
	private final Set<String> failedLang = new HashSet<>();
	private final Map<String, CrossValidation> languageFolds = new HashMap<>();
	private final String task;
	private final String problem;
	private final Configuration parentConf; // the partial scenario config from the parent

	public CrossValidationRunner(BenchmarkRunner benchmarkRunner, String task, String problem, Configuration baseConf) {
		this.parent = benchmarkRunner;
		this.task = task;
		this.problem = problem;
		this.parentConf = baseConf;
		for (final String lang : parent.getDesiredLanguages()) {
			String lpDir = parent.getLearningProblemDir(task, problem, lang);
			try {
				ExampleLoaderBase elPos = ExampleLoader.forLanguage(lang);
				elPos.loadExamples(new File(lpDir+"/"+"pos"+ LanguageInfo.forLanguage(lang).exampleExtension()));
				LinkedHashSet<String> posExamples = elPos.getExamples();

				ExampleLoaderBase elNeg = ExampleLoader.forLanguage(lang);
				elNeg.loadExamples(new File(lpDir+"/"+"neg"+LanguageInfo.forLanguage(lang).exampleExtension()));
				LinkedHashSet<String> negExamples = elNeg.getExamples();

				CrossValidation cv = new CrossValidation(posExamples, negExamples, parent.getFolds(), parent.getSeed());

				languageFolds.put(lang, cv);
			} catch (IOException e) {
				logger.warn("could not load examples for "+lang+": " + e.getMessage());
				failedLang.add(lang);
			}
		}

		writeAllFolds(task, problem, languageFolds, failedLang);


	}

	private void writeAllFolds(String task, String problem, Map<String, CrossValidation> languageFolds, Set<String> failedLang) {
		File dir = new File(parent.getTempDirectory() + "/" + task + "/" + problem);
		dir.mkdirs();
		for (final String lang: parent.getDesiredLanguages()) {
			if (failedLang.contains(lang)) { continue; }
			try {
				CrossValidation crossValidation = languageFolds.get(lang);
				for (Constants.ExType ex : Constants.ExType.values()) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir + "/" + "folds-" + lang + "-" + ex.name().toLowerCase() + LanguageInfo.forLanguage(lang).exampleExtension())));
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
			final String lang = parent.getSystemLanguage(system);
			if (failedLang.contains(lang)) {
				logger.warn("skipping system "+system+" because examples are missing");
				continue;
			}

			/*			parent.getExecutorService().submit(
					new Runnable() {
						@Override
						public void run() { */
			ConfigLoader lpCL = ConfigLoader.findConfig(parent.getLearningProblemDir(task, problem, lang) + "/" + system);
			LearningSystemInfo lsi = parent.getSystemInfo(system);

			parent.getBenchmarkLog().saveLearningSystemInfo(system, lsi);

			for (int fold = 0; fold < parent.getFolds(); ++fold) {
				logger.info("executing scenario " + task + "/" + problem + " with " + system + ", fold " + fold);

				State state = trainingStep(system, lang, lpCL, lsi, fold);

			}

			/*						}
					}); */
		}
	}

	private State trainingStep(String system, String lang, ConfigLoader lpCL, LearningSystemInfo lsi, int fold) {
		File dir = new File(parent.getTempDirectory() + "/" + task + "/" + problem + "/" + "fold-" + fold + "/" + system + "/" + "train");
		dir.mkdirs();

		String posFilename = dir + "/" + lsi.getPosFilename();
		String negFilename = dir + "/" + lsi.getNegFilename();
		String outputFile = dir + "/" + "train.out";
		String configFile = dir + "/" + "config." + lsi.getConfig().getString("configFormat");

		LinkedHashSet<String> trainingPos = languageFolds.get(lang).getTrainingSet(Constants.ExType.POS, fold);
		LinkedHashSet<String> trainingNeg = languageFolds.get(lang).getTrainingSet(Constants.ExType.NEG, fold);

		BaseConfiguration baseConfig = getBaseConfiguration(fold, dir, posFilename, negFilename, outputFile);

		Configuration cc = collectConfig(system, lang, dir, baseConfig, lpCL, lsi);
		writeConfig(configFile, cc);
		writeFolds(lang, posFilename, trainingPos, negFilename, trainingNeg);

		List<String> args = new LinkedList<>();
		args.add(configFile);
		parent.getBenchmarkLog().saveLearningSystemConfig(system, task, problem, fold, configFile);

		final long now = System.nanoTime();
		State state = State.RUNNING;
		try {
			ProcessRunner processRunner = new ProcessRunner(parent.getLearningSystemDir(system), "./run", args, cc, parent.getConfig().getLong("framework.maxExecutionTime", Constants.DefaultMaxExecutionTime));
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
		File outputFileFile =  new File(outputFile);
		if (state.equals(State.OK) && !outputFileFile.isFile()) {
			logger.warn("learning system " + system + " did not produce an output");
			state = State.FAILURE;
		}
		String resultKey =  task + "." + problem + "." + "fold-" + fold + "." + system;
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

		if (!state.equals(State.OK)) {
			return state; // there was an error, no point in continuing
		}

		return validateStep(outputFileFile, system, lang, lpCL, lsi, fold);
	}

	private State validateStep(File trainingResultFile, String system, String lang, ConfigLoader lpCL, LearningSystemInfo lsi, int fold) {
		File dir = new File(parent.getTempDirectory() + "/" + task + "/" + problem + "/" + "fold-" + fold + "/" + system + "/" + "validate");
		dir.mkdirs();

		String posFilename = dir + "/" + lsi.getPosFilename();
		String negFilename = dir + "/" + lsi.getNegFilename();
		String outputFile = dir + "/" + "validateResult.prop";
		String configFile = dir + "/" + "config." + lsi.getConfig().getString("configFormat");

		LinkedHashSet<String> testingPos = languageFolds.get(lang).getTestingSet(Constants.ExType.POS, fold);
		LinkedHashSet<String> testingNeg = languageFolds.get(lang).getTestingSet(Constants.ExType.NEG, fold);

		BaseConfiguration baseConfig = getValidateConfiguration(trainingResultFile, fold, dir, posFilename, negFilename, outputFile);

		Configuration cc = collectConfig(system, lang, dir, baseConfig, lpCL, lsi);
		writeConfig(configFile, cc);
		writeFolds(lang, posFilename, testingPos, negFilename, testingNeg);

		List<String> args = new LinkedList<>();
		args.add(configFile);

		State state = State.RUNNING;
		try {
			ProcessRunner processRunner = new ProcessRunner(parent.getLearningSystemDir(system), "./validate", args, cc, 0);
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
		String resultKey =  task + "." + problem + "." + "fold-" + fold + "." + system;
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
			parent.getBenchmarkLog().saveResultSet(system, task, problem, fold, getResultset());
			return state;
		}

		if (result != null) {
			Iterator<String> keys = result.getKeys();
			while (keys.hasNext()) {
				String key = keys.next();
				getResultset().setProperty(resultKey + "." + "ValidationRaw" + "." + key, result.getProperty(key));
			}
		}
		List<String> measures = parent.getConfig().getList(String.class, "measures", Arrays.asList("pred_acc"));
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

		parent.getBenchmarkLog().saveResultSet(system, task, problem, fold, getResultset());
		return state;
	}

	enum State { RUNNING, OK, TIMEOUT, FAILURE, ERROR };

	private Configuration getResultset() {
		return parent.getResultset();
	}

	private void writeConfig(String configFile, Configuration cc) {
		try {
			ConfigLoader.write(cc, new File(configFile));
		} catch (IOException | ConfigLoaderException | ConfigurationException e) {
			throw new RuntimeException("Writing scenario config failed", e);
		}
	}

	private void writeFolds(String lang, String posFilename, LinkedHashSet<String> testingPos, String negFilename, LinkedHashSet<String> testingNeg) {
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

	private BaseConfiguration getBaseConfiguration(int fold, File dir, String posFilename, String negFilename, String outputFile) {
		BaseConfiguration baseConfig = new BaseConfiguration();
		baseConfig.setProperty("data.workdir", dir.getAbsolutePath());
		baseConfig.setProperty("framework.currentFold", fold);
		baseConfig.setProperty("learningtask", task);
		baseConfig.setProperty("learningproblem", problem);
		baseConfig.setProperty("step", "train");

		baseConfig.setProperty("filename.pos", posFilename);
		baseConfig.setProperty("filename.neg", negFilename);
		baseConfig.setProperty("output", outputFile);
		return baseConfig;
	}

	private BaseConfiguration getValidateConfiguration(File trainingResultFile, int fold, File dir, String posFilename, String negFilename, String outputFile) {
		BaseConfiguration baseConfig = getBaseConfiguration(fold, dir, posFilename, negFilename, outputFile);
		baseConfig.setProperty("step", "validate");
		baseConfig.setProperty("input", trainingResultFile.getAbsolutePath());
		return baseConfig;
	}

	private Configuration collectConfig(String system, String lang, File dir, BaseConfiguration baseConfig, ConfigLoader lpCL, LearningSystemInfo lsi) {
		Configuration runtimeConfig = parent.getConfig();
		// Configuration stepConfig = parentConf;
		Configuration scnRuntimeConfig = runtimeConfig.subset("learningtask." + task);
		Configuration lpRuntimeConfig = runtimeConfig.subset("learningproblem." + task + "." + problem);

		CombinedConfiguration cc = new CombinedConfiguration();
		cc.setNodeCombiner(new MergeCombiner());
		cc.addConfiguration(baseConfig);
		cc.addConfiguration(parentConf);
		cc.addConfiguration(lpRuntimeConfig);
		if (lpCL != null) {
			cc.addConfiguration(lpCL.config());
		}
		List<String> families = lsi.getConfig().getList(String.class, "families");
		if (families != null) {
			for (String family : families) {
				ConfigLoader famLpCL = ConfigLoader.findConfig(parent.getLearningProblemDir(task, problem, lang) + "/" + family);
				if (famLpCL != null) {
					cc.addConfiguration(famLpCL.config());
				}
			}
		}
		cc.addConfiguration(scnRuntimeConfig);
		cc.addConfiguration(lsi.getConfig());
		cc.addConfiguration(parent.getConfig());
		BaseConfiguration defaultConfig = new BaseConfiguration();
		defaultConfig.setProperty("maxExecutionTime", (long)(cc.getLong("framework.maxExecutionTime", Constants.DefaultMaxExecutionTime)*0.86));
		cc.addConfiguration(defaultConfig);
		return cc;
	}
}
