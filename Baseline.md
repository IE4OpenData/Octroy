Octroy Baseline
===============

There is a baseline written in perl in the scripts/ folder.

To evaluate it, there are XMI coded by hand in the data/ folder under folders baseline36 and baseline32.

There are multiple ways to evaluate it, one is using the https://github.com/IE4OpenData/ruta_testing_standalone scripts:

in a folder outside octroy, clone that project:

```
$ cd /path/to/ruta_testing
$ git clone https://github.com/IE4OpenData/ruta_testing_standalone
$ mvn package appassembler:assemble
$ cd /path/to/octroy
$ /path/to/ruta_testing/target/appassembler/bin/ruta-evaluate --gold data/gold36 --eval data/baseline36 \
  --include org.ie4opendata.octroy.Company --include org.ie4opendata.octroy.Amount --include org.ie4opendata.octroy.Reason \
  --typesystem ./src/main/resources/org/ie4opendata/octroy/octroy_eval_ts.xml
```

If you want to see the errors file by file, use the --csv option. If you want to get CASes with the errors use the --result option.

If you want to see only the aggregate numbers for a particular type using --include for only that type.

For example:

```
$ for t in Amount Company Reason; \
  do for s in 36 32; \
    do echo $t $s; \
    /path/to/ruta_testing_standalone/target/appassembler/bin/ruta-evaluate \
      --gold data/gold$s --eval data/baseline$s \
      --include org.ie4opendata.octroy.$t \
      --typesystem ./src/main/resources/org/ie4opendata/octroy/octroy_eval_ts.xml; \
    done; 
  done
```

