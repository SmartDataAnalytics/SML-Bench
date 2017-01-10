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
import org.aksw.mlbenchmark.Constants.ExType;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class ClassificationResult implements Comparable<ClassificationResult> {

    /**
     * 
     */
    private final BigDecimal value;
    /**
     * The original class of the example 
     */
    private final ExType classType;

    public ClassificationResult(BigDecimal paramDouble, ExType paramInt) {
        this.value = paramDouble;
        this.classType = paramInt;
    }

    public ExType getClassification() {
        return this.classType;
    }
    
    public boolean isPositive() {
        return this.classType == ExType.POS;
    }
    
    public boolean isNegative() {
        return this.classType == ExType.NEG;
    }

    public BigDecimal getProb() {
        return this.value;
    }

    @Override
    public int compareTo(ClassificationResult o) {
        BigDecimal d = o.getProb();
        if (this.value.compareTo(d) < 0) {
            return -1;
        }
        if (this.value.compareTo(d) > 0) {
            return 1;
        }
        ExType i = o.getClassification();
        if (i == this.classType) {
            return 0;
        }
        if (this.classType != ExType.NEG) {
            return -1;
        }
        return 1;
    }

}
