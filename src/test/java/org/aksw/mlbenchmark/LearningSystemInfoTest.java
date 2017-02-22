package org.aksw.mlbenchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.junit.Before;
import org.junit.Test;

public class LearningSystemInfoTest {
	private LearningSystemInfo lsi1;
	private LearningSystemInfo lsi2;

	@Before
	public void setUp() {
		BaseHierarchicalConfiguration baseConf = new BaseHierarchicalConfiguration();
		baseConf.setProperty("learningsystems", "aleph");
		BenchmarkConfig benchmarkConfig = new BenchmarkConfig(baseConf);
		FileFinder fileFinder = new FileFinder(new File("."));
		String learningSystemName = "aleph";
		String id = "abc";
		String learningSystem1 = learningSystemName +
				Constants.LEARNINGSYSTEM_ID_SEPARATOR + id;
		
		lsi1 = new LearningSystemInfo(benchmarkConfig, learningSystem1, fileFinder);
		lsi2 = new LearningSystemInfo(benchmarkConfig, learningSystemName, fileFinder);
	}
	
	@Test
	public void testAsString() {
		String expectedString1 =
				"aleph" + Constants.LEARNINGSYSTEM_ID_SEPARATOR + "abc";
		assertEquals(expectedString1, lsi1.asString());
	
		String expectedString2 = "aleph";
		assertEquals(expectedString2, lsi2.asString());
	}

	@Test
	public void testGetLearningSystem() {
		String expectedLearningSystem = "aleph";
		
		assertEquals(expectedLearningSystem, lsi1.getLearningSystem());
		assertEquals(expectedLearningSystem, lsi2.getLearningSystem());
	}

	@Test
	public void testGetIdentifier() {
		String expectedIdentifier1 = "abc";
		
		assertEquals(expectedIdentifier1, lsi1.getIdentifier());
		assertTrue(lsi2.getIdentifier() == null);
	}

	@Test
	public void testHasType() {
		assertTrue(lsi1.hasType("aleph"));
		assertTrue(lsi2.hasType("aleph"));
		
		// since "aleph-abc" is not a type, but a full name
		assertFalse(lsi1.hasType("aleph-abc"));
		assertFalse(lsi1.hasType("dllearner"));
		// case sensitivity
		assertFalse(lsi1.hasType("Aleph"));
	}
}
