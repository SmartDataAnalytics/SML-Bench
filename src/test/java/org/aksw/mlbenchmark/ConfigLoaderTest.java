package org.aksw.mlbenchmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.aksw.mlbenchmark.config.FlatConfigHierarchicalConverter;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.PropertyListConfiguration;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.Test;

import com.google.common.io.Files;

public class ConfigLoaderTest {
	private String testResourcesDir = "src/test/resources/";
			
	@Test
	public void testFindConfig() {
		// plist
		File filePath = new File(testResourcesDir + "example1");
		ConfigLoader confLoader =
				ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(confLoader != null);
		
		// xml
		filePath = new File(testResourcesDir + "example2");
		confLoader = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(confLoader != null);
		
		// ini
		filePath = new File(testResourcesDir + "example3");
		confLoader = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(confLoader != null);
		
		// conf
		filePath = new File(testResourcesDir + "example4");
		confLoader = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(confLoader != null);
		
		// prop
		filePath = new File(testResourcesDir + "example5");
		confLoader = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(confLoader != null);
		
		// properties
		filePath = new File(testResourcesDir + "example6");
		confLoader = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertTrue(confLoader != null);
		
		// not implemented, yet
		filePath = new File(testResourcesDir + "example7");
		confLoader = ConfigLoader.findConfig(filePath.getAbsolutePath());
		assertFalse(confLoader != null);
	}

	@Test
	public void testLoad() {
		// plist
		File filePath = new File(testResourcesDir + "example1.plist");
		ConfigLoader confLoader;
		try {
			confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// xml
		filePath = new File(testResourcesDir + "example2.xml");
		try {
			confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// ini
		filePath = new File(testResourcesDir + "example3.ini");
		try {
			confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// conf
		filePath = new File(testResourcesDir + "example4.conf");
		try {
			confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// prop
		filePath = new File(testResourcesDir + "example5.prop");
		try {
			confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// properties
		filePath = new File(testResourcesDir + "example6.properties");
		try {
			confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// not implemented, yet
		filePath = new File(testResourcesDir + "example7.dummy");
		try {
			confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
			fail();
		} catch (ConfigLoaderException e) {
			// expected to throw exception
		}
		
		// file does not exist
		filePath = new File(testResourcesDir + "example8");
		try {
			confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
			fail();
		} catch (ConfigLoaderException e) {
			// expected to throw an exception
		}
	}

	@Test
	public void testWrite() throws ConfigLoaderException, IOException, ConfigurationException {
		File tmpDir = Files.createTempDir();
		File filePath = new File(testResourcesDir + "example1.plist");
		ConfigLoader confLoader = new ConfigLoader(filePath.getAbsolutePath()).load();
		HierarchicalConfiguration<ImmutableNode> conf = confLoader.config();
		
		// plist
		File outFile = new File(tmpDir, "out.plist");
		ConfigLoader.write(conf, outFile);
		
		try {
			PropertyListConfiguration readBackConf =
					new ConfigLoader(outFile.getAbsolutePath()).loadFile(
							PropertyListConfiguration.class);
			assertEquals(conf.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// xml
		outFile = new File(tmpDir, "out.xml");
		ConfigLoader.write(conf, outFile);
		
		try {
			XMLPropertyListConfiguration readBackConf =
					new ConfigLoader(outFile.getAbsolutePath()).loadFile(
							XMLPropertyListConfiguration.class);
			assertEquals(conf.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// ini
		// FIXME:
		//Tests run: 3, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.221 sec <<< FAILURE! - in org.aksw.mlbenchmark.ConfigLoaderTest
		//testWrite(org.aksw.mlbenchmark.ConfigLoaderTest)  Time elapsed: 0.136 sec  <<< ERROR!
		//java.lang.ArrayStoreException: java.lang.Byte
		//	at org.aksw.mlbenchmark.ConfigLoaderTest.testWrite(ConfigLoaderTest.java:175)

		outFile = new File(tmpDir, "out.ini");
		ConfigLoader.write(conf, outFile);

		try {
			PropertyListConfiguration readBackConf =
					FlatConfigHierarchicalConverter.convert(
							new ConfigLoader(outFile.getAbsolutePath()).loadINIFile());
			assertEquals(conf.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// conf
		// FIXME
		//Tests run: 3, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.221 sec <<< FAILURE! - in org.aksw.mlbenchmark.ConfigLoaderTest
		//testWrite(org.aksw.mlbenchmark.ConfigLoaderTest)  Time elapsed: 0.136 sec  <<< ERROR!
		//java.lang.ArrayStoreException: java.lang.Byte
		//	at org.aksw.mlbenchmark.ConfigLoaderTest.testWrite(ConfigLoaderTest.java:194)
		
		outFile = new File(tmpDir, "out.conf");
		ConfigLoader.write(conf, outFile);

		try {
			PropertyListConfiguration readBackConf =
					FlatConfigHierarchicalConverter.convert(
							new ConfigLoader(outFile.getAbsolutePath()).loadINIFile());
			assertEquals(conf.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// prop
		outFile = new File(tmpDir, "out.prop");
		ConfigLoader.write(conf, outFile);
		
		try {
			PropertyListConfiguration readBackConf =
					FlatConfigHierarchicalConverter.convert(
							new ConfigLoader(outFile.getAbsolutePath()).loadFile(
									PropertiesConfiguration.class));
			assertEquals(conf.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
		
		// properties
		outFile = new File(tmpDir, "out.properties");
		ConfigLoader.write(conf, outFile);
		
		try {
			PropertyListConfiguration readBackConf =
					FlatConfigHierarchicalConverter.convert(
							new ConfigLoader(outFile.getAbsolutePath()).loadFile(
									PropertiesConfiguration.class));
			assertEquals(conf.size(), readBackConf.size());
		} catch (ConfigLoaderException e) {
			fail();
		}
	}
	
//	@Test
//	public void testLoadFile() {
//		// TODO
//	}
}
