#!/bin/bash

rankers=(cosine QL phrase numviews linear)
outFiles=(../result/hw1.3-vsm.tsv ../result/hw1.3-ql.tsv ../result/hw1.3-phrase.tsv ../result/hw1.3-numviews.tsv ../result/hw1.3-linear.tsv)
sed -e 's/ /+/g' ../data/queries.tsv > queries.txt

for((i=0; i<${#rankers[*]}; i++))do
   rm -rf ${outFiles[$i]}
   while read line; do
       curl "localhost:25809/search?query=$line&ranker=${rankers[$i]}&format=text" | \java edu.nyu.cs.cs2580.Evaluator ../data/qrels.tsv ${rankers[$i]}
   done < queries.txt
done

rm -rf queries.txt

exit 0