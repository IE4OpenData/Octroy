package org.ie4opendata.octroy;

import java.util.Iterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.flow.FinalStep;
import org.apache.uima.flow.Flow;
import org.apache.uima.flow.JCasFlowController_ImplBase;
import org.apache.uima.flow.JCasFlow_ImplBase;
import org.apache.uima.flow.SimpleStep;
import org.apache.uima.flow.Step;
import org.apache.uima.jcas.JCas;

public class ContractFlowController extends JCasFlowController_ImplBase {
	public Flow computeFlow(JCas aJCas) throws AnalysisEngineProcessException {
		return new ContractFlow();
	}

	class ContractFlow extends JCasFlow_ImplBase {

		private boolean done = false;

		private boolean hasProcess(JCas aJCas) {
			FSIndex docAnnIndex = aJCas.getAnnotationIndex(DocumentAnnotation.type);
			Iterator docAnnIter = docAnnIndex.iterator();
			if (docAnnIter.hasNext())
				return ((DocumentAnnotation) docAnnIter.next()).getClassified();
			return false;
		}

		private boolean shouldProcess(JCas aJCas) {
			FSIndex docAnnIndex = aJCas.getAnnotationIndex(DocumentAnnotation.type);
			Iterator docAnnIter = docAnnIndex.iterator();
			return ((DocumentAnnotation) docAnnIter.next()).getProcess();
		}

		public Step next() throws AnalysisEngineProcessException {
			JCas cas = getJCas();

			if (!hasProcess(cas))
				return new SimpleStep("ContractClassifier");
			if (!done && shouldProcess(cas)) {
				done = true;
				return new SimpleStep("ContractEngine");
			}
			return new FinalStep();
		}
	}
}
