package org.aksw.mlbenchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.aksw.mlbenchmark.config.FlatConfigHierarchicalConverter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class ConfigLoaderTest {
	private String testResourcesDir = "src/test/resources/";
			
	@Test
	public void testFindConfig() {
		// plist
		File filePath = new File(testResourcesDir + "example1");
		Configuration config =
				ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(config != null);
		
		// xml
		filePath = new File(testResourcesDir + "example2");
		config = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(config != null);
		
		// ini
		filePath = new File(testResourcesDir + "example3");
		config = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(config != null);
		
		// conf
		filePath = new File(testResourcesDir + "example4");
		config = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(config != null);
		
		// prop
		filePath = new File(testResourcesDir + "example5");
		config = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(config != null);
		
		// properties
		filePath = new File(testResourcesDir + "example6");
		config = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(config != null);
		
		// not implemented, yet
		filePath = new File(testResourcesDir + "example7");
		config = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertFalse(config != null);
	}

	@Test
	public void testLoad() {
		// plist
		File filePath = new File(testResourcesDir + "example1.plist");
		Configuration config;
		try {
			config = ConfigLoader.load(filePath.getAbsolutePath());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// xml
		filePath = new File(testResourcesDir + "example2.xml");
		try {
			config = ConfigLoader.load(filePath.getAbsolutePath());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// ini
		filePath = new File(testResourcesDir + "example3.ini");
		try {
			config = ConfigLoader.load(filePath.getAbsolutePath());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// conf
		filePath = new File(testResourcesDir + "example4.conf");
		try {
			config = ConfigLoader.load(filePath.getAbsolutePath());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// prop
		filePath = new File(testResourcesDir + "example5.prop");
		try {
			config = ConfigLoader.load(filePath.getAbsolutePath());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// properties
		filePath = new File(testResourcesDir + "example6.properties");
		try {
			config = ConfigLoader.load(filePath.getAbsolutePath());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// not implemented, yet
		filePath = new File(testResourcesDir + "example7.dummy");
		try {
			config = ConfigLoader.load(filePath.getAbsolutePath());
			fail();
		} catch (ConfigLoaderException e) {
			// expected to throw exception
		}
		
		// file does not exist
		filePath = new File(testResourcesDir + "example8");
		try {
			config = ConfigLoader.load(filePath.getAbsolutePath());
			fail();
		} catch (ConfigLoaderException e) {
			// expected to throw an exception
		}
	}

	@Test
	public void testWrite() throws ConfigLoaderException, IOException, ConfigurationException {
		File tmpDir = Files.createTempDir();
		File filePath = new File(testResourcesDir + "example1.plist");
		Configuration config = ConfigLoader.load(filePath.getAbsolutePath());
		
		// plist
		File outFile = new File(tmpDir, "out.plist");
		ConfigLoader.write(config, outFile);
		
		try {
			PropertyListConfiguration readBackConf =
					ConfigLoader.loadFile(
							PropertyListConfiguration.class,
							outFile.getAbsolutePath());
			
			assertEquals(config.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// xml
		outFile = new File(tmpDir, "out.xml");
		ConfigLoader.write(config, outFile);
		
		try {
			XMLPropertyListConfiguration readBackConf =
					ConfigLoader.loadFile(
							XMLPropertyListConfiguration.class,
							outFile.getAbsolutePath());
			
			assertEquals(config.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// ini
		outFile = new File(tmpDir, "out.ini");
		ConfigLoader.write(config, outFile);

		try {
			PropertyListConfiguration readBackConf =
					FlatConfigHierarchicalConverter.convert(
							ConfigLoader.loadINIFile(outFile.getAbsolutePath()));
			
			assertEquals(config.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// conf
		outFile = new File(tmpDir, "out.conf");
		ConfigLoader.write(config, outFile);

		try {
			PropertyListConfiguration readBackConf =
					FlatConfigHierarchicalConverter.convert(
							ConfigLoader.loadINIFile(outFile.getAbsolutePath()));
			
			assertEquals(config.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// prop
		outFile = new File(tmpDir, "out.prop");
		ConfigLoader.write(config, outFile);
		
		try {
			PropertyListConfiguration readBackConf =
					FlatConfigHierarchicalConverter.convert(
							ConfigLoader.loadFile(
									PropertiesConfiguration.class,
									outFile.getAbsolutePath()));
			
			assertEquals(config.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// properties
		outFile = new File(tmpDir, "out.properties");
		ConfigLoader.write(config, outFile);
		
		try {
			PropertyListConfiguration readBackConf =
					FlatConfigHierarchicalConverter.convert(
							ConfigLoader.loadFile(
									PropertiesConfiguration.class,
									outFile.getAbsolutePath()));
			
			assertEquals(config.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
	}
	
	@Test
	public void testLoadFile() throws ConfigLoaderException, ParseException {
		Configuration conf;
		
		// plist
		/* The loaded config:
		 * {
		 *     foo = "bar";
		 * 
		 *     array = ( value1, value2, value3 );
		 * 
		 *     data = <4f3e0145ab>;
		 * 
		 *     date = <*D2007-05-05 20:05:00 +0100>;
		 * 
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
		 */
		File filePath = new File(testResourcesDir + "example1.plist");
		conf = ConfigLoader.loadFile(
				PropertyListConfiguration.class, filePath.getAbsolutePath());
		
		int expectedNumEntries = 7;
		assertEquals(expectedNumEntries, conf.size());
		
		Set<String> expectedKeys = Sets.newHashSet("foo", "array", "data",
				"date", "nested.key1", "nested.key2", "nested.nested.foo");
		Iterator<String> keyIt = conf.getKeys();
		String key;
		while (keyIt.hasNext()) {
			key = keyIt.next();
			assertTrue(expectedKeys.remove(key));
		}
		assertEquals(0, expectedKeys.size());
		
		assertEquals("bar", conf.getString("foo"));
		
		String[] arrayVals = conf.getStringArray("array");
		assertEquals(3, arrayVals.length);
		assertEquals("value1", arrayVals[0]);
		assertEquals("value2", arrayVals[1]);
		assertEquals("value3", arrayVals[2]);
		
		// TODO: be more precise here
		/* (checking the actual value here would fail; however it's unlikely
		 * one would want to add binary data to a configuration) */
		assertTrue(conf.get(Object.class, "data") instanceof Byte);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X");
		Date date = df.parse("2007-05-05 20:05:00 +0100");
		assertEquals(date, conf.get(Date.class, "date"));
		
		assertEquals("value1", conf.getString("nested.key1"));
		assertEquals("value", conf.getString("nested.key2"));
		assertEquals("bar", conf.getString("nested.nested.foo"));
		
		// xml
		/*
		 * <?xml version="1.0"?>
		 * <!DOCTYPE plist SYSTEM "file://localhost/System/Library/DTDs/PropertyList.dtd">
		 * <plist version="1.0">
		 *     <dict>
		 *         <key>string</key>
		 *         <string>value1</string>
		 * 
		 *         <key>integer</key>
		 *         <integer>12345</integer>
		 * 
		 *         <key>real</key>
		 *         <real>-123.45E-1</real>
		 * 
		 *         <key>boolean</key>
		 *         <true/>
		 * 
		 *         <key>date</key>
		 *         <date>2005-01-01T12:00:00Z</date>
		 * 
		 *         <key>data</key>
		 *         <data>RHJhY28gRG9ybWllbnMgTnVucXVhbSBUaXRpbGxhbmR1cw==</data>
		 * 
		 *         <key>array</key>
		 *         <array>
		 *             <string>value1</string>
		 *             <string>value2</string>
		 *             <string>value3</string>
		 *         </array>
		 * 
		 *         <key>dictionnary</key>
		 *         <dict>
		 *             <key>key1</key>
		 *             <string>value1</string>
		 *             <key>key2</key>
		 *             <string>value2</string>
		 *             <key>key3</key>
		 *             <string>value3</string>
		 *         </dict>
		 * 
		 *         <key>nested</key>
		 *         <dict>
		 *             <key>node1</key>
		 *             <dict>
		 *                 <key>node2</key>
		 *                 <dict>
		 *                     <key>node3</key>
		 *                     <string>value</string>
		 *                 </dict>
		 *             </dict>
		 *         </dict>
		 *     </dict>
		 * </plist>
		 */
		filePath = new File(testResourcesDir + "example2.xml");
		conf = ConfigLoader.loadFile(
				XMLPropertyListConfiguration.class, filePath.getAbsolutePath());
		
		expectedNumEntries = 11;
		assertEquals(expectedNumEntries, conf.size());
		
		expectedKeys = Sets.newHashSet("string", "integer", "real", "boolean",
				"date", "data", "array", "dictionnary.key1", "dictionnary.key2",
				"dictionnary.key3", "nested.node1.node2.node3");
		keyIt = conf.getKeys();
		while (keyIt.hasNext()) {
			key = keyIt.next();
			assertTrue(expectedKeys.remove(key));
		}
		assertEquals(0, expectedKeys.size());
		
		assertEquals("value1", conf.getString("string"));
		assertEquals(12345, conf.getInt("integer"));
		assertEquals(-12.345, conf.getFloat("real"), 0.00001);
		assertTrue(conf.getBoolean("boolean"));
		
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X");
		date = df.parse("2005-01-01 12:00:00 +0000");
		assertEquals(date, conf.get(Date.class, "date"));
		
		assertTrue(conf.get(Object.class, "data") instanceof Byte);
		
		String[] vals = conf.getStringArray("array");
		assertEquals(3, vals.length);
		assertEquals("value1", vals[0]);
		assertEquals("value2", vals[1]);
		assertEquals("value3", vals[2]);
		
		assertEquals("value1", conf.getString("dictionnary.key1"));
		assertEquals("value2", conf.getString("dictionnary.key2"));
		assertEquals("value3", conf.getString("dictionnary.key3"));
		
		assertEquals("value",  conf.getString("nested.node1.node2.node3"));
		
		// ini
		/*
		 * [main]
		 * foo=bar
		 * array=value1,value2,value3
		 * data=4f3e0145ab
		 * date=2007-05-05 20:05:00 +0100
		 * 
		 * [nested]
		 * key1=value1
		 * key2=value2
		 * 
		 * [nested.nested]
		 * foo=bar
		 */
		
		filePath = new File(testResourcesDir + "example3.ini");
		conf = ConfigLoader.loadFile(
				INIConfiguration.class, filePath.getAbsolutePath());
		
		expectedNumEntries = 7;
		
		assertEquals("bar", conf.getString("main.foo"));
		
		vals = conf.getStringArray("main.array");
		assertEquals(3, vals.length);
		assertEquals("value1", vals[0]);
		assertEquals("value2", vals[1]);
		assertEquals("value3", vals[2]);
		
		assertEquals("4f3e0145ab", conf.getString("main.data"));
		
		// seems the time zone part in the example ini file above will be
		// ignored by the default date parser
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		date = df.parse("2007-05-05 20:05:00");
		assertEquals(date, conf.get(Date.class, "main.date"));
		
		assertEquals("value1", conf.getString("nested.key1"));
		assertEquals("value2", conf.getString("nested.key2"));
		// !!! two dots as separator between 'nested' and 'nested'
		assertEquals("bar", conf.getString("nested..nested.foo"));
		
		// conf
		/*
		 * [main]
		 * foo=bar
		 * array=value1,value2,value3
		 * data=4f3e0145ab
		 * date=2007-05-05 20:05:00 +0100
		 * 
		 * [nested]
		 * key1=value1
		 * key2=value2
		 * 
		 * [nested.nested]
		 * foo=bar
		 */
		filePath = new File(testResourcesDir + "example4.conf");
		conf = ConfigLoader.loadFile(
				INIConfiguration.class, filePath.getAbsolutePath());
		
		expectedNumEntries = 7;
		
		assertEquals("bar", conf.getString("main.foo"));
		
		vals = conf.getStringArray("main.array");
		assertEquals(3, vals.length);
		assertEquals("value1", vals[0]);
		assertEquals("value2", vals[1]);
		assertEquals("value3", vals[2]);
		
		assertEquals("4f3e0145ab", conf.getString("main.data"));
		
		date = df.parse("2007-05-05 20:05:00");
		assertEquals(date, conf.get(Date.class, "main.date"));
		
		assertEquals("value1", conf.getString("nested.key1"));
		assertEquals("value2", conf.getString("nested.key2"));
		// !!! two dots as separator between 'nested' and 'nested'
		assertEquals("bar", conf.getString("nested..nested.foo"));
		
		// prop
		/*
		 * # lines starting with # are comments
		 * 
		 * # This is the simplest property
		 * key = value
		 * 
		 * # A long property may be separated on multiple lines
		 * longvalue = aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
		 *             aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
		 * 
		 * # This is a property with many tokens
		 * tokens_on_a_line = first token, second token
		 * 
		 * # This sequence generates exactly the same result
		 * tokens_on_multiple_lines = first token
		 * tokens_on_multiple_lines = second token
		 * 
		 * # commas may be escaped in tokens
		 * commas.escaped = Hi\, what'up?
		 * 
		 * # properties can reference other properties
		 * base.prop = /base
		 * first.prop = ${base.prop}/first
		 * second.prop = ${first.prop}/second
		 */
		filePath = new File(testResourcesDir + "example5.prop");
		conf = ConfigLoader.loadFile(
				PropertiesConfiguration.class, filePath.getAbsolutePath());
		
		expectedNumEntries = 8;
		
		assertEquals("value", conf.getString("key"));
		assertEquals(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				conf.getString("longvalue"));
		
		vals = conf.getStringArray("tokens_on_a_line");
		assertEquals(2, vals.length);
		assertEquals("first token", vals[0]);
		assertEquals("second token", vals[1]);
		
		vals = conf.getStringArray("tokens_on_multiple_lines");
		assertEquals(2, vals.length);
		assertEquals("first token", vals[0]);
		assertEquals("second token", vals[1]);
		
		assertEquals("Hi, what'up?", conf.getString("commas.escaped"));
		assertEquals("/base", conf.getString("base.prop"));
		assertEquals("/base/first", conf.getString("first.prop"));
		assertEquals("/base/first/second", conf.getString("second.prop"));
		
		// properties
		/*
		 * # lines starting with # are comments
		 * 
		 * # This is the simplest property
		 * key = value
		 * 
		 * # A long property may be separated on multiple lines
		 * longvalue = aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
		 *             aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
		 * 
		 * # This is a property with many tokens
		 * tokens_on_a_line = first token, second token
		 * 
		 * # This sequence generates exactly the same result
		 * tokens_on_multiple_lines = first token
		 * tokens_on_multiple_lines = second token
		 * 
		 * # commas may be escaped in tokens
		 * commas.escaped = Hi\, what'up?
		 * 
		 * # properties can reference other properties
		 * base.prop = /base
		 * first.prop = ${base.prop}/first
		 * second.prop = ${first.prop}/second
		 */
		filePath = new File(testResourcesDir + "example6.properties");
		conf = ConfigLoader.loadFile(
				PropertiesConfiguration.class, filePath.getAbsolutePath());
		
		expectedNumEntries = 8;
		
		assertEquals("value", conf.getString("key"));
		assertEquals(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				conf.getString("longvalue"));
		
		vals = conf.getStringArray("tokens_on_a_line");
		assertEquals(2, vals.length);
		assertEquals("first token", vals[0]);
		assertEquals("second token", vals[1]);
		
		vals = conf.getStringArray("tokens_on_multiple_lines");
		assertEquals(2, vals.length);
		assertEquals("first token", vals[0]);
		assertEquals("second token", vals[1]);
		
		assertEquals("Hi, what'up?", conf.getString("commas.escaped"));
		assertEquals("/base", conf.getString("base.prop"));
		assertEquals("/base/first", conf.getString("first.prop"));
		assertEquals("/base/first/second", conf.getString("second.prop"));
	}
	
	@Test
	public void testLoadINIFile() throws ConfigLoaderException, ParseException {
		/*
		 * example3.ini:
		 * 
		 * [main]
		 * foo=bar
		 * array=value1,value2,value3
		 * data=4f3e0145ab
		 * date=2007-05-05 20:05:00 +0100
		 * 
		 * [nested]
		 * key1=value1
		 * key2=value2
		 * 
		 * [nested.nested]
		 * foo=bar
		 */
		File filePath = new File(testResourcesDir + "example3.ini");
		HierarchicalConfiguration<ImmutableNode> conf =
				ConfigLoader.loadINIFile(filePath.getAbsolutePath());
		
		assertEquals(7, conf.size());
		assertEquals("bar", conf.getString("foo"));
		
		String[] vals = conf.getStringArray("array");
		assertEquals(3, vals.length);
		assertEquals("value1", vals[0]);
		assertEquals("value2", vals[1]);
		assertEquals("value3", vals[2]);
		
		assertEquals("4f3e0145ab", conf.getString("data"));
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = df.parse("2007-05-05 20:05:00");
		assertEquals(date, conf.get(Date.class, "date"));
		
		assertEquals("value1", conf.getString("nested.key1"));
		assertEquals("value2", conf.getString("nested.key2"));
		assertEquals("bar", conf.getString("nested.nested.foo"));
		
	}
}
