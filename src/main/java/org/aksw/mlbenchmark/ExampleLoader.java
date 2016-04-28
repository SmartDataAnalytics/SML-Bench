package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.exampleloader.ExampleLoaderBase;
import org.aksw.mlbenchmark.exampleloader.OwlExampleLoader;
import org.aksw.mlbenchmark.exampleloader.PrologExampleLoader;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Load input examples.
 */
public class ExampleLoader {
	public static ExampleLoaderBase forLanguage(String language) {
		switch (Constants.LANGUAGES.valueOf(language.toUpperCase())) {

			case OWL: return new OwlExampleLoader();

			case PROLOG: return new PrologExampleLoader();

			default: throw new NotImplementedException("No Example Loader for " + language);

		}
	}
}
