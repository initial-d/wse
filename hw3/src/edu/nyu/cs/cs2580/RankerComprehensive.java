package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.io.IOException;
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
        double D = _indexer.getDoc(did).getSize();
        double C = _indexer.totalTermFrequency();
        double fq;
        double cq;
        double lambda = 0.5;
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
        score = Math.log(score)/Math.log(2.0);
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
        System.out.println("Doc number:"+cc);
        Vector<ScoredDocument> results = new Vector<ScoredDocument>();
        ScoredDocument scoredDoc = null;
        while ((scoredDoc = rankQueue.poll()) != null) {
            results.add(scoredDoc);
        }
        Collections.sort(results, Collections.reverseOrder());
        try {
            expand(results);
        } catch (IOException e) {
        }
        return results;
    }
    private void handleFile(String filePath,Map<String,Term> terms) {
        Vector<String> words = HTMLParser.parse(filePath);
        for (int i = 0; i<words.size();i++) {
            Term t = terms.get(words.get(i));
            if (t==null) {
                t = new Term(words.get(i),1);
                terms.put(words.get(i),t);
            } else {
                t._count++;
            }
        }
    }
    private void expand(Vector<ScoredDocument> docs) throws IOException{
        Map<String,Term> terms = new HashMap<String,Term> ();
        for (int i = 0; i<docs.size(); i++) {
            handleFile(_options._corpusPrefix+"/"+docs.get(i).getDoc().getTitle(),
                       terms);
        }
        // working on terms
        System.out.println(terms.size());
        Iterator it = terms.entrySet().iterator();
        Vector<Term> tV= new Vector<Term>();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            tV.add((Term) pairs.getValue());
        }
        Collections.sort(tV);
        StopWords stopWords = new StopWords();
        stopWords.load();
        int j = 0;
        for (int i = 0; i<tV.size();i++)
            if (!stopWords.isStopWord(tV.get(i)._term)&&j<20) {
                System.out.println(tV.get(i)._term+" "+
                                   tV.get(i)._count);
                j++;
            }
    }
}
