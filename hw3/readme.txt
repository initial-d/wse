instructions:

Mining:  java -cp src edu.nyu.cs.cs2580.SearchEngine --mode=mining --options=conf/engine.conf

Spearman:  java -cp src edu.nyu.cs.cs2580.Spearman data/index/corpus_pageRank.idx  data/index/corpus_numViews.idx

Indexing:    java -Xmx512m -cp src edu.nyu.cs.cs2580.SearchEngine --mode=index --options=conf/engine.conf

Serving:  java -cp src -Xmx256m edu.nyu.cs.cs2580.SearchEngine --mode=serve --port=25809 --options=conf/engine.conf

spf :  curl 'http://localhost:25809/prf?query=%22Bob%20Dylan%22&ranker=comprehensive&numdocs=10&numterms=5'

Battahaya: java -cp src edu.nyu.cs.cs2580.Bhattacharyya data/index/prf/ data/bat


For section 2.1.1 the result didn't changed much if we chang lambda and iteration number.
However if we assume num views and pagerank is monotonically related, based on spearman rank iteration =2 and lambda = 0.9 give best result

Here's different spearm correlation :

lambda iteration  result
0.9    1          0.459097764595191
0.9    2          0.45987263063866546
0.1    1          0.45909605396622954
0.1    2          0.4547137514318028


As the grading script Bhattacharyya will take an file name as input argument whose format is
 <query>:<prf file>

where prf is the output of prf? command of corresponding query

e.g

assume prf.tsv is the input file it should be like
google:prf-1.tsv
bing:prf-2.tsv
salsa:prf-3.tsv
data mining:prf-4.tsv
web search:prf-5.tsv

That prf-1..5.tsv are the prf output of prf?query=google,bing...


in comprehensive I calculate the score by

score = log2(ql) + 0.1*log2(page_rank) + 0.1*log2(num_view)
