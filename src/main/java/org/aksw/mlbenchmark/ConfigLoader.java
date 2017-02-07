package org.aksw.mlbenchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.mlbenchmark.config.CustomListDelimiterHandler;
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
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileLocatorUtils;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle configuration files in multiple formats like
 * - Property list (.plist)
 * - Property list in XML format (.xml)
 * - INI file format (.ini/.conf)
 * - Property file format (.prop/.property)
 * 
 * The main functionalities of this class include
 * - Finding a configuration file by file name without file ending
 * - Loading a configuration stored in one of the formats above
 * - Writing a given configuration to file
 * 
 * This implementation holds the actual file to load as an instance attribute
 * and cannot be re-used across several files.
 * 
 * TODO: IMHO (Patrick) all this functionality can be implemented in a static fashion
 */
public class ConfigLoader {
	static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
	/** The file path pointing to the file to load */
	private final String filename;
	/** The configuration already read in (or null if nothing read, yet) */
	private HierarchicalConfiguration<ImmutableNode> config;
	/**
	 * Switch defining whether to log on INFO (loadLogInfo = false) or
	 * DEBUG level (loadLogInfo = true)
	 */
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

	/**
	 * Same as ConfigLoader.load( ) but logging on INFO instead of DEBUG level
	 */
	public final ConfigLoader loadWithInfo() throws ConfigLoaderException {
		loadLogInfo = true;
		return load();
	}

	/**
	 * Does the actual loading of a configuration file choosing the right
	 * loading procedure for a given configuration file format (determined by
	 * file suffix).
	 */
	public final ConfigLoader load() throws ConfigLoaderException {
		if (filename.endsWith(".plist")) {
			config = loadFile(PropertyListConfiguration.class);
		
		} else if (filename.endsWith(".xml")) {
			config = loadFile(XMLPropertyListConfiguration.class);
		
		} else if (filename.endsWith(".ini") || filename.endsWith(".conf")) {
			config = FlatConfigHierarchicalConverter.convert(loadINIFile());
		
		} else if (filename.endsWith(".prop") || filename.endsWith(".properties")) {
			config = FlatConfigHierarchicalConverter.convert(
					loadFile(PropertiesConfiguration.class));
		
		} else {
			throw new ConfigLoaderException("Loading of config type not implemented yet.");
		}
		return this;
	}

	/**
	 * Writes out a given configuration to file. The format in which the given
	 * configuration will be written out is determined by the file suffix of
	 * the target file.
	 * 
	 * @param config The configuration to write to file
	 * @param output The target file
	 * @throws IOException
	 * @throws ConfigurationException
	 * @throws ConfigLoaderException
	 */
	public static void write(Configuration config, File output)
			throws IOException, ConfigurationException, ConfigLoaderException {
		
		String filename = output.getAbsolutePath();
		PropertyListConfiguration conf = FlatConfigHierarchicalConverter.convert(config);
		/* The CustomListDelimiterHandler is used here mainly because the
		 * actual default (i.e. DefaultListDelimiterHandler) caused errors
		 * when having binary data in a configuration file (although this is
		 * unlikely to occur in real SML-Bench configurations). */
		ListDelimiterHandler delim = new CustomListDelimiterHandler(',');
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
			BaseConfiguration main = new BaseConfiguration();
			for (ImmutableHierarchicalConfiguration x : conf.immutableChildConfigurationsAt("")) {
				List<String> list = IteratorUtils.toList(x.getKeys());
				
				if (list.size() == 1 && list.get(0).isEmpty()) {
					/*
					 * The assumptions made here are:
					 * - Whenever there is a top level property (i.e. without
					 *   nesting) it will end up in a dedicated immutable child
					 *   x, i.e. x contains just this one property
					 *   => list.size() == 1
					 * - Whenever there is a top level property (i.e. without
					 *   nesting)
					 *   - the property's key will be stored as x's root element
					 *     name
					 *   - x's only key will be empty (i.e. x.getKeys() = [""])
					 *   - the property's value will be stored as x's value
					 *     (which is accessed here via
					 *     x.getArray(Object.class, "") to be as generic as
					 *     possible)
					 * 
					 * All such top level properties will be added to a main
					 * 'section' or sub-configuration to comply with the INI
					 * file format which requires all properties to belong to a
					 * certain configuration section.
					 * 
					 * Example:
					 * 
					 * Input:
					 * {
					 *     foo = "bar";
					 * }
					 * 
					 * Processed as:
					 * x.getRootElementName()
					 * 	 (java.lang.String) foo
					 * 
					 * list
					 * 	 (java.util.ArrayList<E>) []
					 * 
					 * list.size()
					 * 	 (int) 1
					 * 
					 * x.getArray(Object.class, "")
					 * 	 (java.lang.Object[]) [bar]
					 */
					main.setProperty(x.getRootElementName(), x.getArray(Object.class, ""));
				
				} else {
					/* This branch is taken for all nested properties like
					 * 
					 *  nested.key1 = value1
					 *  nested.key2 = value
					 * 
					 * Here the common first part of the nested keys is stored
					 * in x's root element name ("nested"), the remainder of
					 * the keys is stored as actual keys (["key1", "key2"])
					 * with their corresponding values.
					 * 
					 * Example:
					 * 
					 * Input:
					 * {
					 *     nested =
					 *     {
					 *         key1 = value1;
					 *         key2 = value;
					 *         nested =
					 *         {
					 *             foo = bar
					 *         }
					 *     }
					 * }
					 * 
					 * Processed as:
					 * 
					 * x.getRootElementName()
					 * 	 (java.lang.String) nested
					 * 
					 * IteratorUtils.toList(x.getKeys())
					 * 	 (java.util.ArrayList<E>) [key1, key2, nested.foo]
					 * 
					 * x.getArray(Object.class, "key1")
					 * 	 (java.lang.Object[]) [value1]
					 * 
					 * x.getArray(Object.class, "key2")
					 * 	 (java.lang.Object[]) [value]
					 * 
					 * x.getArray(Object.class, "nested.foo")
					 * 	 (java.lang.Object[]) [bar]
					 */
					String sectionName = x.getRootElementName();
					Configuration sectionConf = conf.subset(sectionName);
					cc.addConfiguration(sectionConf, null, sectionName);
				}
			}
			cc.addConfiguration(main, null, "main");
			INIConfiguration iniConfiguration = new INIConfigurationWriteLists(cc);

			iniConfiguration.setListDelimiterHandler(delim);
			out = iniConfiguration;
			
		} else if (filename.endsWith(".prop") || filename.endsWith(".properties")) {
			PropertiesConfiguration propertiesConfiguration =
					new PropertiesConfigurationFromDotkeys();
			propertiesConfiguration.copy(conf);
			propertiesConfiguration.setListDelimiterHandler(delim);
			out = propertiesConfiguration;
		
		} else {
			throw new ConfigLoaderException("Writing of config type not implemented yet.");
		}

		out.write(new FileWriter(output));
	}

	/**
	 * Loads an .ini file and removes the 'main.' part of the keys, which
	 * denotes the default section. This method also normalizes the keys, i.e.
	 * whenever a key has two consecutive dots, e.g. when having read sth. like
	 * 
	 *   [nested.nested]
	 *   foo=bar
	 * 
	 * (which evaluates to the key value pair ("nested..nested.foo", "bar")),
	 * these two dots are replaced by just one dot
	 * (--> ("nested.nested.foo", "bar"))
	 */
	protected HierarchicalConfiguration<ImmutableNode> loadINIFile() throws ConfigLoaderException {
		final String MAIN_SECTION = "main";
		final int MAIN_SECTION_SUBKEY_STRING_INDEX = MAIN_SECTION.length() + 1;
		
		HierarchicalConfiguration<ImmutableNode> ini = loadFile(INIConfiguration.class);
		CombinedConfiguration comb = new CombinedConfiguration();
		
		Iterator<String> keysIt = ini.getKeys();
		String key;
		Object val;
		
		while (keysIt.hasNext()) {
			key = keysIt.next();
			val = ini.getArray(Object.class, key);
			
			if (key.startsWith(MAIN_SECTION)) {
				key = key.substring(MAIN_SECTION_SUBKEY_STRING_INDEX, key.length());
			}
			
			comb.addProperty(key.replace("..", "."), val);
		}
		return comb;
	}

	public HierarchicalConfiguration<ImmutableNode> config() {
		return config;
	}

	/**
	 * Loads a configuration file of a certain type (given as parameter type).
	 * 
	 * @param type Class of the type of configuration; one of
	 * PropertyListConfiguration, XMLPropertyListConfiguration,
	 * INIConfiguration, PropertiesConfiguration
	 * @return The configuration object containing the settings read
	 * @throws ConfigLoaderException
	 */
	protected <T extends FileBasedConfiguration> T loadFile(Class<T> type)
			throws ConfigLoaderException {
		
		Parameters params = new Parameters();
		List<BuilderParameters> config = new ArrayList<BuilderParameters>();
		config.add(params.fileBased().setFileName(filename));
		config.add(params.basic().setThrowExceptionOnMissing(true));
		
		if (INIConfiguration.class.isAssignableFrom(type)
				|| PropertiesConfiguration.class.isAssignableFrom(type)) {
		
			config.add(params.basic().setListDelimiterHandler(
					new DefaultListDelimiterHandler(',')));
		}
		
		FileBasedConfigurationBuilder<T> builder =
				new FileBasedConfigurationBuilder<>(type)
						.configure(config.toArray(new BuilderParameters[0]));

		try {
			T config2 = builder.getConfiguration();
			
			if (loadLogInfo) {
				logger.info("Loaded config file " + builder.getFileHandler().getPath());
			} else if (logger.isDebugEnabled()) {
				logger.debug("Loaded config file (internal) " + builder.getFileHandler().getPath());
			}
			
			return config2;
			
		} catch (ConfigurationException cex) {
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
