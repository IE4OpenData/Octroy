package org.ie4opendata.octroy;

// Licensed under APL-2.0

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.text.BreakIterator;
import java.text.ParsePosition;
import java.util.Locale;

import org.ie4opendata.octroy.Token;
import org.ie4opendata.octroy.Sentence;

/**
 * Annotating Tokens and Sentences in French using the java.text.BreakIterator. Based on the UIMA example.
 */
public class SimpleFrenchTokenAndSentenceAnnotator extends JCasAnnotator_ImplBase {

    static abstract class Maker {
	abstract Annotation newAnnotation(JCas jcas, int start, int end);
    }

    static final BreakIterator sentenceBreak = BreakIterator.getSentenceInstance(Locale.CANADA_FRENCH);
    static final BreakIterator wordBreak = BreakIterator.getWordInstance(Locale.CANADA_FRENCH);

    static final Maker sentenceAnnotationMaker = new Maker() {
	    Annotation newAnnotation(JCas jcas, int start, int end) {
		return new Sentence(jcas, start, end);
	    }
	};

    static final Maker tokenAnnotationMaker = new Maker() {
	    Annotation newAnnotation(JCas jcas, int start, int end) {
		return new Token(jcas, start, end);
	    }
	};

    public void process(JCas aJCas) throws AnalysisEngineProcessException {
	makeAnnotations(aJCas, aJCas.getDocumentText(), sentenceAnnotationMaker, sentenceBreak);
	makeAnnotations(aJCas, aJCas.getDocumentText(), tokenAnnotationMaker, wordBreak);
    }

    void makeAnnotations(JCas jcas, String input, Maker m, BreakIterator b) {
	b.setText(input);
	for (int end = b.next(), start = b.first(); end != BreakIterator.DONE; start = end, end = b
		 .next()) {
	    // eliminate all-whitespace tokens
	    boolean isWhitespace = true;
	    for (int i = start; i < end; i++) {
		if (!Character.isWhitespace(input.charAt(i))) {
		    isWhitespace = false;
		    break;
		}
	    }
	    if (!isWhitespace) {
		m.newAnnotation(jcas, start, end).addToIndexes();
	    }
	}
    }
}
