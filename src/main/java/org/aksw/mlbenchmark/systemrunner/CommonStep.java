package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.*;
import org.aksw.mlbenchmark.config.BenchmarkConfig;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.aksw.mlbenchmark.process.ProcessRunner;
import org.aksw.mlbenchmark.resultloader.ResultLoaderBase;
import org.aksw.mlbenchmark.validation.measures.MeasureMethodTwoValued;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.exec.ExecuteException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import org.aksw.mlbenchmark.validation.measures.ClassificationResult;
import org.aksw.mlbenchmark.validation.measures.MeasureMethodNumericValued;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Actions shared between several types of steps (Cross Validation etc.)
 */
public abstract class CommonStep {

    protected final LearningSystemInfo lsi;
    protected final String system;
    protected final Constants.LANGUAGES lang;
    protected final ScenarioSystem ss;
    protected final ConfigLoader learningProblemConfigLoader;
    protected final SystemRunner parent;
    protected Constants.State state;
    protected String trainingResultFile;

    public CommonStep(SystemRunner parent, ScenarioSystem ss, ConfigLoader learningProblemConfigLoader) {
        this.parent = parent;
        this.lsi = ss.getLearningSystemInfo();
        this.system = ss.getLearningSystem();
        this.lang = lsi.getLanguage();
        this.ss = ss;
        this.learningProblemConfigLoader = learningProblemConfigLoader;
    }

    protected Configuration collectConfig(BaseConfiguration baseConfig) {
        BenchmarkConfig runtimeConfig = parent.getBenchmarkRunner().getConfig();
        Configuration scnRuntimeConfig = runtimeConfig.getLearningTaskConfiguration(ss.getTask());
        Configuration lpRuntimeConfig = runtimeConfig.getLearningProblemConfiguration(ss);

        CombinedConfiguration cc = new CombinedConfiguration();
        cc.setNodeCombiner(new MergeCombiner());
        cc.addConfiguration(baseConfig);
        cc.addConfiguration(parent.getParentConfiguration());
        cc.addConfiguration(lpRuntimeConfig);
        if (learningProblemConfigLoader != null) {
            cc.addConfiguration(learningProblemConfigLoader.config());
        }
        List<String> families = lsi.getFamilies();
        if (families != null) {
            for (String family : families) {
                ConfigLoader famLpCL = ConfigLoader.findConfig(parent.getBenchmarkRunner().getLearningProblemDir(ss) + "/" + family);
                if (famLpCL != null) {
                    cc.addConfiguration(famLpCL.config());
                }
            }
        }
        cc.addConfiguration(scnRuntimeConfig);
        cc.addConfiguration(lsi.getCommonsConfig());
        cc.addConfiguration(parent.getBenchmarkRunner().getCommonsConfig());
        BaseConfiguration defaultConfig = new BaseConfiguration();
        defaultConfig.setProperty("maxExecutionTime", (long) (new BenchmarkConfig(cc).getMaxExecutionTime() * 0.86));
        cc.addConfiguration(defaultConfig);
        return cc;
    }

    protected abstract void saveLearningSystemsConfig(String configFile);

    protected abstract void saveResultSet();

    protected abstract BaseConfiguration getBaseConfiguration(File dir, String posFilename, String negFilename);

    protected abstract BaseConfiguration getValidateConfiguration(File dir, String posFilename, String negFilename, String outputFilename);

    protected abstract Set<String> getTrainingExamples(Constants.LANGUAGES lang, Constants.ExType pos);

    protected abstract Set<String> getValidateExamples(Constants.LANGUAGES lang, Constants.ExType type);

    protected abstract String getResultDir();

    protected abstract String getResultKey();

    protected Constants.State simpleProcessRunner(String command, List<String> args, Configuration cc, long maxExecutionTime, String expectedOutput, String info) {
        Constants.State state = Constants.State.RUNNING;
        try {
            ProcessRunner processRunner = new ProcessRunner(lsi.getDir(), command, args, cc, maxExecutionTime);
            state = Constants.State.OK;
        } catch (ExecuteException e) {
            if (e.getExitValue() == 143) {
                CrossValidationRunner.logger.warn(info + " " + system + " was canceled due to timeout");
                state = Constants.State.TIMEOUT;
            } else {
                CrossValidationRunner.logger.warn(info + " " + system + " did not finish cleanly: " + e.getMessage());
                state = Constants.State.FAILURE;
            }
        } catch (IOException e) {
            CrossValidationRunner.logger.warn(info + " " + system + " could not execute: " + e.getMessage() + "[" + e.getClass() + "]");
            state = Constants.State.ERROR;
        }
        if (expectedOutput != null) {
            File file = new File(expectedOutput);
            if (state.equals(Constants.State.OK) && !file.isFile()) {
                CrossValidationRunner.logger.warn(info + " " + system + " did not produce an output");
                state = Constants.State.FAILURE;
            }
        }
        return state;
    }

    public Constants.State getState() {
        return state;
    }

    public boolean isStateOk() {
        return Constants.State.OK.equals(state);
    }

    public void train() {
        File dir = new File(parent.getBenchmarkRunner().getTempDirectory() + "/" + getResultDir() + "/" + "train");
        dir.mkdirs();

        String posFilename = dir + "/" + lsi.getFilename(Constants.ExType.POS);
        String negFilename = dir + "/" + lsi.getFilename(Constants.ExType.NEG);
        this.trainingResultFile = dir + "/" + "train.out";
        String configFile = dir + "/" + "config." + lsi.getConfigFormat();

        Set<String> trainingPos = getTrainingExamples(lang, Constants.ExType.POS);
        Set<String> trainingNeg = getTrainingExamples(lang, Constants.ExType.NEG);

        BaseConfiguration baseConfig = getBaseConfiguration(dir, posFilename, negFilename);

        Configuration cc = collectConfig(baseConfig);
        AbstractSystemRunner.writeConfig(configFile, cc);
        AbstractSystemRunner.writeExamplesFiles(lang, posFilename, trainingPos, negFilename, trainingNeg);

        List<String> args = new LinkedList<>();
        args.add(configFile);
        saveLearningSystemsConfig(configFile);

        final long now = System.nanoTime();
        state = simpleProcessRunner("./run", args, cc, parent.getBenchmarkRunner().getConfig().getMaxExecutionTime(), trainingResultFile, "learning system");
        long duration = System.nanoTime() - now;
        File outputFileFile = new File(trainingResultFile);
        String resultKey = getResultKey();
        parent.getResultset().setProperty(resultKey + "." + "duration", duration / 1000000000); // nanoseconds -> seconds

        ResultLoaderBase resultLoader = new ResultLoaderBase();
        try {
            resultLoader.loadResults(outputFileFile);
            parent.getResultset().setProperty(resultKey + "." + "trainingRaw", resultLoader.getResults());
        } catch (IOException e) {
            CrossValidationRunner.logger.warn("learning system " + system + " result cannot be read: " + e.getMessage());
            state = state.ERROR;
        }

        parent.getResultset().setProperty(resultKey + "." + "trainingResult", state.toString().toLowerCase());
    }

    public void validate() {
        File dir = new File(parent.getBenchmarkRunner().getTempDirectory() + "/" + getResultDir() + "/" + "validate");
        dir.mkdirs();

        String posFilename = dir + "/" + lsi.getFilename(Constants.ExType.POS);
        String negFilename = dir + "/" + lsi.getFilename(Constants.ExType.NEG);
        String outputFile = dir + "/" + "validateResult.prop";
        String configFile = dir + "/" + "config." + lsi.getConfigFormat();

        Set<String> testingPos = getValidateExamples(lang, Constants.ExType.POS);
        Set<String> testingNeg = getValidateExamples(lang, Constants.ExType.NEG);

        BaseConfiguration baseConfig = getValidateConfiguration(dir, posFilename, negFilename, outputFile);

        Configuration cc = collectConfig(baseConfig);
        AbstractSystemRunner.writeConfig(configFile, cc);
        AbstractSystemRunner.writeExamplesFiles(lang, posFilename, testingPos, negFilename, testingNeg);

        List<String> args = new LinkedList<>();
        args.add(configFile);

        state = simpleProcessRunner("./validate", args, cc, 0, outputFile, "validation system");

        String resultKey = getResultKey();
        HierarchicalConfiguration<ImmutableNode> result = null;
        if (state.equals(Constants.State.OK)) {
            try {
                result = new ConfigLoader(outputFile).load().config();
            } catch (ConfigLoaderException e) {
                CrossValidationRunner.logger.warn("could not load validation result: " + e.getMessage());
                state = Constants.State.FAILURE;
            }
        }
        parent.getResultset().setProperty(resultKey + "." + "validationResult", state.toString().toLowerCase());
        if (!state.equals(Constants.State.OK)) {
            saveResultSet();

            return;
        }

        if (result != null) {
            Iterator<String> keys = result.getKeys();
            while (keys.hasNext()) {
                String key = keys.next();
                parent.getResultset().setProperty(resultKey + "." + "ValidationRaw" + "." + key, result.getProperty(key));
            }
        }
        // Here all the measures are computed
        // This works only for binary classifier, for scoring classifier
        // we can use ROC, PR curves and their areas
        List<String> measures = parent.getBenchmarkRunner().getConfig().getMeasures();
        Constants.SystemType systemType = lsi.getConfig().getSystemType();
        for (String m : measures) {
            if (systemType == Constants.SystemType.PROBABILISTIC || 
                    MeasureMethod.getType(m) == MeasureMethodNumericValued.class) {
                try {
                    List<String> values = result.getList(String.class, "values");
                    List<ClassificationResult> classificationResults = new LinkedList<>();
                    for (String value : values) {
                        String[] v = value.split("-");
                        Double param = new BigDecimal(v[0]).doubleValue();
//                        Double param = Double.parseDouble(v[0]);
                        Constants.ExType exType = Constants.ExType.valueOf(v[1].toUpperCase());
                        classificationResults.add(new ClassificationResult(param, exType));
                    }
//                    for (String m : measures) {
                    MeasureMethodNumericValued method = MeasureMethod.create(
                            m, testingPos.size(), testingNeg.size(), classificationResults);
                    double measure = method.getAUC();
                    parent.getResultset().setProperty(resultKey + "." + "measure" + "." + m, measure);
//                    }
                } catch (Exception e) {
                    CrossValidationRunner.logger.warn("invalid validation results: " + e.getMessage());
                    state = Constants.State.ERROR;
                    parent.getResultset().setProperty(resultKey + "." + "validationResult", state.toString().toLowerCase());
                }

            } else if (systemType == Constants.SystemType.DISCRETE) {
                try {
                    int tp = result.getInt("tp");
                    int fn = result.getInt("fn");
                    int fp = result.getInt("fp");
                    int tn = result.getInt("tn");
//                    for (String m : measures) {
                    MeasureMethodTwoValued method = MeasureMethod.create(m);
                    double measure = method.getMeasure(tp, fn, fp, tn);
                    parent.getResultset().setProperty(resultKey + "." + "measure" + "." + m, measure);
//                    }
                } catch (ConversionException | NoSuchElementException e) {
                    CrossValidationRunner.logger.warn("invalid validation results: " + e.getMessage());
                    state = Constants.State.ERROR;
                    parent.getResultset().setProperty(resultKey + "." + "validationResult", state.toString().toLowerCase());
                }
            } else {
                throw new NotImplementedException("System not compatible with the method type " + MeasureMethod.getType(m).getName());
            }
        }

        saveResultSet();
    }
}
