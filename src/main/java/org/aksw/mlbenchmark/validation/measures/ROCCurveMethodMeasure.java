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
import java.util.LinkedList;
import java.util.List;

/**
 * This class compute the Receiver-Operating Characteristics (ROC) points in the ROC 
 * curve and the area under the curve (AUC). 
 * It uses the method of Tom Fawcett. "An introduction to ROC analysis".  
 * Pattern recognition letters 27.8 (2006): 861-874.
 * 
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class ROCCurveMethodMeasure extends AbstractMeasureMethodNumeric {

    public ROCCurveMethodMeasure(int nPos, int nNeg) {
        super(nPos, nNeg);
    }

    /**
     * It returns the points of the Receiver-Operating Characteristics curve 
     * @param results
     * @return points of the ROC curve
     */
    @Override
    public List<? extends Point> getListMeasures(List<ClassificationResult> results) {
        List<ROCPoint> rocPoints = new LinkedList<>();
        try {
            rocPoints.add(new ROCPoint(0.0, 0.0));
            List<CurvePoint> points = convertIntoCurvePoints(results);

            for (CurvePoint p : points) {
                rocPoints.add(new ROCPoint((double) p.getFP() / nNeg, (double) p.getTP() / nPos));
            }
            rocPoints.add(new ROCPoint(1.0, 1.0));

        } catch (CurvePointGenerationException e) {
            throw new RuntimeException(e);
        }
        return rocPoints;
    }

    /**
     * It returns the area under the Receiver-Operating Characteristics curve.
     * @param rocPoints
     * @return 
     */
    @Override
    public double getArea(List<? extends Point> rocPoints) {
        double area = 0;
        double fprPrev = 0;
        double tprPrev = 0;
        for (Point p : rocPoints) {
            ROCPoint rp = (ROCPoint) p;
            area += trapezoidArea(rp.getTPR(), tprPrev, (rp.getFPR() - fprPrev));
        }
        return area;
    }

    private double trapezoidArea(double base1, double base2, double height) {
        return (base1 + base2) * height / 2;
    }

    /**
     * This class represents a point in the Receiver-Operating Characteristics curve. 
     */
    private class ROCPoint extends Point {

        ROCPoint(double fpr, double tpr) {
            super(fpr, tpr);
        }

        /**
         * It returns the false positive rate
         * @return False positive rate
         */
        double getFPR() {
            return getX();
        }

        /**
         * It returns the true positive rate
         * @return True positive rate
         */
        double getTPR() {
            return getY();
        }

    }

}
