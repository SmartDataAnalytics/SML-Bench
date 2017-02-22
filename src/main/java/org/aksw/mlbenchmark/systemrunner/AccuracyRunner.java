package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.*;
import org.aksw.mlbenchmark.container.ScenarioAttributes;
import org.aksw.mlbenchmark.container.ScenarioLang;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.aksw.mlbenchmark.examples.PosNegExamples;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoader;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoaderBase;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Simon Bin on 16-5-24.
 */
public class AccuracyRunner extends AbstractSystemRunner {
	final static Logger logger = LoggerFactory.getLogger(AccuracyRunner.class);
	private final Set<Constants.LANGUAGES> failedLang = new HashSet<>();
	private final Map<Constants.LANGUAGES, PosNegExamples> languageExamples = new HashMap<>();

	public AccuracyRunner(BenchmarkRunner parent, Scenario scn, Configuration baseConf) {
		super(parent, scn, baseConf);
		for (final Constants.LANGUAGES lang : parent.getDesiredLanguages()) {
			ScenarioLang sl = scn.addLanguage(lang);

			languageExamples.put(lang, new PosNegExamples());
			try {
				for (Constants.ExType ex : Constants.ExType.values()) {
					ExampleLoaderBase el = ExampleLoader.forLanguage(lang);
					el.loadExamples(sl.getExamplesFile(parent, ex));
					languageExamples.get(lang).put(ex, el.getExamples());
				}
			} catch (IOException e) {
				logger.warn("could not load examples for " + lang + ": " + e.getMessage());
				failedLang.add(lang);
			}
		}
	}

	@Override
	public void run() {
		for (final String system: parent.getDesiredSystems()) {
			final LearningSystemInfo lsi = parent.getSystemInfo(system);
			final Constants.LANGUAGES lang = lsi.getLanguage();
			if (failedLang.contains(lang)) {
				logger.warn("skipping system " + system + " because examples are missing");
				continue;
			}

			ScenarioSystem ss = scn.addSystem(lsi);
			
			CombinedConfiguration lsConfig = new CombinedConfiguration();
			lsConfig.setNodeCombiner(new MergeCombiner());
			lsConfig.addConfiguration(getBenchmarkRunner().getCommonsConfig());
			
			File lpSpecificConfigFile =
					new File(parent.getLearningProblemDir(ss), system);
			Configuration learningProblemConfig =
					ConfigLoader.findConfig(lpSpecificConfigFile.getAbsolutePath());

			if (learningProblemConfig != null) {
				lsConfig.addConfiguration(learningProblemConfig, system);
			}
			lsConfig.addProperty(Constants.MEASURES_KEY, parent.getConfig().getMeasures());
			lsConfig.addProperty(
					Constants.MAX_EXECUTION_TIME_KEY,
					parent.getConfig().getMaxExecutionTime());
			parent.getBenchmarkLog().saveLearningSystemInfo(lsi);

			logger.info("executing scenario " + ss.getTask() + "/" + ss.getProblem() + " with " + ss.getLearningSystem());

			CommonStep step = new AbsoluteStep(this, ss, learningProblemConfigLoader);
			step.train();

			if (step.isStateOk()) {
				step.validate();
			}
		}
	}
	
	/**
	 * @param scenarioSystem the Scenario and LearningSystem
	 * @return the key in the resultset configuration output for a single cross validation fold
	 */
	public static String getResultKey(ScenarioSystem scenarioSystem) {
		return scenarioSystem.getTask() + "." + scenarioSystem.getProblem() + "." + "absolute" + "." + scenarioSystem.getLearningSystem();
	}

	/**
	 * @param ss the Scenario and LearningSystem
	 * @return the relative subdirectory in the working directory for a single cross validation fold
	 */
	public static String getResultDir(ScenarioSystem ss) {
		return ss.getTask() + "/" + ss.getProblem() + "/" + "absolute" + "/" + ss.getLearningSystem();
	}

	public Set<String> getLanguageExamples(Constants.LANGUAGES lang, Constants.ExType ex) {
		return languageExamples.get(lang).get(ex);
	}


	public static BaseConfiguration getBaseConfiguration(ScenarioAttributes scn, File dir, String posFilename, String negFilename, String outputFile) {
		BaseConfiguration baseConfig = new BaseConfiguration();
		baseConfig.setProperty("data.workdir", dir.getAbsolutePath());
		baseConfig.setProperty("framework.absoluteAccuracy", true);
		baseConfig.setProperty("learningtask", scn.getTask());
		baseConfig.setProperty("learningproblem", scn.getProblem());
		baseConfig.setProperty("step", "train");

		baseConfig.setProperty("filename.pos", posFilename);
		baseConfig.setProperty("filename.neg", negFilename);
		baseConfig.setProperty("output", outputFile);
		return baseConfig;
	}

	public static BaseConfiguration getValidateConfiguration(ScenarioAttributes scn, File trainingResultFile, File dir, String posFilename, String negFilename, String outputFile) {
		BaseConfiguration baseConfig = getBaseConfiguration(scn, dir, posFilename, negFilename, outputFile);
		baseConfig.setProperty("step", "validate");
		baseConfig.setProperty("input", trainingResultFile.getAbsolutePath());
		return baseConfig;
	}
}
