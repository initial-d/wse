1. beta for linear

I'm not using simply linear, since the Value of QL, numviews and phrase score are varied too much, I compute log2 for these values to combine the together.

The final equation is 

linear_score = beta1(1.0)*cosine_score +
               beta2(0.1)* log2(QL_score) +
               beta3(0.1)* log2(phrase_score) +
               beta4(0.05)* log2(numviews_score)

2. bash script

In src/folder, there is a evaluator.sh, it will run over each queries with each ranker, and do the evaluation.
simply use src/evaluator.sh to run it

3. for evaluator's output

you can either is >> to direct stdout to file

e.g java edu.nyu.cs.cs2580.Evaluator ../data/qrels.tsv >> ../result/hw1.3-linear.tsv

or you can give evaluator a ranker type parameter,so that evaluator will automatically output the result to corresponding file

e.g java edu.nyu.cs.cs2580.Evaluator ../data/qrels.tsv cosine

3.bonus:

use following instruction to simulate a click:

curl "localhost:25809/click?did=<did>&sid=<sessionID>"

did is the document id
SessionID is give by server associate with a query. SessionID has to be associate with a previous query, otherwise the click will be ignored.
