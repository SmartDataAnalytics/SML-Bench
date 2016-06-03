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

    /**
     * This class represents a point in the Receiver-Operating Characteristics curve. 
     */
    public class ROCPoint extends Point {

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