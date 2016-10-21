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

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class BenchmarkIT {

    public BenchmarkIT() {
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
     * Test of main method, of class Benchmark.
     */
    //@Test
    public void testMain1() throws ConfigLoaderException {
        System.out.println("main");
        String configFile = "src/main/resources/test.plist";
        String[] args = new String[1];
        args[0] = configFile;
        Benchmark.main(args);
    }

    //@Test
    public void testMain2() throws ConfigLoaderException {
        System.out.println("main");
        String configFile = "src/main/resources/trains.plist";
        String[] args = new String[1];
        args[0] = configFile;
        Benchmark.main(args);
    }

//    @Test
    public void testMain3() throws ConfigLoaderException {
        System.out.println("main");
        String configFile = "src/main/resources/leap.plist";
        String[] args = new String[1];
        args[0] = configFile;
        Benchmark.main(args);
    }

    //@Test
    public void testMain4() throws ConfigLoaderException {
        System.out.println("main");
        String configFile = "src/main/resources/trains.plist";
        String[] args = new String[1];
        args[0] = configFile;
        Benchmark.main(args);
    }
    
    @Test
    public void testMain5() throws ConfigLoaderException {
        System.out.println("main");
        String configFile = "src/main/resources/test_5.plist";
        String[] args = new String[1];
        args[0] = configFile;
        Benchmark.main(args);
    }
}
