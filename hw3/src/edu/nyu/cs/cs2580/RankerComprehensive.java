package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.lang.Math;
import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3 based on your {@code RankerFavorite}
 * from HW2. The new Ranker should now combine both term features and the
 * document-level features including the PageRank and the NumViews.
 */
public class RankerComprehensive extends Ranker {
    private class Term implements Comparable<Term> {
        public Term (String t,int i) {
            _term = t;
            _count = i;
        }
        public String _term;
        public int _count;
        @Override
            public int compareTo(Term t) {
            if (this._count>t._count) return -1;
            if (this._count<t._count) return 1;
            return this._term.compareTo(t._term);
        }
    }
    public RankerComprehensive(Options options,
                               CgiArguments arguments, Indexer indexer) {
        super(options, arguments, indexer);
        System.out.println("Using Ranker: " + this.getClass().getSimpleName());
    }

    private double jmsScore (Query query, int did) {
        Vector<String[]> qp = query.getPhrases();
        Vector<String> qv = query.getTokenNotInPhrase();
        /*        for (int i = 0; i<qv.size();i++)
            qv.set(i,HTMLParser.stemm(qv.get(i)));
        for (int i = 0; i<qp.size();i++)
            for (int j = 0; j<qp.get(i).length;j++)
                qp.get(i)[j] = HTMLParser.stemm(qp.get(i)[j]);
        */
        double D = _indexer.getDoc(did).getSize();
        double C = _indexer.totalTermFrequency();
        double fq;
        double cq;
        double lambda = 0.8;
        double score = 1.0;

        for (int i=0; i<qv.size();i++) {
            fq = _indexer.documentTermFrequency(qv.get(i),did);
            cq = _indexer.corpusTermFrequency(qv.get(i));
            double ret = (1-lambda)*fq/D + lambda*cq/C;
            score *= ret;
        }

        for (int i=0; i<qp.size();i++) {
            fq = _indexer.docPhraseCount( qp.get(i),did);
            double ret = fq/D;
            score *=ret;
        }
        Document doc = _indexer.getDoc(did);
        double pg = doc.getPageRank();
        double nv = doc.getNumViews();
        score = Math.log(score)/Math.log(2.0);
        score = score + 0.1*Math.log(pg+1)/Math.log(2.0);
        score = score + 0.1*Math.log(nv+1)/Math.log(2.0);
        return score;
    }

    @Override
        public Vector<ScoredDocument> runQuery(Query query, int numResults) {
        System.out.println("compehensive:"+
                           _arguments.getNumDocs()+" "+
                           _arguments.getNumTerms());
        Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
        Document doc = null;
        int docid = -1;
        System.out.println("run query!");
        int cc = 0;
        while ((doc = _indexer.nextDoc(query, docid)) != null) {
            //          double score = cosineScore(query,doc._docid);
            cc++;
            double score = jmsScore(query,doc._docid);
            rankQueue.add(new ScoredDocument(doc, score));
            if (rankQueue.size() > _arguments.getNumDocs()) {
                rankQueue.poll();
            }
            docid = doc._docid;
            //            System.out.println(doc.getTitle());
        }
        Vector<ScoredDocument> results = new Vector<ScoredDocument>();
        ScoredDocument scoredDoc = null;
        while ((scoredDoc = rankQueue.poll()) != null) {
            results.add(scoredDoc);
        }
        Collections.sort(results, Collections.reverseOrder());
        return results;
    }
}
