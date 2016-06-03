/*
 * Copyright 2016 AKSW.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aksw.mlbenchmark.validation.measures;

import java.util.LinkedList;
import java.util.List;
import org.aksw.mlbenchmark.validation.measures.exceptions.CurvePointGenerationException;

/**
 * This class computes the Area Under the Precision Recall curve using the
 * method of Davis, Jesse, and Mark Goadrich. "The relationship between
 * Precision-Recall and ROC curves." Proceedings of the 23rd international
 * conference on Machine learning. ACM, 2006.
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class PRCurveMethodMeasure extends AbstractMeasureMethodNumeric {

    public PRCurveMethodMeasure(int nPos, int nNeg, List<ClassificationResult> results) {
        super(nPos, nNeg, results);
    }

    @Override
    public List<? extends Point> getCurvePoints() {
        List<PRPoint> prPoints = new LinkedList<>();
        try {

            ConfusionPoint a = curvePoints.get(0); // (0,0)
            ConfusionPoint next = curvePoints.get(1);

            // 
            if (next.getTP() == 1 && next.getFP() == 0) {
                prPoints.add(new PRPoint(0.0, 1.0));
            } else if (next.getTP() == 0 && next.getFP() == 1) {
                prPoints.add(new PRPoint(0.0, 0.0));
            } else { // Unknown condition!
                throw new CurvePointGenerationException("Impossible condition: "
                        + "First point TP=" + next.getTP() + " FP=" + next.getFP());
            }

            for (ConfusionPoint b : curvePoints.subList(1, curvePoints.size())) {
//                int tpB = b.getTP();
//                int fpB = b.getFP();

                if (b.getTP() == a.getTP() && b.getFP() <= a.getFP()) {
                    throw new CurvePointGenerationException("Impossible condition: "
                            + "tpB==tpA && fpB <= fpA");
                }

                a = b;
                prPoints.add(new PRPoint((double) b.getTP() / nPos,
                        (double) b.getTP() / (b.getTP() + b.getFP())));
            }
            prPoints.add(new PRPoint(1.0, (double) nPos / (nPos + nNeg)));
        } catch (CurvePointGenerationException e) {
            throw new RuntimeException(e);
        }

        return prPoints;
    }

    @Override
    public double getAUC() {
        List<PRPoint> prPoints = new LinkedList<>();
        try {

            ConfusionPoint a = curvePoints.get(0); // (0,0)
            ConfusionPoint next = curvePoints.get(1);

            // 
            if (next.getTP() == 1 && next.getFP() == 0) {
                prPoints.add(new PRPoint(0.0, 1.0));
            } else if (next.getTP() == 0 && next.getFP() == 1) {
                prPoints.add(new PRPoint(0.0, 0.0));
            } else { // Unknown condition!
                throw new CurvePointGenerationException("Impossible condition: "
                        + "First point TP=" + next.getTP() + " FP=" + next.getFP());
            }

            for (ConfusionPoint b : curvePoints.subList(1, curvePoints.size())) {
//                int tpB = b.getTP();
//                int fpB = b.getFP();
                if (b.getTP() == a.getTP()) {
                    if (b.getFP() <= a.getFP()) {
                        throw new CurvePointGenerationException("Impossible condition: "
                                + "tpB==tpA && fpB <= fpA");
                    }
                } else {
                    List<PRPoint> interPRPoints = interpolate(a, b);
                    prPoints.addAll(interPRPoints);
                }
                a = b;
                prPoints.add(new PRPoint((double) b.getTP() / nPos,
                        (double) b.getTP() / (b.getTP() + b.getFP())));
            }
            prPoints.add(new PRPoint(1.0, (double) nPos / (nPos + nNeg)));
        } catch (CurvePointGenerationException e) {
            throw new RuntimeException(e);
        }

        return getAUC(prPoints);

    }

    private List<PRPoint> interpolate(ConfusionPoint a, ConfusionPoint b) throws CurvePointGenerationException {
        List<PRPoint> interPoints = new LinkedList<>();
        double tpA = a.getTP();
        double tpB = b.getTP();
        double fpA = a.getFP();
        double fpB = b.getFP();
        double d1 = tpB - tpA;
        if (tpB == tpA) {
            throw new CurvePointGenerationException("Division per 0 during interpolation!");
        }
        if (tpB < tpA) {
            throw new CurvePointGenerationException("Impossible condition during interpolation: tpB < tpA");
        }
        for (int x = 1; x <= d1; x++) {
            double recall = (tpA + x) / nPos;
            double precision = (tpA + x) / (tpA + x + fpA + (fpB - fpA) / d1 * x);
            interPoints.add(new PRPoint(recall, precision));
        }

        return interPoints;
    }

}
