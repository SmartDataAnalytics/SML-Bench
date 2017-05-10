package org.aksw.mlbenchmark.systemrunner;

import java.util.Set;

import org.aksw.mlbenchmark.BenchmarkLog;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.aksw.mlbenchmark.examples.PosNegExamples;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.configuration2.Configuration;

/**
 * A step processing all the input examples (no folds, validation equals training)
 */
public class AbsoluteStep extends CommonStep {
	private final ScenarioSystem ss;

	public AbsoluteStep(Scenario scenario, LearningSystemInfo lsi,
			PosNegExamples examples, Configuration runtimeConfig,
			FileFinder fileFinder, BenchmarkLog log) {
		super(scenario, lsi, examples, runtimeConfig, fileFinder, log);
		this.ss = scenario.addSystem(lsi);
	}
	
	@Override
	protected String getResultKey() {
		return AccuracyRunner.getResultKey(scenario.addSystem(lsi));
	}

	@Override
	protected Set<String> getPositiveTrainingExamples() {
		return ((PosNegExamples) examples).get(Constants.ExType.POS);
	}

	@Override
	protected Set<String> getNegativeTrainingExamples() {
		return ((PosNegExamples) examples).get(Constants.ExType.NEG);
	}

	@Override
	protected Set<String> getPositiveValidationExamples() {
		return getPositiveTrainingExamples();
	}

	@Override
	protected Set<String> getNegativeValidationExamples() {
		return getNegativeTrainingExamples();
	}

	@Override
	protected void saveLearningSystemConfig(String configFilePath) {
		log.saveAbsoluteLearningSystemConfig(ss, configFilePath);
	}

	@Override
	protected void saveResultSet(Configuration result) {
		log.saveAbsoluteResultSet(ss, result);
	}
}
