package org.aksw.mlbenchmark.config;

import java.util.List;

import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.container.LanguageInfo;
import org.aksw.mlbenchmark.languages.LanguageInfoBase;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.MergeCombiner;

/**
 * Configuration specific to LearningSystems (wrapping the underlying key-value config)
 */
public class LearningSystemConfig {
	private final Configuration config;
	private final String FILENAMEKEY = "filename";
	final String LANGUAGEKEY = "language";

	public LearningSystemConfig(BenchmarkConfig runtimeConfig, LearningSystemInfo lsi) {
		Configuration defaultConfig = new BaseConfiguration();
		// FIXME: we need to find a generic way to determine the language
		defaultConfig.setProperty(LANGUAGEKEY, lsi.hasType("dllearner") ? "owl" : "prolog");
		// Extract a sub-configuration from the configuration passed by the user.
		// The extracted sub-configuration should contain information about
		// learning system
		Configuration lsRuntimeConfig = runtimeConfig.getLearningSystemConfiguration(lsi);
		
		HierarchicalConfiguration<ImmutableNode> systemConf =
				ConfigLoader.findConfig(lsi.getDir()+"/"+ Constants.LEARNINGSYSTEMCONFIG);
		
		CombinedConfiguration cc = new CombinedConfiguration();
		cc.setNodeCombiner(new MergeCombiner());
		cc.addConfiguration(lsRuntimeConfig);
		if (systemConf != null) {
			cc.addConfiguration(systemConf);
		}
		cc.addConfiguration(defaultConfig);
			this.config = cc;
		defaultConfig.setProperty("configFormat", Constants.LANGUAGES.OWL.equals(getLanguage()) ? "prop" : "conf");
	}

	public LanguageInfoBase getLanguageInfo() {
		return LanguageInfo.forLanguage(getLanguage());
	}

	public String getFilename(Constants.ExType type) {
		return config.getString(FILENAMEKEY+"."+type.asString(), getLanguageInfo().getFilename(type));
	}

	public String getBaseFilename() {
		return config.getString(FILENAMEKEY+".base", getLanguageInfo().getBaseFilename());
	}

	public List<String> getFamilies() {
		return config.getList(String.class, "families");
	}

	public Configuration getConfig() {
		return config;
	}

	public Constants.LANGUAGES getLanguage() {
		return Constants.LANGUAGES.valueOf(
				config.getString(LANGUAGEKEY).toUpperCase());
	}

	public String getConfigFormat() {
		return config.getString("configFormat");
	}
}
