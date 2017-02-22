package org.aksw.mlbenchmark.systemrunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.mlbenchmark.BenchmarkRunner;
import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.container.ScenarioLang;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.aksw.mlbenchmark.examples.CrossValidation;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoader;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoaderBase;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute Cross Validation scenario
 */
public class CrossValidationRunner extends AbstractSystemRunner {
	final static Logger logger = LoggerFactory.getLogger(CrossValidationRunner.class);
	private final Set<Constants.LANGUAGES> failedLang = new HashSet<>();
	private final Map<Constants.LANGUAGES, CrossValidation> languageFolds = new HashMap<>();

	public CrossValidationRunner(BenchmarkRunner benchmarkRunner, Scenario scn, Configuration baseConf) {
		super(benchmarkRunner, scn, baseConf);
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
				logger.warn("could not load examples for " + lang + ": " + e.getMessage());
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
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							new File(dir + "/" + "folds-" + lang + "-" + ex.asString() +
									lang.getInfo().exampleExtension())));

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

	public CrossValidation getLanguageFolds(Constants.LANGUAGES lang) {
		return languageFolds.get(lang);
	}

	@Override
	public void run() {
		FileFinder fileFinder = new FileFinder(parent.getRootDir(), scn);
		
		for (final String system : parent.getDesiredSystems()) {
			final LearningSystemInfo lsi = parent.getSystemInfo(system);
			fileFinder = fileFinder.updateLearningSytemInfo(lsi);
			
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
			ConfigLoader learningProblemConfigLoader =
					ConfigLoader.findConfig(parent.getLearningProblemDir(ss) + "/" + system);

			parent.getBenchmarkLog().saveLearningSystemInfo(lsi);

			for (int fold = 0; fold < parent.getFolds(); ++fold) {
				fileFinder = fileFinder.updateWorkDir(new File(parent.getTempDirectory().toString(), getResultDir(ss, fold)));
				logger.info("executing scenario " + ss.getTask() + "/" +
						ss.getProblem() + " with " + ss.getLearningSystem() +
						", fold " + fold);

				CommonStep step = new CrossValidationStep(this, ss, learningProblemConfigLoader, fold);
				step.train();

				if (step.isStateOk()) {
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

	public static BaseConfiguration getBaseConfiguration(ScenarioAttributes scn,
			int fold, File dir, String posFilename, String negFilename, String outputFile) {

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

	public static BaseConfiguration getValidateConfiguration(ScenarioAttributes scn,
			File trainingResultFile, int fold, File dir, String posFilename,
			String negFilename, String outputFile) {
		BaseConfiguration baseConfig = getBaseConfiguration(scn, fold, dir, posFilename, negFilename, outputFile);
		baseConfig.setProperty("step", "validate");
		baseConfig.setProperty("input", trainingResultFile.getAbsolutePath());
		return baseConfig;
	}

}
