
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
public class IndexerInvertedDoconly extends Indexer implements Serializable {
  private static final long serialVersionUID = 1067111905740085030L;

  private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
  private Vector<String> _terms = new Vector<String>();
  private Map<Integer,Integer> _termCorpusFrequency = new HashMap<Integer, Integer>();
  private Map<Integer, Vector<Integer> > _termToDocs =
      new HashMap<Integer, Vector<Integer> > ();
  private Vector<Document> _documents = new Vector<Document>();


  public IndexerInvertedDoconly(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

  @Override
  public void constructIndex() throws IOException {
      if (_options._corpusPrefix.equals("data/simple")) {
          String corpusFile = _options._corpusPrefix + "/corpus.tsv";
          System.out.println("Construct index from: " + corpusFile);

          BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
          try {
              String line = null;
              while ((line = reader.readLine()) != null) {
                  processDocument(line);
              }
          } finally {
              reader.close();
          }
      }
      else {
          final File folder = new File(_options._corpusPrefix);
          for (final File fileEntry : folder.listFiles()) {
              if (!fileEntry.isDirectory()) {
                  handleFile(fileEntry.getName());
              }
          }
      }
      System.out.println(
                         "Indexed " + Integer.toString(_numDocs) + " docs with " +
                         Long.toString(_totalTermFrequency) + " terms.");

    String indexFile = _options._indexPrefix + "/corpus_invertedDoconly.idx";
    System.out.println("Store index to: " + indexFile);
    ObjectOutputStream writer =
        new ObjectOutputStream(new FileOutputStream(indexFile));
    writer.writeObject(this);
    writer.close();
    //    output();
  }

  private void handleFile(String fileName) {
      if (fileName.equals(".DS_Store"))
          return;
      System.out.println(fileName);
  }
  private void processDocument(String content) {
    Scanner s = new Scanner(content).useDelimiter("\t");

    String title = s.next();
    Vector<Integer> titleTokens = new Vector<Integer>();
    readTermVector(title, titleTokens);
    String body = s.next();
    Vector<Integer> bodyTokens = new Vector<Integer>();
    readTermVector(body, bodyTokens);

    int numViews = Integer.parseInt(s.next());
    s.close();

    DocumentIndexed doc = new DocumentIndexed (_documents.size(), this);
    doc.setTitle(title);
    doc.setNumViews(numViews);
    //    doc.setTitleTokens(titleTokens);
    //    doc.setBodyTokens(bodyTokens);
    _documents.add(doc);
    ++_numDocs;

    Set<Integer> uniqueTerms = new HashSet<Integer>();
    updateStatistics(titleTokens, uniqueTerms);
    updateStatistics(bodyTokens, uniqueTerms);
    int did = _documents.size()-1;
    for (Integer idx : uniqueTerms) {
        _termToDocs.get(idx).add(did);
        //      _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
    }
  }

  private void readTermVector(String content, Vector<Integer> tokens) {
    Scanner s = new Scanner(content);  // Uses white space by default.
    while (s.hasNext()) {
      String token = s.next();
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
    return;
  }

  private void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques) {
    for (int idx : tokens) {
      uniques.add(idx);
      _termCorpusFrequency.put(idx, _termCorpusFrequency.get(idx) + 1);
      ++_totalTermFrequency;
    }
  }

  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
      String indexFile = _options._indexPrefix + "/corpus_invertedDoconly.idx";
      System.out.println("Load index from: " + indexFile);

      ObjectInputStream reader =
          new ObjectInputStream(new FileInputStream(indexFile));
      IndexerInvertedDoconly loaded = (IndexerInvertedDoconly) reader.readObject();

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
  }

  @Override
  public Document getDoc(int docid) {
    SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
    return null;
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}
   */
  @Override
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
