package org.ie4opendata.octroy;

// licensed under APL-2.0

import java.util.ArrayList;
import java.util.List;

import java.io.*;
import java.util.*;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ie4opendata.octroy.Amount;

public class AmountAnnotator extends JCasAnnotator_ImplBase {

    private Pattern amountPattern = 
        Pattern.compile("(\\d?\\d?(?:\\s?|\\.?)\\d{3}(?:\\s?|,?)(?:\\d{3})?(?:\\,\\d{2})?\\s?\\$)");

    public void process(JCas aJCas) {
	String docText = aJCas.getDocumentText();
	Matcher matcher = amountPattern.matcher(docText);
	int pos = 0;
	while (matcher.find(pos)) {
	    Amount amount = new Amount(aJCas);
	    amount.setBegin(matcher.start());
	    amount.setEnd(matcher.end());
	    amount.setDollarValue(extractDollarValue(matcher.group()));
	    amount.addToIndexes();
	    pos = matcher.end();
	}
    }

    private float extractDollarValue(String s){
	try{
	    return Float.parseFloat(s.replaceAll("\\$|\\.\\s","").replaceAll("\\,",".").replaceAll("[^0-9.]",""));
	}catch(NumberFormatException e){
	    return -1f;
	}
    }
}
