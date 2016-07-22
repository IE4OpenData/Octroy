package org.ie4opendata.octroy.apps;

import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.util.XMLInputSource;
import org.cleartk.ml.jar.Train;
import org.cleartk.util.cr.XReader;
import org.ie4opendata.octroy.ReasonAnnotator;

/**
 * Based on http://code.google.com/p/cleartk/wiki/TutorialPartOfSpeechClassifier
 * 
 */
public class ReasonTrainer {
	public static void main(String[] args) throws Exception {
		// A collection reader that reads XMIs
		CollectionReader reader = CollectionReaderFactory.createReader(XReader.class, null, XReader.PARAM_ROOT_FILE,
				args[0]);

		// The pipeline of annotators
		AggregateBuilder builder = new AggregateBuilder();

		// other annotators, if needed
		builder.add(UIMAFramework.getXMLParser().parseAnalysisEngineDescription(
				new XMLInputSource("src/main/resources/org/ie4opendata/octroy/SimpleFrenchTokenAndSentenceAnnotator.xml")));

		// The reason classifier annotator, configured to write training data
		builder.add(ReasonAnnotator.getWriterDescription("src/main/resources/org/ie4opendata/octroy/reason"));

		// Run the pipeline of annotators on each of the CASes produced by the
		// reader
		SimplePipeline.runPipeline(reader, builder.createAggregateDescription());

		// Train a classifier on the training data, and package it into a .jar
		// file
		Train.main("src/main/resources/org/ie4opendata/octroy/reason");
	}
}
