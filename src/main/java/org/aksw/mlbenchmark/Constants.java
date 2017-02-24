package org.aksw.mlbenchmark;

import org.aksw.mlbenchmark.container.LanguageInfo;
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
	public static final String LEARNING_SYSTEM_OUTPUT_FILE_NAME = "lsoutput.log";

	/** the config file name for learning system specific config */
	public static final String LEARNINGSYSTEMCONFIG = "system";
	
	/**
	 * The string separating a learning system's name from an optional
	 * learning system identifier in case multiple instances of one learning
	 * system are in use.
	 */
	public static final String LEARNINGSYSTEM_ID_SEPARATOR = "-";
	
	// config keys
	public static final String WORKDIR_KEY = "filename.workdir";
	public static final String POS_EXAMPLE_FILE_KEY = "filename.pos";
	public static final String NEG_EXAMPLE_FILE_KEY = "filename.neg";
	public static final String MAX_EXECUTION_TIME_KEY = "framework.maxExecutionTime";
	public static final String LS_MAX_EXECUTION_TIME_KEY = "maxExecutionTime";
	public static final String MEASURES_KEY = "framework.measures";
	public static final String SEED_KEY = "framework.currentSeed";
	public static final String OUTPUT_FILE_KEY = "filename.output";
	public static final String INPUT_FILE_KEY = "filename.input";
	public static final String LEARNING_TASK_KEY = "learningtask";
	public static final String LEARNING_PROBLEM_KEY = "learningproblem";
	public static final String LEARNING_SYSTEM_KEY = "learningsystem";
	public static final String STEP_KEY = "step";
	public static final String STEP_TRAIN = "train";
	public static final String STEP_VALIDATE = "validate";
	public static final String LEARNING_SYSTEMS_KEY = "learningsystems";
	public static final String LS_SPECIFIC_SETTINGS_KEY = "settings";
	public static final String TRUE_POSITIVES_KEY = "tp";
	public static final String TRUE_NEGATIVES_KEY = "tn";
	public static final String FALSE_POSITIVES_KEY = "fp";
	public static final String FALSE_NEGATIVES_KEY = "fn";
	public static final String TRAIN_STATUS_KEY_PART = "train_status";
	public static final String ABSOLUTE_RESULT_KEY_PART = "absolute";
	public static final String TRAINING_RES_RAW_KEY_PART = "trainingRaw";

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

	public enum State {
		RUNNING, OK, TIMEOUT, ERROR, NO_RESULT;
		
		public static State getStateFor(String stateString) {
			if (stateString == null) {
				throw new RuntimeException("State string was null");
			}
			String s = stateString.toUpperCase();
			if (s.equals(State.ERROR.toString()))
				return State.ERROR;
			else if (s.equals(State.NO_RESULT.toString()))
				return State.NO_RESULT;
			else if (s.equals(State.RUNNING.toString()))
				return State.RUNNING;
			else if (s.equals(State.TIMEOUT.toString()))
				return State.TIMEOUT;
			else
				return State.OK;
		}
	}
	
	public enum Stage { TRAINING, VALIDATION }
}
