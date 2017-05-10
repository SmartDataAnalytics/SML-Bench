package org.aksw.mlbenchmark.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.ConfigLoaderException;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

public class BenchmarkConfigTest {
	private String testResourcesDir = "src/test/resources/";

	@Test
	public void testGetLearningSystemConfiguration() throws ConfigLoaderException {
		File fileName = new File(testResourcesDir, "benchmark_config2.conf");
		
		/* learningsystems=aleph,dllearner-1,dllearner-2,progol
		 * 
		 * learningsystems.dllearner-1.algorithm.type = celoe
		 * learningsystems.dllearner-1.algorithm.maxClassExpressionTests = 54321
		 * learningsystems.dllearner-2.algorithm.type = ocel
		 * learningsystems.dllearner-2.algorithm.maxExecutionTimeInSeconds = 63
		 * learningsystems.aleph.caching = ON
		 */
		BenchmarkConfig benchmarkConfig = new BenchmarkConfig(
				ConfigLoader.load(fileName.getAbsolutePath()));
		FileFinder fileFinder = new FileFinder(new File("."));
		
		LearningSystemInfo lsi1 = new LearningSystemInfo(
				benchmarkConfig, "aleph", fileFinder);
		LearningSystemInfo lsi2 = new LearningSystemInfo(
				benchmarkConfig, "dllearner-1", fileFinder);
		LearningSystemInfo lsi3 = new LearningSystemInfo(
				benchmarkConfig, "dllearner-2", fileFinder);
		LearningSystemInfo lsi4 = new LearningSystemInfo(
				benchmarkConfig, "progol", fileFinder);
		
		// aleph
		Configuration lsConf1 = benchmarkConfig.getLearningSystemConfiguration(lsi1);
		BaseHierarchicalConfiguration expectedConf = new BaseHierarchicalConfiguration();
		expectedConf.setProperty("caching", "ON");
		
		assertEquals(IteratorUtils.toList(expectedConf.getKeys()),
				IteratorUtils.toList(lsConf1.getKeys()));
		assertEquals(expectedConf.getString("caching"), lsConf1.getString("caching"));
		
		// dllearner-1
		Configuration lsConf2 = benchmarkConfig.getLearningSystemConfiguration(lsi2);
		expectedConf = new BaseHierarchicalConfiguration();
		expectedConf.setProperty("algorithm.type", "celoe");
		expectedConf.setProperty("algorithm.maxClassExpressionTests", 54321);
		
		assertEquals(IteratorUtils.toList(expectedConf.getKeys()),
				IteratorUtils.toList(lsConf2.getKeys()));
		assertEquals(expectedConf.getString("algorithm.type"),
				lsConf2.getString("algorithm.type"));
		assertEquals(expectedConf.getInt("algorithm.maxClassExpressionTests"),
				lsConf2.getInt("algorithm.maxClassExpressionTests"));
		
		// dllearner-2
		Configuration lsConf3 = benchmarkConfig.getLearningSystemConfiguration(lsi3);
		expectedConf = new BaseHierarchicalConfiguration();
		expectedConf.setProperty("algorithm.type", "ocel");
		expectedConf.setProperty("algorithm.maxExecutionTimeInSeconds", 63);
		
		assertEquals(IteratorUtils.toList(expectedConf.getKeys()),
				IteratorUtils.toList(lsConf3.getKeys()));
		assertEquals(expectedConf.getString("algorithm.type"),
				lsConf3.getString("algorithm.type"));
		assertEquals(expectedConf.getInt("algorithm.maxExecutionTimeInSeconds"),
				lsConf3.getInt("algorithm.maxExecutionTimeInSeconds"));
		
		// progol
		Configuration lsConf4 = benchmarkConfig.getLearningSystemConfiguration(lsi4);
		assertTrue(lsConf4.isEmpty());
	}
}
