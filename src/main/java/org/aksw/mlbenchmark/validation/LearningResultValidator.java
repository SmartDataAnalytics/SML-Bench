/**
 * 
 */
package org.aksw.mlbenchmark.validation;

import org.aksw.mlbenchmark.learningproblems.LearningProblem;
import org.aksw.mlbenchmark.learningsystems.LearningResult;
import org.aksw.mlbenchmark.learningtasks.Dataset;
import org.aksw.mlbenchmark.validation.measures.Measure;

/**
 * @author Lorenz Buehmann
 *
 */
public interface LearningResultValidator {
	
	Score getScore(LearningResult learningResult, Dataset dataset, LearningProblem learningProblem, Measure measure);

}
