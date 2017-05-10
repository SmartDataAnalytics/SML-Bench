package org.aksw.mlbenchmark.systemrunner;

import java.util.Set;

import org.aksw.mlbenchmark.BenchmarkLog;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.aksw.mlbenchmark.examples.CrossValidation;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.configuration2.Configuration;

/**
 * Single step of cross validation
 */
class CrossValidationStep extends CommonStep {
	private final int fold;
	private final ScenarioSystem ss;

	public CrossValidationStep(Scenario scenario, LearningSystemInfo lsi,
			CrossValidation examples, Configuration runtimeConfig, int fold,
			FileFinder fileFinder, BenchmarkLog log) {
		
		super(scenario, lsi, examples, runtimeConfig, fileFinder, log);
		this.fold = fold;
		this.ss = scenario.addSystem(lsi);
	}
	
	@Override
	public Set<String> getPositiveTrainingExamples() {
		return ((CrossValidation) examples).getTrainingSet(Constants.ExType.POS, this.fold);
	}
	
	@Override
	public Set<String> getNegativeTrainingExamples() {
		return ((CrossValidation) examples).getTrainingSet(Constants.ExType.NEG, this.fold);
	}

	@Override
	public Set<String> getPositiveValidationExamples() {
		return ((CrossValidation) examples).getTestingSet(Constants.ExType.POS, this.fold);
	}
	
	@Override
	public Set<String> getNegativeValidationExamples() {
		return ((CrossValidation) examples).getTestingSet(Constants.ExType.NEG, this.fold);
	}

	@Override
	protected void saveLearningSystemConfig(String configFilePath) {
		log.saveLearningSystemConfig(ss, fold, configFilePath);
	}

	@Override
	protected void saveResultSet(Configuration result) {
		log.saveResultSet(ss, fold, result);
	}
	
	@Override
	public String getResultKey() {
		return ss.getTask() + "." + ss.getProblem() + "." + "fold-" + fold + "." + ss.getLearningSystem();
	}
}
