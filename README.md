# Octroy

Octroy Pipeline for Open Data in Quebec

# Install

You need Java 7 and Maven 3 installed in your system.

The commands below use wget to download a dictionary file but you can
download it with any web browser.

Please note: the dictionary is released under its original license CC BY-NC-SA as it was obtained from
https://www.donneesquebec.ca/recherche/fr/dataset/registre-des-entreprises
The dictionary file is 62Mb compressed and contains 3.6M variants for enterprise names.

(if you want the dictionary to be someplace else, edit
./src/main/resources/org/ie4opendata/octroy/NeqConceptMapper.xml)

```
$ git clone https://github.com/IE4OpenData/Octroy
$ cd Octroy
$ wget http://duboue.net/download/neq_dict.xml.gz
$ gzip -d neq_dict.xml.gz
$ mv neq_dict.xml /tmp
```

if you want to use a dummy dictionary instead:

```
$ cp ./src/main/resources/org/ie4opendata/octroy/neq_dict_dummy.xml /tmp/neq_dict.xml
```

# Build

```
$ mvn compile
```

(will download plenty of dependencies...)

```
$ mvn package appassembler:assemble
```

# Execute

After having successfully build (see above):

## Analyze documents (batch)

You might need to set-up JAVA_OPTS to 6G heap:

```
$ export JAVA_OPTS=-Xmx6G
```

Tab delimited output:

```
$ ./target/appassembler/bin/run-pipeline-tsv org/ie4opendata/octroy/OctroyEngine.xml ./docs/dev36 dev36.tsv
```

(or OctroyEngineOpenNLP.xml)

XMI output:

```
$ ./target/appassembler/bin/run-pipeline-xmi org/ie4opendata/octroy/OctroyEngine.xml ./docs/dev36 /tmp/dev36/
```

Then evaluate the XMIs with

```
/path/to/ruta_testing/target/appassembler/bin/ruta-evaluate --gold data/gold36 --eval /tmp/dev36 \
  --include org.ie4opendata.octroy.Company  \
  --typesystem ./src/main/resources/org/ie4opendata/octroy/octroy_eval_ts.xml
```

RuTA Baseline:

```
./target/appassembler/bin/run-pipeline-tsv org/ie4opendata/octroy/baselineRutaEngine.xml ./docs/dev36 dev36-ruta-baseline.tsv
```

## Analyze documents (interactive)

```
$ ./target/appassembler/bin/document-analyzer
```

set input folder to docs/dev100
set output folder to output
set the analysis engine XML descriptor to run to src/main/resources/org/ie4opendata/octroy/OctroyEngine.xml
set the language to French

Press run or interactive

Serialized CASes with the results will be in output folder

# Train

## Train the contract classifier model

```
$ ./target/appassembler/bin/contract-trainer data/contract.training36
```

(output goes to src/main/resources/org/ie4opendata/octroy/contract-doccat.bin)

## Train the company identifier

see https://opennlp.apache.org/documentation/1.6.0/manual/opennlp.html#tools.namefind.training for details.

Extract the training file

```
$ ./target/appassembler/bin/opennlp-trainer-extractor ./docs/dev36 ./data/company.training36
```

(annotate the training file, add ' ' <START:company> ' ' and ' ' <END> ' ' around each company instance)

```
$ ./target/appassembler/bin/company-trainer ./data/company.training36
```

(output goes to src/main/resources/org/ie4opendata/octroy/fr-ner-company.bin)

## Train the company identifier from already annotated XMIs

```
$ ./target/appassembler/bin/xmi-to-opennlp-trainer ./data/gold36 ./data/company.training36
```

(the training file will contain the <START:company> and <END> tags already)

then follow with the company-trainer as described above.

Notice that changing the sentence boundary detector or the tokenizer will not require 
re-annotating in this case (just modifying the program to use a different descriptor).

## Annotate documents for evaluation with Apache RuTA and training with ClearTk

```
$ ./target/appassembler/bin/texts-to-xmis docs/dev36 data/gold36
```

then copy the typesystem to a place the Eclipse UIMA plugins can find it easily: 

```
$ cp ./src/main/resources/org/ie4opendata/octroy/octroy_types.xml ./TypeSystem.xml
```

(do not commit this file)

open each file under data/gold36 with the UIMA CAS Editor under Eclipse (working with Eclipse 4.3 Kepler), 
you might need to refresh your workspace (select the project, right click > refresh).  

click with the right button and under "show annotations" select Reason, Amount and Company.

then use the cursor keys plus shift to select the span of the annotation to annotate, 
press shift+enter and select the annotation from the menu (or use the quick key).

## Train the CRF Reason identifier

```
./target/appassembler/bin/reason-trainer ./data/gold36
```

Output goes to
src/main/resources/org/ie4opendata/octroy/reason/model.jar, other
files in that folder contain the Mallet train data (do not commit
them).

# Classroom use

See [TEACHING](TEACHING.md) for classroom activies using the different branches.

Courses taught using this project:

* [ECI 2016](https://github.com/IE4OpenData/ECI2016T2), Buenos Aires, Argentina
* [FaMAF 2017](https://github.com/IE4OpenData/IE4OD2017FAMAF), Cordoba, Argentina


