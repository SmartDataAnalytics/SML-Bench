package org.aksw.mlbenchmark.examples;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.aksw.mlbenchmark.Constants;

/**
 * Performs cross validation for the given problem. Supports
 * k-fold cross-validation.
 *
 * @author Jens Lehmann
 *
 */
public class CrossValidation implements ExamplesSplit {

	private final int folds;
	private final long seed;
	private final Random random;
	private final Map<Constants.ExType, List<String>> examples = new HashMap<>();
	private final Map<Constants.ExType, int[]> splits = new HashMap<>();
	private final Map<Constants.ExType, Map<Integer, LinkedHashSet<String>>> foldMapTest = new HashMap<>();
	private final Map<Constants.ExType, Map<Integer, LinkedHashSet<String>>> foldMapTrain = new HashMap<>();

	public CrossValidation(LinkedHashSet<String> posExamples, LinkedHashSet<String> negExamples, int folds, long seed) {
		this.examples.put(Constants.ExType.POS, new LinkedList<>(posExamples));
		this.examples.put(Constants.ExType.NEG, new LinkedList<>(negExamples));
		this.folds = folds;
		this.seed = seed;
		this.random = new Random(seed);
		for (Constants.ExType type : Constants.ExType.values()) {
			splits.put(type, calculateSplits(examples.get(type).size()));
			foldMapTest.put(type, new TreeMap<Integer, LinkedHashSet<String>>());
			foldMapTrain.put(type, new TreeMap<Integer, LinkedHashSet<String>>());
		}
	}

	private int[] calculateSplits(int nrOfExamples) {
		int[] splits = new int[folds];
		for(int i=1; i<=folds; i++) {
			// we always round up to the next integer
			splits[i-1] = (int)Math.ceil(i*nrOfExamples/(double)folds);
		}
		return splits;
	}

	public LinkedHashSet<String> getTestingSet(Constants.ExType type, int fold) {
		LinkedHashSet<String> done = foldMapTest.get(type).get(fold);
		if (done != null) { return done; }

		int fromIndex;
		// we either start from 0 or after the last fold ended
		if(fold == 0)
			fromIndex = 0;
		else
			fromIndex = splits.get(type)[fold-1];
		// the split corresponds to the ends of the folds
		int toIndex = splits.get(type)[fold];


		LinkedHashSet<String> testingSet = new LinkedHashSet<>();
		// +1 because 2nd element is exclusive in subList method
		testingSet.addAll(examples.get(type).subList(fromIndex, toIndex));
		foldMapTest.get(type).put(fold, testingSet);
		return testingSet;
	}

	public LinkedHashSet<String> getTrainingSet(Constants.ExType type, int fold) {
		LinkedHashSet<String> done = foldMapTrain.get(type).get(fold);
		if (done != null) { return done; }

		LinkedHashSet<String> temp = new LinkedHashSet<>(examples.get(type));
		temp.removeAll(getTestingSet(type, fold));
		foldMapTrain.get(type).put(fold, temp);
		return temp;
	}
}
