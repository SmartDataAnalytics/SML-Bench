package org.aksw.mlbenchmark;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * Created by Simon Bin on 16-4-13.
 */
public class BenchmarkRunner {
	static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
	private final String currentDir;
	private final URL sourceDir;
	private final HierarchicalConfiguration<ImmutableNode> config;


	public BenchmarkRunner(HierarchicalConfiguration<ImmutableNode> config) {
		currentDir = new File(".").getAbsolutePath();
		sourceDir = BenchmarkRunner.class.getProtectionDomain().getCodeSource().getLocation();
		this.config = config;
	}


	public BenchmarkRunner(String configFilename) throws ConfigLoaderException {
		// load the properties file
		this(new ConfigLoader(configFilename).load().getConfig());
	}

	public void run() {
	}
}
