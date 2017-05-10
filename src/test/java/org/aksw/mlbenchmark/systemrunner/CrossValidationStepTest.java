package org.aksw.mlbenchmark.systemrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.mlbenchmark.BenchmarkLog;
import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.examples.CrossValidation;
import org.aksw.mlbenchmark.util.FileFinder;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class CrossValidationStepTest {
	private String resourcesDir = "src/test/resources/";
	private HierarchicalConfiguration<ImmutableNode> runtimeConfig;
	private Scenario scenario;
	private File tmpDir;
	private FileFinder fileFinder;
	private CrossValidation examples;
	private BenchmarkLog log;
	private int numberOfFolds;

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
		
		LinkedHashSet<String> posExamples = new LinkedHashSet<String>();
		posExamples.add("one");
		posExamples.add("two");
		posExamples.add("three");
		posExamples.add("four");
		
		LinkedHashSet<String> negExamples = new LinkedHashSet<String>();
		negExamples.add("five");
		negExamples.add("six");
		negExamples.add("seven");
		negExamples.add("eight");
		negExamples.add("nine");
		
		numberOfFolds = 3;
		int seed = 123;
		
		examples = new CrossValidation(posExamples, negExamples, numberOfFolds, seed);
		log = new BenchmarkLog();
	}

	@After
	public void tearDown() {
		tmpDir.delete();
	}

	@Test
	public void testGetPositiveTrainingExamples() {
		/*
		 * pos examples:
		 * 
		 * <1> <2> <3> <4>
		 * 
		 * folds (just for visualization, actual folds might look different):
		 * 
		 * 0) <1> <2> <3> | <4>
		 * 1) <1> <2> <4> | <3>
		 * 2) <3> <4>     | <1> <2>
		 */
		// <example, number of occurrences in any fold>
		Map<String, Integer> expectedPosExamples = new HashMap<String, Integer>();
		expectedPosExamples.put("one", 0);
		expectedPosExamples.put("two", 0);
		expectedPosExamples.put("three", 0);
		expectedPosExamples.put("four", 0);
		
		int numExpectedExamples = expectedPosExamples.size();
		int maxExamplesPerFold = (int) (numExpectedExamples -
				Math.floor(numExpectedExamples / (double) numberOfFolds));
		int minExamplesPerFold = (int) (numExpectedExamples -
				Math.ceil(numExpectedExamples / (double) numberOfFolds));
		
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		
		// fold 0 -------------------------------------------------------------
		CrossValidationStep step = new CrossValidationStep(scenario, lsi,
				examples, runtimeConfig, 0, alephFileFinder, log);

		Set<String> posTrainExamples = step.getPositiveTrainingExamples();
		// all the folds's examples should be part of the expected examples
		for (String ex : posTrainExamples) {
			boolean containsKey = expectedPosExamples.containsKey(ex);
			assertTrue("The positive example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedPosExamples.get(ex);
				expectedPosExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + posTrainExamples.size() + ")",
				posTrainExamples.size() <= maxExamplesPerFold &&
				posTrainExamples.size() >= minExamplesPerFold);
		
		// fold 1 -------------------------------------------------------------
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				1, alephFileFinder, log);
		
		posTrainExamples = step.getPositiveTrainingExamples();
		// all the fold's examples should be part of the expected examples
		for (String ex : posTrainExamples) {
			boolean containsKey = expectedPosExamples.containsKey(ex);
			assertTrue("The positive example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedPosExamples.get(ex);
				expectedPosExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + posTrainExamples.size() + ")",
				posTrainExamples.size() <= maxExamplesPerFold &&
				posTrainExamples.size() >= minExamplesPerFold);
		
		// fold 2 -------------------------------------------------------------
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				2, alephFileFinder, log);
		
		posTrainExamples = step.getPositiveTrainingExamples();
		// all the fold's examples should be part of the expected examples
		for (String ex : posTrainExamples) {
			boolean containsKey = expectedPosExamples.containsKey(ex);
			assertTrue("The positive example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedPosExamples.get(ex);
				expectedPosExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + posTrainExamples.size() + ")",
				posTrainExamples.size() <= maxExamplesPerFold &&
				posTrainExamples.size() >= minExamplesPerFold);
		
		// all expected examples should have been seen in one of the folds
		for (String key : expectedPosExamples.keySet()) {
			assertTrue("The positive example " + key + " should have "
					+ "appeared in at least one fold",
					expectedPosExamples.get(key) >= 1);
		}
	}

	@Test
	public void testGetNegativeTrainingExamples() {
		/*
		 * neg examples:
		 * 
		 * <1> <2> <3> <4> <5>
		 * 
		 * folds (just for visualization, actual folds might look different):
		 * 
		 * 0) <1> <2> <3> <4> | <5>
		 * 1) <1> <2> <5>     | <3> <4>
		 * 2) <3> <4> <5>     | <1> <2>
		 */
		// <example, number of occurrences in any fold>
		Map<String, Integer> expectedNegExamples = new HashMap<String, Integer>();
		expectedNegExamples.put("five", 0);
		expectedNegExamples.put("six", 0);
		expectedNegExamples.put("seven", 0);
		expectedNegExamples.put("eight", 0);
		expectedNegExamples.put("nine", 0);
		
		int numExpectedExamples = expectedNegExamples.size();
		int maxExamplesPerFold = (int) (numExpectedExamples -
				Math.floor(numExpectedExamples / (double) numberOfFolds));
		int minExamplesPerFold = (int) (numExpectedExamples -
				Math.ceil(numExpectedExamples / (double) numberOfFolds));
		
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		
		// fold 0 -------------------------------------------------------------
		CrossValidationStep step = new CrossValidationStep(scenario, lsi,
				examples, runtimeConfig, 0, alephFileFinder, log);

		Set<String> negTrainExamples = step.getNegativeTrainingExamples();
		// all the folds's examples should be part of the expected examples
		for (String ex : negTrainExamples) {
			boolean containsKey = expectedNegExamples.containsKey(ex);
			assertTrue("The negative example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedNegExamples.get(ex);
				expectedNegExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + negTrainExamples.size() + ")",
				negTrainExamples.size() <= maxExamplesPerFold &&
				negTrainExamples.size() >= minExamplesPerFold);
		
		// fold 1 -------------------------------------------------------------
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				1, alephFileFinder, log);
		
		negTrainExamples = step.getNegativeTrainingExamples();
		// all the fold's examples should be part of the expected examples
		for (String ex : negTrainExamples) {
			boolean containsKey = expectedNegExamples.containsKey(ex);
			assertTrue("The negative example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedNegExamples.get(ex);
				expectedNegExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + negTrainExamples.size() + ")",
				negTrainExamples.size() <= maxExamplesPerFold &&
				negTrainExamples.size() >= minExamplesPerFold);
		
		// fold 2 -------------------------------------------------------------
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				2, alephFileFinder, log);
		
		negTrainExamples = step.getNegativeTrainingExamples();
		// all the fold's examples should be part of the expected examples
		for (String ex : negTrainExamples) {
			boolean containsKey = expectedNegExamples.containsKey(ex);
			assertTrue("The negative example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedNegExamples.get(ex);
				expectedNegExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + negTrainExamples.size() + ")",
				negTrainExamples.size() <= maxExamplesPerFold &&
				negTrainExamples.size() >= minExamplesPerFold);
		
		// all expected examples should have been seen in one of the folds
		for (String key : expectedNegExamples.keySet()) {
			assertTrue("The negative example " + key + " should have "
					+ "appeared in at least one fold",
					expectedNegExamples.get(key) >= 1);
		}
	}

	@Test
	public void testGetPositiveValidationExamples() {
		/*
		 * pos examples:
		 * 
		 * <1> <2> <3> <4>
		 * 
		 * folds (just for visualization, actual folds might look different):
		 * 
		 * 0) <1> <2> <3> | <4>
		 * 1) <1> <2> <4> | <3>
		 * 2) <3> <4>     | <1> <2>
		 */
		// <example, number of occurrences in any fold>
		Map<String, Integer> expectedPosExamples = new HashMap<String, Integer>();
		expectedPosExamples.put("one", 0);
		expectedPosExamples.put("two", 0);
		expectedPosExamples.put("three", 0);
		expectedPosExamples.put("four", 0);
		
		int numExpectedExamples = expectedPosExamples.size();
		int maxExamplesPerFold =
				(int) Math.ceil(numExpectedExamples / (double) numberOfFolds);
		int minExamplesPerFold =
				(int) Math.floor(numExpectedExamples / (double) numberOfFolds);
		
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		
		// fold 0 -------------------------------------------------------------
		CrossValidationStep step = new CrossValidationStep(scenario, lsi,
				examples, runtimeConfig, 0, alephFileFinder, log);

		Set<String> posValidationExamples = step.getPositiveValidationExamples();
		// all the folds's examples should be part of the expected examples
		for (String ex : posValidationExamples) {
			boolean containsKey = expectedPosExamples.containsKey(ex);
			assertTrue("The positive example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedPosExamples.get(ex);
				expectedPosExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + posValidationExamples.size() + ")",
				posValidationExamples.size() <= maxExamplesPerFold &&
				posValidationExamples.size() >= minExamplesPerFold);
		
		// fold 1 -------------------------------------------------------------
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				1, alephFileFinder, log);
		
		posValidationExamples = step.getPositiveValidationExamples();
		// all the fold's examples should be part of the expected examples
		for (String ex : posValidationExamples) {
			boolean containsKey = expectedPosExamples.containsKey(ex);
			assertTrue("The positive example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedPosExamples.get(ex);
				expectedPosExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + posValidationExamples.size() + ")",
				posValidationExamples.size() <= maxExamplesPerFold &&
				posValidationExamples.size() >= minExamplesPerFold);
		
		// fold 2 -------------------------------------------------------------
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				2, alephFileFinder, log);
		
		posValidationExamples = step.getPositiveValidationExamples();
		// all the fold's examples should be part of the expected examples
		for (String ex : posValidationExamples) {
			boolean containsKey = expectedPosExamples.containsKey(ex);
			assertTrue("The positive example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedPosExamples.get(ex);
				expectedPosExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + posValidationExamples.size() + ")",
				posValidationExamples.size() <= maxExamplesPerFold &&
				posValidationExamples.size() >= minExamplesPerFold);
		
		// all expected examples should have been seen in one of the folds
		for (String key : expectedPosExamples.keySet()) {
			assertTrue("The positive example " + key + " should have "
					+ "appeared in at least one fold",
					expectedPosExamples.get(key) >= 1);
		}
	}

	@Test
	public void testGetNegativeValidationExamples() {
		/*
		 * neg examples:
		 * 
		 * <1> <2> <3> <4> <5>
		 * 
		 * folds (just for visualization, actual folds might look different):
		 * 
		 * 0) <1> <2> <3> <4> | <5>
		 * 1) <1> <2> <5>     | <3> <4>
		 * 2) <3> <4> <5>     | <1> <2>
		 */
		// <example, number of occurrences in any fold>
		Map<String, Integer> expectedNegExamples = new HashMap<String, Integer>();
		expectedNegExamples.put("five", 0);
		expectedNegExamples.put("six", 0);
		expectedNegExamples.put("seven", 0);
		expectedNegExamples.put("eight", 0);
		expectedNegExamples.put("nine", 0);
		
		int numExpectedExamples = expectedNegExamples.size();
		int maxExamplesPerFold =
				(int) Math.ceil(numExpectedExamples / (double) numberOfFolds);
		int minExamplesPerFold =
				(int) Math.floor(numExpectedExamples / (double) numberOfFolds);
		
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		
		// fold 0 -------------------------------------------------------------
		CrossValidationStep step = new CrossValidationStep(scenario, lsi,
				examples, runtimeConfig, 0, alephFileFinder, log);

		Set<String> negValidationExamples = step.getNegativeValidationExamples();
		// all the folds's examples should be part of the expected examples
		for (String ex : negValidationExamples) {
			boolean containsKey = expectedNegExamples.containsKey(ex);
			assertTrue("The negative example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedNegExamples.get(ex);
				expectedNegExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + negValidationExamples.size() + ")",
				negValidationExamples.size() <= maxExamplesPerFold &&
				negValidationExamples.size() >= minExamplesPerFold);
		
		// fold 1 -------------------------------------------------------------
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				1, alephFileFinder, log);
		
		negValidationExamples = step.getNegativeValidationExamples();
		// all the fold's examples should be part of the expected examples
		for (String ex : negValidationExamples) {
			boolean containsKey = expectedNegExamples.containsKey(ex);
			assertTrue("The negative example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedNegExamples.get(ex);
				expectedNegExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + negValidationExamples.size() + ")",
				negValidationExamples.size() <= maxExamplesPerFold &&
				negValidationExamples.size() >= minExamplesPerFold);
		
		// fold 2 -------------------------------------------------------------
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				2, alephFileFinder, log);
		
		negValidationExamples = step.getNegativeValidationExamples();
		// all the fold's examples should be part of the expected examples
		for (String ex : negValidationExamples) {
			boolean containsKey = expectedNegExamples.containsKey(ex);
			assertTrue("The negative example \"" + ex + "\" should be one of "
					+ "the expected examples", containsKey);
			
			if (containsKey) {
				int tmp = expectedNegExamples.get(ex);
				expectedNegExamples.put(ex, ++tmp);
			}
		}
		
		// the number of examples in fold 0 should make sense
		assertTrue("The expected number of training examples should be "
				+ "between " + minExamplesPerFold + " and "
				+ maxExamplesPerFold + " (was " + negValidationExamples.size() + ")",
				negValidationExamples.size() <= maxExamplesPerFold &&
				negValidationExamples.size() >= minExamplesPerFold);
		
		// all expected examples should have been seen in one of the folds
		for (String key : expectedNegExamples.keySet()) {
			assertTrue("The negative example " + key + " should have "
					+ "appeared in at least one fold",
					expectedNegExamples.get(key) >= 1);
		}
	}

	@Test
	public void testGetResultKey() {
		LearningSystemInfo lsi = new LearningSystemInfo(
				new BenchmarkConfig(runtimeConfig), "aleph", fileFinder);
		FileFinder alephFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		
		CrossValidationStep step = new CrossValidationStep(scenario, lsi,
				examples, runtimeConfig, 23, alephFileFinder, log);
		
		assertEquals("animals.mammal.fold-23.aleph", step.getResultKey());
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
		CrossValidationStep step = new CrossValidationStep(scenario, lsi,
				examples, runtimeConfig, 0, alephFileFinder, log);
		
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

		Iterator<String> keysIt = collectedConfig.getKeys(
				Constants.LS_SPECIFIC_SETTINGS_KEY);
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
		lsi = new LearningSystemInfo(new BenchmarkConfig(runtimeConfig),
				"dllearner", fileFinder);
		
		FileFinder dllearnerFileFinder = fileFinder.updateLearningSytemInfo(lsi);
		
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				1, dllearnerFileFinder, log);
		
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
		lsi = new LearningSystemInfo(new BenchmarkConfig(runtimeConfig),
				"dllearner-1", fileFinder);
		
		FileFinder dllearnerFileFinder1 = fileFinder.updateLearningSytemInfo(lsi);
		
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				2, dllearnerFileFinder1, log);
		
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
		step = new CrossValidationStep(
				scenario, lsi, examples, runtimeConfig, 3, dllearnerFileFinder2, log);
		
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
		lsi = new LearningSystemInfo(new BenchmarkConfig(runtimeConfig),
				"progol", fileFinder);
		
		FileFinder progolFileFinder2 = fileFinder.updateLearningSytemInfo(lsi);
		
		step = new CrossValidationStep(scenario, lsi, examples, runtimeConfig,
				4, progolFileFinder2, log);
		
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
		CrossValidationStep step = new CrossValidationStep(scenario, lsi,
				examples, runtimeConfig, 0, alephFileFinder, log);
		
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
}
