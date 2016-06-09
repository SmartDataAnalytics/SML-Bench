package org.aksw.mlbenchmark.config;

import org.aksw.mlbenchmark.*;
import org.aksw.mlbenchmark.languages.LanguageInfoBase;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.tree.MergeCombiner;

import java.util.List;

/**
 * Configuration specific to LearningSystems (wrapping the underlying key-value
 * config)
 */
public class LearningSystemConfig {

    private final Configuration config;
    private final String FILENAMEKEY = "filename";
    final String LANGUAGEKEY = "language";
    public static final String PREPROCESSING_FIELDS_KEY = "preproc_fields";
    public static final String PREPROCESSING_KEY = "preprocessing";
    public static final String SYSTEM_TYPE_KEY = "type";

    public LearningSystemConfig(BenchmarkRunner br, LearningSystemInfo lsi) {
        Configuration defaultConfig = new BaseConfiguration();
        // FIXME: we need to find a generic way to determine the language
        defaultConfig.setProperty(LANGUAGEKEY, lsi.hasType("dllearner") ? "owl" : "prolog");
        BenchmarkConfig runtimeConfig = br.getConfig();
                // Extract a subconfiguration from the configuration passed by the user.
        // The extracted subconfiguration should contain information about 
        // learning system
        Configuration lsRuntimeConfig = runtimeConfig.getLearningSystemConfiguration(lsi);
        ConfigLoader systemCL = ConfigLoader.findConfig(lsi.getDir() + "/" + Constants.LEARNINGSYSTEMCONFIG);
        CombinedConfiguration cc = new CombinedConfiguration();
        cc.setNodeCombiner(new MergeCombiner());
        cc.addConfiguration(lsRuntimeConfig);
        if (systemCL != null) {
            cc.addConfiguration(systemCL.config());
        }
        cc.addConfiguration(defaultConfig);
        this.config = cc;
        defaultConfig.setProperty("configFormat", Constants.LANGUAGES.OWL.equals(getLanguage()) ? "prop" : "conf");
        /* 
        // add a property about output of preprocessing
        */
    }

    public LearningSystemConfig(BenchmarkRunner br, String learningSystem) {
        this(br, br.getSystemInfo(learningSystem));
    }

    public LanguageInfoBase getLanguageInfo() {
        return LanguageInfo.forLanguage(getLanguage());
    }

    public String getFilename(Constants.ExType type) {
        return config.getString(FILENAMEKEY + "." + type.asString(), getLanguageInfo().getFilename(type));
    }

    public String getBaseFilename() {
        return config.getString(FILENAMEKEY + ".base", getLanguageInfo().getBaseFilename());
    }

    public List<String> getFamilies() {
        return config.getList(String.class, "families");
    }

    public Configuration getConfig() {
        return config;
    }

    public Boolean needsPreprocessing() {
        return config.getBoolean(PREPROCESSING_KEY, Boolean.FALSE);
    }
    
    public List<String> getPreprocessingFields() {
        return config.getList(String.class, "preproc_fields");
    }
    
    public Constants.SystemType getType() {
        return Constants.SystemType.valueOf(
        config.getString(SYSTEM_TYPE_KEY, Constants.SystemType.DISCRETE.asString()));
    }

    public Constants.LANGUAGES getLanguage() {
        return Constants.LANGUAGES.valueOf(
                config.getString(LANGUAGEKEY).toUpperCase());
    }

    public String getConfigFormat() {
        return config.getString("configFormat");
    }
}
