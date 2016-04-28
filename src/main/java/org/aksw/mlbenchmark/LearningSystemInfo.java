package org.aksw.mlbenchmark;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.tree.MergeCombiner;

/**
 * Created by Simon Bin on 16-4-27.
 */
public class LearningSystemInfo {
	final String learningSystem;
	final BenchmarkRunner br;
	private Configuration config;

	public LearningSystemInfo(BenchmarkRunner parent, String learningSystem) {
		this.learningSystem = learningSystem;
		this.br = parent;
		Configuration defaultConfig = new BaseConfiguration();
		defaultConfig.setProperty("language", learningSystem.toLowerCase().equals("dllearner") ? "owl" : "prolog");
		Configuration runtimeConfig = br.getConfig();
		Configuration lsRuntimeConfig = runtimeConfig.subset("learningsystems." + learningSystem);
		ConfigLoader systemCL = ConfigLoader.findConfig(getDir()+"/"+"system");
		CombinedConfiguration cc = new CombinedConfiguration();
		cc.setNodeCombiner(new MergeCombiner());
		cc.addConfiguration(lsRuntimeConfig);
		if (systemCL != null) {
			cc.addConfiguration(systemCL.config());
		}
		cc.addConfiguration(defaultConfig);
		defaultConfig.setProperty("configFormat", "owl".equals(cc.getString("language")) ? "prop" : "conf");
		config = cc;
	}
	public String getDir() {
		return br.getLearningSystemDir(learningSystem);
	}
	public Configuration getConfig() {
		return config;
	}

	public String getPosFilename() {
		return config.getString("filename.pos",
				LanguageInfo.forLanguage(config.getString("language")).getPosFilename());
	}

	public String getNegFilename() {
		return config.getString("filename.neg",
				LanguageInfo.forLanguage(config.getString("language")).getNegFilename());
	}

	public String getBaseFilename() {
		return config.getString("filename.base",
				LanguageInfo.forLanguage(config.getString("language")).getBaseFilename());
	}
}
