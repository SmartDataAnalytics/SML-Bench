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

import java.math.BigDecimal;
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
public class PRCurveMethodMeasure extends AbstractMeasureMethodNumericCurve {

    public PRCurveMethodMeasure(int nPos, int nNeg, List<ClassificationResult> results) {
        super(nPos, nNeg, results);
    }

    @Override
    public List<? extends Point> getCurvePoints() {
        List<PRPoint> prPoints = new LinkedList<>();
        try {

            ConfusionPoint a = curvePoints.get(0); // (0,0)
            if (curvePoints.size() > 1) {
                ConfusionPoint next = curvePoints.get(1);
                // 
                prPoints.add(new PRPoint(
                        new BigDecimal(0), 
                        new BigDecimal(next.getTP()).divide(
                                new BigDecimal(next.getTP() + next.getFP()), SCALE, ROUNDINGMODE)));
                /*
                if (next.getTP() == 1 && next.getFP() == 0) {
                    prPoints.add(new PRPoint(0.0, 1.0));
                } else if (next.getTP() == 0 && next.getFP() == 1) {
                    prPoints.add(new PRPoint(0.0, 0.0));
                } else { // Unknown condition!
                    throw new CurvePointGenerationException("Impossible condition: "
                            + "First point TP=" + next.getTP() + " FP=" + next.getFP());
                }
                */
            } else {
                prPoints.add(new PRPoint(
                        new BigDecimal(0), 
                        new BigDecimal(nPos).divide(new BigDecimal(nPos + nNeg), SCALE, ROUNDINGMODE)));
            }

            for (ConfusionPoint b : curvePoints.subList(1, curvePoints.size())) {
//                int tpB = b.getTP();
//                int fpB = b.getFP();

                if (b.getTP() == a.getTP() && b.getFP() <= a.getFP()) {
                    throw new CurvePointGenerationException("Impossible condition: "
                            + "tpB==tpA && fpB <= fpA");
                }

                a = b;
                prPoints.add(new PRPoint(
                        new BigDecimal(b.getTP()).divide(new BigDecimal(nPos), SCALE, ROUNDINGMODE),
                        new BigDecimal(b.getTP()).divide(new BigDecimal(b.getTP() + b.getFP()), SCALE, ROUNDINGMODE)));
            }
            prPoints.add(new PRPoint(
                    BigDecimal.ONE, 
                    new BigDecimal(nPos).divide(new BigDecimal(nPos + nNeg), SCALE, ROUNDINGMODE)));
        } catch (CurvePointGenerationException e) {
            throw new RuntimeException(e);
        }

        return prPoints;
    }

    @Override
    public BigDecimal getAUC() {
        List<PRPoint> prPoints = new LinkedList<>();
        try {
            ConfusionPoint a = curvePoints.get(0); // (0,0)
            if (curvePoints.size() > 1) {
                ConfusionPoint next = curvePoints.get(1);
                // 
                prPoints.add(new PRPoint(
                        BigDecimal.ZERO, 
                        new BigDecimal(next.getTP())
                                .divide(new BigDecimal(next.getTP() + next.getFP()), SCALE, ROUNDINGMODE)));
                /*
                if (next.getTP() == 1 && next.getFP() == 0) {
                    prPoints.add(new PRPoint(0.0, 1.0));
                } else if (next.getTP() == 0 && next.getFP() == 1) {
                    prPoints.add(new PRPoint(0.0, 0.0));
                } else { // Strange condition!
                    prPoints.add(new PRPoint(0.0, (double) next.getTP() / (next.getTP() + next.getFP())));
                    MeasureMethodNumericValued.logger.warn("Strange condition: "
                            + "First point TP=" + next.getTP() + " FP=" + next.getFP());
//                    throw new CurvePointGenerationException("Impossible condition: "
//                            + "First point TP=" + next.getTP() + " FP=" + next.getFP());
                }*/
            } else {
                prPoints.add(new PRPoint(
                        BigDecimal.ZERO, 
                        new BigDecimal(nPos).divide(new BigDecimal(nPos + nNeg), SCALE, ROUNDINGMODE)));
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
                prPoints.add(new PRPoint(
                        new BigDecimal(b.getTP()).divide(new BigDecimal(nPos), SCALE, ROUNDINGMODE),
                        new BigDecimal(b.getTP()).divide(new BigDecimal(b.getTP() + b.getFP()), SCALE, ROUNDINGMODE)));
            }
            prPoints.add(new PRPoint(
                    BigDecimal.ONE, 
                    new BigDecimal(nPos).divide(new BigDecimal(nPos + nNeg), SCALE, ROUNDINGMODE)));
        } catch (CurvePointGenerationException e) {
            throw new RuntimeException(e);
        }

        return getAUC(prPoints);

    }

    /**
     * It creates a set of new points between two points of the curve.
     * @param a
     * @param b
     * @return
     * @throws CurvePointGenerationException 
     */
    private List<PRPoint> interpolate(ConfusionPoint a, ConfusionPoint b) 
            throws CurvePointGenerationException {
        List<PRPoint> interPoints = new LinkedList<>();
        int tpA = a.getTP();
        int tpB = b.getTP();
        int fpA = a.getFP();
        int fpB = b.getFP();
        int d1 = tpB - tpA;
        int d2 = fpB - fpA;
        if (tpB == tpA) {
            throw new CurvePointGenerationException("Division per 0 during interpolation!");
        }
        if (tpB < tpA) {
            throw new CurvePointGenerationException("Impossible condition during interpolation: tpB < tpA");
        }
        for (int x = 1; x <= d1; x++) {
            BigDecimal recall =  new BigDecimal(tpA + x).divide(new BigDecimal(nPos), SCALE, ROUNDINGMODE);
            BigDecimal s = new BigDecimal(d2).divide(new BigDecimal(d1), SCALE, ROUNDINGMODE).multiply(new BigDecimal(x));
            BigDecimal precision = new BigDecimal(tpA + x).divide(new BigDecimal(tpA + x + fpA).add(s), SCALE, ROUNDINGMODE);
            interPoints.add(new PRPoint(recall, precision));
        }

        return interPoints;
    }

}
