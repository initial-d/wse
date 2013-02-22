package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.util.Scanner;
import java.io.OutputStream;
import java.lang.Math;

class Ranker {
    private Index _index;

    public Ranker(String index_source){
        _index = new Index(index_source);
    }

    public Vector < ScoredDocument > runquery(String query){
        Vector < ScoredDocument > retrieval_results = new Vector < ScoredDocument > ();
        for (int i = 0; i < _index.numDocs(); ++i){
            retrieval_results.add(runquery(query, i));
        }
        return retrieval_results;
    }

    public Vector <String> ParseQuery (String query) {
        Scanner s = new Scanner(query);
        s.useDelimiter("[ ]");
        Vector < String > qv = new Vector < String > ();
        while (s.hasNext()){
            String term = s.next();
            qv.add(term);
        }
        return qv;
    }

    private double getTf (String qv, int did){
        double ret = 0;
        Document d = _index.getDoc(did);
        Vector<String> body = d.get_body_vector();
        for (int i = 0; i<body.size(); i++)
            if (qv.equals(body.get(i)))
                ret ++;
        return ret;
    }

    private double getIdf (String qv) {
        double n = _index.numDocs();
        double ret = Document.documentFrequency(qv);
        if (ret != 0) {
            ret = Math.log(n/ret)/Math.log(2)+1;
        }
        return ret;
    }

    private Vector<Double> normalize( Vector<Double> input) {
        Vector<Double> output = new Vector<Double>();
        double sum = 0;
        for (int i = 0; i<input.size();i++) 
            sum = sum + input.get(i)*input.get(i);
        sum = Math.sqrt(sum);
        if (sum!=0)
        for (int i = 0; i<input.size();i++)
            output.add(input.get(i)/sum);
        return output;
    }

    private Vector<Double>  getTFidf(String query,int did) {
        // Get the document vector. For hw1, you don't have to worry about the
        // details of how index works.
        Vector < String > qv = ParseQuery(query);
        Vector<Double> ret = new Vector<Double>();
        Document d = _index.getDoc(did);

        for (int j = 0; j < qv.size(); ++j) {
            ret.add(getTf(qv.get(j),did)*getIdf(qv.get(j)));
        }
        ret = normalize(ret);
        return ret;
    }

    public ScoredDocument runqueryWithCosine(String query, int did){
        Vector<Double> tfIdf = getTFidf(query,did);
        double upper=0;
        double lower=0;
        Document d = _index.getDoc(did);
        for (int i = 0; i<tfIdf.size();i++) {
            upper = upper + tfIdf.get(i);
            lower = lower + tfIdf.get(i)*tfIdf.get(i);
        }
        lower = Math.sqrt(lower);
        double score=0;
        if (lower != 0)
            score = upper/lower;
        return new ScoredDocument(did, d.get_title_string(), score);
    }

    public Vector < ScoredDocument > runqueryWithCosine(String query){
        Vector < ScoredDocument > retrieval_results = new Vector < ScoredDocument > ();
        for (int i = 0; i < _index.numDocs(); ++i){
            retrieval_results.add(runqueryWithCosine(query, i));
        }
        return retrieval_results;
    }


    private double JMS(String qv, int did) {
        double lambda = 0.5;
        double ret;
        double D = _index.getDoc(did).get_body_vector().size();
        double fq = getTf(qv,did);
        double cq = Document.termFrequency(qv);
        double C = Document.termFrequency();
        ret = Math.log((1-lambda)*fq/D + lambda*cq/C);
        return ret;
    }


    public double lmpWithJMS(String query, int did) {
        Vector<String> qv = ParseQuery(query);
        double ret = 0;
        for (int i=0; i<qv.size();i++)
            ret += JMS(qv.get(i),did);
        return ret;
    }
    public ScoredDocument runquery(String query, int did){

        // Build query vector
        Vector < String > qv = ParseQuery(query);

        // Get the document vector. For hw1, you don't have to worry about the
        // details of how index works.
        Document d = _index.getDoc(did);
        Vector < String > dv = d.get_title_vector();

        // Score the document. Here we have provided a very simple ranking model,
        // where a document is scored 1.0 if it gets hit by at least one query term.
        double score = 0.0;
        for (int i = 0; i < dv.size(); ++i){
            for (int j = 0; j < qv.size(); ++j){
                if (dv.get(i).equals(qv.get(j))){
                    score = 1.0;
                    break;
                }
            }
        }
        return new ScoredDocument(did, d.get_title_string(), score);
    }
}
