package edu.nyu.cs.cs2580;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.File;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import java.lang.ref.WeakReference;
/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedOccurrence extends IndexerInverted implements Serializable{
  private int tmpFileCount = 0;
  private static final long serialVersionUID = 1057111905740085030L;

    // _termToOccus[0] info for term[0]
    // _termToOccus[0][0] info for term[0] at a doc
    // _termToOccus[0][0][0] docid
    // _termToOccus[0][0][x] position
  private ArrayList<ArrayList<ArrayList<Integer> > > _termToOccus =
        new ArrayList<ArrayList<ArrayList<Integer> > > ();


  public IndexerInvertedOccurrence(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }


  @Override
  public void loadAdditional (BufferedReader reader) {
      System.out.println("to be implemented!");
  }
  @Override
  public void appendToFile(BufferedWriter out) {
      try {
      flushToFile();
      mergeTmps();
      out.write(Integer.toString(_termToOccus.size())+"\n");
      for (int i = 0; i<_termToOccus.size();i++) {
          out.write(Integer.toString(_termToOccus.get(i).size())+"\n");
          for (int j = 0; j<_termToOccus.get(i).size();j++) {
              out.write(Integer.toString(_termToOccus.get(i).get(j).size())+"\n");
              for (int p = 0; p<_termToOccus.get(i).get(j).size();p++)
                  out.write(Integer.toString(_termToOccus.get(i).get(j).get(p))+" ");
              out.newLine();
          }
          out.flush();
      }
      } catch (IOException e) {
      }
  }
  @Override
  public void removeStopwordsInfo(int idx) {
      _termToOccus.get(idx).clear();
      //      System.out.println("to be implemented!!!");
  }
  private void mergeTmps() throws IOException {
      System.out.println("merging.....");
      String fileName="";
      for (int i = 1; i<=tmpFileCount;i++) {
          System.out.println(i);
          fileName = _options._indexPrefix+"/"+Integer.toString(i) + "000s.tmp";
          FileReader filereader = new FileReader(fileName);
          BufferedReader bufferedreader = new BufferedReader(filereader);
          String line = bufferedreader.readLine();
          String[] poss;
          int outerSize = Integer.parseInt(line);
          int docSize;
          int posSize;
          for (int terms = 0; terms<outerSize;terms++) {
              line = bufferedreader.readLine();
              docSize = Integer.parseInt(line);
              for (int docs = 0; docs<docSize;docs ++) {
                  ArrayList<Integer> docInfo = new ArrayList<Integer>();
                  line = bufferedreader.readLine();
                  posSize = Integer.parseInt(line);
                  line = bufferedreader.readLine();
                  poss = line.split(" ");
                  for (int posI = 0; posI<posSize;posI++) {
                      docInfo.add(Integer.parseInt(poss[posI]));
                  }
                  _termToOccus.get(terms).add(docInfo);
              }
          }
          bufferedreader.close();
          gc();
      }
  }
  private void flushToFile() throws IOException{
      tmpFileCount++;
      String s = Integer.toString(tmpFileCount) + "000s.tmp";
      s = _options._indexPrefix +"/"+ s;
      FileWriter fstream = new FileWriter(s);
      BufferedWriter out = new BufferedWriter(fstream);

      out.write(Integer.toString(_termToOccus.size())+"\n");
      for (int i = 0; i<_termToOccus.size();i++) {
          out.write(Integer.toString(_termToOccus.get(i).size())+"\n");
          for (int j = 0; j<_termToOccus.get(i).size();j++) {
              out.write(Integer.toString(_termToOccus.get(i).get(j).size())+"\n");
              for (int p = 0; p<_termToOccus.get(i).get(j).size();p++)
                  out.write(Integer.toString(_termToOccus.get(i).get(j).get(p))+" ");
              out.newLine();
          }
          out.flush();
      }
      for (int i = 0; i<_termToOccus.size();i++) {
          _termToOccus.get(i).clear();
      }
      out.close();
      gc();
  }
  @Override
  public String getIndexFilePath() {
      return _options._indexPrefix + "/corpus_invertedOccurrence.idx";
  }
  @Override
  public void updateStatistics(ArrayList<Integer> tokens, Set<Integer> uniques,
                                  int did, int offset) {
    Integer token;
    for (int i = 0; i<tokens.size();i++) {
        if (tokens.get(i)>=0){
            uniques.add(tokens.get(i));
            token = tokens.get(i);
            ArrayList<ArrayList<Integer> > dop = _termToOccus.get(token);
            if (dop.size()==0 || dop.get(dop.size()-1).get(0)!=did) {
                //empty or last entry is not about did
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                tmp.add(did);
                tmp.add(offset+i);
                dop.add(tmp);
            }
            else {
                dop.get(dop.size()-1).add(offset+i);
            }
        }
        ++_totalTermFrequency;
    }
  }
  @Override
  // this function is called once a file and after all work, good place to
  // flush
  public void updateUniqueTerms (Set<Integer> uniqueTerms,int did) {
      try {
          if((did+1)%1000 == 0) {
              flushToFile();
          }
      } catch (Exception e) {
      }
  }
  @Override
  public void addToken(String token, ArrayList<Integer> tokens) {
      int idx = -1;
      if (_dictionary.containsKey(token)) {
        idx = _dictionary.get(token);
      } else {
        idx = _termNum++;
        //        _terms.add(token);
        _dictionary.put(token, idx);
        _termToOccus.add(idx,new ArrayList<ArrayList<Integer> >());
        //        _termDocFrequency.put(idx, 0);
      }
      tokens.add(idx);
  }

  @Override
  public Document getDoc(int docid) {
    SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
    return null;
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}.
   */
  public Document nextDoc(Query query, int docid) {
      
    return null;
  }

  public int corpusDocFrequencyByTerm(String term) {
      if (!_dictionary.containsKey(term))
          return 0;
      Integer idx = _dictionary.get(term);
      if (idx<0) return 0;
      return _termToOccus.get(idx).size();
  }

  public int corpusTermFrequency(String term) {
      if (!_dictionary.containsKey(term))
          return 0;
      Integer idx = _dictionary.get(term);
      if (idx<0) return 0;
      ArrayList<ArrayList<Integer> > dops = _termToOccus.get(idx);
      int ret = 0;
      for (ArrayList<Integer> dop : dops)
          ret+=dop.size()-1;
      return ret;
  }

  public int documentTermFrequency(String term, String url) {
    SearchEngine.Check(false, "Not implemented!");
    return 0;
  }
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
          //          it.remove(); // avoids a ConcurrentModificationException
      }
      /*      for (int i = 0; i<_terms.size();i++) {
          System.out.println(_terms.get(i)+
                           ":"+Integer.toString(corpusTermFrequency(_terms.get(i)))+
                           ":"+Integer.toString(corpusDocFrequencyByTerm(_terms.get(i))));*/
          /*          ArrayList<DocOccPair> dop = _termToOccus.get(i);
          for (int j = 0;j<dop.size();j++) {
              System.out.println(Integer.toString(i)+" "+Integer.toString(dop.get(j).getOcc())+" "+Integer.toString(dop.get(j).getDid()));
          }
          System.out.println("===");*/
  }

    public static void gc() {
        Object obj = new Object();
        WeakReference ref = new WeakReference<Object>(obj);
        obj = null;
        while(ref.get() != null) {
            System.gc();
        }
    }

}
