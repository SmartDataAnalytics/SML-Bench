package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.validation.measures.*;
import org.apache.commons.lang3.NotImplementedException;

/**
 * MeasureMethod factory
 */
public class MeasureMethod {
	public static MeasureMethodTwoValued create(String method) {
		switch (method.toLowerCase().replaceAll("_acc","acc")) {
			case "ameasure": return new AccMethodAMeasure();
			case "fmeasure": return new AccMethodFMeasure();
			case "jaccard": return new AccMethodJaccard();
			case "predacc": return new AccMethodPredAcc();
			case "weighted.predacc": {
				AccMethodPredAccWeighted am = new AccMethodPredAccWeighted();
				am.setBalanced(true);
				return am;
			}
			default: throw new NotImplementedException("Measure " + method + " not implemented or not mapped.");
		}
	}
}
