package org.aksw.mlbenchmark.languages;

/**
 * Created by Simon Bin on 16-4-28.
 */
public abstract class LanguageInfoBase {
	abstract public String exampleExtension();

	public String getPosFilename() {
		return "pos" + exampleExtension();
	}

	public String getNegFilename() {
		return "neg" + exampleExtension();
	}

	public String getBaseFilename() {
		return null;
	}
}
