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

/**
 * This class computes the Receiver-Operating Characteristics (ROC) points in
 * the ROC curve and the area under the curve (AUC). It uses the method of Tom
 * Fawcett. "An introduction to ROC analysis". Pattern recognition letters 27.8
 * (2006): 861-874.
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class ROCCurveMethodMeasure extends AbstractMeasureMethodNumericCurve {

    public ROCCurveMethodMeasure(int nPos, int nNeg, List<ClassificationResult> results) {
        super(nPos, nNeg, results);
    }

    /**
     * It returns the points of the Receiver-Operating Characteristics curve
     *
     * @return points of the ROC curve
     */
    @Override
    public List<? extends Point> getCurvePoints() {
        List<ROCPoint> rocPoints = new LinkedList<>();

            //rocPoints.add(new ROCPoint(0.0, 0.0));
        for (ConfusionPoint p : this.curvePoints) {
            rocPoints.add(new ROCPoint(
                    new BigDecimal(p.getFP()).divide(new BigDecimal(nNeg), SCALE, ROUNDINGMODE), 
                    new BigDecimal(p.getTP()).divide(new BigDecimal(nPos), SCALE, ROUNDINGMODE)
            ));
        }
        rocPoints.add(new ROCPoint(
                BigDecimal.ONE, 
                BigDecimal.ONE
        ));

        return rocPoints;
    }

    @Override
    public BigDecimal getAUC() {
        List<? extends Point> rocPoints = getCurvePoints();
        return getAUC(rocPoints);
    }

}
