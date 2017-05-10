package org.aksw.mlbenchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class CSVWriter {

	public static void write(BenchmarkLog log, String filePath) throws IOException {
		Appendable out = new FileWriter(new File(filePath));
		CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT);
		
		List<String> learningSystems = new ArrayList<String>(log.getLearningSystems());
		// print header
		List<String> header = new ArrayList<>();
		header.add("scenario");
		for (String system : learningSystems) {
			header.add(system + " (f-score)");
			header.add(system + " (f-score stddev)");
			header.add(system + " (accuracy)");
			header.add(system + " (accuracy stddev)");
		}
		printer.printRecord(header);

		for (String task : log.getLearningTasks()) {
			for (String problem : log.getLearningProblems(task)) {
				Scenario scenario = new Scenario(task, problem);
				
				
				Values values = new Values(scenario, learningSystems);
				for (String system : learningSystems) {
					LearningSystemInfo lsi = log.getLearningSystemInfo(system);
					Stats stats;
					ScenarioSystem ss = scenario.addSystem(lsi);
					
					if (log.getNumFolds() > 1) {
						// no need to get stats if state != OK
						if (log.getTrainingState(ss).equals(Constants.State.OK)) {
							stats = getNFoldsStats(log, ss);
							
							values.addFScore(stats.fScore, system);
							values.addFScoreStddev(stats.fScoreStdev, system);
							values.addAccuracy(stats.acc, system);
							values.addAccuracyStddev(stats.accStdev, system);
							
						} else {
							Constants.State state = log.getTrainingState(ss);
							values.addFScore(state, system);
							values.addFScoreStddev(state, system);
							values.addAccuracy(state, system);
							values.addAccuracyStddev(state, system);
						}
					} else {
						// no need to get stats if state != OK
						if (log.getSingleFoldTrainingState(ss).equals(Constants.State.OK)) {
							stats = getSingleFoldStats(log, ss);
							
							values.addFScore(stats.fScore, system);
							values.addFScoreStddev(stats.fScoreStdev, system);
							values.addAccuracy(stats.acc, system);
							values.addAccuracyStddev(stats.accStdev, system);
							
						} else {
							Constants.State state = log.getSingleFoldTrainingState(ss);
							values.addFScore(state, system);
							values.addFScoreStddev(state, system);
							values.addAccuracy(state, system);
							values.addAccuracyStddev(state, system);
						}
					}
				}
				printer.printRecord(values);
			}
		}
		
		printer.close();
	}
	
	private static Stats getSingleFoldStats(BenchmarkLog log, ScenarioSystem ss) {
		Configuration valRes = log.getSingleFoldValidationResults(ss);
		Stats stats = new Stats();
		
		if (!valRes.isEmpty()) {
			Result res = new Result();
			res.tp = valRes.getInt(Constants.TRUE_POSITIVES_KEY);
			res.fp = valRes.getInt(Constants.FALSE_POSITIVES_KEY);
			res.tn = valRes.getInt(Constants.TRUE_NEGATIVES_KEY);
			res.fn = valRes.getInt(Constants.FALSE_NEGATIVES_KEY);
			
			stats.acc = accuracy(res);
			stats.accStdev = 0;
			stats.fScore = fScore(res);
			stats.fScoreStdev = 0;
		}
		
		return stats;
	}
	
	private static Stats getNFoldsStats(BenchmarkLog log, ScenarioSystem ss) {
		Configuration valRes;
		Result res;
		DescriptiveStatistics fScoresStats = new DescriptiveStatistics();
		DescriptiveStatistics accStats = new DescriptiveStatistics();
		for (int i=0; i<log.getNumFolds(); i++) {
			valRes = log.getValidationResults(ss, i);
			if (!valRes.isEmpty()) {
				res = new Result();
				res.tp = valRes.getInt(Constants.TRUE_POSITIVES_KEY);
				res.fp = valRes.getInt(Constants.FALSE_POSITIVES_KEY);
				res.tn = valRes.getInt(Constants.TRUE_NEGATIVES_KEY);
				res.fn = valRes.getInt(Constants.FALSE_NEGATIVES_KEY);
				
				fScoresStats.addValue(fScore(res));
				accStats.addValue(accuracy(res));
			}
		}
		
		Stats stats = new Stats();
		stats.acc = accStats.getMean();
		stats.accStdev = accStats.getStandardDeviation();
		stats.fScore = fScoresStats.getMean();
		stats.fScoreStdev = fScoresStats.getStandardDeviation();
		
		return stats;
	}
	
	private static double accuracy(Result res) {
		return (res.tp + res.tn) / ((double) (res.tp + res.fp + res.tn + res.fn));
	}
	
	private static double fScore(Result res) {
		double precision = precision(res);
		double recall = recall(res);
		return (precision + recall == 0) ? 0 :
			(2 * (precision * recall) / (precision + recall));
	}
	
	private static double precision(Result res) {
		if (res.tp + res.fp == 0) return 0;
		
		return res.tp / ((double) (res.tp + res.fp));
	}
	
	private static double recall(Result res) {
		if (res.tp + res.fn == 0) return 0;
		return res.tp / ((double) (res.tp + res.fn));
	}
	
	static class Stats {
		double acc, accStdev, fScore, fScoreStdev;
		
		@Override
		public String toString() {
			return "F-Score: " + fScore + " +/- " + fScoreStdev
					+ " | Accuracy: " + acc + " +/- " + accStdev;
		}
	}
	
	static class Result {
		int tp, tn, fp, fn;
		
		@Override
		public String toString() {
			return "tp: " + tp + "; tn: " + tn + " ; fp: " + fp + " ; fn: " + fn;
		}
	}
	
	static class Values extends ArrayList<String> {
		private static final long serialVersionUID = -150257079031755718L;
		private final List<String> learningSystems;

		Values(Scenario scenario, List<String> learningSystems) {
			/*
			 * for each learning system four values are reported (f-score,
			 * f-score stddev, accuracy, accuracy stddev), and there will be
			 * one column for the scenario
			 */
			super((learningSystems.size()*4) + 1);
			
			for (int i=0; i<((learningSystems.size()*4) + 1); i++) {
				this.add(null);
			}
			
			this.learningSystems = learningSystems;
			this.set(0, scenario.toString());
		}

		void addFScore(double fScore, String learningSystem) {
			int idx = 1 + (learningSystems.indexOf(learningSystem) * 4);
			this.set(idx, Double.toString(fScore));
		}
		
		void addFScore(Constants.State state, String learningSystem) {
			int idx = 1 + (learningSystems.indexOf(learningSystem) * 4);
			this.set(idx, state.toString());
		}
		
		void addFScoreStddev(double fScoreStddev, String learningSystem) {
			int idx = 1 + (learningSystems.indexOf(learningSystem) * 4) + 1;
			this.set(idx, Double.toString(fScoreStddev));
		}
		
		void addFScoreStddev(Constants.State state, String learningSystem) {
			int idx = 1 + (learningSystems.indexOf(learningSystem) * 4) + 1;
			this.set(idx, state.toString());
		}
		
		void addAccuracy(double acc, String learningSystem) {
			int idx = 1 + (learningSystems.indexOf(learningSystem) * 4) + 2;
			this.set(idx, Double.toString(acc));
		}
		
		void addAccuracy(Constants.State state, String learningSystem) {
			int idx = 1 + (learningSystems.indexOf(learningSystem) * 4) + 2;
			this.set(idx, state.toString());
		}
		
		void addAccuracyStddev(double accStddev, String learningSystem) {
			int idx = 1 + (learningSystems.indexOf(learningSystem) * 4) + 3;
			this.set(idx, Double.toString(accStddev));
		}
		
		void addAccuracyStddev(Constants.State state, String learningSystem) {
			int idx = 1 + (learningSystems.indexOf(learningSystem) * 4) + 3;
			this.set(idx, state.toString());
		}
	}
}
