package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.apache.commons.configuration2.BaseConfiguration;

import java.io.File;
import java.util.*;

/**
 * Single step of cross validation
 */
class CrossValidationStep extends CommonStep {
	protected CrossValidationRunner parent;
	private final int fold;
	private String trainingResultFile;

	public CrossValidationStep(CrossValidationRunner parent, ScenarioSystem ss, ConfigLoader learningProblemConfigLoader, int fold) {
		super(parent, ss, learningProblemConfigLoader);
		this.parent = parent;
		this.fold = fold;
	}

	public String getResultDir() {
		return CrossValidationRunner.getResultDir(ss, fold);
	}

	public String getResultKey() {
		return CrossValidationRunner.getResultKey(ss, fold);
	}

	public Set<String> getTrainingExamples(Constants.LANGUAGES lang, Constants.ExType type) {
		return parent.getLanguageFolds(lang).getTrainingSet(type, fold);
	}

	public Set<String> getValidateExamples(Constants.LANGUAGES lang, Constants.ExType type) {
		return parent.getLanguageFolds(lang).getTestingSet(type, fold);
	}

	protected BaseConfiguration getBaseConfiguration(File dir, String posFilename, String negFilename) {
		return parent.getBaseConfiguration(ss, fold, dir, posFilename, negFilename, trainingResultFile);
	}

	protected BaseConfiguration getValidateConfiguration(File dir, String posFilename, String negFilename, String outputFilename) {
		return parent.getValidateConfiguration(ss, new File(trainingResultFile), fold, dir, posFilename, negFilename, outputFilename);
	}

	protected void saveLearningSystemsConfig(String configFile) {
		parent.getBenchmarkRunner().getBenchmarkLog().saveLearningSystemConfig(ss, fold, configFile);
	}

	protected void saveResultSet() {
		parent.getBenchmarkRunner().getBenchmarkLog().saveResultSet(ss, fold, parent.getResultset());
	}
}
