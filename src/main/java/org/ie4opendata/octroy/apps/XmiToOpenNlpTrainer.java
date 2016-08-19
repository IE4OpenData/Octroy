package org.ie4opendata.octroy.apps;

import static org.apache.uima.fit.factory.ResourceCreationSpecifierFactory.createResourceCreationSpecifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.ie4opendata.octroy.Company;
import org.ie4opendata.octroy.DocumentAnnotation;
import org.ie4opendata.octroy.Sentence;
import org.ie4opendata.octroy.Token;
import org.xml.sax.SAXException;

public class XmiToOpenNlpTrainer {

  public static void main(String[] args) throws IOException, InvalidXMLException,
          ResourceInitializationException, AnalysisEngineProcessException, CASException,
          SAXException {
    if (args.length != 2) {
      System.err.println("Usage: XmiToOpenNlpTrainer <input folder with xmis> <output file>");
    }

    AnalysisEngineDescription descriptor = (AnalysisEngineDescription) createResourceCreationSpecifier(
            new XMLInputSource(XmiToOpenNlpTrainer.class.getClassLoader().getResourceAsStream(
                    "org/ie4opendata/octroy/SimpleFrenchTokenAndSentenceAnnotator.xml"), new File(
                    ".")), new Object[0]);
    AnalysisEngine engine = AnalysisEngineFactory.createEngine(descriptor);
    CAS cas = engine.newCAS();

    PrintWriter pw = new PrintWriter(new FileWriter(args[1]));

    for (File file : new File(args[0]).listFiles()) {
      cas.reset();

      InputStream is = new FileInputStream(file);
      XmiCasDeserializer.deserialize(is, cas, true);

      JCas jcas = cas.getJCas();

      if (!JCasUtil.exists(jcas, DocumentAnnotation.class)) {
        DocumentAnnotation documentAnnotation = new DocumentAnnotation(cas.getJCas());
        documentAnnotation.setDocumentName(file.getName());
        documentAnnotation.setClassified(false);
        documentAnnotation.addToIndexes();
      }

      if (!JCasUtil.exists(jcas, Sentence.class))
        engine.process(cas);

      List<int[]> companyBoundaries = new ArrayList<int[]>();
      for (Company company : JCasUtil.select(jcas, Company.class))
        companyBoundaries.add(new int[] { company.getBegin(), company.getEnd() });

      int currentCompany = 0;
      boolean inCompany = false;

      // one sentence per line, one token separated by spaces
      for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
        if (inCompany) {
          if (sentence.getBegin() >= companyBoundaries.get(currentCompany)[1]) {
            inCompany = false;
            currentCompany++;
          } else
            pw.print(" <START:company> ");
        }

        for (Token token : JCasUtil.selectCovered(Token.class, sentence)) {
          if (currentCompany < companyBoundaries.size()) {
            if (inCompany) {
              if (token.getBegin() >= companyBoundaries.get(currentCompany)[1]) {
                pw.println(" <END> ");
                inCompany = false;
                currentCompany++;
              }
            } else {
              if (token.getBegin() >= companyBoundaries.get(currentCompany)[0]) {
                pw.print(" <START:company> ");
                inCompany = true;
              }
            }
          }
          pw.print(token.getCoveredText() + " ");
        }
        if (inCompany) {
          pw.println(" <END> ");
        }
        pw.println();
      }
      // each document separated by an empty line
      pw.println();
    }
    pw.close();
  }

}
