package org.ie4opendata.octroy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.ViewUriUtil;

/**
 * Based on XmiWriter, Copyright (c) 2014, Regents of the University of Colorado
 * 
 **/

public class TsvWriter extends JCasAnnotator_ImplBase {

  public static AnalysisEngineDescription getDescription(File outputDirectory)
          throws ResourceInitializationException {
    return AnalysisEngineFactory.createEngineDescription(TsvWriter.class,
            TsvWriter.PARAM_OUTPUT_FILE, outputDirectory);
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    PrintWriter pw;
    try {
      pw = new PrintWriter(new FileWriter(outputFile));
      pw.println("Document\tAmount\tAmount Start\tAmount End\tCompany\tCompany Start\tCompany End\tReason\tReason Start\tReason End");
      pw.close();
    } catch (IOException e) {
      throw new ResourceInitializationException(e);
    }
  }

  public static final String PARAM_OUTPUT_FILE = "outputDirectory";

  @ConfigurationParameter(name = PARAM_OUTPUT_FILE)
  protected File outputFile;

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      PrintWriter pw = new PrintWriter(new FileWriter(outputFile, true)); // slow, but effective
      String doc = ViewUriUtil.getURI(jCas).getFragment();
      boolean printed = false;
      for (Amount amt : JCasUtil.select(jCas, Amount.class)) {
        boolean printHere = false;
        for (Company cmp : JCasUtil.select(jCas, Company.class)) {
          boolean printCmp = false;
          for (Reason rsn : JCasUtil.select(jCas, Reason.class)) {
            pw.print(doc);
            printAnnotation(pw, amt);
            printAnnotation(pw, cmp);
            printAnnotation(pw, rsn);
            pw.println();
            printCmp = true;
          }
          if (!printCmp) {
            pw.print(doc);
            printAnnotation(pw, amt);
            printAnnotation(pw, cmp);
            printAnnotation(pw, null);
            pw.println();
          }
          printHere = true;
        }
        if (!printHere) {
          pw.print(doc);
          printAnnotation(pw, amt);
          printAnnotation(pw, null);
          printAnnotation(pw, null);
          pw.println();
        }
        printed = true;
      }
      if (!printed) // no amounts
        for (Company cmp : JCasUtil.select(jCas, Company.class)) {
          boolean printCmp = false;
          for (Reason rsn : JCasUtil.select(jCas, Reason.class)) {
            pw.print(doc);
            printAnnotation(pw, null);
            printAnnotation(pw, cmp);
            printAnnotation(pw, rsn);
            pw.println();
            printCmp = true;
          }
          if (!printCmp) {
            pw.print(doc);
            printAnnotation(pw, null);
            printAnnotation(pw, cmp);
            printAnnotation(pw, null);
            pw.println();
          }
          printed = true;
        }
      if (!printed) // no amounts, no companies
        for (Reason rsn : JCasUtil.select(jCas, Reason.class)) {
          pw.print(doc);
          printAnnotation(pw, null);
          printAnnotation(pw, null);
          printAnnotation(pw, rsn);
          pw.println();
        }
      pw.close();
    } catch (IOException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  private void printAnnotation(PrintWriter pw, Annotation ann) throws IOException {
    if (ann == null)
      pw.print("\t\t\t");
    else
      pw.print("\t" + ann.getCoveredText().replaceAll("\n", " ").replaceAll("\t", " ").trim() + "\t"
              + ann.getBegin() + "\t" + ann.getEnd());
  }
}
