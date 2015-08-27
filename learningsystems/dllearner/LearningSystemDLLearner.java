import org.aksw.mlbenchmark.learningproblems.LearningProblem;
import org.aksw.mlbenchmark.learningtasks.LearningTask;
import org.aksw.mlbenchmark.learningsystems.*;
/**
 * @author Lorenz Buehmann
 *
 */
public class LearningSystemDLLearner implements LearningSystem{

	/* (non-Javadoc)
	 * @see org.aksw.mlbenchmark.learningsystems.LearningSystem#getLanguage()
	 */
	public Language getLanguage() {
		return Language.DL;
	}

	/* (non-Javadoc)
	 * @see org.aksw.mlbenchmark.learningsystems.LearningSystem#run(org.aksw.mlbenchmark.datasets.Dataset, org.aksw.mlbenchmark.learningproblems.LearningProblem)
	 */
	public LearningResult run(LearningTask dataset, LearningProblem learningProblem) {
		return null;
	}

}
