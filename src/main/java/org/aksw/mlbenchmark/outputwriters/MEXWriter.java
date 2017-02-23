package org.aksw.mlbenchmark.outputwriters;

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
import org.aksw.mlbenchmark.Constants;
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

/**
 * Class to write out the benchmark results as RDF data using the MEX ontology.
 * Since the MEX ontology is still under development there are some open issues
 * that will be fixed soon:
 * -
 */
public class MEXWriter {
	private static String authorName = "SML-Bench";
	private static String authorEmailAddress = "sml-bench@googlegroups.com";
	private static String datasetInfoFileName = "dataset.ttl";
	private static List<String> nonConfigKeys = Arrays.asList("learningtask",
			"learningproblem", "step", "output", "configFormat",
			"learningsystems", "scenarios", "maxExecutionTime");

	/**
	 * Main method to be called to write out the benchmark results collected
	 * in a BenchmarkLog object.
	 * 
	 * @param log The benchmark results
	 * @param filePath File path where to save the MEX RDF file
	 * @throws Exception Thrown in log4mex
	 */
	public static void write(BenchmarkLog log, String filePath) throws Exception {
		MyMEX mex = new MyMEX();
		mex.setAuthor(authorName, authorEmailAddress);

		for (String dataset : log.getLearningTasks()) {
			for (String learningProblem : log.getLearningProblems(dataset)) {
				for (String tool : log.getLearningSystems()) {
					addResults(mex, log, dataset, learningProblem, tool);
				}
			}
		}

		// TODO: handle file suffix
		MEXSerializer.getInstance().saveToDisk(filePath,
				"http://sml-bench.aksw.org/res/", mex, MEXConstant.EnumRDFFormats.TTL);
	}
	
	/**
	 * Extracts all the necessary bits to describe a mex:Experiment, i.e. the
	 * mex:Configuration with
	 * - a mex:Dataset (dataset parameter)
	 * - a mex:Execution comprising
	 *   - the (positive and negative) mex:Example instances (given by the
	 *     parameter learningProblem)
	 *   - the mex:Tool or mex:Algrotithm (defined by the tool parameter)
	 * @param mex The overall benchmark description object
	 * @param log The benchmarking results
	 * @param dataset The id of an SML-Bench benchmark dataset
	 * @param learningProblem The id of an SML-Bench learning problem
	 * @param tool The id of an SML-Bench learning system
	 * @throws Exception Whatever is thrown in log4mex
	 */
	private static void addResults(MyMEX mex, BenchmarkLog log, String dataset,
			String learningProblem, String tool) throws Exception {
		
		ScenarioSystem scenarioSystem =
				new Scenario(dataset, learningProblem).addSystem(
						log.getLearningSystemInfo(tool));

		/*
		 * Currently, the tool (or learning system) configuration is held per
		 * fold, i.e. the benchmark results log contains the detailed settings
		 * applied for each fold. This means that each fold configuration
		 * differs in
		 * - the actual positive and negative example files (which obviously
		 *   define the fold)
		 * - working and output directories
		 * 
		 * However all the individual tool specific settings like noise, sample
		 * sizes, i, j, ... remain the same across all fold configuration
		 * files. Thus I'm picking the config of fold 0 here as a
		 * representative here.
		 * 
		 * Further down this tool config will be filtered, ignoring all these
		 * fold-specific entries (skipping all entries from
		 * this.nonConfigKeys).
		 */
		Configuration toolConf = log.getLearningSystemConfig(scenarioSystem, 0);

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

	private static void addResults(MyMEX mex, String conf, ScenarioSystem scenarioSystem, int fold, int numFolds, BenchmarkLog log) throws Exception {

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

		if (res.containsKey(Constants.TRUE_POSITIVES_KEY) &&
				res.containsKey(Constants.FALSE_POSITIVES_KEY) &&
				res.containsKey(Constants.TRUE_NEGATIVES_KEY) &&
				res.containsKey(Constants.FALSE_NEGATIVES_KEY)) {

			int tp = res.getInt(Constants.TRUE_POSITIVES_KEY);
			int fp = res.getInt(Constants.FALSE_POSITIVES_KEY);
			int tn = res.getInt(Constants.TRUE_NEGATIVES_KEY);
			int fn = res.getInt(Constants.FALSE_NEGATIVES_KEY);
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

	private static DatasetInfo buildDatasetInfo(String datasetPath) {
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

	static class DatasetInfo {
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
