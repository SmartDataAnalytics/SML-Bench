package org.aksw.mlbenchmark.systemrunner;

import org.aksw.mlbenchmark.*;
import org.aksw.mlbenchmark.exampleloader.ExampleLoaderBase;
import org.aksw.mlbenchmark.process.ProcessRunner;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.exec.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Execute Cross Validation scenario
 */
public class CrossValidationRunner {
	final static Logger logger = LoggerFactory.getLogger(CrossValidationRunner.class);
	private final BenchmarkRunner parent;
	private final Set<String> failedLang = new HashSet<>();
	private final Map<String, CrossValidation> languageFolds = new HashMap<>();
	private final String task;
	private final String problem;
	private final Configuration parentConf;

	public CrossValidationRunner(BenchmarkRunner benchmarkRunner, String task, String problem, Configuration baseConf) {
		this.parent = benchmarkRunner;
		this.task = task;
		this.problem = problem;
		this.parentConf = baseConf;
		for (final String lang : parent.getDesiredLanguages()) {
			String lpDir = parent.getLearningProblemDir(task, problem, lang);
			try {
				ExampleLoaderBase elPos = ExampleLoader.forLanguage(lang);
				elPos.loadExamples(new File(lpDir+"/"+"pos"+ LanguageInfo.forLanguage(lang).exampleExtension()));
				LinkedHashSet<String> posExamples = elPos.getExamples();

				ExampleLoaderBase elNeg = ExampleLoader.forLanguage(lang);
				elNeg.loadExamples(new File(lpDir+"/"+"neg"+LanguageInfo.forLanguage(lang).exampleExtension()));
				LinkedHashSet<String> negExamples = elNeg.getExamples();

				CrossValidation cv = new CrossValidation(posExamples, negExamples, parent.getFolds(), parent.getSeed());

				languageFolds.put(lang, cv);
			} catch (IOException e) {
				logger.warn("could not load examples for "+lang+": " + e.getMessage());
				failedLang.add(lang);
			}
		}

		writeAllFolds(task, problem, languageFolds, failedLang);


	}

	private void writeAllFolds(String task, String problem, Map<String, CrossValidation> languageFolds, Set<String> failedLang) {
		File dir = new File(parent.getTempDirectory() + "/" + task + "/" + problem);
		dir.mkdirs();
		for (final String lang: parent.getDesiredLanguages()) {
			if (failedLang.contains(lang)) { continue; }
			try {
				CrossValidation crossValidation = languageFolds.get(lang);
				for (Constants.ExType ex : Constants.ExType.values()) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dir + "/" + "folds-" + lang + "-" + ex.name().toLowerCase() + LanguageInfo.forLanguage(lang).exampleExtension())));
					for (int i = 0; i < parent.getFolds(); ++i) {
						writer.write("; fold " + i); writer.newLine();
						LinkedHashSet<String> testingSet = crossValidation.getTestingSet(ex, i);
						ExampleLoaderBase elf = ExampleLoader.forLanguage(lang);
						elf.setExamples(testingSet);
						elf.writeExamples(writer);
					}
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		for (final String system: parent.getDesiredSystems()) {
			final String lang = parent.getSystemLanguage(system);
			if (failedLang.contains(lang)) {
				logger.warn("skipping system "+system+" because examples are missing");
				continue;
			}

/*			parent.getExecutorService().submit(
					new Runnable() {
						@Override
						public void run() { */
			for (int fold = 0; fold < parent.getFolds(); ++fold) {
				logger.info("executing scenario " + task + "/" + problem + " with " + system + ", fold " + fold);

				File dir = new File(parent.getTempDirectory() + "/" + task + "/" + problem + "/" + "fold-" + fold + "/" + system);
				dir.mkdirs();

				LearningSystemInfo lsi = parent.getSystemInfo(system);

				String posFilename = dir + "/" + lsi.getPosFilename();
				String negFilename = dir + "/" + lsi.getNegFilename();
				String outputFile = dir + "/" + "train.out";
				String configFile = dir + "/" + "config." + lsi.getConfig().getString("configFormat");

				LinkedHashSet<String> testingPos = languageFolds.get(lang).getTestingSet(Constants.ExType.POS, fold);
				LinkedHashSet<String> testingNeg = languageFolds.get(lang).getTestingSet(Constants.ExType.NEG, fold);

				BaseConfiguration baseConfig = getBaseConfiguration(fold, dir, posFilename, negFilename, outputFile);

				Configuration cc = collectConfig(system, lang, dir, baseConfig, lsi);
				writeConfig(configFile, cc);
				writeFolds(lang, posFilename, testingPos, negFilename, testingNeg);

				List<String> args = new LinkedList<>();
				args.add(configFile);

				final long now = System.nanoTime();
				try {
					ProcessRunner processRunner = new ProcessRunner(parent.getLearningSystemDir(system), "./run", args, cc);
				} catch (ExecuteException e) {
					logger.warn("learning system " + system + " did not finish cleanly: " + e.getMessage());
				} catch (IOException e) {
					logger.warn("learning system " + system + " could not execute: " + e.getMessage() + "[" + e.getClass() + "]");
				}
				long duration = System.nanoTime() - now;
			}

/*						}
					}); */
		}
	}

	private void writeConfig(String configFile, Configuration cc) {
		try {
			ConfigLoader.write(cc, new File(configFile));
		} catch (IOException | ConfigLoaderException | ConfigurationException e) {
			throw new RuntimeException("Writing scenario config failed", e);
		}
	}

	private void writeFolds(String lang, String posFilename, LinkedHashSet<String> testingPos, String negFilename, LinkedHashSet<String> testingNeg) {
		try {
			ExampleLoaderBase posWriter = ExampleLoader.forLanguage(lang);
			posWriter.setExamples(testingPos);
			posWriter.writeExamples(new File(posFilename));

			ExampleLoaderBase negWriter = ExampleLoader.forLanguage(lang);
			negWriter.setExamples(testingNeg);
			negWriter.writeExamples(new File(negFilename));
		} catch (IOException e) {
			throw new RuntimeException("Could not write fold file", e);
		}
	}

	private BaseConfiguration getBaseConfiguration(int fold, File dir, String posFilename, String negFilename, String outputFile) {
		BaseConfiguration baseConfig = new BaseConfiguration();
		baseConfig.setProperty("data.workdir", dir.getAbsolutePath());
		baseConfig.setProperty("framework.currentFold", fold);
		baseConfig.setProperty("learningtask", task);
		baseConfig.setProperty("learningproblem", problem);
		baseConfig.setProperty("step", "train");

		baseConfig.setProperty("filename.pos", posFilename);
		baseConfig.setProperty("filename.neg", negFilename);
		baseConfig.setProperty("output", outputFile);
		return baseConfig;
	}

	private Configuration collectConfig(String system, String lang, File dir, BaseConfiguration baseConfig, LearningSystemInfo lsi) {
		Configuration runtimeConfig = parent.getConfig();
		// Configuration stepConfig = parentConf;
		Configuration scnRuntimeConfig = runtimeConfig.subset("learningtask." + task);
		Configuration lpRuntimeConfig = runtimeConfig.subset("learningproblem." + task + "." + problem);

		ConfigLoader lpCL = ConfigLoader.findConfig(parent.getLearningProblemDir(task, problem, lang) + "/" + system);
		CombinedConfiguration cc = new CombinedConfiguration();
		cc.setNodeCombiner(new MergeCombiner());
		cc.addConfiguration(baseConfig);
		cc.addConfiguration(parentConf);
		cc.addConfiguration(lpRuntimeConfig);
		if (lpCL != null) {
			cc.addConfiguration(lpCL.config());
		}
		List<String> families = lsi.getConfig().getList(String.class, "families");
		if (families != null) {
			for (String family : families) {
				ConfigLoader famLpCL = ConfigLoader.findConfig(parent.getLearningProblemDir(task, problem, lang) + "/" + family);
				if (famLpCL != null) {
					cc.addConfiguration(famLpCL.config());
				}
			}
		}
		cc.addConfiguration(scnRuntimeConfig);
		cc.addConfiguration(lsi.getConfig());

		return cc;
	}
}
