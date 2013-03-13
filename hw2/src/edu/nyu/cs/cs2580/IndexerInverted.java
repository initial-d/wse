package edu.nyu.cs.cs2580;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public abstract class IndexerInverted extends Indexer implements Serializable {
    private static final long serialVersionUID = 967111905740085030L;
    protected Map<String, Integer> _dictionary = new HashMap<String, Integer>();
    protected ArrayList<Document> _documents = new ArrayList<Document>();
    protected int _termNum = 0;
    public IndexerInverted() {}
    public IndexerInverted(Options options) {
        super(options);
    }
    public abstract String getIndexFilePath();
    //give body or title vectors, update info
    public abstract  void updateStatistics(ArrayList<Integer> tokens,Set<Integer> uniques,int did,int offset) ;
    //give uniqueterms in the file, update info
    public abstract  void updateUniqueTerms(Set<Integer> uniqueTerms,int did);
    //handle a token just red from file
    public abstract  void addToken(String token,ArrayList<Integer> tokens);
    //output info for debug
    public abstract void output() ;
    public abstract void removeStopwordsInfo(int idx) ;
    public abstract void appendToFile(BufferedWriter out) ;
    public abstract void loadAdditional(BufferedReader out) ;
    private void writeToFile () {
        try{
            // Create file 
            FileWriter fstream = new FileWriter(getIndexFilePath());
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(Integer.toString(_numDocs)+" "+
                      Long.toString(_totalTermFrequency)+" "+
                      Integer.toString(_termNum)+"\n");
            out.write(Integer.toString(_documents.size())+"\n");
            for (Document doc : _documents) {
                out.write(((DocumentIndexed) doc).toString());
            }
            out.flush();
            out.write(Integer.toString(_dictionary.size())+"\n");
            Iterator it = _dictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                out.write((String)pairs.getKey() + " " + 
                          Integer.toString((Integer)pairs.getValue())+"\n");
            }
            out.flush();
            appendToFile(out);
            //Close the output stream
            out.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }
    private void filterStopwords(String term) {
        System.out.println("rm stop word:"+term);
        int idx = _dictionary.get(term);
        removeStopwordsInfo(idx);
        if (idx == 0)
            idx = -1;
        _dictionary.put(term,-idx);
    }
    private void detectStopWords () {
        Iterator it = _dictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            String term= (String)pairs.getKey();
            int ctf = corpusTermFrequency(term);
            int cdf = corpusDocFrequencyByTerm(term);
            if (((double)cdf/(double)_numDocs)>0.9) {
                filterStopwords(term);
            }
        }
    }

    @Override
    public void constructIndex () throws IOException {
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
                           Long.toString(_totalTermFrequency) + " terms, @"+
                           Integer.toString(_termNum)+" unique terms.");

        String indexFile = getIndexFilePath();

        System.out.println("Store index to: " + indexFile);
        /*        ObjectOutputStream writer =
            new ObjectOutputStream(new FileOutputStream(indexFile));
        System.out.println("here?");
        writer.writeObject(this);
        writer.close();*/
        writeToFile();
        //        output();
    }

    private void handleFile(String fileName) throws IOException{
        //      String body = readFile(_options._corpusPrefix+"/"+fileName);
      //      System.out.println(fileName);
      ArrayList<Integer> titleTokens = new ArrayList<Integer>();
      readTermArrayListV2Title(fileName, titleTokens);

      ArrayList<Integer> bodyTokens = new ArrayList<Integer>();
      readTermArrayListV2Body(_options._corpusPrefix+"/"+fileName, bodyTokens);

      DocumentIndexed doc = new DocumentIndexed (_documents.size(), this);
      doc.setTitle(fileName);
      doc.setSize(titleTokens.size()+bodyTokens.size());
      //      doc.setNumViews(numViews);
      //    doc.setTitleTokens(titleTokens);
      //    doc.setBodyTokens(bodyTokens);
      _documents.add(doc);
      ++_numDocs;

      Set<Integer> uniqueTerms = new HashSet<Integer>();
      int did = _documents.size()-1;
      updateStatistics(titleTokens, uniqueTerms,did,0);
      updateStatistics(bodyTokens, uniqueTerms,did,titleTokens.size());
      if (_documents.size()%1000==0 ) {
          //          detectStopWords();
          System.out.println(Integer.toString(_documents.size()/1000)+"000files");
      }
      this.updateUniqueTerms(uniqueTerms,did);
    }

  private String readFile( String file ) throws IOException {
      BufferedReader reader = new BufferedReader( new FileReader (file));
      String         line = null;
      StringBuilder  stringBuilder = new StringBuilder();
      String         ls = System.getProperty("line.separator");
      while( ( line = reader.readLine() ) != null ) {
          stringBuilder.append( line );
          stringBuilder.append( ls );
      }
      return stringBuilder.toString();
  }

  private void processDocument(String content) {
    Scanner s = new Scanner(content).useDelimiter("\t");

    String title = s.next();
    ArrayList<Integer> titleTokens = new ArrayList<Integer>();
    readTermArrayList(title, titleTokens);
    String body = s.next();
    ArrayList<Integer> bodyTokens = new ArrayList<Integer>();
    readTermArrayList(body, bodyTokens);

    int numViews = Integer.parseInt(s.next());
    s.close();

    DocumentIndexed doc = new DocumentIndexed (_documents.size(), this);
    doc.setTitle(title);
    doc.setNumViews(numViews);
    doc.setSize(titleTokens.size()+bodyTokens.size());
    System.out.println("Doc Size"+doc.getSize());
    //    doc.setTitleTokens(titleTokens);
    //    doc.setBodyTokens(bodyTokens);
    _documents.add(doc);
    ++_numDocs;

    Set<Integer> uniqueTerms = new HashSet<Integer>();
    int did = _documents.size()-1;
    this.updateStatistics(titleTokens, uniqueTerms,did,0);
    this.updateStatistics(bodyTokens, uniqueTerms,did,0);

    this.updateUniqueTerms(uniqueTerms,did);
  }

  private void readTermArrayListV2Title(String content, ArrayList<Integer> tokens) {
      String spl = "[^a-zA-Z0-9]";
      String[] tmp = content.split(spl);
      for (String s:tmp) 
          if (!s.equals("")){
              //  System.out.print(HTMLParser.stemm(s));
          addToken(HTMLParser.stemm(s),tokens);
      }
      //      System.out.println();
  }
  private void readTermArrayListV2Body(String path, ArrayList<Integer> tokens) {
      HTMLParser.parse(path,this,tokens);
  }

  private void readTermArrayList(String content, ArrayList<Integer> tokens) {
    Scanner s = new Scanner(content);  // Uses white space by default.
    while (s.hasNext()) {
      String token = s.next();
      this.addToken(token,tokens);
    }
    return;
  }
    //    @Override 
  public void loadIndex() {
      try {
      FileReader filereader = new FileReader(getIndexFilePath());
      BufferedReader bufferedreader = new BufferedReader(filereader);
      String line = bufferedreader.readLine();
      String [] firstThree = line.split(" ");
      _numDocs = Integer.parseInt(firstThree[0]);
      _totalTermFrequency = Long.parseLong(firstThree[1]);
      _termNum = Integer.parseInt(firstThree[2]);
      //      System.out.println(_numDocs);
      //      System.out.println(_totalTermFrequency);
      //      System.out.println(_termNum);

      line = bufferedreader.readLine();
      int dsize = Integer.parseInt(line);
      String title,url;
      int docid,numview;
      Float pagerank;
      for (int i = 0; i<dsize;i++) {
          title = bufferedreader.readLine();
          url = bufferedreader.readLine();
          line = bufferedreader.readLine();
          firstThree = line.split(" ");
          docid = Integer.parseInt(firstThree[0]);
          pagerank = new Float(firstThree[1]);
          numview = Integer.parseInt(firstThree[2]);
          int size = Integer.parseInt(firstThree[3]);
          DocumentIndexed doc = new DocumentIndexed(docid,this);
          doc.setTitle(title);
          doc.setUrl(url);
          doc.setPageRank(pagerank);
          doc.setNumViews(numview);
          doc.setSize(size);
          _documents.add(doc);
          //          System.out.println(doc.toString());
      }
      System.out.println("document loaded!");
      line = bufferedreader.readLine();
      dsize = Integer.parseInt(line);
      for (int i = 0; i<dsize;i++) {
          line = bufferedreader.readLine();
          firstThree= line.split(" ");
          _dictionary.put(firstThree[0],Integer.parseInt(firstThree[1]));
      }
      System.out.println("dictionary loaded!");
      /*      Iterator it = _dictionary.entrySet().iterator();
      while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry)it.next();
          System.out.println((String)pairs.getKey() + " " + 
                    Integer.toString((Integer)pairs.getValue()));
                    }*/
      loadAdditional(bufferedreader);
      bufferedreader.close();
      } catch (IOException e) {
          System.out.println("error");
      }
      //      output();
      //      throw new ClassNotFoundException("this should be override");
  }
  public int documentTermFrequency(String s,String s1) {
      //      throw new ClassNotFoundException("this should be override");
      return 0;
  }
  public int corpusTermFrequency(String s) {
      //      throw new ClassNotFoundException("this should be override");
      return 0;
  }
  public int corpusDocFrequencyByTerm(String s) {
      //      throw new ClassNotFoundException("this should be override");
      return 0;
  }
  public int corpusDocFrequencyByTerm(Query q, int i) {
      //      throw new ClassNotFoundException("this should be override");
      return 0;
  }
    public int documentTermFrequency (String term, int did) {
        return 0;
    }
    public int docPhraseCount(String[] phrase, int did) {
        return 0;
    }
  public Document nextDoc(Query query, int docid) {
      //      throw new ClassNotFoundException("this should be override");
      return null;
  }
   public Document getDoc( int docid) {
       //       throw new ClassNotFoundException("this should be override");
       return null;
  }
  public Vector<Integer> convertTermsToIdx (Vector<String> terms) {

      Vector<Integer> ret = new Vector<Integer>();
      for (String term: terms) {
          ret.add(_dictionary.get(HTMLParser.stemm(term)));
          //          System.out.println(term+" "+ret.get(ret.size()-1));
      }
      return ret;
  }
}