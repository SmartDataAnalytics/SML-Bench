/**
 * 
 */
package org.aksw.mlbenchmark.learningtasks;



import java.util.List;

import org.aksw.mlbenchmark.learningproblems.LearningProblem;
import org.aksw.mlbenchmark.learningsystems.Language;

/**
 * @author Lorenz Buehmann
 *
 */
public interface LearningTask {
	
	int getID();
	
	String getName();
	
	Dataset getDataset(Language language);
	
	/**
	 * @param language
	 * @return all available learning problems
	 */
	List<LearningProblem> getLearningProblems(Language language);
	
	LearningProblem getLearningProblem(String id);

}
