package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.languages.LanguageInfoBase;

/**
 * Created by Simon Bin on 16-4-26.
 */
public class Constants {
	/** the folder name containing the learning systems */
	public static final String LEARNINGSYSTEMS = "learningsystems";
	/** the folder name containing the learning tasks */
	public static final String LEARNINGTASKS = "learningtasks";
	/** the folder name containing the learning problems */
	public static final String LEARNINGPROBLEMS = "lp";

	/** the config file name for learning system specific config */
	public static final String LEARNINGSYSTEMCONFIG = "system";

	/** the default maximum execution time in seconds for the training step */
	public static final long DefaultMaxExecutionTime = 35; // seconds

	/** Supported knowledge representation languages */
	public enum LANGUAGES {
		OWL, PROLOG, language, lang;

		public String asString() {
			return name().toLowerCase();
		}
		public LanguageInfoBase getInfo() {
			return LanguageInfo.forLanguage(this);
		}
	}

	/** example types for example based learning */
	public enum ExType {
		POS, NEG;

		public String asString() {
			return name().toLowerCase();
		}
	}

	public enum State { RUNNING, OK, TIMEOUT, FAILURE, ERROR }
}
