package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.apache.commons.configuration2.BaseConfiguration;

import java.io.File;
import java.util.Set;

/**
 * A step processing all the input examples (no folds, validation equals training)
 */
public class AbsoluteStep extends CommonStep {
	protected final AccuracyRunner parent;

	public AbsoluteStep(AccuracyRunner parent, ScenarioSystem ss, ConfigLoader learningProblemConfigLoader) {
		super(parent, ss, learningProblemConfigLoader);
		this.parent = parent;
	}

	public String getResultDir() {
		return parent.getResultDir(ss);
	}

	public String getResultKey() {
		return parent.getResultKey(ss);
	}

	public Set<String> getLanguageExamples(Constants.LANGUAGES lang, Constants.ExType type) {
		return parent.getLanguageExamples(lang, type);
	}

	public Set<String> getTrainingExamples(Constants.LANGUAGES lang, Constants.ExType type) {
		return getLanguageExamples(lang, type);
	}

	public Set<String> getValidateExamples(Constants.LANGUAGES lang, Constants.ExType type) {
		return getLanguageExamples(lang, type);
	}

	@Override
	protected void saveLearningSystemsConfig(String configFile) {
		parent.getBenchmarkRunner().getBenchmarkLog().saveAbsoluteLearningSystemConfig(ss, configFile);
	}

	@Override
	protected void saveResultSet() {
		parent.getBenchmarkRunner().getBenchmarkLog().saveAbsoluteResultSet(ss, parent.getResultset());
	}

	protected BaseConfiguration getBaseConfiguration(File dir, String posFilename, String negFilename) {
		return parent.getBaseConfiguration(ss, dir, posFilename, negFilename, trainingResultFile);
	}

	@Override
	protected BaseConfiguration getValidateConfiguration(File dir, String posFilename, String negFilename, String outputFilename) {
		return parent.getValidateConfiguration(ss, new File(trainingResultFile), dir, posFilename, negFilename, outputFilename);
	}

	@Override
	public void validate() {

	}
}
