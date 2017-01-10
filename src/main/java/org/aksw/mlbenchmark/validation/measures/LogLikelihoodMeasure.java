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
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author giuseppe
 */
public class LogLikelihoodMeasure implements MeasureMethodNumericValued {

    // total number of positive examples
    protected int nPos;
    // total number of negative examples
    protected int nNeg;

    protected List<ClassificationResult> results;

    public LogLikelihoodMeasure(int nPos, int nNeg, List<ClassificationResult> results) {
        this.nPos = nPos;
        this.nNeg = nNeg;
        this.results = results;
    }

    @Override
    public double getMeasure() {
        double LL = getLogLikelihood();
        return Math.round(LL * Math.pow(10D, SCALE)) / Math.pow(10D, SCALE);
    }

    private double getLogLikelihood() {
        Collections.sort(results, Collections.reverseOrder());
        Double LL = 0D;

        for (ClassificationResult res : results) {
            if (res.isPositive()) {
                LL += Math.log(res.getProb().setScale(SCALE, ROUNDINGMODE).doubleValue());
            } else {
                LL += Math.log(new BigDecimal(1).subtract(res.getProb())
                        .setScale(SCALE, ROUNDINGMODE).doubleValue());
            }

        }

        return LL;
    }

}
