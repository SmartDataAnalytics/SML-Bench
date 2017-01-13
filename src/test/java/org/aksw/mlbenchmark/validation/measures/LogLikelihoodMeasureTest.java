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
 * @author giuseppe
 */
public class LogLikelihoodMeasureTest {

    public LogLikelihoodMeasureTest() {
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
     * Test of getMeasure method, of class LogLikelihoodMeasure.
     */
    @Test
    public void testGetMeasure() {
        System.out.println("getMeasure");
        List<ClassificationResult> classificationResults = new LinkedList<>();
        
        List<Double> listParams = Arrays.asList(
                0.4294291147645299, 0.4294291147645299, 0.4294291147645299, 
                0.4294291147645299, 0.4294291147645299, 0.4294291147645299,
                0.4294291147645299, 0.6647779247964769, 0.6647779247964769,
                0.6647779247964769);
        List<Integer> listClasses = Arrays.asList(
                1, 1, 1, 0, 0, 0, 0, 1, 1, 0);
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
        int nPos = 5;
        int nNeg = 5;
        LogLikelihoodMeasure instance = new LogLikelihoodMeasure(nPos, nNeg, classificationResults);
        double expResult = -6.68993378200733;
        double result = instance.getMeasure();
        // approximation
        expResult = Math.round(expResult * Math.pow(10D, SCALE)) / Math.pow(10D, SCALE);
        //result = Math.round(result * Math.pow(10D, SCALE)) / Math.pow(10D, SCALE);
        // assert equality
        assertEquals(expResult, result, 0.0);
    }
    
}
