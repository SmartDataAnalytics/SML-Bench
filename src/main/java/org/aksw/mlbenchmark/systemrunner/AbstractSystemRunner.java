package org.aksw.mlbenchmark.systemrunner;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.aksw.mlbenchmark.BenchmarkRunner;
import org.aksw.mlbenchmark.ConfigLoader;
import org.aksw.mlbenchmark.ConfigLoaderException;
import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoader;
import org.aksw.mlbenchmark.examples.loaders.ExampleLoaderBase;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Methods common to system runners
 */
public abstract class AbstractSystemRunner implements SystemRunner {
	protected final BenchmarkRunner parent;
	protected final Configuration parentConf; // the partial scenario config from the parent
	protected final Scenario scn;

	public AbstractSystemRunner(BenchmarkRunner benchmarkRunner, Scenario scn, Configuration baseConf) {
		this.parent = benchmarkRunner;
		this.parentConf = baseConf;
		this.scn = scn;
	}

	protected void updateResultSet(Configuration results) {
		Iterator<String> keysIt = results.getKeys();
		String key;
		Object[] val;
		Configuration resultSet = getResultset();
		
		while (keysIt.hasNext()) {
			key = keysIt.next();
			val = (Object[]) results.getArray(Object.class, key);
			
			resultSet.addProperty(key, val);
		}
	}

	@Override
	public BenchmarkRunner getBenchmarkRunner() {
		return parent;
	}

	@Override
	public Configuration getParentConfiguration() {
		return parentConf;
	}

	@Override
	public Configuration getResultset() {
		return parent.getResultset();
	}
}
