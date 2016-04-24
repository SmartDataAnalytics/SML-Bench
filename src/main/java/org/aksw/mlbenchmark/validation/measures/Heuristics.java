package org.aksw.mlbenchmark.validation.measures;

/**
 * Implementation of various heuristics. The methods can be used in learning
 * problems and various evaluation scripts. They are verified in unit tests
 * and, thus, should be fairly stable.
 *
 * @author Jens Lehmann
 *
 */
public class Heuristics {

	/**
	 * Computes F1-Score.
	 * @param recall Recall.
	 * @param precision Precision.
	 * @return Harmonic mean of precision and recall.
	 */
	public static double getFScore(double recall, double precision) {
		return (precision + recall == 0) ? 0 :
			  ( 2 * (precision * recall) / (precision + recall) );
	}

	/**
	 * Computes F-beta-Score.
	 * @param recall Recall.
	 * @param precision Precision.
	 * @param beta Weights precision and recall. If beta is >1, then recall is more important
	 * than precision.
	 * @return Harmonic mean of precision and recall weighted by beta.
	 */
	public static double getFScore(double recall, double precision, double beta) {
		return (precision + recall == 0) ? 0 :
			  ( (1+ beta * beta) * (precision * recall)
					/ (beta * beta * precision + recall) );
	}

	/**
	 * Computes arithmetic mean of precision and recall, which is called "A-Score"
	 * here (A=arithmetic), but is not an established notion in machine learning.
	 * @param recall Recall.
	 * @param precision Precison.
	 * @return Arithmetic mean of precision and recall.
	 */
	public static double getAScore(double recall, double precision) {
		return (recall + precision) / 2;
	}

	/**
	 * Computes arithmetic mean of precision and recall, which is called "A-Score"
	 * here (A=arithmetic), but is not an established notion in machine learning.
	 * @param recall Recall.
	 * @param precision Precison.
	 * @param beta Weights precision and recall. If beta is >1, then recall is more important
	 * than precision.
	 * @return Arithmetic mean of precision and recall.
	 */
	public static double getAScore(double recall, double precision, double beta) {
		return (beta * recall + precision) / (beta + 1);
	}

	/**
	 * Computes the Jaccard coefficient of two sets.
	 * @param elementsIntersection Number of elements in the intersection of the two sets.
	 * @param elementsUnion Number of elements in the union of the two sets.
	 * @return #intersection divided by #union.
	 */
	public static double getJaccardCoefficient(int elementsIntersection, int elementsUnion) {
		if(elementsIntersection > elementsUnion || elementsUnion < 1) {
			throw new IllegalArgumentException();
		}
		return elementsIntersection / (double) elementsUnion;
	}

	/**
	 * Helper method that returns 0 on division instead of division by zero
	 * @param numerator
	 * @param denominator
	 * @return numerator / denominator
	 */
	public static double divideOrZero(int numerator, int denominator) {
		return denominator == 0 ? 0 : numerator / (double)denominator;
	}

}
