/**
 * 
 */
package org.aksw.mlbenchmark;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author Lorenz Buehmann
 *
 */
public class Benchmark {
	
	private static final String LEARNING_SYSTEMS_PATH = "learningsystems/aleph/";
	
	
	public static void main(String[] args) {
		
		String file = "";
		
		// load the properties file
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
		    new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
		    .configure(params.properties()
		        .setFileName("usergui.properties"));
		try
		{
			PropertiesConfiguration config = builder.getConfiguration();
		    List<Object> list = config.getList("learningsystems");
		}
		catch(ConfigurationException cex)
		{
		    // loading of the configuration file failed
		}
		
		
		
		File dir = new File(LEARNING_SYSTEMS_PATH);
		URL url = null;
		try {
			url = new File(LEARNING_SYSTEMS_PATH).toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		URLClassLoader loader = URLClassLoader.newInstance(new URL[]{url});

		try {
			loader.loadClass("AlephLearningSystem");
			loader.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Benchmark.class.getClassLoader().loadClass(LEARNING_SYSTEMS_PATH + "AlephLearningSystem");
		
		
	}
}
