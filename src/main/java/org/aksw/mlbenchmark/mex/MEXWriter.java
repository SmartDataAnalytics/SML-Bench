package org.aksw.mlbenchmark.mex;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aksw.mex.log4mex.MEXSerializer;
import org.aksw.mex.log4mex.MyMEX;
import org.aksw.mex.util.MEXConstant;
import org.aksw.mex.util.MEXEnum.EnumExamplesType;
import org.aksw.mex.util.MEXEnum.EnumExecutionsType;
import org.aksw.mex.util.MEXEnum.EnumMeasures;
import org.aksw.mex.util.MEXEnum.EnumPhases;
import org.aksw.mex.util.MEXEnum.EnumSamplingMethods;
import org.aksw.mlbenchmark.BenchmarkLog;
import org.aksw.mlbenchmark.Scenario;
import org.aksw.mlbenchmark.container.ScenarioSystem;
import org.apache.commons.configuration2.Configuration;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotNotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.vocabulary.RDF;

public class MEXWriter {
	private String authorName = "SML-Bench";
	private String authorEmailAddress = "sml-bench@googlegroups.com";
	private static String datasetInfoFileName = "dataset.ttl";
	private List<String> nonConfigKeys = Arrays.asList("learningtask",
			"learningproblem", "step", "output", "configFormat",
			"learningsystems", "scenarios", "maxExecutionTime");

	public void write(BenchmarkLog log, String filePath) throws Exception {
		MyMEX mex = new MyMEX();
		mex.setAuthor(authorName, authorEmailAddress);

		for (String dataset : log.getLearningTasks()) {
			for (String learningProblem : log.getLearningProblems(dataset)) {
				for (String tool : log.getLearningSystems()) {
					ScenarioSystem scenarioSystem =
							new Scenario(dataset, learningProblem).addSystem(log.getLearningSystemInfo(tool));

					// FIXME: take config from fold 0 sufficient?
					Configuration toolConf =
							log.getLearningSystemConfig(scenarioSystem, 0);

					// mex-core:ExperimentConfiguration
					String conf = mex.addConfiguration();

					// mex-algo:Tool
					mex.Configuration(conf).setTool(tool, "0.0");

					Iterator<String> keyIt = toolConf.getKeys();
					String key, val;

					while (keyIt.hasNext()) {
						key = keyIt.next();
						// FIXME: all learning system config entries should go into one ini section
						if (nonConfigKeys.contains(key)) continue;

						val = toolConf.getString(key);

						// mex-algo:ToolParameter
						// TODO: add IPL tools and parameters to MEX ontology
						mex.Configuration(conf).addToolParameters(key, val);
					}

					int numFolds = log.getNumFolds();
					for (int fold=0; fold<numFolds; fold++) {
						addResults(mex, conf, scenarioSystem, fold, numFolds, log);
					}
				}
			}
		}

		MEXSerializer.getInstance().saveToDisk(filePath,
				"http://sml-bench.aksw.org/res/", mex, MEXConstant.EnumRDFFormats.TTL);
	}

	private void addResults(MyMEX mex, String conf, ScenarioSystem scenarioSystem, int fold, int numFolds, BenchmarkLog log) throws Exception {

		// ----------------------------- mex-core -----------------------------
		// mex-core:Execution
		String exec = mex.Configuration(conf).addExecution(
				EnumExecutionsType.SINGLE, EnumPhases.VALIDATION);

		// TODO mex-core:Execution > startedAtTime
		//mex.Configuration(conf).setExecutionStartTime(exec, startTime);

		// TODO mex-core:Execution > endedAtTime
		//mex.Configuration(conf).setExecutionEndTime(exec, endTime);

		// TODO mex-core:HardwareConfiguration
		// mex.Configuration(conf).setHardwareConfiguration(os, EnumProcessors.INTEL_COREI5, EnumRAM.SIZE_16GB, hd, EnumCaches.CACHE_2MB);

		// FIXME: hard coded (did this just for now since we don't support other sampling methods)
		// TODO: determine sampling method
		mex.Configuration(conf).setSamplingMethod(EnumSamplingMethods.N_FOLDS_CROSS_VALIDATION, numFolds);

		// mex-core:Dataset
		DatasetInfo datasetInfo = buildDatasetInfo(
				log.getLearningTaskPath(scenarioSystem));

		mex.Configuration(conf).setDataSet(datasetInfo.landingPageURI,
				datasetInfo.description, datasetInfo.name);

		// mex-core:Example
		long datasetRow = 0;
		long datasetColumn = 0;

		for (String example : log.getPosExamples(scenarioSystem, fold)) {
			String id = "+" + example;
			String value = example;
			mex.Configuration(conf).Execution(exec).addDatasetExample(id,
					value, datasetRow, datasetColumn, EnumExamplesType.POS);
		}

		for (String example : log.getNegExamples(scenarioSystem, fold)) {
			String id = "-" + example;
			String value = example;
			mex.Configuration(conf).Execution(exec).addDatasetExample(id,
					value, datasetRow, datasetColumn, EnumExamplesType.NEG);
		}

		// mex-core:SamplingMethod
		// FIXME: adapt to other cases of train/test, ...
//		mex.Configuration(conf).setSamplingMethod(
//				EnumSamplingMethods.N_FOLDS_CROSS_VALIDATION, numFolds);
		// --------------------------------------------------------------------

		// ----------------------------- mex-algo -----------------------------
		// mex-algo:Algorithm
		String algorithmId = scenarioSystem.getLearningSystem() + "-alg";
		String algorithmName = scenarioSystem.getLearningSystem() + " Algorithm";
		String alg = mex.Configuration(conf).addAlgorithm(algorithmId, algorithmName);
		mex.Configuration(conf).Execution(exec).setAlgorithm(alg);

		// mex-algo:LearningMethod
		// TODO: Add the ILP tools to MEX ontology

		// mex-algo:LearningProblem
		// TODO: Add the ILP tools to MEX ontology

		// mex-algo:AlgorithmClass
		// TODO: Add the ILP tools to MEX ontology
		// --------------------------------------------------------------------

		// ----------------------------- mex-perf -----------------------------
		Configuration res = log.getValidationResults(scenarioSystem, fold);

		if (res.containsKey(BenchmarkLog.tp) &&
				res.containsKey(BenchmarkLog.fp) &&
				res.containsKey(BenchmarkLog.tn) &&
				res.containsKey(BenchmarkLog.fn)) {

			int tp = res.getInt(BenchmarkLog.tp);
			int fp = res.getInt(BenchmarkLog.fp);
			int tn = res.getInt(BenchmarkLog.tn);
			int fn = res.getInt(BenchmarkLog.fn);
			mex.Configuration(conf).Execution(exec).addPerformance(
					EnumMeasures.TRUEPOSITIVE, tp);
			mex.Configuration(conf).Execution(exec).addPerformance(
					EnumMeasures.FALSEPOSITIVE, fp);
			mex.Configuration(conf).Execution(exec).addPerformance(
					EnumMeasures.TRUENEGATIVE, tn);
			mex.Configuration(conf).Execution(exec).addPerformance(
					EnumMeasures.FALSENEGATIVE, fn);


			// ----------------- accuracy -----------------
			double sum = (tp + fp + tn + fn);
			if (sum != 0) {
				double acc = (tp + tn) / sum;
				mex.Configuration(conf).Execution(exec).addPerformance(
						EnumMeasures.ACCURACY, acc);
			}

			// ----------------- F-score ------------------
			double prec_denom = tp + fp;
			double rec_denom = tp + fn;

			if (prec_denom != 0 && rec_denom != 0) {
				double precision = tp / (prec_denom);
				double recall = tp /rec_denom;

				if ((precision + recall) > 0) {
					double fscore = 2 * ((precision * recall) / (precision + recall));

					mex.Configuration(conf).Execution(exec).addPerformance(
							EnumMeasures.F1MEASURE, fscore);
				}
			}
		} else {
			mex.Configuration(conf).Execution(exec).setErrorMessage("An error occurred");
		}
		// --------------------------------------------------------------------

	}


	private DatasetInfo buildDatasetInfo(String datasetPath) {
		String datasetInfoFilePath = datasetPath + File.separator + datasetInfoFileName;
		String landingPageURI ="";
		String description = "";
		String name = "";

		try {
			Model model = RDFDataMgr.loadModel(datasetInfoFilePath);

			// name
			NodeIterator it = model.listObjectsOfProperty(
					new PropertyImpl("http://purl.org/dc/terms/title"));
			if (it.hasNext()) {
				name = it.next().asLiteral().getLexicalForm();
			}

			// description
			it = model.listObjectsOfProperty(new PropertyImpl(
					"http://purl.org/dc/terms/description"));
			if (it.hasNext()) {
				description = it.next().asLiteral().getLexicalForm();
			}

			// landingPage
			ResIterator resIt = model.listSubjectsWithProperty(
					RDF.type, new ResourceImpl("http://rdfs.org/ns/void#Dataset"));

			landingPageURI = resIt.nextResource().getURI();

		} catch (RiotNotFoundException e) {
			String[] tmpParts = datasetInfoFilePath.split(File.separator);
			int numParts = tmpParts.length;

			if (numParts>=3) name = tmpParts[numParts-3];
			else name = datasetInfoFilePath;
		}
		return new DatasetInfo(landingPageURI, description, name);
	}

	class DatasetInfo {
		String landingPageURI;
		String description;
		String name;

		public DatasetInfo(String landingPageURI, String description, String name) {
			this.landingPageURI = landingPageURI;
			this.description = description;
			this.name = name;
		}
	}
}
