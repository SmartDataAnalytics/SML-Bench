package org.aksw.mlbenchmark.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 */
public class OntologyMerger {

	public static void main(String[] args) throws Exception {

		OptionParser parser = new OptionParser();
		OptionSpec<String> inputSpec = parser.acceptsAll(
				Lists.newArrayList("i", "input"),
				"the input files as comma-separated list")
				.withRequiredArg().ofType(String.class).required();
		OptionSpec<File> outputSpec = parser.acceptsAll(
				Lists.newArrayList("o", "output"),
				"the output file")
				.withRequiredArg().ofType(File.class).required();
		OptionSpec<String> languageSpec = parser.acceptsAll(
				Lists.newArrayList("l", "language"),
				"the language used for serialization. Options: TURTLE, RDFXML")
				.withRequiredArg().ofType(String.class)
				.defaultsTo("RDFXML");


		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (Exception e) {
			System.err.println("Wrong usage of OntologyMerger!");
			System.err.println(e.getMessage());
			parser.printHelpOn(System.out);

			System.exit(0);
		}

		// parse the input file(s)
		List<File> inputFiles = Splitter
				.on(",")
				.omitEmptyStrings()
				.trimResults()
				.splitToList(options.valueOf(inputSpec))
				.stream().map(path -> new File(path))
				.collect(Collectors.toList());

		// if set, get the output file
		File outputFile = options.has(outputSpec) ? options.valueOf(outputSpec) : null;

		// if set, get the language
		String lang = options.valueOf(languageSpec);

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		// get the merged ontology
		OWLOntology merged = merge(man, inputFiles);

		// the output stream is either to a file or standard out
		try(OutputStream os = outputFile == null ? System.out : new FileOutputStream(outputFile)) {
			man.saveOntology(merged, documentFormatFor(lang), os);
		} catch (Exception e) {
			System.err.println("Failed to write merged ontology");
			e.printStackTrace();

			System.exit(1);
		}
	}

	/*
		Merges the ontologies
	 */
	private static OWLOntology merge(OWLOntologyManager man, List<File> files) throws OWLOntologyCreationException {
		// We merge all of the loaded ontologies. We also need to
		// specify the IRI of the new ontology that will be created.
		files.forEach(file -> {
			try {
				man.loadOntologyFromOntologyDocument(file);
			} catch (OWLOntologyCreationException e) {
				System.err.println("Failed to load ontology from " + file);
				e.printStackTrace();

				System.exit(1);
			}
		});
		OWLOntologyMerger merger = new OWLOntologyMerger(man);
		IRI mergedOntologyIRI = IRI.create("http://www.sml-bench.org", "merged_ontology");

		return merger.createMergedOntology(man, mergedOntologyIRI);
	}

	/**
	 * Returns the ontology document format for the given language string.
	 *
	 * @param lang the language
	 * @return the ontology document format
	 */
	private static OWLDocumentFormat documentFormatFor(String lang) {
		switch (lang) {
			case "RDFXML": return new RDFXMLDocumentFormat();
			case "TURTLE": return new TurtleDocumentFormat();
			default: return new RDFXMLDocumentFormat();
		}
	}
}
