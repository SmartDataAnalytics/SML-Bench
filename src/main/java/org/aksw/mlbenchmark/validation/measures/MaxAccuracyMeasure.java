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
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.validation.measures.exceptions.ProbabilisticResultOrderException;

/**
 *
 * @author giuseppe
 */
public class MaxAccuracyMeasure implements MeasureMethodNumericValued {

    // total number of positive examples
    protected int nPos;
    // total number of negative examples
    protected int nNeg;

    protected List<ClassificationResult> results;

    public MaxAccuracyMeasure(int nPos, int nNeg, List<ClassificationResult> results) {
        this.nPos = nPos;
        this.nNeg = nNeg;
        this.results = results;
    }

    BigDecimal getMaxAccuracy()
            throws ProbabilisticResultOrderException {
        Collections.sort(results, Collections.reverseOrder());
        BigDecimal maxAccuracy = new BigDecimal(0);
        int truePos = 0;
        int trueNeg = nNeg;
        BigDecimal fPrev = BigDecimal.valueOf(Double.MAX_VALUE);
        for (ClassificationResult res : results) {
            if (res.getProb().compareTo(fPrev) <= 0) {
                BigDecimal accuracy = new BigDecimal(truePos + trueNeg)
                        .divide(new BigDecimal(nPos + nNeg), SCALE, ROUNDINGMODE);
                fPrev = res.getProb();
                if (accuracy.compareTo(maxAccuracy) > 0) {
                    maxAccuracy = accuracy;
                }
            } else if (res.getProb().compareTo(fPrev) > 0) {
                throw new ProbabilisticResultOrderException("current score: " + res.getProb()
                        + " is greater than previous one: " + fPrev);
            }
            if (res.getClassification() == Constants.ExType.POS) {
                truePos++;
            } else {
                trueNeg--;
            }
        }
        return maxAccuracy;
    }

    @Override
    public double getMeasure() {
        try {
            return getMaxAccuracy().setScale(SCALE, ROUNDINGMODE).doubleValue();
        } catch (ProbabilisticResultOrderException ex) {
            throw new RuntimeException(ex);
        }
    }
}
