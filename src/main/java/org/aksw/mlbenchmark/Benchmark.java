/**
 * 
 */
package org.aksw.mlbenchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class Benchmark {
	static {
		if (System.getProperty("log4j.configuration") == null)
			System.setProperty("log4j.configuration", "log4j.properties");
	}

	static final Logger logger = LoggerFactory.getLogger(Benchmark.class);

	public static void main(String[] args) {
		logger.info("Welcome to SML-Bench");

		if (args.length != 1) {
			logger.error("The SML-Bench requires a config file name as its first argument.");
			System.exit(1);
		}

		BenchmarkRunner runner = null;
		try {
			runner = new BenchmarkRunner(args[0]);
		} catch (ConfigLoaderException e) {
			logger.error("There was a problem processing the config file: " + e.getMessage());
			System.exit(1);
		}

		runner.run();

	}
}
