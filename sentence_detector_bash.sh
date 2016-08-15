for file in ./docs/dev36/*
do
  ./target/appassembler/bin/sentence-detection < "$file" >> ./data/sentence.detection36
done
