package org.aksw.mlbenchmark.validation.measures;

public class AccMethodJaccard implements MeasureMethodTwoValued {

	public double getMeasure(int tp, int fn, int fp, int tn) {
		return Heuristics.getJaccardCoefficient(tp, tp + fn + fp);
	}

}
