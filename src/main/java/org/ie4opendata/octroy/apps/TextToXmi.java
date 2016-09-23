package org.ie4opendata.octroy.apps;

import java.io.File;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.util.ae.XmiWriter;
import org.ie4opendata.octroy.FilesCollectionReader;

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
}
