package org.ie4opendata.octroy.apps;

import static org.apache.uima.fit.factory.ResourceCreationSpecifierFactory.createResourceCreationSpecifier;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
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
import org.apache.uima.util.XMLInputSource;
import org.cleartk.util.ViewUriUtil;
import org.ie4opendata.octroy.TsvWriter;

// Run an IE pipeline for Amount / Company / Reason over text files, output a TSV file

public class TextToTsv {

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out.println("arguments: " //
              + "path to XML descriptor for pipeline, " //
              + "folder with text files, " //
              + "path to output tsv file");
      System.exit(-1);
    }

    // A collection reader that reads text files
    CollectionReader reader = CollectionReaderFactory.createReader(FilesCollectionReader.class,
            null, FilesCollectionReader.PARAM_ROOT_FILE, args[1]);

    AggregateBuilder builder = new AggregateBuilder();
    AnalysisEngineDescription descriptor = (AnalysisEngineDescription) createResourceCreationSpecifier(
            new XMLInputSource(TextToTsv.class.getClassLoader().getResourceAsStream(args[0]),
                    new File(".")), new Object[0]);
    builder.add(descriptor);
    builder.add(TsvWriter.getDescription(new File(args[2])));

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
