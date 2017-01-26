package org.aksw.mlbenchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.mlbenchmark.config.FlatConfigHierarchicalConverter;
import org.aksw.mlbenchmark.config.INIConfigurationWriteDotkeys;
import org.aksw.mlbenchmark.config.INIConfigurationWriteLists;
import org.aksw.mlbenchmark.config.PropertiesConfigurationFromDotkeys;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ImmutableHierarchicalConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loading of config files
 */
public class ConfigLoader {
	static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
	private final String filename;
	private HierarchicalConfiguration<ImmutableNode> config;
	private boolean loadLogInfo;

	public ConfigLoader(String filename) {
		this.filename = filename;
	}

	/**
	 * Tries to find a config file with the given prefix and a set of
	 * implemented extensions
	 * 
	 * @param prefix The configuration file name without file suffix
	 * @return In case of success a ConfigLoader object with the loaded config;
	 * 		null otherwise
	 */
	public static ConfigLoader findConfig(String prefix) {
		final String[] extns = { ".plist", ".xml", ".ini", ".conf", ".prop",
				".properties" };
		
		for (String ext: extns) {
			try {
				return new ConfigLoader(prefix + ext).load();
			} catch (ConfigLoaderException e) {
				// ignore any errors
			}
		}
		
		return null;
	}

	public final ConfigLoader loadWithInfo() throws ConfigLoaderException {
		loadLogInfo = true;
		return load();
	}

	public final ConfigLoader load() throws ConfigLoaderException {
		if (filename.endsWith(".plist")) {
			config = loadFile(PropertyListConfiguration.class);
		} else if (filename.endsWith(".xml")) {
			config = loadFile(XMLPropertyListConfiguration.class);
		} else if (filename.endsWith(".ini") || filename.endsWith(".conf")) {
			config = FlatConfigHierarchicalConverter.convert(loadINIFile());
		} else if (filename.endsWith(".prop") || filename.endsWith(".properties")) {
			config = FlatConfigHierarchicalConverter.convert(loadFile(PropertiesConfiguration.class));
		} else {
			throw new ConfigLoaderException("Loading of config type not implemented yet.");
		}
		return this;
	}

	public static void write(Configuration config, File output) throws IOException, ConfigurationException, ConfigLoaderException {
		String filename = output.getAbsolutePath();
		PropertyListConfiguration conf = FlatConfigHierarchicalConverter.convert(config);
		DefaultListDelimiterHandler delim = new DefaultListDelimiterHandler(',');
		FileBasedConfiguration out;
		if (filename.endsWith(".plist")) {
			out = conf;
		} else if (filename.endsWith(".xml")) {
			XMLPropertyListConfiguration xml = new XMLPropertyListConfiguration(conf);
			xml.initFileLocator(FileLocatorUtils.fileLocator().create());
			out = xml;
		} else if (filename.endsWith(".ini") || filename.endsWith(".conf")) {
			CombinedConfiguration cc = new CombinedConfiguration();
			cc.setNodeCombiner(new MergeCombiner());
			cc.addConfiguration(conf);
			BaseConfiguration main = new BaseConfiguration();
			for (ImmutableHierarchicalConfiguration x : cc.immutableChildConfigurationsAt("")) {
				if (x.size() == 1) {
					List list = IteratorUtils.toList(x.getKeys());
					if ("".equals(list.get(0))) {
						main.setProperty(x.getRootElementName(), x.get(Object.class, ""));
						//System.err.println("adding "+x.getRootElementName() + " => "+x.get(Object.class,""));
					}
				}
			}
			cc.addConfiguration(main, null, "main");
			INIConfiguration iniConfiguration = new INIConfigurationWriteLists(cc);

			iniConfiguration.setListDelimiterHandler(delim);
			out = iniConfiguration;
		} else if (filename.endsWith(".prop") || filename.endsWith(".properties")) {
			PropertiesConfiguration propertiesConfiguration = new PropertiesConfigurationFromDotkeys();
			propertiesConfiguration.copy(conf);
			propertiesConfiguration.setListDelimiterHandler(delim);
			out = propertiesConfiguration;
		} else {
			throw new ConfigLoaderException("Writing of config type not implemented yet.");
		}
		out.write(new FileWriter(output));

	}

	protected HierarchicalConfiguration<ImmutableNode> loadINIFile() throws ConfigLoaderException {
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

	protected <T extends FileBasedConfiguration>
	T loadFile(Class<T> type) throws ConfigLoaderException {
		Parameters params = new Parameters();
		List<BuilderParameters> config = new ArrayList<BuilderParameters>();
		config.add(params.fileBased().setFileName(filename));
		config.add(params.basic().setThrowExceptionOnMissing(true));
		if (INIConfiguration.class.isAssignableFrom(type)
				|| PropertiesConfiguration.class.isAssignableFrom(type)) {
			config.add(params.basic().setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
		}
		FileBasedConfigurationBuilder<T> builder =
				new FileBasedConfigurationBuilder<>(type)
						.configure(config.toArray(new BuilderParameters[0]));

		try
		{
			T config2 = builder.getConfiguration();
			if (loadLogInfo) {
				logger.info("Loaded config file " + builder.getFileHandler().getPath());
			} else if (logger.isDebugEnabled()) {
				logger.debug("Loaded config file (internal) " + builder.getFileHandler().getPath());
			}
			return config2;
		}
		catch(ConfigurationException cex)
		{
			throw new ConfigLoaderException(cex.getMessage(), cex);
		}
	}

	public static void main(String[] args) throws ConfigLoaderException, ConfigurationException, IOException {
		logger.info("---- testing ini ----");
		HierarchicalConfiguration<ImmutableNode> conf = new ConfigLoader("test.ini").load().config();
		INIConfiguration iniConfiguration = new INIConfigurationWriteDotkeys(conf);
		iniConfiguration.write(new FileWriter("test2.ini"));
		PropertiesConfiguration propertiesConfiguration = new PropertiesConfigurationFromDotkeys();
		propertiesConfiguration.copy(conf);
		propertiesConfiguration.write(new FileWriter("test2.prop"));
		//new Hierarchicalconfigurationcon
		//Testing testing = new Testing();
		//testing.conf(conf);

		new PropertyListConfiguration(conf).write(new FileWriter("test-from-ini.plist"));
		logger.info("---- testing plist ----");
		HierarchicalConfiguration<ImmutableNode> config1 = new ConfigLoader("test.plist").load().config();
		//testing.conf(config1);
		PropertyListConfiguration plc = new PropertyListConfiguration(config1);
		plc.write(new FileWriter("test-copy.plist"));
	}

}
