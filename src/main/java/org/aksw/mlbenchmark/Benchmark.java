/**
 * 
 */
package org.aksw.mlbenchmark;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * @author Lorenz Buehmann
 *
 */
public class Benchmark {
	
	private static final String LEARNING_SYSTEMS_PATH = "learningsystems/aleph/";
	
	
	public static void main(String[] args) throws Exception {
		
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
		URL url = new File(LEARNING_SYSTEMS_PATH).toURI().toURL();
		URLClassLoader loader = URLClassLoader.newInstance(new URL[]{url});
		
		loader.loadClass("AlephLearningSystem");
		loader.close();
//		Benchmark.class.getClassLoader().loadClass(LEARNING_SYSTEMS_PATH + "AlephLearningSystem");
		
		
	}
}
