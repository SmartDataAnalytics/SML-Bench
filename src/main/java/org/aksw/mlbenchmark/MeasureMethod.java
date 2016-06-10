package org.aksw.mlbenchmark;

import java.util.List;
import org.aksw.mlbenchmark.validation.measures.*;
import org.apache.commons.lang3.NotImplementedException;

/**
 * MeasureMethod factory
 */
public class MeasureMethod {

    public static MeasureMethodTwoValued create(String method) {
        switch (method.toLowerCase().replaceAll("_acc", "acc")) {
            case "ameasure":
                return new AccMethodAMeasure();
            case "fmeasure":
                return new AccMethodFMeasure();
            case "jaccard":
                return new AccMethodJaccard();
            case "predacc":
                return new AccMethodPredAcc();
            case "weighted.predacc": {
                AccMethodPredAccWeighted am = new AccMethodPredAccWeighted();
                am.setBalanced(true);
                return am;
            }
            default:
                throw new NotImplementedException("Measure " + method + " not implemented or not mapped.");
        }
    }

    public static MeasureMethodNumericValued create(String method, int nPos, int nNeg, List<ClassificationResult> cResults) {
        switch (method.toLowerCase()) {
            case "aucroc":
                return new ROCCurveMethodMeasure(nPos, nNeg, cResults);
            case "aucpr":
                return new PRCurveMethodMeasure(nPos, nNeg, cResults);
            default:
                throw new NotImplementedException("Measure " + method + " not implemented or not mapped.");
        }
    }
}
