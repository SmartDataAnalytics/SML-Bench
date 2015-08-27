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
public class LearningResultValidatorProgol implements LearningResultValidator{

	/* (non-Javadoc)
	 * @see org.aksw.mlbenchmark.validation.LearningResultValidator#getScore(org.aksw.mlbenchmark.learningsystems.LearningResult, org.aksw.mlbenchmark.learningtasks.Dataset, org.aksw.mlbenchmark.learningproblems.LearningProblem, org.aksw.mlbenchmark.validation.measures.Measure)
	 */
	public Score getScore(LearningResult learningResult, Dataset dataset, LearningProblem learningProblem,
			Measure measure) {
		
		switch (measure) {
		case FSCORE:
			break;
		default:
			break;
		
		}
		return null;
	}

}
