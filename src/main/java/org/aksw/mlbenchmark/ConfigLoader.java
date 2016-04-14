package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.config.INIConfigurationWriteDotkeys;
import org.aksw.mlbenchmark.config.PropertiesConfigurationFromDotkeys;
import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Loading of config files
 */
public class ConfigLoader {
	static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
	private final String filename;
	private HierarchicalConfiguration<ImmutableNode> config;

	public ConfigLoader(String filename) {
		this.filename = filename;
	}

	public final ConfigLoader load() throws ConfigLoaderException {
		if (filename.endsWith(".plist")) {
			config = loadFile(PropertyListConfiguration.class);
		} else if (filename.endsWith(".xml")) {
			config = loadFile(XMLPropertyListConfiguration.class);
		} else if (filename.endsWith(".ini") || filename.endsWith(".conf")) {
			config = loadINIFile();
		} else if (filename.endsWith(".prop") || filename.endsWith(".properties")) {
			CombinedConfiguration config2 = new CombinedConfiguration();
			config2.addConfiguration(loadFile(PropertiesConfiguration.class));
			config = config2;
		} else {
			throw new ConfigLoaderException("Loading of config type not implemented yet.");
		}
		return this;
	}

	private HierarchicalConfiguration<ImmutableNode> loadINIFile() throws ConfigLoaderException {
		final String MAIN_SECTION = "main";
		HierarchicalConfiguration<ImmutableNode> ini = loadFile(INIConfiguration.class);
		CombinedConfiguration comb = new CombinedConfiguration();
		comb.addConfiguration(ini);
		Configuration subset = ini.subset(MAIN_SECTION);

		if (subset.isEmpty()) {
			comb.addConfiguration(ini, MAIN_SECTION, MAIN_SECTION);
		} else {
			comb.addConfiguration(subset);
		}

		return comb;
	}

	public HierarchicalConfiguration<ImmutableNode> config() {
		return config;
	}

	private <T extends FileBasedConfiguration>
	T loadFile(Class<T> type) throws ConfigLoaderException {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<T> builder =
		    new FileBasedConfigurationBuilder<T>(type)
		    .configure(
				    params.fileBased()
						    .setFileName(filename),
				    params.basic()
						    .setThrowExceptionOnMissing(true));

		try
		{
			T config2 = builder.getConfiguration();
			logger.info("Loaded config file " + builder.getFileHandler().getPath());
			return config2;
		}
		catch(ConfigurationException cex)
		{
			throw new ConfigLoaderException(cex.getMessage(), cex);
		}
	}

	/*
	public static void main(String[] args) throws ConfigLoaderException, IOException, ConfigurationException {
		XMLPropertyListConfiguration xml = new XMLPropertyListConfiguration(cl.config());
		xml.initFileLocator(FileLocatorUtils.fileLocator().create());
		xml.write(new FileWriter("out.xml"));
	}
	*/
}
