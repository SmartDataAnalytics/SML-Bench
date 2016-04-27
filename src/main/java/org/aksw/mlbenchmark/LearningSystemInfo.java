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
	public LearningSystemInfo(BenchmarkRunner parent, String learningSystem) {
		this.learningSystem = learningSystem;
		this.br = parent;
	}
	public String getDir() {
		return br.getLearningSystemDir(learningSystem);
	}
	public Configuration getConfig() {
		Configuration defaultConfig = new BaseConfiguration();
		defaultConfig.setProperty("language", learningSystem.toLowerCase().equals("dllearner") ? "owl" : "prolog");
		Configuration runtimeConfig = br.getConfig();
		Configuration lsRuntimeConfig = runtimeConfig.subset("learningsystems." + learningSystem);
		ConfigLoader systemCL = ConfigLoader.findConfig(getDir()+"/"+"system");
		CombinedConfiguration cc = new CombinedConfiguration();
		cc.setNodeCombiner(new MergeCombiner());
		if (systemCL != null) {
			cc.addConfiguration(systemCL.config());
		}
		cc.addConfiguration(lsRuntimeConfig);
		cc.addConfiguration(defaultConfig);
		return cc;
	}
}
