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

import org.ie4opendata.octroy.DocumentAnnotation;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

public class ContractClassifier extends JCasAnnotator_ImplBase {

    public static final String MODEL_NAME_PARAM = "model";

    private DocumentCategorizerME categorizer;

    public void initialize(UimaContext aContext)
	throws ResourceInitializationException {
	super.initialize(aContext);

	try {
	    InputStream is = this.getClass().getResourceAsStream((String) aContext.getConfigParameterValue(MODEL_NAME_PARAM));
	    DoccatModel m = new DoccatModel(is);
	    categorizer = new DocumentCategorizerME(m);
	}catch(IOException e){
	    throw new ResourceInitializationException(e);
	}
    }

    public void process(JCas aJCas) {
	double[] outcomes = categorizer.categorize(aJCas.getDocumentText());
	String category = categorizer.getBestCategory(outcomes);

	FSIndex docAnnIndex = aJCas.getAnnotationIndex(DocumentAnnotation.type);
	Iterator docAnnIter = docAnnIndex.iterator();
	DocumentAnnotation docAnn = null;
	if(docAnnIter.hasNext()){
	    docAnn = (DocumentAnnotation) docAnnIter.next();
	}else{
	    docAnn = new DocumentAnnotation(aJCas);
	    docAnn.setBegin(0);
	    docAnn.setEnd(aJCas.getDocumentText().length());
	    docAnn.addToIndexes();
	}

	docAnn.setProcess(category.equals("contract"));
	docAnn.setClassified(true);
    }
}
