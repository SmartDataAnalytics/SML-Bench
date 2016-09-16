package org.aksw.mlbenchmark.container;

import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.Scenario;

/**
 * A scenario and its system where it is run on
 */
public class ScenarioSystem implements ScenarioLangAttributes {
	private final Scenario scenario;
	private final LearningSystemInfo learningSystemInfo;

	public ScenarioSystem(Scenario scn, LearningSystemInfo learningSystemInfo) {
		this.scenario = scn;
		this.learningSystemInfo = learningSystemInfo;
	}

	@Override
	public Constants.LANGUAGES getLanguage() {
		return learningSystemInfo.getLanguage();
	}

	@Override
	public String getTask() {
		return scenario.getTask();
	}

	@Override
	public String getProblem() {
		return scenario.getProblem();
	}

	public String getLearningSystem() {
		return learningSystemInfo.asString();
	}

	public LearningSystemInfo getLearningSystemInfo() {
		return learningSystemInfo;
	}
}
