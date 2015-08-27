import org.aksw.mlbenchmark.learningproblems.LearningProblem;
import org.aksw.mlbenchmark.learningtasks.LearningTask;
import org.aksw.mlbenchmark.learningsystems.*;

/**
 * A Learning Engine for Proposing Hypotheses (Aleph) is an Inductive Logic
 * Programming (ILP) system.
 * 
 * @author Lorenz Buehmann
 *
 */
public class LearningSystemAleph implements LearningSystem{

	/* (non-Javadoc)
	 * @see org.aksw.mlbenchmark.learningsystems.LearningSystem#getLanguage()
	 */
	public Language getLanguage() {
		return Language.PROLOG;
	}

	/* (non-Javadoc)
	 * @see org.aksw.mlbenchmark.learningsystems.LearningSystem#run(org.aksw.mlbenchmark.datasets.Dataset, org.aksw.mlbenchmark.learningproblems.LearningProblem)
	 */
	public LearningResult run(LearningTask dataset, LearningProblem learningProblem) {
		return null;
	}

}
