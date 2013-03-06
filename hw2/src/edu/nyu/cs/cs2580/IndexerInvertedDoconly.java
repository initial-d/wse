
package edu.nyu.cs.cs2580;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;
/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedDoconly extends IndexerInverted implements Serializable {
  private static final long serialVersionUID = 1067111905740085030L;



  private Map<Integer,Integer> _termCorpusFrequency = new HashMap<Integer, Integer>();
  private Map<Integer, Vector<Integer> > _termToDocs =
      new HashMap<Integer, Vector<Integer> > ();



  public IndexerInvertedDoconly(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }
  @Override
  public String getIndexFilePath () {
      return _options._indexPrefix + "/corpus_invertedDoconly.idx";
  }
  @Override
  public void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques) {
    for (int idx : tokens) {
      uniques.add(idx);
      _termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
      ++_totalTermFrequency;
    }
  }
  @Override
  public void updateUniqueTerms(Set<Integer> uniqueTerms,int did) {
      for (Integer idx : uniqueTerms) {
          _termToDocs.get(idx).add(did);
      }
  }
  @Override
  public void addToken(String token,Vector<Integer> tokens) {
      int idx = -1;
      if (_dictionary.containsKey(token)) {
          idx = _dictionary.get(token);
      } else {
          idx = _terms.size();
          _terms.add(token);
          _dictionary.put(token, idx);
          _termCorpusFrequency.put(idx, 0);
          _termToDocs.put(idx, new Vector<Integer>());
      }
      tokens.add(idx);
  }

    //  public abstract void loadIndex() throws IOException, ClassNotFoundException;
    //    @Override
  public void loadIndex()  {
      try {
      String indexFile = getIndexFilePath();
      System.out.println("Load index from: " + indexFile);

      ObjectInputStream reader =
          new ObjectInputStream(new FileInputStream(indexFile));
      IndexerInvertedDoconly loaded = null;
      try {
          loaded = (IndexerInvertedDoconly) reader.readObject();
      } catch (ClassNotFoundException e) {
      }

      this._documents = loaded._documents;

      // Compute numDocs and totalTermFrequency b/c Indexer is not serializable.
      this._numDocs = _documents.size();
      for (Integer freq : loaded._termCorpusFrequency.values()) {
          this._totalTermFrequency += freq;
      }
      this._dictionary = loaded._dictionary;
      this._terms = loaded._terms;
      this._termCorpusFrequency = loaded._termCorpusFrequency;
      this._termToDocs = loaded._termToDocs;
      reader.close();

      System.out.println(Integer.toString(_numDocs) + " documents loaded " +
                         "with " + Long.toString(_totalTermFrequency) + " terms!");
      //      output();
      } catch (IOException e) {
      }
  }



  @Override
  public Document getDoc(int docid) {
    SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
    return null;
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}
   */
    //  @Override
  public Document nextDoc(Query query, int docid) {
    return null;
  }

  @Override
  public int corpusDocFrequencyByTerm(String term) {
      if (!_dictionary.containsKey(term))
        return 0;
    Integer idx = _dictionary.get(term);
    return _termToDocs.get(idx).size();
  }

  @Override
  public int corpusTermFrequency(String term) {
    if (!_dictionary.containsKey(term))
        return 0;
    Integer idx = _dictionary.get(term);
    return _termCorpusFrequency.get(idx);
  }

  @Override
  public int documentTermFrequency(String term, String url) {
    SearchEngine.Check(false, "Not implemented!");
    return 0;
  }
    //  @Override
  @Override
  public void output() {
      System.out.println("_numDocs="+Integer.toString(_numDocs));
      System.out.println("_totalTermFrequency="+Long.toString(_totalTermFrequency));
      for (int i = 0; i<_terms.size();i++) {
          System.out.println(_terms.get(i)+
                           ":"+Integer.toString(corpusTermFrequency(_terms.get(i)))+
                           ":"+Integer.toString(corpusDocFrequencyByTerm(_terms.get(i))));
      }
  }
}
