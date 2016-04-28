package org.aksw.mlbenchmark.languages;

/**
 * Created by Simon Bin on 16-4-28.
 */
public class PrologLanguageInfo extends LanguageInfoBase {
	@Override
	public String exampleExtension() {
		return ".pl";
	}

	@Override
	public String getBaseFilename() {
		return "data.b";
	}

	@Override
	public String getPosFilename() {
		return "data.f";
	}

	@Override
	public String getNegFilename() {
		return "data.n";
	}
}
