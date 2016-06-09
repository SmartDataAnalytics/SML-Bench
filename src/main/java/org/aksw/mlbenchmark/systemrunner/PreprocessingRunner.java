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
package org.aksw.mlbenchmark.systemrunner;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.aksw.mlbenchmark.BenchmarkRunner;
import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.config.LearningSystemConfig;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Giuseppe Cota <giuseppe.cota@unife.it>
 */
public class PreprocessingRunner extends AbstractSystemRunner {
    final static Logger logger = LoggerFactory.getLogger(PreprocessingRunner.class);
    final static String CONFIG_FILEBASE = "config_preproc";
    ScenarioSystem ss;
    String system;

    public PreprocessingRunner(String system, BenchmarkRunner benchmarkRunner, Scenario scn, Configuration baseConf) {
        super(benchmarkRunner, scn, baseConf);
        this.system = system;
        ss = scn.addSystem(parent.getSystemInfo(system));
    }

    public String getResultDir() {
        return PreprocessingRunner.getResultDir(ss);
    }

    public static String getResultDir(ScenarioSystem ss) {
        return ss.getTask() + "/" + ss.getProblem() + "/" + ss.getLearningSystem();
    }

    @Override
    public void run() {
        File dir = new File(parent.getTempDirectory() + "/" + getResultDir());
        dir.mkdirs();
        String configFile = dir + "/" + "config." + ss.getLearningSystemInfo().getConfigFormat();
        LearningSystemInfo lsi = ss.getLearningSystemInfo();
        //Configuration cc = collectConfig(baseConfig);
        BenchmarkConfig runtimeConfig = getBenchmarkRunner().getConfig();
        Configuration scnRuntimeConfig = runtimeConfig.getLearningTaskConfiguration(ss.getTask());
        Configuration lpRuntimeConfig = runtimeConfig.getLearningProblemConfiguration(ss);

        BaseConfiguration baseConfig = new BaseConfiguration();
	baseConfig.setProperty("data.workdir", dir.getAbsolutePath());
	baseConfig.setProperty("learningtask", scn.getTask());
	baseConfig.setProperty("learningproblem", scn.getProblem());
        baseConfig.addProperty(LearningSystemConfig.PREPROCESSING_KEY + ".output",
                dir + "/" + CONFIG_FILEBASE +"." + ss.getLearningSystemInfo().getConfigFormat());
        
        CombinedConfiguration cc = new CombinedConfiguration();
        cc.setNodeCombiner(new MergeCombiner());
        cc.addConfiguration(baseConfig);
        ss.getLearningSystemInfo().getCommonsConfig().addProperty(LearningSystemConfig.PREPROCESSING_KEY + ".output",
                dir + "/" + "config_preproc." + ss.getLearningSystemInfo().getConfigFormat());
        String configPreprocFilename = ss.getLearningSystemInfo().getCommonsConfig().getString("preprocessing.output");
//        getParentConfiguration().addProperty(LearningSystemConfig.PREPROCESSING_KEY + ".output",
//                dir + "/" + "config_preproc." + ss.getLearningSystemInfo().getConfigFormat());
        //cc.addConfiguration(ConfigurationUtils.cloneConfiguration(getParentConfiguration()));
        cc.addConfiguration(lpRuntimeConfig);
//        cc.clearProperty(LearningSystemConfig.PREPROCESSING_KEY);
//        cc.clearProperty(LearningSystemConfig.PREPROCESSING_FIELDS_KEY);
//        cc.addProperty(LearningSystemConfig.PREPROCESSING_KEY + ".output",
//                dir + "/" + "config_preproc." + ss.getLearningSystemInfo().getConfigFormat());
        
        ConfigLoader learningProblemConfigLoader = ConfigLoader.findConfig(parent.getLearningProblemDir(ss) + "/" + this.system);
        if (learningProblemConfigLoader != null) {
            cc.addConfiguration(learningProblemConfigLoader.config());
        }
        List<String> families = lsi.getFamilies();
        if (families != null) {
            for (String family : families) {
                ConfigLoader famLpCL = ConfigLoader.findConfig(parent.getLearningProblemDir(ss) + "/" + family);
                if (famLpCL != null) {
                    cc.addConfiguration(famLpCL.config());
                }
            }
        }
        cc.addConfiguration(scnRuntimeConfig);
        cc.addConfiguration(lsi.getCommonsConfig());
        cc.addConfiguration(parent.getCommonsConfig());

        AbstractSystemRunner.writeConfig(configFile, cc);

        DefaultExecutor e = new DefaultExecutor();
        e.setWorkingDirectory(new File(lsi.getDir()));
        CommandLine cmd = new CommandLine("./preprocessing");

        List<String> args = new LinkedList<>();
        args.add(configFile);

        cmd.addArguments(args.toArray(new String[0]));
        try {
            e.execute(cmd);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

}
