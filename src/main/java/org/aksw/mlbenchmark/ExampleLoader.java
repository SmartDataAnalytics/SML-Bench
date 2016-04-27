package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.exampleloader.ExampleLoaderBase;
import org.aksw.mlbenchmark.exampleloader.OwlExampleLoader;
import org.aksw.mlbenchmark.exampleloader.PrologExampleLoader;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Load input examples.
 */
public class ExampleLoader {
	static ExampleLoaderBase forLanguage(String language) {
		switch (language.toLowerCase()) {

			case "owl": return new OwlExampleLoader();

			case "prolog": return new PrologExampleLoader();

			default: throw new NotImplementedException("No Example Loader for " + language);

		}
	}
}
