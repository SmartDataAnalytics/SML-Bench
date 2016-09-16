package org.aksw.mlbenchmark.languages;

import org.aksw.mlbenchmark.Constants;

/**
 * Default values for language specific framework values
 */
public abstract class LanguageInfoBase {
	abstract public String exampleExtension();

	public String getFilename(Constants.ExType type) {
		return type.asString() + exampleExtension();
	}

	public String getBaseFilename() {
		return null;
	}
}
