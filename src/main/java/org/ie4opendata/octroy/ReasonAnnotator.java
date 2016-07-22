package org.ie4opendata.octroy;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instances;
import org.cleartk.ml.chunking.BioChunking;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Following;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.extractor.CombinedExtractor1;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.function.CharacterCategoryPatternFunction;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;
import org.uimafit.util.JCasUtil;

@TypeCapability(outputs = { "org.ie4opendata.octroy.Reason" }, inputs = { "org.ie4opendata.octroy.Token",
		"org.ie4opendata.octroy.Sentence", "org.ie4opendata.octroy.DocumentAnnotation",
		"org.ie4opendata.octroy.Company", "org.ie4opendata.octroy.Amount" })
public class ReasonAnnotator extends CleartkSequenceAnnotator<String> {

	private FeatureExtractor1<Token> extractor;

	private CleartkExtractor<Token, Token> contextExtractor;

	private BioChunking<Token, Reason> chunking;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);

		// the token feature extractor: text, char pattern (uppercase, digits,
		// etc.), and part-of-speech
		this.extractor = new CombinedExtractor1<Token>(new CoveredTextExtractor<Token>(),
				new FeatureFunctionExtractor<Token>(new CoveredTextExtractor<Token>(),
						new CharacterCategoryPatternFunction<Token>(
								CharacterCategoryPatternFunction.PatternType.REPEATS_MERGED))
		/* , new TypePathExtractor(Token.class, "pos") */);

		// the context feature extractor: the features above for the 3 preceding
		// and 3 following tokens
		this.contextExtractor = new CleartkExtractor<Token, Token>(Token.class, this.extractor, new Preceding(3),
				new Following(3));

		// the chunking definition: Tokens will be combined to form Reason
		// annotation, with labels from the "entityType" attribute so that we
		// get B-location, I-person, etc.
		this.chunking = new BioChunking<Token, Reason>(Token.class, Reason.class, "entityType");
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

			// extract features for each token in the sentence
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			List<List<Feature>> featureLists = new ArrayList<List<Feature>>();
			for (Token token : tokens) {
				List<Feature> features = new ArrayList<Feature>();
				features.addAll(this.extractor.extract(jCas, token));
				features.addAll(this.contextExtractor.extract(jCas, token));
				featureLists.add(features);
			}
			// during training, convert Reason in the CAS into expected
			// classifier outcomes
			if (this.isTraining()) {

				// extract the gold (human annotated) Reason annotations
				List<Reason> reasons = JCasUtil.selectCovered(jCas, Reason.class, sentence);

				// convert the Reason annotations into token-level BIO outcome
				// labels
				List<String> outcomes = this.chunking.createOutcomes(jCas, tokens, reasons);

				// write the features and outcomes as training instances
				this.dataWriter.write(Instances.toInstances(outcomes, featureLists));
			}

			// during classification, convert classifier outcomes into
			// Reason in the CAS
			else {

				// get the predicted BIO outcome labels from the classifier
				List<String> outcomes = this.classifier.classify(featureLists);

				// create the NamedEntityMention annotations in the CAS
				this.chunking.createChunks(jCas, tokens, outcomes);
			}
		}
	}

	public static AnalysisEngineDescription getWriterDescription(String outputDirectory)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(ReasonAnnotator.class,
				CleartkSequenceAnnotator.PARAM_IS_TRAINING, true, DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
				outputDirectory, DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
				MalletCrfStringOutcomeDataWriter.class);
	}

	public static AnalysisEngineDescription getClassifierDescription(String modelFileName)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(ReasonAnnotator.class,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelFileName);
	}

}
