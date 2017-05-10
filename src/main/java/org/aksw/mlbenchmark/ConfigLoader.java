package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.config.*;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.configuration2.*;
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

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 */
public class ConfigLoader {
	static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

	/**
	 * Tries to find a config file with the given prefix and a set of
	 * implemented extensions
	 * 
	 * @param prefix The configuration file name without file suffix
	 * @return In case of success a ConfigLoader object with the loaded config;
	 * 		null otherwise
	 */
	public static HierarchicalConfiguration<ImmutableNode> findConfig(String prefix) {
		final String[] extns = { ".plist", ".xml", ".ini", ".conf", ".prop",
				".properties" };
		
		for (String ext: extns) {
			try {
				return ConfigLoader.load(prefix + ext);
			} catch (ConfigLoaderException e) {
				// ignore any errors
			}
		}
		
		return null;
	}

	/**
	 * Does the actual loading of a configuration file choosing the right
	 * loading procedure for a given configuration file format (determined by
	 * file suffix).
	 */
	public static final HierarchicalConfiguration<ImmutableNode> load(
			String filePath) throws ConfigLoaderException {
		
		HierarchicalConfiguration<ImmutableNode> config;
		
		if (filePath.endsWith(".plist")) {
			config = loadFile(PropertyListConfiguration.class, filePath);
		
		} else if (filePath.endsWith(".xml")) {
			config = loadFile(XMLPropertyListConfiguration.class, filePath);
		
		} else if (filePath.endsWith(".ini") || filePath.endsWith(".conf")) {
			config = FlatConfigHierarchicalConverter.convert(loadINIFile(filePath));
		
		} else if (filePath.endsWith(".prop") || filePath.endsWith(".properties")) {
			config = FlatConfigHierarchicalConverter.convert(
					loadFile(PropertiesConfiguration.class, filePath));
		
		} else {
			throw new ConfigLoaderException("Loading of config type not implemented yet.");
		}
		return config;
	}

	private static void write(Configuration config, String filename, Writer writer) throws ConfigLoaderException, IOException, ConfigurationException {
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

		out.write(writer);
	}

	/**
	 * Writes out a given configuration as string. The format in which the given
	 * configuration will be written out is determined by the file suffix of
	 * the target file.
	 *
	 * @param config The configuration to write to file
	 * @param filename The hypothetic filename decides the output format
	 * @throws ConfigurationException
	 * @throws ConfigLoaderException
	 */
	public static String writeAsString(Configuration config, String filename) throws ConfigLoaderException, ConfigurationException {
		StringWriter stringWriter = new StringWriter();
		try {
			write(config, filename, stringWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringWriter.getBuffer().toString();
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
		write(config, filename, new FileWriter(output));
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
	protected static HierarchicalConfiguration<ImmutableNode> loadINIFile(
			String filePath) throws ConfigLoaderException {
		
		final String MAIN_SECTION = "main";
		final int MAIN_SECTION_SUBKEY_STRING_INDEX = MAIN_SECTION.length() + 1;
		
		HierarchicalConfiguration<ImmutableNode> ini =
				loadFile(INIConfiguration.class, filePath);
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

	/**
	 * Loads a configuration file of a certain type (given as parameter type).
	 * 
	 * @param type Class of the type of configuration; one of
	 * PropertyListConfiguration, XMLPropertyListConfiguration,
	 * INIConfiguration, PropertiesConfiguration
	 * @return The configuration object containing the settings read
	 * @throws ConfigLoaderException
	 */
	protected static <T extends FileBasedConfiguration> T loadFile(
			Class<T> type, String filePath) throws ConfigLoaderException {
		
		Parameters params = new Parameters();
		List<BuilderParameters> config = new ArrayList<BuilderParameters>();
		config.add(params.fileBased().setFileName(filePath));
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
			logger.debug("Loaded config file " + builder.getFileHandler().getPath());
			
			return config2;
			
		} catch (ConfigurationException cex) {
			throw new ConfigLoaderException(cex.getMessage(), cex);
		}
	}

	public static void main(String[] args) throws ConfigLoaderException,
			ConfigurationException, IOException {
		
		logger.info("---- testing ini ----");
		HierarchicalConfiguration<ImmutableNode> conf = ConfigLoader.load("test.ini");
		INIConfiguration iniConfiguration = new INIConfigurationWriteDotkeys(conf);
		iniConfiguration.write(new FileWriter("test2.ini"));
		PropertiesConfiguration propertiesConfiguration =
				new PropertiesConfigurationFromDotkeys();
		propertiesConfiguration.copy(conf);
		propertiesConfiguration.write(new FileWriter("test2.prop"));
		//new Hierarchicalconfigurationcon
		//Testing testing = new Testing();
		//testing.conf(conf);

		new PropertyListConfiguration(conf).write(new FileWriter("test-from-ini.plist"));
		logger.info("---- testing plist ----");
		HierarchicalConfiguration<ImmutableNode> config1 = ConfigLoader.load("test.plist");
		//testing.conf(config1);
		PropertyListConfiguration plc = new PropertyListConfiguration(config1);
		plc.write(new FileWriter("test-copy.plist"));
	}

}
