package org.aksw.mlbenchmark.validation.measures;

public class AccMethodAMeasure implements MeasureMethodTwoValued, MeasureMethodWithBeta {

	protected double beta = 0;

	public double getMeasure(int tp, int fn, int fp, int tn) {
		double recall = Heuristics.divideOrZero( tp , tp+fn );
		double precision = Heuristics.divideOrZero( tp , tp+fp );

		if (beta == 0) {
			return Heuristics.getAScore(recall, precision);
		} else {
			return Heuristics.getAScore(recall, precision, beta);
		}
	}

	public void setBeta(double beta) {
		this.beta = beta;
	}
}
