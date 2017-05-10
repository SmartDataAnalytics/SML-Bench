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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class BenchmarkRunnerTest {

    static BenchmarkRunner instance;
    
    public static BenchmarkConfig configMock;
    static Long seed = 0L;
    static Integer crossValidationFolds = 3;
    static Boolean leaveOneOut = false;
    static Integer threadsCount = 3;
    static String mexOutputFile = "/tmp/smlbench_outfile";
    static List<String> learningSystem = Arrays.asList("dllearner", "golem");
    static List<String> scenarios = Arrays.asList("carcinogenesis/1", "mutagenesis/42");
    
    // initialize the Mokito classes
//    @ClassRule
//    public static MockitoRule mockitoRule = MockitoJUnit.rule();

    public BenchmarkRunnerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws ConfigLoaderException {
        // set up mock class
        configMock = mock(BenchmarkConfig.class);
        when(configMock.getLearningSystems()).thenReturn(learningSystem);
        when(configMock.getSeed()).thenReturn(seed);
        when(configMock.getCrossValidationFolds()).thenReturn(crossValidationFolds);
        when(configMock.isLeaveOneOut()).thenReturn(leaveOneOut);
        when(configMock.getThreadsCount()).thenReturn(threadsCount);
        when(configMock.getMexOutputFile()).thenReturn(mexOutputFile);
        when(configMock.getScenarios()).thenReturn(scenarios);
        when(configMock.getLearningSystemConfiguration(any(LearningSystemInfo.class))).thenReturn(new BaseConfiguration());
        when(configMock.getLearningTaskConfiguration(anyString())).thenReturn(new BaseConfiguration());
        when(configMock.getLearningProblemConfiguration(any(Scenario.class))).thenReturn(new BaseConfiguration());
        when(configMock.getConfig()).thenReturn(new BaseHierarchicalConfiguration());
        // create instance
        instance = new BenchmarkRunner(configMock);
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

    @Test
    public void testBenchmarkRunnerConstructorTakesBenchmarkConfig() throws ConfigLoaderException {

    }

//    /**
//     * Test of getExecutorService method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetExecutorService() {
//        System.out.println("getExecutorService");
//        BenchmarkRunner instance = null;
//        ExecutorService expResult = null;
//        ExecutorService result = instance.getExecutorService();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDesiredLanguages method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetDesiredLanguages() {
//        System.out.println("getDesiredLanguages");
//        BenchmarkRunner instance = null;
//        Set<Constants.LANGUAGES> expResult = null;
//        Set<Constants.LANGUAGES> result = instance.getDesiredLanguages();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of getTempDirectory method, of class BenchmarkRunner.
     */
    @Test
    public void testGetTempDirectory() {
        Path result = instance.getTempDirectory();
        assertTrue(result.toFile().exists());
    }
//
//    /**
//     * Test of getDesiredSystems method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetDesiredSystems() {
//        System.out.println("getDesiredSystems");
//        BenchmarkRunner instance = null;
//        List<String> expResult = null;
//        List<String> result = instance.getDesiredSystems();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of getSeed method, of class BenchmarkRunner.
     */
    @Test
    public void testGetSeed() {
        Long result = instance.getSeed();
        assertEquals(seed, result);
    }

    /**
     * Test of getFolds method, of class BenchmarkRunner.
     */
    @Test
    public void testGetFolds() {
        Integer result = instance.getFolds();
        assertEquals(crossValidationFolds, result);
    }

    /**
     * Test of getLearningSystemDir method, of class BenchmarkRunner.
     */
    @Test
    public void testGetLearningSystemDir() {
        String learningSystem = "golem";
        String expResult = instance.getLearningSystemsDir() + "/" + learningSystem;
        String result = instance.getLearningSystemDir(learningSystem);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLearningTasksDir method, of class BenchmarkRunner.
     */
    @Test
    public void testGetLearningTasksDir() {
        String result = instance.getLearningTasksDir();
        assertTrue(result.endsWith(Constants.LEARNINGTASKS) || result.endsWith(Constants.LEARNINGTASKS + "/"));
    }
//
//    /**
//     * Test of getLearningProblemsDir method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetLearningProblemsDir() {
//        System.out.println("getLearningProblemsDir");
//        String learningTask = "";
//        Constants.LANGUAGES languageType = null;
//        BenchmarkRunner instance = null;
//        String expResult = "";
//        String result = instance.getLearningProblemsDir(learningTask, languageType);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLearningProblemDir method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetLearningProblemDir_Scenario_ConstantsLANGUAGES() {
//        System.out.println("getLearningProblemDir");
//        Scenario scn = null;
//        Constants.LANGUAGES languageType = null;
//        BenchmarkRunner instance = null;
//        String expResult = "";
//        String result = instance.getLearningProblemDir(scn, languageType);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLearningProblemDir method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetLearningProblemDir_ScenarioLangAttributes() {
//        System.out.println("getLearningProblemDir");
//        ScenarioLangAttributes sl = null;
//        BenchmarkRunner instance = null;
//        String expResult = "";
//        String result = instance.getLearningProblemDir(sl);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLearningProblemsDirFromScenario method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetLearningProblemsDirFromScenario() {
//        System.out.println("getLearningProblemsDirFromScenario");
//        ScenarioLangAttributes sl = null;
//        BenchmarkRunner instance = null;
//        String expResult = "";
//        String result = instance.getLearningProblemsDirFromScenario(sl);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getConfig method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetConfig() {
//        System.out.println("getConfig");
//        BenchmarkRunner instance = null;
//        BenchmarkConfig expResult = null;
//        BenchmarkConfig result = instance.getConfig();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCommonsConfig method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetCommonsConfig() {
//        System.out.println("getCommonsConfig");
//        BenchmarkRunner instance = null;
//        HierarchicalConfiguration<ImmutableNode> expResult = null;
//        HierarchicalConfiguration<ImmutableNode> result = instance.getCommonsConfig();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of run method, of class BenchmarkRunner.
     */
//    @Test
//    public void testRun() {
//        System.out.println("run");
//        instance.run();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSystemLanguage method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetSystemLanguage() {
//        System.out.println("getSystemLanguage");
//        String sys = "";
//        BenchmarkRunner instance = null;
//        Constants.LANGUAGES expResult = null;
//        Constants.LANGUAGES result = instance.getSystemLanguage(sys);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSystemInfo method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetSystemInfo() {
//        System.out.println("getSystemInfo");
//        String system = "";
//        BenchmarkRunner instance = null;
//        LearningSystemInfo expResult = null;
//        LearningSystemInfo result = instance.getSystemInfo(system);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getResultset method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetResultset() {
//        System.out.println("getResultset");
//        BenchmarkRunner instance = null;
//        Configuration expResult = null;
//        Configuration result = instance.getResultset();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of cleanTemp method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testCleanTemp() {
//        System.out.println("cleanTemp");
//        BenchmarkRunner instance = null;
//        instance.cleanTemp();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of getBenchmarkLog method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetBenchmarkLog() {
//        System.out.println("getBenchmarkLog");
//        BenchmarkRunner instance = null;
//        BenchmarkLog expResult = null;
//        BenchmarkLog result = instance.getBenchmarkLog();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getExamplesFile method, of class BenchmarkRunner.
//     */
//    @Test
//    public void testGetExamplesFile() {
//        System.out.println("getExamplesFile");
//        ScenarioLang sl = null;
//        Constants.ExType type = null;
//        BenchmarkRunner instance = null;
//        String expResult = "";
//        String result = instance.getExamplesFile(sl, type);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
