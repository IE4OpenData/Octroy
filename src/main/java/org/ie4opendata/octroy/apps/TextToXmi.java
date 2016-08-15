package org.ie4opendata.octroy.apps;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.descriptor.SofaCapability;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.cleartk.util.ViewUriUtil;
import org.cleartk.util.ae.XmiWriter;

// pre-training file, from folder with txt to folder with empty XMIs

public class TextToXmi {

  public static void main(String[] args) throws Exception {

    // A collection reader that reads text files
    CollectionReader reader = CollectionReaderFactory.createReader(FilesCollectionReader.class,
            null, FilesCollectionReader.PARAM_ROOT_FILE, args[0]);

    AggregateBuilder builder = new AggregateBuilder();
    builder.add(XmiWriter.getDescription(new File(args[1])));

    SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
  }

  /**
   * Based on org.cleartk.util.cr FilesCollectionReader
   * 
   * Copyright (c) 2007-2008, Regents of the University of Colorado All rights reserved.
   */

  @SofaCapability(outputSofas = ViewUriUtil.URI)
  public static class FilesCollectionReader extends org.cleartk.util.cr.FilesCollectionReader {

    public static CollectionReaderDescription getDescription(String fileOrDir)
            throws ResourceInitializationException {
      return CollectionReaderFactory.createReaderDescription(FilesCollectionReader.class, null,
              PARAM_ROOT_FILE, fileOrDir);
    }

    public void getNext(JCas jCas) throws IOException, CollectionException {
      if (!hasNext()) {
        throw new RuntimeException("getNext(jCas) was called but hasNext() returns false");
      }
      // get a JCas object
      JCas view;
      try {
        view = ViewCreatorAnnotator.createViewSafely(jCas, CAS.NAME_DEFAULT_SOFA);
      } catch (AnalysisEngineProcessException e) {
        throw new CollectionException(e);
      }

      String text = FileUtils.file2String(currentFile, "UTF-8");
      view.setSofaDataString(text, "text/plain");

      view.setDocumentLanguage("fr");

      // set the document URI
      try {
        ViewUriUtil.setURI(jCas, new java.net.URI("http://ie4opendata.org/octoroy#"
                + currentFile.getName().replaceAll("\\.txt", "")));
      } catch (URISyntaxException e) {
        throw new CollectionException(e);
      }

      completed++;
      currentFile = null;
    }

  }
}
