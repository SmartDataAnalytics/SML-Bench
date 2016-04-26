package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.process.ProcessRunner;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Simon Bin on 16-4-13.
 */
public class BenchmarkRunner {
	static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
	private final String currentDir;
	private final URL sourceDir;
	private final String rootDir;
	private List<String> availableLearningSystems = new LinkedList<>();
	private final HierarchicalConfiguration<ImmutableNode> config;


	public BenchmarkRunner(HierarchicalConfiguration<ImmutableNode> config) {
		currentDir = new File(".").getAbsolutePath();
		sourceDir = BenchmarkRunner.class.getProtectionDomain().getCodeSource().getLocation();
		logger.info("source dir = " + sourceDir);
		rootDir = new File(sourceDir.getFile()+"/../..").getAbsolutePath();
		logger.info("root = " + rootDir);
		for (File file: new File(rootDir+"/"+Constants.LEARNINGSYSTEMS).listFiles()) {
			if (file.isDirectory()) {
				availableLearningSystems.add(file.getName());
			}
		}
		logger.info("available learning systems:" + availableLearningSystems);
		this.config = config;
	}

	public String getLearningSystemDir(String learningSystem) {
		return getLearningSystemsDir()+"/"+learningSystem;
	}
	public String getLearningSystemsDir() {
		return rootDir+"/"+ Constants.LEARNINGSYSTEMS;
	}
	public String getLearningTasksDir() {
		return rootDir+"/"+Constants.LEARNINGTASKS;
	}
	public String getLearningProblemDir(String learningTask, String learningProblem, String languageType) {
		return rootDir+"/"+Constants.LEARNINGTASKS+"/"+languageType+"/"+Constants.LEARNINGPROBLEMS+"/"+learningProblem;
	}

	public BenchmarkRunner(String configFilename) throws ConfigLoaderException {
		// load the properties file
		this(new ConfigLoader(configFilename).load().config());
	}

	public void run() throws IOException {
		List<String> desiredSystems = config.getList(String.class, "learningsystems");
		logger.info("benchmarking systems: " + desiredSystems);
		Path tempDirectory = Files.createTempDirectory(new File(currentDir).toPath(), "sml-temp");
		for (String system: desiredSystems) {
			try {
				ProcessRunner processRunner = new ProcessRunner(getLearningSystemDir(system), "./run");
			} catch (IOException e) {
				logger.warn("learning system "+ system + " could not execute: " + e.getMessage());
			}
		}
		Testing testing = new Testing();
		testing.conf(config);
	}
}
