/**
 * 
 */
package org.aksw.mlbenchmark;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Lorenz Buehmann
 * The main entry point to SMLBench
 */
public class Benchmark {
	static {
		if (System.getProperty("log4j.configuration") == null)
			System.setProperty("log4j.configuration", "log4j.properties");
	}

	static final Logger logger = LoggerFactory.getLogger(Benchmark.class);

	/**
	 * create a new BenchmarkRunner and store the resultset, optionally cleaning up afterwards
	 * @param args command line arguments: config file
	 */
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
		try {
                        // change the extension
			String resultOutput = runner.getConfig().getResultOutputFile(args[0].replaceAll("[.][^.]*$", "") + ".result.plist");
			File file = new File(resultOutput);
			logger.info("writing results to " + file.getAbsolutePath());
			ConfigLoader.write(runner.getResultset(), file);
		} catch (IOException | ConfigurationException | ConfigLoaderException e) {
			logger.error("Could not write results: " + e.getMessage());
		}
		runner.cleanTemp();

	}
}
