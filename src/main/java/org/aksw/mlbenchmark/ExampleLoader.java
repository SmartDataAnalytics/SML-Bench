package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.exampleloader.ExampleLoaderBase;
import org.aksw.mlbenchmark.exampleloader.OwlExampleLoader;
import org.aksw.mlbenchmark.exampleloader.PrologExampleLoader;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Examples loader factory
 */
public class ExampleLoader {
	public static ExampleLoaderBase forLanguage(Constants.LANGUAGES language) {
		switch (language) {

			case OWL: return new OwlExampleLoader();

			case PROLOG: return new PrologExampleLoader();

			default: throw new NotImplementedException("No Example Loader for " + language);

		}
	}
}
