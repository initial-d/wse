package edu.nyu.cs.cs2580;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;
import java.io.IOException;
import java.io.BufferedReader;
import java.nio.ByteBuffer;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
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
public class IndexerInvertedCompressed extends IndexerInverted implements Serializable{
  private int loadedTermCount = 0;
  private int tmpFileCount = 0;
  private int currentLoaded = -1;
  private static final int seperateNum = 100;
  private static final long serialVersionUID = 1057111905740085030L;
    // _termToOccus[0] info for term[0]
    // _termToOccus[0][0] info for term[0] at a doc
    // _termToOccus[0][0][0] docid
    // _termToOccus[0][0][x] position
  private ArrayList<ArrayList<ArrayList<Integer> > > _termToOccus =
        new ArrayList<ArrayList<ArrayList<Integer> > > ();
  private ArrayList<Integer> _termDocFreq = new ArrayList<Integer>();
  private ArrayList<Integer> _termCorFreq = new ArrayList<Integer>();

  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }


  @Override
  public void loadAdditional (BufferedReader reader) {
      try {
      System.out.println("load additional!");
      String [] tokens;
      String line = reader.readLine();
      int size = Integer.parseInt(line);
      line = reader.readLine();
      tokens = line.split(" ");
      System.out.println("term doc freq loading!");
      for (int i = 0; i<size; i++) {
          _termDocFreq.add(Integer.parseInt(tokens[i]));
      }
      System.out.println("term doc freq loaded!");
      line = reader.readLine();
      size = Integer.parseInt(line);
      line = reader.readLine();
      tokens = line.split(" ");
      System.out.println("term corpus freq loading!");
      for (int i = 0; i<size; i++) {
          _termCorFreq.add(Integer.parseInt(tokens[i]));
      }
      System.out.println("term corpus freq loaded!");
      line = reader.readLine();
      tmpFileCount = Integer.parseInt(line);

      for (int i = 0; i<size;i++) {
          _termToOccus.add(new ArrayList<ArrayList<Integer> >());
      }
      System.out.println(tmpFileCount);
      loadedTermCount = 0;
      } catch (IOException e) {
      }
  }

  @Override
  public void appendToFile(BufferedWriter out) {
      try {
      flushToFile();
      mergeTmps();
      out.write(Integer.toString(_termDocFreq.size())+"\n");
      for (int i = 0; i<_termDocFreq.size();i++)
          out.write(Integer.toString(_termDocFreq.get(i))+" ");
      out.newLine();
      out.write(Integer.toString(_termCorFreq.size())+"\n");
      for (int i = 0; i<_termCorFreq.size();i++)
          out.write(Integer.toString(_termCorFreq.get(i))+" ");
      out.newLine();
      out.write(Integer.toString(tmpFileCount)+"\n");
      } catch (IOException e) {
      }
  }
  @Override
  public void removeStopwordsInfo(int idx) {
      _termToOccus.get(idx).clear();
      //      System.out.println("to be implemented!!!");
  }
  private void loadTerms (Vector<Integer> idxs) throws IOException{
      for (int i = 0; i<idxs.size();i++) {
          //      System.out.println("load term i");
          //          System.out.println(i);
          //          System.out.println(_termToOccus.get(idxs.get(i)).size());
          if (_termToOccus.get(idxs.get(i)).size()==0) {
              //  System.out.println("ininin");
              //              System.out.println("size term:"+idxs.get(i)+" "+_termToOccus.get(idxs.get(i)).size());
              loadTermI(idxs.get(i));
          }
      }
  }

    private void readDocsAndPosFromFile1(BufferedReader reader,
                                        ArrayList<ArrayList<Integer> > list)
        throws IOException {
        //        System.out.println("inside read");
        /*        String t = reader.readLine();
        if (t==null) return ;
        String line="";
        while (!t.equals("xxx")) {
            if (line.length()!=0)
                line = line+"\n";
            line = line+t;
            t = reader.readLine();
            if (t==null) break;;
        }
        if (line == "") return;*/
        String line = getLine(reader);

        vByte vb = new vByte();
        System.out.println(line);
        vb.loadFromString(line);
        ArrayList<Integer> flattened = vb.getInts();
        for (int i = 0; i<flattened.size();i++)
            System.out.print(flattened.get(i)+" ");
        System.out.println();
        System.out.println("xxxx");
        int index = 0;
        int s1 = flattened.get(0);
        index++;
        int s2=0;
        for (int i = 0; i<s1; i++) {
            ArrayList<Integer> docInfo = new ArrayList<Integer>();
            s2 = flattened.get(index);
            index++;
            for (int j = 0; j<s2; j++) {
                System.out.print(flattened.get(index)+" ");
                if (j>3 && flattened.get(index)<flattened.get(index-1)) {
                    docInfo.add(flattened.get(index-1));
                } else
                    docInfo.add(flattened.get(index));
                index ++;
                if (index>=flattened.size()) {
                    list.add(docInfo);
                    return;
                }
            }
            list.add(docInfo);
        }
    }
    private void loadTermI1 (int i) throws IOException{
        //      System.out.println("load term:"+i);
        //      clearMemory();
        String fileName = getDir() +
            Integer.toString(i%seperateNum+1) + "terms.idx";

        File file = new File(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));

        for (int j = i%seperateNum; j< i; j+=seperateNum ) {
            getLine(reader);
        }

        ArrayList<ArrayList<Integer> > list = new ArrayList<ArrayList<Integer> >();
        readDocsAndPosFromFile1(reader,list);
        _termToOccus.set(i,list);
        loadedTermCount++;
        reader.close();
        gc();
    }
  private void loadTermI (int i) throws IOException{
      //      System.out.println("load term:"+i);
      //      clearMemory();
      String fileName = getDir() + 
          Integer.toString(i%seperateNum+1) + "terms.idx";
      //      System.out.println("load term from:"+fileName);
      BufferedReader reader = new BufferedReader ( new FileReader(fileName));
      for (int j = i%seperateNum; j< i; j+=seperateNum )
          reader.readLine();

      ArrayList<ArrayList<Integer> > list = new ArrayList<ArrayList<Integer> >();
      readDocsAndPosFromFile(reader,list);
      _termToOccus.set(i,list);
      System.out.println("Set "+i+":"+_termToOccus.get(i).size());
      //      list = _termToOccus.get(i);
      loadedTermCount++;
      reader.close();
      gc();
  }
  private void writeDocsAndPosToFile (BufferedWriter writer,
                                      ArrayList<ArrayList<Integer> >list) 
    throws IOException{
      writer.write(Integer.toString(list.size())+" ");
      for (int i = 0; i<list.size();i++) {
          writer.write(Integer.toString(list.get(i).size()) + " ");
          for (int j = 0; j<list.get(i).size(); j++) {
              writer.write(Integer.toString(list.get(i).get(j)) + " ");
          }
      }
      writer.newLine();
  }
  private String getLine(BufferedReader reader) {
      String ret = "";
      short c = 0;
      System.out.println("get line");
      while (true){
          try {
              //              System.out.println(ret);
              c = (short)reader.read();
              System.out.println(c);
              //              byte[] b = intToByteArray(c);
              byte[] b = ByteBuffer.allocate(4).putInt(c).array();
              String s = new String(b,"iso-8859-1");
              //              System.out.println(b[0]);
              //              System.out.println(b[1]);
              //              System.out.println(b[2]);
              //              System.out.println(b[3]);
              //              System.out.println(s);
              //System.out.println(s);
              ret = ret+s;
              //              System.out.println(ret);
          } catch (IOException e) {
          }
          if (ret.length()>=5)
          if (ret.substring(ret.length()-4,ret.length()).equals("\u1234\u1234\u1234\u1234"))
              break;
      }
      //      System.out.println(ret);
      ret = ret.substring(0,ret.length()-4);
      return ret;
  }

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
        (byte)(value >>> 24),
        (byte)(value >>> 16),
        (byte)(value >>> 8),
        (byte)value};
    }
  private void readDocsAndPosFromFile(BufferedReader reader,
                                      ArrayList<ArrayList<Integer> > list) 
    throws IOException {
      String line = reader.readLine();
      //      System.out.println(line);
      if (line == null) return;
      Scanner s = new Scanner(line);
      int s1 = s.nextInt ();
      int s2;
      for (int i = 0; i<s1; i++) {
          ArrayList<Integer> docInfo = new ArrayList<Integer>();
          s2 = s.nextInt();
          for (int j = 0; j<s2; j++) {
              int x = s.nextInt();
              docInfo.add(x);
          }
          list.add(docInfo);
      }
  }

    private void writeDocsAndPosToFile1 (BufferedWriter writer,
                                        ArrayList<ArrayList<Integer> >list)
        throws IOException{
        ArrayList<Integer> flattened = new ArrayList<Integer> ();
        vByte vb = new vByte();
        //        vb.push(-1);
        //        flattened.add(list.size());
        vb.push(list.size());
        for (int i = 0;i<list.size();i++) {
            vb.push(list.get(i).size());
            for (int j = 0; j<list.get(i).size();j++) {
                vb.push(list.get(i).get(j));
            }
        }
        writer.write(vb.toString());
        writer.newLine();
        writer.write("\u1234\u1234\u1234\u1234");
    }

  private void mergeTmps() throws IOException {
      System.out.println("merging.....");
      Vector<BufferedReader> reader = new Vector<BufferedReader> ();
      for (int i = 1; i<=tmpFileCount;i++) {
          String fileName = getDir()+
              Integer.toString(i) +"000s.tmp";
          reader.add(new BufferedReader(new FileReader(fileName)));
          reader.get(reader.size()-1).readLine();
      }
      Vector<BufferedWriter> writers = new Vector<BufferedWriter> ();
      for (int i = 0; i<seperateNum; i++) {
          String fileName = getDir() +
              Integer.toString(i+1) + "terms.idx";
          writers.add(new BufferedWriter ( new FileWriter(fileName)));
          // BufferedWriter out = new BufferedWriter
          //   (new OutputStreamWriter(new FileOutputStream(fileName),"ISO-8859-1"));
          //writers.add(out);
      }
      for (int i = 0; i<_termToOccus.size();i++) {
          if (i%1000 == 0) System.out.println(i+"terms");
          ArrayList<ArrayList<Integer> > tto = new ArrayList<ArrayList<Integer> > ();
          for (int j = 0; j<tmpFileCount;j++)
              readDocsAndPosFromFile(reader.get(j),tto);
          //writeDocsAndPosToFile1(writers.get(i%seperateNum),tto);
          writeDocsAndPosToFile(writers.get(i%seperateNum),tto);
      }

      for (int i = 0; i<seperateNum;i++)
          writers.get(i).close();
      for (int i = 0; i<tmpFileCount;i++)
          reader.get(i).close();
      for (int i = 1; i<=tmpFileCount;i++) {
          String fileName = getDir()+
              Integer.toString(i) +"000s.tmp";
          File f = new File(fileName);
          f.delete();
      }
  }
  private void clearMemory() {
      for (int i = 0; i<_termToOccus.size();i++) {
          _termToOccus.get(i).clear();
      }
      loadedTermCount = 0;
  }
  private void flushToFile() throws IOException{
      tmpFileCount++;
      String s = Integer.toString(tmpFileCount) + "000s.tmp";
      s =  getDir()+ s;
      FileWriter fstream = new FileWriter(s);
      BufferedWriter out = new BufferedWriter(fstream);

      out.write(Integer.toString(_termToOccus.size())+"\n");
      for (int i = 0; i<_termToOccus.size();i++) {
          writeDocsAndPosToFile(out,_termToOccus.get(i));
          out.flush();
          /*          out.write(Integer.toString(_termToOccus.get(i).size())+"\n");
          for (int j = 0; j<_termToOccus.get(i).size();j++) {
              out.write(Integer.toString(_termToOccus.get(i).get(j).size())+"\n");
              for (int p = 0; p<_termToOccus.get(i).get(j).size();p++)
                  out.write(Integer.toString(_termToOccus.get(i).get(j).get(p))+" ");
                  out.newLine();*/

      }
      out.close();
      clearMemory();
      gc();
  }
  public String getDir() {
      return _options._indexPrefix+"/index_compressed/";
  }
  @Override
  public String getIndexFilePath() {
      return _options._indexPrefix + "/index_compressed/corpus_invertedCompressed.idx";
  }
  @Override
  public void updateStatistics(ArrayList<Integer> tokens, Set<Integer> uniques,
                                  int did, int offset) {
    Integer token;
    for (int i = 0; i<tokens.size();i++) {
        if (tokens.get(i)>=0){
            uniques.add(tokens.get(i));
            token = tokens.get(i);
            _termCorFreq.set(token,_termCorFreq.get(token)+1);
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
      for (Integer dix:uniqueTerms) {
          _termDocFreq.set(dix,_termDocFreq.get(dix)+1);
      }
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
          idx = getIndex(token);
      } else {
        idx = _termNum++;
        //        _terms.add(token);
        _dictionary.put(token, idx);
        _termToOccus.add(idx,new ArrayList<ArrayList<Integer> >());
        _termDocFreq.add(idx, 0);
        _termCorFreq.add(idx, 0);
      }
      tokens.add(idx);
  }

  @Override
  public Document getDoc(int docid) {
      return _documents.get(docid);
      //    SearchEngine.Check(false, "Do NOT change, not used for this Indexer!");
      //    return null;
  }

  /**
   * In HW2, you should be using {@link DocumentIndexed}.
   */
  private int getNextDoc (Integer idx, int did) {
      // binary search on term[idx]'s first document whose document >= did
      ArrayList<ArrayList<Integer> > ttd = _termToOccus.get(idx);
      int head = 0;
      int tail = ttd.size()-1;
      int mid;
      while (head<=tail) {
          //          System.out.println("head+tail:"+head+"+"+tail);
          if (tail - head<5) {
              for (int i = head;i<=tail;i++)
                  if (ttd.get(i).get(0)>=did)
                      return ttd.get(i).get(0);
              return -1;
          }
          mid = (head+tail)/2;
          if (ttd.get(mid).get(0)==did) {
              return did;
          }

          if (ttd.get(mid).get(0)<did) {
              head = mid+1;
          } else {
              tail = mid;
          }
      }
      return -1;
  }

  private Vector<Vector<Integer> > convertPhrases (Vector<String[] > phrases) {
      Vector<Vector<Integer> > ret = new Vector<Vector<Integer> >();
      for (int i = 0; i<phrases.size();i++) {
          Vector<Integer> tmp = new Vector<Integer>();
          for (int j=0; j<phrases.get(i).length;j++) {
              tmp.add(getIndex(phrases.get(i)[j]));
          }
          ret.add(tmp);
      }
      return ret;
  }
  private ArrayList<Integer> getTermDocInfo(int term, int did) {
      ArrayList<ArrayList<Integer> > ttd = _termToOccus.get(term);
      int head = 0;
      int tail = ttd.size()-1;
      int mid;
      while (head<=tail) {
          if (tail - head<5) {
              for (int i = head;i<=tail;i++)
                  if (ttd.get(i).get(0)==did)
                      return ttd.get(i);
              return null;
          }
          mid = (head+tail)/2;
          if (ttd.get(mid).get(0)==did) {
              return ttd.get(mid);
          }

          if (ttd.get(mid).get(0)<did) {
              head = mid+1;
          } else {
              tail = mid-1;
          }
      }
      return null;
  }
  public int docPhraseCount(Vector<Integer> phrase,int did) {
      int ret = 0;
      //      System.out.println("docPhraseCount:"+_documents.get(did).getTitle());
      Vector<ArrayList<Integer> > termDocInfo = new Vector<ArrayList<Integer> >();
      Vector<Integer> pointer = new Vector<Integer>();

      for (int i = 0; i<phrase.size();i++) {
          pointer.add(1);
          termDocInfo.add(getTermDocInfo(phrase.get(i),did));
          if (termDocInfo.get(i)==null)
              return 0;
          //          for (int j = 0; j<termDocInfo.get(i).size();j++)
          //  System.out.print(termDocInfo.get(i).get(j)+" ");
          //          System.out.println();
      }
      if (phrase.size()==1)
          return termDocInfo.get(0).size()-1;
      //      System.out.print("Start!");
      //      for (int i = 0; i<phrase.size();i++) {
      //          System.out.print(termDocInfo.get(i).get(pointer.get(i))+" ");
      //      }
      //            System.out.println();


      while (pointer.get(0)<termDocInfo.get(0).size()) {
          // check if continus
          //          System.out.println("ret"+ret);

          // update c
          for (int i = 1; i<phrase.size();i++) {
              //              System.out.println(i+"th term");
              int lastPos = termDocInfo.get(i-1).get(pointer.get(i-1));
              //              System.out.println("lastPos:"+lastPos);
              while (pointer.get(i)<termDocInfo.get(i).size() &&
                     termDocInfo.get(i).get(pointer.get(i))<=lastPos) {
                  pointer.set(i,pointer.get(i)+1);
              }
              //              System.out.println(pointer.get(i));
              if (pointer.get(i)>=termDocInfo.get(i).size())
                  return ret;
          }

          boolean r =true;
          for (int i = 1; i<phrase.size();i++) {
              if(termDocInfo.get(i).get(pointer.get(i))!=
                 termDocInfo.get(i-1).get(pointer.get(i-1))+1) {
                  r = false;
              }
          }
          if (r) ret++;

          pointer.set(0,pointer.get(0)+1);
          //          for (int i = 0; i<phrase.size();i++) {
          //              System.out.print(termDocInfo.get(i).get(pointer.get(i))+" ");
          //          }
          //          System.out.println();
      }
      return ret;
  }

  public Document nextDoc(Query query, int docid) {
      //      System.out.println("nextdoc");
      Vector<Integer> idxs = convertTermsToIdx(query.getTokens());
      Vector<Vector<Integer> > phrases = convertPhrases(query.getPhrases());

      if (loadedTermCount>1000) {
          clearMemory();
      }
      //      System.out.println("idxs:"+idxs.size());
      for (int i = 0; i<idxs.size();i++) {
          //          System.out.println("idxs:"+idxs.get(i));
          if (idxs.get(i)==null)
              return null;
      }
      try {
          loadTerms(idxs);
      } catch (Exception e) {
          System.out.println("cannot load!");
      }

      int did;
      int searchID = docid+1;
      int min = _documents.size();
      int max = -1;
      while (true) {
          while (min!=max&&min>=0) {
              min = _documents.size();
              max = -1;
              for (int i = 0; i<idxs.size();i++) {
                  did = getNextDoc(idxs.get(i),searchID);
                  if (did>max)
                      max = did;
                  if (did<min)
                      min = did;
              }
              searchID = max;
          }
          if (min == -1)
              return null;
          int c = 0;
          for (int i = 0; i <phrases.size();i++) {
              if (docPhraseCount(phrases.get(i),min)>0)
                  c++;
          }
          if (c==phrases.size())
              return _documents.get(min);
          searchID = min+1;
          min = _documents.size();
          max = -1;
      }
  }

  public int corpusDocFrequencyByTerm(String term) {
      if (!_dictionary.containsKey(term))
          return 0;
      Integer idx = getIndex(term);
      if (idx<0) return 0;
      return _termDocFreq.get(idx);
  }

  public int corpusTermFrequency(String term) {
      if (!_dictionary.containsKey(term))
          return 0;
      Integer idx = getIndex(term);
      if (idx<0) return 0;
      return _termCorFreq.get(idx);
  }

    public int documentTermFrequency(String term, String url) {
        for (int i = 0; i<_documents.size();i++)
            if (url.equals(_documents.get(i).getTitle()))
                return documentTermFrequency(term,_documents.get(i)._docid);
        return 0;
    }


  public int documentTermFrequency(String term, int did) {
      int dix = getIndex(term);
      for (int i = 0; i<_termToOccus.get(dix).size();i++) {
          if (_termToOccus.get(dix).get(i).get(0)==did)
              return _termToOccus.get(dix).size()-1;
      }
      return 0;
  }
    public int docPhraseCount(String[] phrase, int did) {
        Vector<Integer> idsx = new Vector<Integer>();
        for (String s:phrase) {
            idsx.add(getIndex(s));
        }
        return docPhraseCount(idsx,did);
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
      }
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
