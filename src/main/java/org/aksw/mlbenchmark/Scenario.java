package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.container.ScenarioAttributes;
import org.aksw.mlbenchmark.container.ScenarioLang;
import org.aksw.mlbenchmark.container.ScenarioSystem;

/**
 * Container for scenario (task + problem)
 */
public class Scenario implements ScenarioAttributes {
	private final String task;
	private final String problem;

	public Scenario(String task, String problem) {
		this.task = task;
		this.problem = problem;
	}

	@Override
	public String getProblem() {
		return problem;
	}

	@Override
	public String getTask() {
		return task;
	}

	public ScenarioLang addLanguage(Constants.LANGUAGES lang) {
		return new ScenarioLang(this, lang);
	}

	/**
	 * Gets the learning task and learning problem IDs from a string like, e.g.
	 *
	 *   carcinogenesis/42
	 *
	 * Here 'carcinogenesis' is the ID of the learning task (which determines
	 * the background knowledge base and bias declarations to be used), and
	 * '42' is the learning problem ID (which determines the sets of training
	 * examples and optional, learning problem-specific settings).
	 */
	public static Scenario fromString(String scn) {
		final String[] split = scn.split("/");
		final String task = split[0];
		final String problem = split[1];
		return new Scenario(task, problem);
	}

	public ScenarioSystem addSystem(LearningSystemInfo system) {
		return new ScenarioSystem(this, system);
	}
	
	@Override
	public String toString() {
		return task + "/" + problem;
	}
}
