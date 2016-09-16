package org.aksw.mlbenchmark.container;

import org.aksw.mlbenchmark.BenchmarkRunner;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.Scenario;

/**
 * Scenario with language
 */
public class ScenarioLang implements ScenarioLangAttributes {
	private final Scenario scenario;
	private final Constants.LANGUAGES language;

	public ScenarioLang(Scenario scn, Constants.LANGUAGES lang) {
		this.scenario = scn;
		this.language = lang;
	}

	public String getProblem() {
		return scenario.getProblem();
	}

	public String getTask() {
		return scenario.getTask();
	}

	public Scenario getScenario() {
		return scenario;
	}

	public String getExamplesFile(BenchmarkRunner br, Constants.ExType type) {
		return br.getExamplesFile(this, type);
	}

	public Constants.LANGUAGES getLanguage() {
		return language;
	}
}
