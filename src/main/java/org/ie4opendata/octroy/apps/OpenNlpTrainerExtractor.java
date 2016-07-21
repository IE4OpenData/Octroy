package org.ie4opendata.octroy.apps;

import static org.apache.uima.fit.factory.ResourceCreationSpecifierFactory.createResourceCreationSpecifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.ie4opendata.octroy.DocumentAnnotation;
import org.ie4opendata.octroy.Sentence;
import org.ie4opendata.octroy.Token;

public class OpenNlpTrainerExtractor {

	public static void main(String[] args) throws IOException, InvalidXMLException, ResourceInitializationException,
			AnalysisEngineProcessException, CASException {
		if (args.length != 2) {
			System.err.println("Usage: OpenNlpTrainerExtractor <input folder> <output file>");
		}

		AnalysisEngineDescription descriptor = (AnalysisEngineDescription) createResourceCreationSpecifier(
				new XMLInputSource(OpenNlpTrainerExtractor.class.getClassLoader().getResourceAsStream(
						"org/ie4opendata/octroy/SimpleFrenchTokenAndSentenceAnnotator.xml"), new File(".")),
				new Object[0]);
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(descriptor);
		CAS cas = engine.newCAS();

		PrintWriter pw = new PrintWriter(new FileWriter(args[1]));

		for (File file : new File(args[0]).listFiles()) {
			BufferedReader br = new BufferedReader(new FileReader(file));

			StringBuilder doc = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				doc.append(line).append('\n');
				line = br.readLine();
			}
			br.close();

			cas.reset();
			cas.setDocumentText(doc.toString());
			cas.setDocumentLanguage("fr");

			// document annotation goes into the (empty) initial view
			DocumentAnnotation documentAnnotation = new DocumentAnnotation(cas.getJCas());
			documentAnnotation.setDocumentName(file.getName());
			documentAnnotation.setClassified(false);
			documentAnnotation.addToIndexes();

			engine.process(cas);

			// one sentence per line, one token separated by spaces
			JCas jcas = cas.getJCas();
			for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
				for (Token token : JCasUtil.selectCovered(Token.class, sentence)) {
					pw.print(token.getCoveredText() + " ");
				}
				pw.println();
			}
			// each document separated by an empty line
			pw.println();
		}
		pw.close();
	}

}
