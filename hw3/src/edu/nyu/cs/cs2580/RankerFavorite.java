package edu.nyu.cs.cs2580;

import java.util.Vector;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Collections;
import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2 based on a refactoring of your favorite
 * Ranker (except RankerPhrase) from HW1. The new Ranker should no longer rely
 * on the instructors' {@link IndexerFullScan}, instead it should use one of
 * your more efficient implementations.
 */
public class RankerFavorite extends Ranker {

  public RankerFavorite(Options options,
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
      Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
      Document doc = null;
      int docid = -1;
      System.out.println("run query!");
      while ((doc = _indexer.nextDoc(query, docid)) != null) {
          //          double score = cosineScore(query,doc._docid);
          double score = jmsScore(query,doc._docid);
          rankQueue.add(new ScoredDocument(doc, score));
          if (rankQueue.size() > numResults) {
              rankQueue.poll();
          }
          docid = doc._docid;
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
