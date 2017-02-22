package org.aksw.mlbenchmark.util;

import java.io.File;

import org.aksw.mlbenchmark.Constants;
import org.aksw.mlbenchmark.LearningSystemInfo;
import org.aksw.mlbenchmark.Scenario;

/**
 * Depending on the execution context (cross validation/no cross validation,
 * current fold, used knowledge representation language, currently solving
 * scenario, currently executing learning system, ...) essential files
 * like
 * 
 * - positives examples file
 * - negative examples file
 * - learning task directory
 * - learning problem directory
 * - learning system directory
 * - working/output directory
 * 
 * may reside in different places. This class provides the helper functionality
 * providing pointers to the right locations. A FileFinder object is intended
 * to be initialized appropriately and passed down, e.g. to individual
 * execution steps.
 */
public class FileFinder implements Cloneable {
	private File projectRoot;
	private LearningSystemInfo lsi;
	private Scenario scenario;
	private File workingDir;
	
	public FileFinder(File projectRoot) {
		this.projectRoot = projectRoot;
	}
	
	public FileFinder(String projectRoot) {
		this(new File(projectRoot));
	}
	
	public FileFinder(String projectRoot, Scenario scenario) {
		this(projectRoot);
		setScenario(scenario);
	}
	
	private void setLearningSystemInfo(LearningSystemInfo lsi) {
		this.lsi = lsi;
	}
	public FileFinder updateLearningSytemInfo(LearningSystemInfo lsi) {
		FileFinder copy = this.clone();
		copy.setLearningSystemInfo(lsi);
		
		return copy;
	}
	
	private void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}
	
	public FileFinder updateScenario(Scenario scenario) {
		FileFinder copy = this.clone();
		copy.setScenario(scenario);
		
		return copy;
	}
	
	private void setWorkDir(File workingDir) {
		this.workingDir = workingDir;
	}
	
	public FileFinder updateWorkDir(File workDir) {
		FileFinder copy = this.clone();
		copy.setWorkDir(workDir);
		
		return copy;
	}
	
	public File getWorkingDir() {
		return workingDir;
	}
	
	public File getLearningSystemDir(String learningSystem) {
		File lsDir = new File(this.projectRoot, Constants.LEARNINGSYSTEMS);
		return new File(lsDir, learningSystem);
	}
	
	public File getTrainingDir() {
		return new File(workingDir, "train");
	}
	
	public File getPositiveTrainingExamplesFile() {
		return new File(getTrainingDir(), lsi.getFilename(Constants.ExType.POS));
	}
	
	public File getNegativeTrainingExamplesFile() {
		return new File(getTrainingDir(), lsi.getFilename(Constants.ExType.NEG));
	}
	
	public File getTrainingResultOutputFile() {
		return new File(getTrainingDir(), "train.out");
	}
	
	public File getValidationDir() {
		return new File(workingDir, "validate");
	}
	
	public File getPositiveValidationExamplesFile() {
		return new File(getValidationDir(), lsi.getFilename(Constants.ExType.POS));
	}
	
	public File getNegativeValidationExamplesFile() {
		return new File(getValidationDir(), lsi.getFilename(Constants.ExType.NEG));
	}
	
	public File getValidationResultOutputFile() {
		return new File(getValidationDir(), "validateResult.prop");
	}
	@Override
	protected FileFinder clone() {
		FileFinder copy = new FileFinder(this.projectRoot);
		
		copy.setLearningSystemInfo(lsi);
		copy.setScenario(scenario);
		copy.setWorkDir(workingDir);
		
		return copy;
	}
}
