
package edu.nyu.cs.cs2580;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
//import java.util.Vector;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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



    //  private ArrayList<Integer> _termCorpusFrequency = new ArrayList<Integer>();
  private ArrayList<ArrayList<Integer> > _termToDocs =
      new ArrayList<ArrayList<Integer> > ();



  public IndexerInvertedDoconly(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }
  @Override
  public void loadAdditional (BufferedReader reader) {
      try {
      String line = reader.readLine();
      int outSize = Integer.parseInt(line);
      int innerSize;
      String [] tokens;
      for (int i = 0; i<outSize; i++) {
          ArrayList<Integer> list = new ArrayList<Integer>();
          line = reader.readLine();
          innerSize = Integer.parseInt(line);
          line = reader.readLine();
          tokens = line.split(" ");
          for (int j = 0; j<innerSize;j++)
              list.add(Integer.parseInt(tokens[j]));
          _termToDocs.add(list);
      }
      } catch (IOException e) {
      }
      /*      for (int i = 0; i<_termToDocs.size();i++) {
          System.out.println(_termToDocs.get(i).size());
          for (int j = 0; j<_termToDocs.get(i).size();j++)
              System.out.print(Integer.toString(_termToDocs.get(i).get(j))+" ");
          System.out.println();
          }*/
  }
  @Override
  public void appendToFile(BufferedWriter out) {
      try {
      out.write(Integer.toString(_termToDocs.size())+"\n");
      for (int i = 0; i<_termToDocs.size();i++) {
          out.write(Integer.toString(_termToDocs.get(i).size())+"\n");
          for (int j = 0; j<_termToDocs.get(i).size();j++) {
              out.write(Integer.toString(_termToDocs.get(i).get(j))+" ");
          }
          out.newLine();
          out.flush();
      }
      } catch (IOException e) {
      }
  }
  @Override 
  public void removeStopwordsInfo(int idx) {
      _termToDocs.get(idx).clear();
  }
  @Override
  public String getIndexFilePath () {
      return _options._indexPrefix + "/corpus_invertedDoconly.idx";
  }
  @Override
  public void updateStatistics(ArrayList<Integer> tokens, Set<Integer> uniques,int did,int offset) {
    for (int idx : tokens) {
        if (idx>=0) {
            uniques.add(idx);
            _termToDocs.get(idx).set(0,_termToDocs.get(idx).get(0)+1);
        }
      //      _termCorpusFrequency.set(idx, _termCorpusFrequency.get(idx) + 1);
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
  public void addToken(String token,ArrayList<Integer> tokens) {
      int idx = -1;
      if (_dictionary.containsKey(token)) {
          idx = _dictionary.get(token);
      } else {
          idx = _termNum++;
          //          _terms.add(token);
          _dictionary.put(token, idx);
          //  _termCorpusFrequency.add(idx, 0);
          _termToDocs.add(idx, new ArrayList<Integer>());
          _termToDocs.get(idx).add(0,0);
      }
      tokens.add(idx);
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
    if (idx<0) return 0;
    return _termToDocs.get(idx).size()-1;
  }

  @Override
  public int corpusTermFrequency(String term) {
    if (!_dictionary.containsKey(term))
        return 0;
    Integer idx = _dictionary.get(term);
    if (idx <0) return 0;
    return _termToDocs.get(idx).get(0);
  }

  @Override
  public int documentTermFrequency(String term, String url) {
    SearchEngine.Check(false, "Not implemented!");
    return 0;
  }

  @Override
  public void output() {
      System.out.println("_numDocs="+Integer.toString(_numDocs));
      System.out.println("_totalTermFrequency="+Long.toString(_totalTermFrequency));
      Iterator it = _dictionary.entrySet().iterator();
      while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry)it.next();
          String term= (String)pairs.getKey();
          System.out.println(pairs.getKey() + ":" + 
                             Integer.toString(corpusTermFrequency(term))+":"+
                             Integer.toString(corpusDocFrequencyByTerm(term)));
          //   it.remove(); // avoids a ConcurrentModificationException
      }
  }
}

