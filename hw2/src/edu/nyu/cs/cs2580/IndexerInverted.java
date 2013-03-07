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

public abstract class IndexerInverted extends Indexer implements Serializable {
    private static final long serialVersionUID = 967111905740085030L;
    protected Map<String, Integer> _dictionary = new HashMap<String, Integer>();
    protected Vector<Document> _documents = new Vector<Document>();
    protected Vector<String> _terms = new Vector<String>();

    public IndexerInverted() {}
    public IndexerInverted(Options options) {
        super(options);
    }
    public abstract String getIndexFilePath();
    //give body or title vectors, update info
    public abstract  void updateStatistics(Vector<Integer> tokens,Set<Integer> uniques,int did,int offset) ;
    //give uniqueterms in the file, update info
    public abstract  void updateUniqueTerms(Set<Integer> uniqueTerms,int did);
    //handle a token just red from file
    public abstract  void addToken(String token,Vector<Integer> tokens);
    //output info for debug
    public abstract void output() ;

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
                           Integer.toString(_terms.size())+" unique terms.");

        String indexFile = getIndexFilePath();

        System.out.println("Store index to: " + indexFile);
        ObjectOutputStream writer =
            new ObjectOutputStream(new FileOutputStream(indexFile));
        System.out.println("here?");
        writer.writeObject(this);
        writer.close();
        output();
    }

    private void handleFile(String fileName) throws IOException{
        //      String body = readFile(_options._corpusPrefix+"/"+fileName);
      //      System.out.println(fileName);
      Vector<Integer> titleTokens = new Vector<Integer>();
      readTermVectorV2Title(fileName, titleTokens);

      Vector<Integer> bodyTokens = new Vector<Integer>();
      readTermVectorV2Body(_options._corpusPrefix+"/"+fileName, bodyTokens);

      DocumentIndexed doc = new DocumentIndexed (_documents.size(), this);
      doc.setTitle(fileName);
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
    int did = _documents.size()-1;
    this.updateStatistics(titleTokens, uniqueTerms,did,0);
    this.updateStatistics(bodyTokens, uniqueTerms,did,0);

    this.updateUniqueTerms(uniqueTerms,did);
  }

  private void readTermVectorV2Title(String content, Vector<Integer> tokens) {
      String spl = "[^a-zA-Z0-9]";
      String[] tmp = content.split(spl);
      for (String s:tmp) 
          if (!s.equals("")){
              //  System.out.print(HTMLParser.stemm(s));
          addToken(HTMLParser.stemm(s),tokens);
      }
      //      System.out.println();
  }
  private void readTermVectorV2Body(String path, Vector<Integer> tokens) {
      HTMLParser.parse(path,this,tokens);
  }

  private void readTermVector(String content, Vector<Integer> tokens) {
    Scanner s = new Scanner(content);  // Uses white space by default.
    while (s.hasNext()) {
      String token = s.next();
      this.addToken(token,tokens);
    }
    return;
  }
    //    @Override 
  public void loadIndex() {
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
  public Document nextDoc(Query query, int docid) {
      //      throw new ClassNotFoundException("this should be override");
      return null;
  }
   public Document getDoc( int docid) {
       //       throw new ClassNotFoundException("this should be override");
       return null;
  }

}