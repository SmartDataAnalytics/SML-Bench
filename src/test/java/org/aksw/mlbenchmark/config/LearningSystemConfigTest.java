package org.aksw.mlbenchmark.config;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collections;

import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.junit.Test;

/**
 * Tests for LearningSystemConfig
 */
public class LearningSystemConfigTest {
	@Test
	public void testConfigFormat() {
		BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
		config.setProperty("learningsystems", Collections.singletonList("dllearner"));
		BenchmarkConfig benchmarkConfig = new BenchmarkConfig(config);
		
		FileFinder fileFinder = new FileFinder(new File("."));

		LearningSystemInfo lsi = new LearningSystemInfo(
				benchmarkConfig, "dllearner", fileFinder);
		LearningSystemConfig learningSystemConfig =
				new LearningSystemConfig(benchmarkConfig, lsi);
		
		String configFormat = learningSystemConfig.getConfigFormat();
		
		assertEquals("prop", configFormat);
	}
}
