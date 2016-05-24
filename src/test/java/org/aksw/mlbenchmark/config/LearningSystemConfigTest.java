package org.aksw.mlbenchmark.config;

import org.aksw.mlbenchmark.BenchmarkRunner;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.Assert.assertTrue;

/**
 * Tests for LearningSystemConfig
 */
public class LearningSystemConfigTest {
	@Test
	public void testConfigFormat() {
		BaseHierarchicalConfiguration config = new BaseHierarchicalConfiguration();
		config.setProperty("learningsystems", Collections.singletonList("dllearner"));
		BenchmarkRunner benchmarkRunner = new BenchmarkRunner(config);
		LearningSystemConfig learningSystemConfig = new LearningSystemConfig(benchmarkRunner, "dllearner");
		String configFormat = learningSystemConfig.getConfigFormat();
		assertTrue("config format for dllearner does not equal prop ["+configFormat+"]", "prop".equals(configFormat));
	}
}
