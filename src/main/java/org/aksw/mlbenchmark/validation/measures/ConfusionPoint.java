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

/**
 * This class contains information for the confusion matrix associated to each
 * point of the curve.
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class ConfusionPoint extends Point{

//    // Count of true positive examples
//    private int tp;
//    // Count of false negative examples
//    private int fp;

    public ConfusionPoint(int fp, int tp) {
        super(new BigDecimal(fp),new BigDecimal(tp));
    }
    
    public int getFP() {
        BigDecimal d = getX();
        return d.intValue();
    }
    
    public int getTP() {
        BigDecimal d = getY();
        return d.intValue();
    }

}
