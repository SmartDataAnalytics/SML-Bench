package org.aksw.mlbenchmark.languages;

import org.aksw.mlbenchmark.Constants;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Framework values specific to prolog language
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

	public String getPosFilename() {
		return "data.f";
	}

	public String getNegFilename() {
		return "data.n";
	}

	@Override
	public String getFilename(Constants.ExType type) {
		switch (type) {
			case POS: return getPosFilename();
			case NEG: return getNegFilename();
			default: throw new NotImplementedException("Missing case in switch ExType!");
		}
	}
}
