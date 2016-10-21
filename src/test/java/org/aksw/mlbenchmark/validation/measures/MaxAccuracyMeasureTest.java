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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.aksw.mlbenchmark.Constants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author giuseppe
 */
public class MaxAccuracyMeasureTest {

    public MaxAccuracyMeasureTest() {

    }

    /**
     * Test of getMaxAccuracy method, of class MaxAccuracyMeasure.
     */
    @Test
    public void testGetMaxAccuracy() throws Exception {
        System.out.println("getMaxAccuracy");
        List<ClassificationResult> classificationResults = new LinkedList<>();
        List<Double> listParams = Arrays.asList(
                0.6, 0.55, 0.9, 0.8, 0.7, 0.55, 0.54, 0.53, 0.52, 0.51, 0.505, 0.34, 0.3);
        List<Integer> listClasses = Arrays.asList(
                1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1);
        for (int i = 0; i < listParams.size(); i++) {
            if (listClasses.get(i) == 1) {
                classificationResults.add(new ClassificationResult(listParams.get(i), Constants.ExType.POS));
            } else {
                classificationResults.add(new ClassificationResult(listParams.get(i), Constants.ExType.NEG));
            }
        }
        int nPos = 7;
        int nNeg = 6;
        MaxAccuracyMeasure instance = new MaxAccuracyMeasure(nPos, nNeg, classificationResults);
        double expResult = 0.6923076923076923;
        expResult = Math.round(expResult * Math.pow(10D, 6)) / Math.pow(10D, 6);
        double result = instance.getMaxAccuracy();
        result = Math.round(result * Math.pow(10D, 6)) / Math.pow(10D, 6);
        assertEquals(expResult, result, 0.0);
    }

}
