r#!/bin/bash
rm -f prf*.tsv
i=0
while read q ; do
    i=$((i + 1));
    prfout=prf-$i.tsv;
    Q=`echo $q| sed "s/ /%20/g"`;
    curl "http://localhost:25809/prf?query=$Q&ranker=comprehensive&numdocs=20&numterms=10" > $prfout;
    echo $q:$prfout >> prf.tsv
done < queries.tsv
java -cp src edu.nyu.cs.cs2580.Bhattacharyya prf.tsv qsim.tsv