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

import org.aksw.mlbenchmark.validation.measures.exceptions.CurvePointGenerationException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.aksw.mlbenchmark.Constants;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public abstract class AbstractMeasureMethodNumeric implements MeasureMethodNumericValued {

    // total number of positive examples
    protected int nPos;
    // total number of negative examples
    protected int nNeg;

    protected List<ConfusionPoint> curvePoints;

    public AbstractMeasureMethodNumeric(int nPos, int nNeg, List<ClassificationResult> results) {
        this.nPos = nPos;
        this.nNeg = nNeg;
        try {
            this.curvePoints = convertIntoCurvePoints(results);
        } catch (CurvePointGenerationException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ConfusionPoint> convertIntoCurvePoints(List<ClassificationResult> results) throws CurvePointGenerationException {
        List<ConfusionPoint> curvePoints = new LinkedList<>();
        Collections.sort(results, Collections.reverseOrder());
        int truePos = 0;
        int falsePos = 0;
        double fPrev = Double.MAX_VALUE;
        for (ClassificationResult res : results) {
            if (res.getProb() < fPrev) {
                curvePoints.add(new ConfusionPoint(falsePos, truePos));
                fPrev = res.getProb();
            } else {
                if (res.getProb() > fPrev) {
                    throw new CurvePointGenerationException("current score: " + res.getProb()
                            + " is greater than previous one: " + fPrev);
                }
            }
            if (res.getClassification() == Constants.ExType.POS) {
                truePos++;
            } else {
                falsePos++;
            }
        }
        return curvePoints;
    }

    /**
     * It returns the area under the curve.
     *
     * @param points
     * @return
     */
    protected double getAUC(List<? extends Point> points) {
        double area = 0;
        double x = points.get(0).getX();
        double y = points.get(0).getY();
        for (Point p : points.subList(1, points.size())) {
            area += trapezoidArea(p.getY(), y, (p.getX() - x));
            x = p.getX();
            y = p.getY();
        }
        return area;
    }

    /**
     * It computes the area of a trapezoid.
     *
     * @param base1
     * @param base2
     * @param height
     * @return
     */
    private double trapezoidArea(double base1, double base2, double height) {
        return (base1 + base2) * height / 2;
    }
}
