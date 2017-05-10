package org.aksw.mlbenchmark.config;

import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.ConfigLoaderException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Convert property files to hierarchical config layout (like HierarchicalConfigurationConverter)
 */
public class FlatConfigHierarchicalConverter {
	final static Logger logger = LoggerFactory.getLogger(FlatConfigHierarchicalConverter.class);
	static void process(Configuration in, HierarchicalConfiguration<ImmutableNode> out) {
		ExpressionEngine ee = out.getExpressionEngine();
		out.setExpressionEngine(DefaultExpressionEngine.INSTANCE);
		Iterator<String> keys = in.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			String qkey = key.replaceAll(Pattern.quote(".."), ".");
			int idx = qkey.lastIndexOf(".");
			logger.trace("converting: " + key + ": "+ idx);
			String rootKey = idx >= 0 ? qkey.substring(0, idx) : "";
			String lastPart = qkey.substring(idx + 1);
			logger.trace("--> [" + rootKey + "|" + lastPart +"]");
			Object p = out.getProperty(qkey);
			Object ip = in.getProperty(key);
			out.clearProperty(qkey);
			ArrayList list = new ArrayList();
			if (ip != null) {
				if (ip instanceof List)
					list.addAll((List) ip);
				else
					list.add(ip);
			}
			if (p != null) {
				if (p instanceof List)
					list.addAll((List) p);
				else
					list.add(p);
			}
			out.addNodes(rootKey, Arrays.asList(new ImmutableNode.Builder()
					.name(lastPart)
					.value(list.size()>1?list:list.get(0))
					.create()));
		}
		out.setExpressionEngine(ee);
	}

	public static PropertyListConfiguration convert(Configuration config) {
		PropertiesConfigurationFromDotkeys temp = new PropertiesConfigurationFromDotkeys();
		temp.copy(config);
		PropertyListConfiguration ret = new PropertyListConfiguration();
		process(temp, ret);
		return ret;
	}

	public static String convertAsString(Configuration config) {
		PropertyListConfiguration convert = convert(config);
		StringWriter out = new StringWriter();
		try {
			convert.write(out);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return out.getBuffer().toString();
	}

	public static void main(String[] args) throws ConfigLoaderException, IOException, ConfigurationException {
		HierarchicalConfiguration<ImmutableNode> config = ConfigLoader.load("test.ini");
		PropertyListConfiguration c1 = new PropertyListConfiguration(config);
		PropertyListConfiguration c2 = new PropertyListConfiguration();
		process(config, c2);
		PropertyListConfiguration c3 = convert(config);
		c1.write(new FileWriter("c1.plist"));
		c2.write(new FileWriter("c2.plist"));
		c3.write(new FileWriter("c3.plist"));
		//new INIConfigurationWriteDotkeys(c3).write(new FileWriter("c3.ini"));
		INIConfigurationWriteLists listIni = new INIConfigurationWriteLists(c3);
		listIni.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		listIni.write(new FileWriter("c3a.ini"));
		new INIConfiguration(c3).write(new FileWriter("c3b.ini"));
		PropertiesConfiguration listProps = new PropertiesConfiguration();
		listProps.copy(c3);
		listProps.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
		listProps.write(new FileWriter("c3a.prop"));
	}
}
