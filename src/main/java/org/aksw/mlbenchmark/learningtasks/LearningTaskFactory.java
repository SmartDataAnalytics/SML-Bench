/**
 * 
 */
package org.aksw.mlbenchmark.learningtasks;

import java.nio.file.Path;

/**
 * @author Lorenz Buehmann
 *
 */
public class LearningTaskFactory {
	
	private final Path baseDirectory;

	/**
	 * 
	 */
	public LearningTaskFactory(Path baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	
	public LearningTask createLearningTask(String name) {
		return null;
	}

}
