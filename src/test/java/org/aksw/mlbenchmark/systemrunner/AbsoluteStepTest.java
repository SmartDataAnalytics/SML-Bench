package org.aksw.mlbenchmark.systemrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.aksw.mlbenchmark.BenchmarkLog;
import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.examples.PosNegExamples;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class AbsoluteStepTest {
	String resourcesDir = "src/test/resources/";
	HierarchicalConfiguration<ImmutableNode> runtimeConfig;
	Scenario scenario;
	File tmpDir;
	FileFinder fileFinder;
	PosNegExamples examples;
	BenchmarkLog log;

	@Before
	public void setUp() throws Exception {
		/*
		 * learningsystems=aleph,dllearner,dllearner-1,dllearner-2,progol
		 * 
		 * learningsystems.dllearner-1.algorithm.type = celoe
		 * learningsystems.dllearner-1.algorithm.maxClassExpressionTests = 54321
		 * learningsystems.dllearner-2.algorithm.type = ocel
		 * learningsystems.dllearner-2.algorithm.maxExecutionTimeInSeconds = 63
		 * learningsystems.dllearner.lp.type = "posnegstandard"
		 * learningsystems.aleph.caching = ON
		 * 
		 * scenarios=animals/*
		 */
		runtimeConfig = ConfigLoader.load(
				new File(resourcesDir,
						"benchmark_config_with_ls_specific_settings.conf").getAbsolutePath());
		
		scenario = new Scenario("animals", "mammal");
		
		tmpDir = Files.createTempDir();
		tmpDir.mkdirs();
		
		fileFinder = new FileFinder(new File(".").getAbsolutePath(), scenario);
		// arbitrarily set to tmpDir
		fileFinder = fileFinder.updateWorkDir(tmpDir);
		
		examples = new PosNegExamples();
		examples.put(Constants.ExType.POS, Sets.newHashSet("one", "two", "three"));
		examples.put(Constants.ExType.NEG, Sets.newHashSet("four", "five", "six", "seven"));
		log = new BenchmarkLog();
	}

	@After
	public void tearDown() {
		tmpDir.delete();
	}
	
	@Test
	public void testCollectTrainingConfig() {
		/*
		 * aleph:
		 * 
		 * settings.caching = ON
		 */
		
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		AbsoluteStep step = new AbsoluteStep(scenario, lsi, examples,
				runtimeConfig, alephFileFinder, log);
		
		Configuration collectedConfig = step.collectTrainingConfig(runtimeConfig);
		
		/*
		 * Check general config part: workdir, pos, neg, output, learningtask,
		 * learningproblem, step
		 */
		HashSet<String> expectedKeys = Sets.newHashSet(
				Constants.WORKDIR_KEY,
				Constants.POS_EXAMPLE_FILE_KEY,
				Constants.NEG_EXAMPLE_FILE_KEY,
				Constants.OUTPUT_FILE_KEY,
				Constants.LEARNING_TASK_KEY,
				Constants.LEARNING_PROBLEM_KEY,
				Constants.STEP_KEY);
		int expectedNumberOfLSSpecificSettings = 1;
		
		assertEquals(
				expectedKeys.size() + expectedNumberOfLSSpecificSettings,
				collectedConfig.size());
		
		HashSet<String> testKeys = Sets.newHashSet(collectedConfig.getKeys());
		for (String key : expectedKeys) {
			assertTrue(testKeys.contains(key));
		}
		
		assertEquals(scenario.getTask(),
				collectedConfig.getString(Constants.LEARNING_TASK_KEY));
		assertEquals(scenario.getProblem(),
				collectedConfig.getString(Constants.LEARNING_PROBLEM_KEY));
		assertEquals(Constants.STEP_TRAIN,
				collectedConfig.getString(Constants.STEP_KEY));
		
		// Test learning system-specific settings
		assertEquals(expectedNumberOfLSSpecificSettings,
				collectedConfig.subset(Constants.LS_SPECIFIC_SETTINGS_KEY).size());
		Map<String,Object> expectedSettings = new HashMap<>();
		expectedSettings.put(Constants.LS_SPECIFIC_SETTINGS_KEY + ".caching", "ON");

		Iterator<String> keysIt = collectedConfig.getKeys(Constants.LS_SPECIFIC_SETTINGS_KEY);
		String key;
		Object value;
		while (keysIt.hasNext()) {
			key = keysIt.next();
			value = collectedConfig.get(Object.class, key);
			assertTrue(expectedSettings.containsKey(key));
			assertEquals(expectedSettings.get(key), value);
		}
		
		/*
		 * dllearner
		 * 
		 * settings.lp.type = "posnegstandard"
		 */
		lsi = new LearningSystemInfo(new BenchmarkConfig(runtimeConfig), "dllearner", fileFinder);
		FileFinder dllearnerFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		step = new AbsoluteStep(scenario, lsi, examples, runtimeConfig, dllearnerFileFinder, log);
		collectedConfig = step.collectTrainingConfig(runtimeConfig);
		
		// Test general config part
		expectedNumberOfLSSpecificSettings = 1;
		assertEquals(
				expectedKeys.size() + expectedNumberOfLSSpecificSettings,
				collectedConfig.size());
		
		testKeys = Sets.newHashSet(collectedConfig.getKeys());
		for (String k : expectedKeys) {
			assertTrue(testKeys.contains(k));
		}
		
		assertEquals(scenario.getTask(),
				collectedConfig.getString(Constants.LEARNING_TASK_KEY));
		assertEquals(scenario.getProblem(),
				collectedConfig.getString(Constants.LEARNING_PROBLEM_KEY));
		assertEquals(Constants.STEP_TRAIN,
				collectedConfig.getString(Constants.STEP_KEY));
		
		// Test learning system-specific settings
		assertEquals(
				expectedNumberOfLSSpecificSettings,
				collectedConfig.subset(Constants.LS_SPECIFIC_SETTINGS_KEY).size());
		expectedSettings = new HashMap<>();
		expectedSettings.put(
				Constants.LS_SPECIFIC_SETTINGS_KEY + ".lp.type", "posnegstandard");

		keysIt = collectedConfig.getKeys(Constants.LS_SPECIFIC_SETTINGS_KEY);
		while (keysIt.hasNext()) {
			key = keysIt.next();
			value = collectedConfig.get(Object.class, key);
			assertTrue(expectedSettings.containsKey(key));
			assertEquals(expectedSettings.get(key), value);
		}
		
		/*
		 * dllearner-1
		 * 
		 * settings.algorithm.type = celoe
		 * settings.algorithm.maxClassExpressionTests = 54321
		 * settings.lp.type = "posnegstandard"
		 */
		lsi = new LearningSystemInfo(new BenchmarkConfig(runtimeConfig), "dllearner-1", fileFinder);
		FileFinder dllearnerFileFinder1 = fileFinder.updateLearningSytemInfo(lsi);
		step = new AbsoluteStep(scenario, lsi, examples, runtimeConfig, dllearnerFileFinder1, log);
		collectedConfig = step.collectTrainingConfig(runtimeConfig);
		
		// Test general config part
		expectedNumberOfLSSpecificSettings = 3;
		assertEquals(
				expectedKeys.size() + expectedNumberOfLSSpecificSettings,
				collectedConfig.size());
		
		testKeys = Sets.newHashSet(collectedConfig.getKeys());
		for (String k : expectedKeys) {
			assertTrue(testKeys.contains(k));
		}
		
		assertEquals(scenario.getTask(),
				collectedConfig.getString(Constants.LEARNING_TASK_KEY));
		assertEquals(scenario.getProblem(),
				collectedConfig.getString(Constants.LEARNING_PROBLEM_KEY));
		assertEquals(Constants.STEP_TRAIN,
				collectedConfig.getString(Constants.STEP_KEY));
		
		// Test learning system-specific settings
		assertEquals(
				expectedNumberOfLSSpecificSettings,
				collectedConfig.subset(Constants.LS_SPECIFIC_SETTINGS_KEY).size());
		
		expectedSettings = new HashMap<>();
		expectedSettings.put(
				Constants.LS_SPECIFIC_SETTINGS_KEY + ".algorithm.type", "celoe");
		expectedSettings.put(
				Constants.LS_SPECIFIC_SETTINGS_KEY + ".algorithm.maxClassExpressionTests",
				"54321");
		expectedSettings.put(
				Constants.LS_SPECIFIC_SETTINGS_KEY + ".lp.type", "posnegstandard");

		keysIt = collectedConfig.getKeys(Constants.LS_SPECIFIC_SETTINGS_KEY);
		while (keysIt.hasNext()) {
			key = keysIt.next();
			value = collectedConfig.get(Object.class, key);
			assertTrue(expectedSettings.containsKey(key));
			assertEquals(expectedSettings.get(key), value);
		}
		
		/*
		 * dllearner-2
		 * 
		 * settings.algorithm.type = ocel
		 * settings.algorithm.maxExecutionTimeInSeconds = 63
		 * settings.lp.type = "posnegstandard"
		 */
		lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "dllearner-2", fileFinder);
		
		FileFinder dllearnerFileFinder2 = fileFinder.updateLearningSytemInfo(lsi);
		step = new AbsoluteStep(
				scenario, lsi, examples, runtimeConfig, dllearnerFileFinder2, log);
		
		collectedConfig = step.collectTrainingConfig(runtimeConfig);
		
		// Test general config part
		expectedNumberOfLSSpecificSettings = 3;
		assertEquals(
				expectedKeys.size() + expectedNumberOfLSSpecificSettings,
				collectedConfig.size());
		
		testKeys = Sets.newHashSet(collectedConfig.getKeys());
		for (String k : expectedKeys) {
			assertTrue(testKeys.contains(k));
		}
		
		assertEquals(scenario.getTask(),
				collectedConfig.getString(Constants.LEARNING_TASK_KEY));
		assertEquals(scenario.getProblem(),
				collectedConfig.getString(Constants.LEARNING_PROBLEM_KEY));
		assertEquals(Constants.STEP_TRAIN,
				collectedConfig.getString(Constants.STEP_KEY));
		
		// Test learning system-specific settings
		assertEquals(
				expectedNumberOfLSSpecificSettings,
				collectedConfig.subset(Constants.LS_SPECIFIC_SETTINGS_KEY).size());
		
		expectedSettings = new HashMap<>();
		expectedSettings.put(
				Constants.LS_SPECIFIC_SETTINGS_KEY + ".algorithm.type", "ocel");
		expectedSettings.put(
				Constants.LS_SPECIFIC_SETTINGS_KEY + ".algorithm.maxExecutionTimeInSeconds",
				"63");
		expectedSettings.put(
				Constants.LS_SPECIFIC_SETTINGS_KEY + ".lp.type", "posnegstandard");

		keysIt = collectedConfig.getKeys(Constants.LS_SPECIFIC_SETTINGS_KEY);
		while (keysIt.hasNext()) {
			key = keysIt.next();
			value = collectedConfig.get(Object.class, key);
			assertTrue(expectedSettings.containsKey(key));
			assertEquals(expectedSettings.get(key), value);
		}
		
		/*
		 * progol
		 * 
		 * <no learning problem-specific settings>
		 */
		lsi = new LearningSystemInfo(new BenchmarkConfig(runtimeConfig), "progol", fileFinder);
		FileFinder progolFileFinder2 = fileFinder.updateLearningSytemInfo(lsi);
		step = new AbsoluteStep(scenario, lsi, examples, runtimeConfig, progolFileFinder2, log);
		collectedConfig = step.collectTrainingConfig(runtimeConfig);
		
		// Test general config part
		expectedNumberOfLSSpecificSettings = 0;
		assertEquals(
				expectedKeys.size() + expectedNumberOfLSSpecificSettings,
				collectedConfig.size());
		
		testKeys = Sets.newHashSet(collectedConfig.getKeys());
		for (String k : expectedKeys) {
			assertTrue(testKeys.contains(k));
		}
		
		assertEquals(scenario.getTask(),
				collectedConfig.getString(Constants.LEARNING_TASK_KEY));
		assertEquals(scenario.getProblem(),
				collectedConfig.getString(Constants.LEARNING_PROBLEM_KEY));
		assertEquals(Constants.STEP_TRAIN,
				collectedConfig.getString(Constants.STEP_KEY));
		
		// Test learning system-specific settings
		assertEquals(
				expectedNumberOfLSSpecificSettings,
				collectedConfig.subset(Constants.LS_SPECIFIC_SETTINGS_KEY).size());
	}

	@Test
	public void testCollectValidationConfig() {
		/*
		 * aleph:
		 */
		
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		AbsoluteStep step = new AbsoluteStep(scenario, lsi, examples,
				runtimeConfig, alephFileFinder, log);
		
		Configuration collectedConfig = step.collectValidationConfig(runtimeConfig);
		
		/*
		 * Check general config part: workdir, pos, neg, input, output,
		 * learningtask, learningproblem, step
		 */
		HashSet<String> expectedKeys = Sets.newHashSet(
				Constants.WORKDIR_KEY,
				Constants.POS_EXAMPLE_FILE_KEY,
				Constants.NEG_EXAMPLE_FILE_KEY,
				Constants.INPUT_FILE_KEY,
				Constants.OUTPUT_FILE_KEY,
				Constants.LEARNING_TASK_KEY,
				Constants.LEARNING_PROBLEM_KEY,
				Constants.STEP_KEY);
		
		assertEquals(expectedKeys.size(), collectedConfig.size());
		
		HashSet<String> testKeys = Sets.newHashSet(collectedConfig.getKeys());
		for (String key : expectedKeys) {
			assertTrue(key + " not contained in the set of keys",
					testKeys.contains(key));
		}
		
		assertEquals(scenario.getTask(),
				collectedConfig.getString(Constants.LEARNING_TASK_KEY));
		assertEquals(scenario.getProblem(),
				collectedConfig.getString(Constants.LEARNING_PROBLEM_KEY));
		assertEquals(Constants.STEP_VALIDATE,
				collectedConfig.getString(Constants.STEP_KEY));
	}

	@Test
	public void testGetPositiveTrainingExamples() {
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		AbsoluteStep step = new AbsoluteStep(scenario, lsi, examples,
				runtimeConfig, alephFileFinder, log);
		
		assertEquals(examples.get(Constants.ExType.POS),
				step.getPositiveTrainingExamples());
	}
	
	@Test
	public void testGetNegativeTrainingExamples() {
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		AbsoluteStep step = new AbsoluteStep(scenario, lsi, examples,
				runtimeConfig, alephFileFinder, log);
		
		assertEquals(examples.get(Constants.ExType.NEG),
				step.getNegativeTrainingExamples());
	}
}
