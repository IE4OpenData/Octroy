package org.ie4opendata.octroy.apps;

import static org.apache.uima.fit.factory.ResourceCreationSpecifierFactory.createResourceCreationSpecifier;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.util.XMLInputSource;
import org.cleartk.util.ae.XmiWriter;
import org.ie4opendata.octroy.FilesCollectionReader;

// Run an IE pipeline for Amount / Company / Reason over text files, output a TSV file

public class RunPipelineXmi {

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out.println("arguments: " //
              + "path to XML descriptor for pipeline, " //
              + "folder with text files, " //
              + "folder to output xmi files");
      System.exit(-1);
    }

    // A collection reader that reads text files
    CollectionReader reader = CollectionReaderFactory.createReader(FilesCollectionReader.class,
            null, FilesCollectionReader.PARAM_ROOT_FILE, args[1]);

    AggregateBuilder builder = new AggregateBuilder();
    AnalysisEngineDescription descriptor = (AnalysisEngineDescription) createResourceCreationSpecifier(
            new XMLInputSource(RunPipelineXmi.class.getClassLoader().getResourceAsStream(args[0]),
                    new File(".")), new Object[0]);
    builder.add(descriptor);
    builder.add(XmiWriter.getDescription(new File(args[2])));
    
    SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
  }
}
