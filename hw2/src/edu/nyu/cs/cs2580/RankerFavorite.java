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


  @Override
  public Vector<ScoredDocument> runQuery(Query query, int numResults) {
      Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();
      Document doc = null;
      int docid = -1;
      System.out.println("run query!");
      while ((doc = _indexer.nextDoc(query, docid)) != null) {
          rankQueue.add(new ScoredDocument(doc, 1.0));
          if (rankQueue.size() > numResults) {
              rankQueue.poll();
          }
          docid = doc._docid;
          System.out.println(doc.getTitle());
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
