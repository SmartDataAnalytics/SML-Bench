/**
 * 
 */
package org.aksw.mlbenchmark.learningsystems;

import org.aksw.mlbenchmark.learningproblems.LearningProblem;
import org.aksw.mlbenchmark.learningtasks.LearningTask;

/**
 * A learning system 
 * @author Lorenz Buehmann
 *
 */
public interface LearningSystem {
	
	/**
	 * @return the underlying system language
	 */
	Language getLanguage();
	
	/**
	 * Performs the specified learning problem on the given dataset.
	 * @param dataset the dataset
	 * @param learningProblem the learning problem
	 * @return the result of the learning process
	 */
	LearningResult run(LearningTask dataset, LearningProblem learningProblem);

}
