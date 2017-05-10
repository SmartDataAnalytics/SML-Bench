package org.aksw.mlbenchmark.examples;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;

import org.aksw.mlbenchmark.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CrossValidationTest {
	int folds;
	int seed;

	@Before
	public void setUp() throws Exception {
		folds = 3;
		seed = 123;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCrossValidation0() {
		CrossValidation cv = new CrossValidation(
				new LinkedHashSet<String>(), new LinkedHashSet<String>(), folds, seed);
		
		assertEquals(0, cv.getTrainingSet(Constants.ExType.POS, 0).size());
		assertEquals(0, cv.getTrainingSet(Constants.ExType.NEG, 0).size());
	}
	
	@Test
	public void testCrossValidation1() {
		/*
		 * examples:
		 * 
		 * <1>
		 * 
		 * folds:
		 * 
		 * 1) <1> |
		 * 2) <1> |
		 * 3)     | <1>
		 */
		LinkedHashSet<String> posExamples = new LinkedHashSet<String>();
		posExamples.add("p1");
		
		LinkedHashSet<String> negExamples = new LinkedHashSet<String>();
		negExamples.add("n1");
		
		CrossValidation cv = new CrossValidation(posExamples, negExamples, folds, seed);
		
		// pos training examples
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int sum = 0;
		int tmp;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(0, min);
		assertEquals(1, max);
		assertEquals(0.666, sum / (double) folds, 0.01);
		
		// neg training examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(0, min);
		assertEquals(1, max);
		assertEquals(0.666, sum / (double) folds, 0.01);
		
		// pos validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(0, min);
		assertEquals(1, max);
		assertEquals(0.333, sum / (double) folds, 0.01);
		
		// neg validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(0, min);
		assertEquals(1, max);
		assertEquals(0.333, sum / (double) folds, 0.01);
	}
	
	@Test
	public void testCrossValidation2() {
		/*
		 * examples:
		 * 
		 * <1> <2>
		 * 
		 * folds:
		 * 
		 * 1) <1> <2> |
		 * 2) <1>     | <2>
		 * 3) <2>     | <1>
		 */
		LinkedHashSet<String> posExamples = new LinkedHashSet<String>();
		posExamples.add("p1");
		posExamples.add("p2");
		
		LinkedHashSet<String> negExamples = new LinkedHashSet<String>();
		negExamples.add("n1");
		negExamples.add("n2");
		
		CrossValidation cv = new CrossValidation(posExamples, negExamples, folds, seed);
		
		// pos training examples
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int sum = 0;
		int tmp;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(1, min);
		assertEquals(2, max);
		assertEquals(1.333, sum / (double) folds, 0.01);
		
		// neg training examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(1, min);
		assertEquals(2, max);
		assertEquals(1.333, sum / (double) folds, 0.01);
		
		// pos validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(0, min);
		assertEquals(1, max);
		assertEquals(0.666, sum / (double) folds, 0.01);
		
		// neg validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(0, min);
		assertEquals(1, max);
		assertEquals(0.666, sum / (double) folds, 0.01);
	}
	
	@Test
	public void testCrossValidation3() {
		/*
		 * examples:
		 * 
		 * <1> <2> <3>
		 * 
		 * folds:
		 * 
		 * 1) <1> <2> | <3>
		 * 2) <1> <3> | <2>
		 * 3) <2> <3> | <1>
		 */
		LinkedHashSet<String> posExamples = new LinkedHashSet<String>();
		posExamples.add("p1");
		posExamples.add("p2");
		posExamples.add("p3");
		
		LinkedHashSet<String> negExamples = new LinkedHashSet<String>();
		negExamples.add("n1");
		negExamples.add("n2");
		negExamples.add("n3");
		
		CrossValidation cv = new CrossValidation(posExamples, negExamples, folds, seed);
		
		// pos training examples
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int sum = 0;
		int tmp;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(2, min);
		assertEquals(2, max);
		assertEquals(2, sum / (double) folds, 0.01);
		
		// neg training examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(2, min);
		assertEquals(2, max);
		assertEquals(2, sum / (double) folds, 0.01);
		
		// pos validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(1, min);
		assertEquals(1, max);
		assertEquals(1, sum / (double) folds, 0.01);
		
		// neg validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(1, min);
		assertEquals(1, max);
		assertEquals(1, sum / (double) folds, 0.01);
	}
	
	@Test
	public void testCrossValidation4() {
		/*
		 * examples:
		 * 
		 * <1> <2> <3> <4>
		 * 
		 * folds:
		 * 
		 * 1) <1> <2> <3> | <4>
		 * 2) <1> <2> <4> | <3>
		 * 3) <3> <4>     | <1> <2>
		 */
		LinkedHashSet<String> posExamples = new LinkedHashSet<String>();
		posExamples.add("p1");
		posExamples.add("p2");
		posExamples.add("p3");
		posExamples.add("p4");
		
		LinkedHashSet<String> negExamples = new LinkedHashSet<String>();
		negExamples.add("n1");
		negExamples.add("n2");
		negExamples.add("n3");
		negExamples.add("n4");
		
		CrossValidation cv = new CrossValidation(posExamples, negExamples, folds, seed);
		
		// pos training examples
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int sum = 0;
		int tmp;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(2, min);
		assertEquals(3, max);
		assertEquals(2.66, sum / (double) folds, 0.01);
		
		// neg training examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(2, min);
		assertEquals(3, max);
		assertEquals(2.66, sum / (double) folds, 0.01);
		
		// pos validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(1, min);
		assertEquals(2, max);
		assertEquals(1.33, sum / (double) folds, 0.01);
		
		// neg validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(1, min);
		assertEquals(2, max);
		assertEquals(1.33, sum / (double) folds, 0.01);
	}
	
	@Test
	public void testCrossValidation5() {
		/*
		 * examples:
		 * 
		 * <1> <2> <3> <4> <5>
		 * 
		 * folds:
		 * 
		 * 1) <1> <2> <3> <4> | <5>
		 * 2) <1> <2> <5>     | <3> <4>
		 * 3) <3> <4> <5>     | <1> <2>
		 */
		LinkedHashSet<String> posExamples = new LinkedHashSet<String>();
		posExamples.add("p1");
		posExamples.add("p2");
		posExamples.add("p3");
		posExamples.add("p4");
		posExamples.add("p5");
		
		LinkedHashSet<String> negExamples = new LinkedHashSet<String>();
		negExamples.add("n1");
		negExamples.add("n2");
		negExamples.add("n3");
		negExamples.add("n4");
		negExamples.add("n5");
		
		CrossValidation cv = new CrossValidation(posExamples, negExamples, folds, seed);
		
		// pos training examples
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int sum = 0;
		int tmp;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(3, min);
		assertEquals(4, max);
		assertEquals(3.33, sum / (double) folds, 0.01);
		
		// neg training examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTrainingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(3, min);
		assertEquals(4, max);
		assertEquals(3.33, sum / (double) folds, 0.01);
		
		// pos validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.POS, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(1, min);
		assertEquals(2, max);
		assertEquals(1.66, sum / (double) folds, 0.01);
		
		// neg validation examples
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		sum = 0;
		for (int i=0; i < folds; i++) {
			tmp = cv.getTestingSet(Constants.ExType.NEG, i).size();
			min = Math.min(min, tmp);
			max = Math.max(max, tmp);
			sum += tmp;
		}
		
		assertEquals(1, min);
		assertEquals(2, max);
		assertEquals(1.66, sum / (double) folds, 0.01);
	}
}
