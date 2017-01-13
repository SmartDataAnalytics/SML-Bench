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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.aksw.mlbenchmark.Constants;
import static org.aksw.mlbenchmark.validation.measures.MeasureMethodNumericValued.SCALE;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class ROCCurveMethodMeasureTest {

    ROCCurveMethodMeasure instance;
    List<ClassificationResult> classificationResults;

    public ROCCurveMethodMeasureTest() {
        classificationResults = new LinkedList<>();
        List<Double> listParams = Arrays.asList(
                0.6, 0.55, 0.9, 0.8, 0.7, 0.55, 0.54, 0.53, 0.52, 0.51, 0.505, 0.34, 0.3);
        List<Integer> listClasses = Arrays.asList(
                1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1);
        for (int i = 0; i < listParams.size(); i++) {
            if (listClasses.get(i) == 1) {
                classificationResults.add(new ClassificationResult(
                        BigDecimal.valueOf(listParams.get(i)),
                        Constants.ExType.POS));
            } else {
                classificationResults.add(new ClassificationResult(
                        BigDecimal.valueOf(listParams.get(i)),
                        Constants.ExType.NEG));
            }
        }
        int nPos = 7;
        int nNeg = 6;
        instance = new ROCCurveMethodMeasure(nPos, nNeg, classificationResults);
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getListMeasures method, of class ROCCurveMethodMeasure.
     */
    @Test
    public void testGetListMeasures() {
        System.out.println("getListMeasures");

        // expected results
        List<Double> expFPRAxis = Arrays.asList(
                0.00000,
                0.0,
                0.0,
                0.16666666666666666,
                0.16666666666666666,
                0.3333333333333333,
                0.3333333333333333,
                0.5,
                0.6666666666666666,
                0.6666666666666666,
                0.8333333333333334,
                1.0,
                1.0);
        List<Double> expTPRAxis = Arrays.asList(
                0.00000,
                0.14285714285714285,
                0.2857142857142857,
                0.2857142857142857,
                0.42857142857142855,
                0.5714285714285714,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.8571428571428571,
                0.8571428571428571,
                0.8571428571428571,
                1.0);
        List<ROCPoint> expResult = new LinkedList<>();
        for (int i = 0; i < expTPRAxis.size(); i++) {
            expResult.add(new ROCPoint(
                    BigDecimal.valueOf(expFPRAxis.get(i)), 
                    BigDecimal.valueOf(expTPRAxis.get(i))));
        }

        List<? extends Point> result = instance.getCurvePoints();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMeasure() {
//        List<? extends Point> result = instance.getListMeasures(classificationResults);
        double result = instance.getMeasure();
        double expResult = 0.6547619047619048;
        expResult = Math.round(expResult * Math.pow(10D, SCALE)) / Math.pow(10D, SCALE);
        double lim = 0.000;
//        assertTrue(result <= expResult + lim && result >= expResult - lim);
        assertEquals(expResult, result, lim);
    }

}