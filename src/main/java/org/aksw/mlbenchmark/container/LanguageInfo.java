package org.aksw.mlbenchmark.container;

import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.languages.LanguageInfoBase;
import org.aksw.mlbenchmark.languages.OwlLanguageInfo;
import org.aksw.mlbenchmark.languages.PrologLanguageInfo;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Factory for LanguageInfoBase implementations
 */
public class LanguageInfo {
	public static LanguageInfoBase forLanguage(Constants.LANGUAGES language) {
		switch (language) {

			case OWL: return new OwlLanguageInfo();

			case PROLOG: return new PrologLanguageInfo();

			default: throw new NotImplementedException("No Language Info for " + language);

		}
	}
}
