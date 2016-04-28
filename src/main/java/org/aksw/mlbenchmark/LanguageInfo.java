package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.languages.LanguageInfoBase;
import org.aksw.mlbenchmark.languages.OwlLanguageInfo;
import org.aksw.mlbenchmark.languages.PrologLanguageInfo;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Created by Simon Bin on 16-4-28.
 */
public class LanguageInfo {
	public static LanguageInfoBase forLanguage(String language) {
		switch (Constants.LANGUAGES.valueOf(language.toUpperCase())) {

			case OWL: return new OwlLanguageInfo();

			case PROLOG: return new PrologLanguageInfo();

			default: throw new NotImplementedException("No Language Info for " + language);

		}
	}
}
