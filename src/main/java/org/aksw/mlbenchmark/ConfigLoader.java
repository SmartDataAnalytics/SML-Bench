package org.aksw.mlbenchmark;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			loadFile(PropertyListConfiguration.class);
		} else if (filename.endsWith(".xml")) {
			loadFile(XMLPropertyListConfiguration.class);
		/* } else if (filename.endsWith(".ini")) {
			loadFile(INIConfiguration.class); */
		} else {
			throw new ConfigLoaderException("Loading of config type not implemented yet.");
		}
		return this;
	}

	public HierarchicalConfiguration<ImmutableNode> getConfig() {
		return config;
	}

	private <T extends HierarchicalConfiguration<ImmutableNode> & FileBasedConfiguration>
	void loadFile(Class<T> type) throws ConfigLoaderException {
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
			config = builder.getConfiguration();
			logger.info("Loaded config file " + builder.getFileHandler().getPath());
		}
		catch(ConfigurationException cex)
		{
			throw new ConfigLoaderException(cex.getMessage(), cex);
		}
	}

	/*
	public static void main(String[] args) throws ConfigLoaderException, IOException, ConfigurationException {
		XMLPropertyListConfiguration xml = new XMLPropertyListConfiguration(cl.getConfig());
		xml.initFileLocator(FileLocatorUtils.fileLocator().create());
		xml.write(new FileWriter("out.xml"));
	}
	*/
}
