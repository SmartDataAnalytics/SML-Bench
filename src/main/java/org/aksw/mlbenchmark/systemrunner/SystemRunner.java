package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.BenchmarkRunner;
import org.apache.commons.configuration2.Configuration;

/**
 * System runner interface
 */
public interface SystemRunner {

	BenchmarkRunner getBenchmarkRunner();

	Configuration getParentConfiguration();

	void run();

	Configuration getResultset();
}
