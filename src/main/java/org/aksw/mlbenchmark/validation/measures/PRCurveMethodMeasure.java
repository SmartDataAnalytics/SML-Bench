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

    public PRCurveMethodMeasure(int nPos, int nNeg) {
        super(nPos, nNeg);
    }

    @Override
    public List<? extends Point> getListMeasures(List<ClassificationResult> results) {
        List<PRPoint> prPoints = new LinkedList<>();
        try {
            List<CurvePoint> points = convertIntoCurvePoints(results);
            CurvePoint a = points.get(0);
            prPoints.add(new PRPoint((double) a.getTP() / nPos,
                    (double) a.getTP() / (a.getTP() + a.getFP())));
            for (CurvePoint b : points.subList(1, points.size())) {
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

        return prPoints;
    }

    private List<PRPoint> interpolate(CurvePoint a, CurvePoint b) throws CurvePointGenerationException {
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
        for (int i = 1; i < d1; i++) {
            double recall = (tpA + i) / nPos;
            double precision = (tpA + i) / (tpA + i + fpA + (fpB - fpA) / d1 * i);
            interPoints.add(new PRPoint(recall, precision));
        }

        return interPoints;
    }

    private class PRPoint extends Point {

        PRPoint(double recall, double precision) {
            super(recall, precision);
        }

        double getRecall() {
            return getX();
        }

        double getPrecision() {
            return getY();
        }

    }

}
