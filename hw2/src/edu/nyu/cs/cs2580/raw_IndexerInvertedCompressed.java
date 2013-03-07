package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedCompressed extends Indexer implements Serializable{
  

	private static final long serialVersionUID = 4574090044401455221L;
	  transient private Map<String, Integer> _dictionary = new HashMap<String, Integer>();
	  transient  private Vector<String> _terms = new Vector<String>();
	  transient private Map<Integer, Integer> _termDocFrequency = new HashMap<Integer,Integer>();
	  transient private Map<Integer, Vector<DocOccPair> > _termToOccus =
	        new HashMap<Integer, Vector<DocOccPair> > ();
	  private Vector<Document> _documents = new Vector<Document>();
	  private Map<String, ArrayList<Byte>> _termOccCompressed = new HashMap<String,ArrayList<Byte>>();
	  
public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
  }

private void compress(){
	for(String s:_terms){
		Integer idx=_dictionary.get(s);
		Vector<DocOccPair> docpos=_termToOccus.get(idx);
		int docnow=docpos.get(0).getDid();
		int locnow=0;
		int timenow=1;//postion of the counter
		LinkedList<Integer> al=new LinkedList<Integer>();
		for(int i=0;i<docpos.size();i++){
			al.add(docnow);
			al.add(1);
			if(i+1==docpos.size()){//all finished
				al.add(docpos.get(i).getOcc()-locnow);
			}
			else if(docpos.get(i+1).getDid()!=docnow){//next doc, all need update
				al.add(docpos.get(i).getOcc()-locnow);
				docnow=docpos.get(i+1).getDid()-docnow;
				locnow=0;
				timenow=al.size()+1;
				al.add(docnow);
				al.add(1);
			}
			else{
				al.add(docpos.get(i).getOcc()-locnow);
				locnow=docpos.get(i).getOcc();
				al.set(timenow,al.get(timenow)+1);
			}
		}
		vByte vb = new vByte(al);
		ArrayList<Byte> bal=vb.getBytes();
		_termOccCompressed.put(s, bal);
	}
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

    String indexFile = _options._indexPrefix + "/corpus_invertedCompressed" +
    		".idx";
    System.out.println("Store index to: " + indexFile);
    ObjectOutputStream writer =
        new ObjectOutputStream(new FileOutputStream(indexFile));
    //output();
    try {
        writer.writeObject(this);
        writer.close();
    } catch (Exception e) {
    }
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

    _documents.add(doc);
    ++_numDocs;

    int did = _documents.size()-1;
    Set<Integer> uniqueTerms = new HashSet<Integer>();
    updateStatistics(titleTokens, uniqueTerms,did,0);
    updateStatistics(bodyTokens, uniqueTerms,did,titleTokens.size());
    updateUniqueTerms(uniqueTerms);
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
        _termToOccus.put(idx,new Vector<DocOccPair>());
        _termDocFrequency.put(idx, 0);
      }
      tokens.add(idx);
    }
    return;
  }

  private void updateUniqueTerms (Set<Integer> uniqueTerms) {
    for (Integer idx : uniqueTerms) {
        _termDocFrequency.put(idx, _termDocFrequency.get(idx) + 1);
    }
  }

  private void updateStatistics(Vector<Integer> tokens, Set<Integer> uniques,
                                  int did, int offset) {
    for (int i = 0; i<tokens.size();i++) {
        uniques.add(tokens.get(i));
        _termToOccus.get(tokens.get(i)).add(new DocOccPair(did,offset+i));
        ++_totalTermFrequency;
    }
  }
  
  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
      String indexFile = _options._indexPrefix + "/corpus_invertedCompressed.idx";
      System.out.println("Load index from: " + indexFile);

      ObjectInputStream reader =
          new ObjectInputStream(new FileInputStream(indexFile));
      IndexerInvertedCompressed loaded = 
          (IndexerInvertedCompressed) reader.readObject();

      this._termOccCompressed=loaded._termOccCompressed;
      this._documents = loaded._documents;//Seems index won't store that
      this._numDocs = _documents.size();
      //May change the idx of every word
      this._totalTermFrequency=0;
      this._termDocFrequency.clear();
      this._dictionary.clear();
      this._terms.clear();
      
      Iterator<Entry<String, ArrayList<Byte>>> it=_termOccCompressed.entrySet().iterator();
      while(it.hasNext()){
    	  Map.Entry<String, ArrayList<Byte>> entry=(Map.Entry<String, ArrayList<Byte>>) it.next();
    	  String key=entry.getKey();
    	  ArrayList<Byte> al=entry.getValue();
    	  this._terms.add(key);
    	  int idx=this._dictionary.size();
    	  this._dictionary.put(key, idx);
    	  vByte vb=new vByte(al);
    	  LinkedList<Integer> ll=vb.getInts();
    	  int termdoc=0;
    	  Vector<DocOccPair> dop=new Vector<DocOccPair>();
    	  while(ll.size()!=0){
    		  termdoc++;
    		  
    		  int ncount=ll.get(1);
    		  this._totalTermFrequency+=ncount;
    		  int group=ncount+2;
    		  
    		  if(ll.size()==group){
    			  //last group
    		  }
    		  else{
    			  ll.set(group,ll.get(0)+ll.get(group) );
    		  }
    		  int loc=0;
    		  for(int i=2;i<group;i++){
    			  loc+=ll.get(i);
    			  dop.add(new DocOccPair(ll.get(0),loc));
    		  }
    		  for(int i=0;i<group;i++){
    			  ll.remove(0);
    		  }
    	  }
    	  this._termDocFrequency.put(idx, termdoc);
    	  this._termToOccus.put(idx, dop);
      }
      
      reader.close();

      System.out.println(Integer.toString(_numDocs) + " documents loaded " +
                         "with " + Long.toString(_totalTermFrequency) + " terms!");
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
      return _termDocFrequency.get(idx);
  }

  @Override
  public int corpusTermFrequency(String term) {
      if (!_dictionary.containsKey(term))
          return 0;
      Integer idx = _dictionary.get(term);
      //return 0;
      return _termToOccus.get(idx).size();
  }

  /**
   * @CS2580: Implement this for bonus points.
   */
  @Override
  public int documentTermFrequency(String term, String url) {
    return 0;
  }
}
