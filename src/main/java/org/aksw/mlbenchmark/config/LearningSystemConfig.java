package org.aksw.mlbenchmark.config;

import org.aksw.mlbenchmark.*;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.tree.MergeCombiner;

import java.util.List;

/**
 * Configuration specific to LearningSystems
 */
public class LearningSystemConfig {
	private final Configuration config;
	private final String FILENAMEKEY = "filename";
	final String LANGUAGEKEY = "language";

	public LearningSystemConfig(BenchmarkRunner br, LearningSystemInfo lsi) {
		Configuration defaultConfig = new BaseConfiguration();
		// FIXME: we need to find a generic way to determine the language
		defaultConfig.setProperty(LANGUAGEKEY, "dllearner".equals(lsi.asString()) ? "owl" : "prolog");
		BenchmarkConfig runtimeConfig = br.getConfig();
		Configuration lsRuntimeConfig = runtimeConfig.getLearningSystemConfiguration(lsi);
		ConfigLoader systemCL = ConfigLoader.findConfig(lsi.getDir()+"/"+ Constants.LEARNINGSYSTEMCONFIG);
		CombinedConfiguration cc = new CombinedConfiguration();
		cc.setNodeCombiner(new MergeCombiner());
		cc.addConfiguration(lsRuntimeConfig);
		if (systemCL != null) {
			cc.addConfiguration(systemCL.config());
		}
		cc.addConfiguration(defaultConfig);
		defaultConfig.setProperty("configFormat", "owl".equals(cc.getString(LANGUAGEKEY)) ? "prop" : "conf");
		this.config = cc;
	}

	public String getPosFilename() {
		return config.getString(FILENAMEKEY+".pos",
				LanguageInfo.forLanguage(config.getString(LANGUAGEKEY)).getPosFilename());
	}

	public String getNegFilename() {
		return config.getString(FILENAMEKEY+".neg",
				LanguageInfo.forLanguage(config.getString(LANGUAGEKEY)).getNegFilename());
	}

	public String getBaseFilename() {
		return config.getString(FILENAMEKEY+".base",
				LanguageInfo.forLanguage(config.getString(LANGUAGEKEY)).getBaseFilename());
	}

	public List<String> getFamilies() {
		return config.getList(String.class, "families");
	}

	public Configuration getConfig() {
		return config;
	}
}
