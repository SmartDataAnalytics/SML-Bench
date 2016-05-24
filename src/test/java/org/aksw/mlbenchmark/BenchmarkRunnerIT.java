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
package org.aksw.mlbenchmark;

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
public class BenchmarkRunnerIT {

    static BenchmarkRunner instance;

    public BenchmarkRunnerIT() {
    }

    @BeforeClass
    public static void setUpClass() throws ConfigLoaderException {
        System.out.println("Current directory: " + System.getProperty("user.dir"));
        String configFile = "src/main/resources/test.plist";
        instance = new BenchmarkRunner(configFile);
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
     * Test of run method, of class BenchmarkRunner.
     */
    @Test
    public void testRun() {
        System.out.println("run (without any final test)");
        instance.run();
        fail();
    }

}
