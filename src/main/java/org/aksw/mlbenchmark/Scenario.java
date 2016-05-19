package org.aksw.mlbenchmark;

/**
 * Created by Simon Bin on 16-5-19.
 */
public class Scenario {
	private final String task;
	private final String problem;

	public Scenario(String task, String problem) {
		this.task = task;
		this.problem = problem;
	}

	public String getProblem() {
		return problem;
	}

	public String getTask() {
		return task;
	}

}
