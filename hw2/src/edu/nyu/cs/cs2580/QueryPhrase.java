package edu.nyu.cs.cs2580;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
/**
 * @CS2580: implement this class for HW2 to handle phrase. If the raw query is
 * ["new york city"], the presence of the phrase "new york city" must be
 * recorded here and be used in indexing and ranking.
 */
public class QueryPhrase extends Query {
  public Vector<String[]>  _phrases = new Vector<String[] >();
    public Set<String> phraseSet = new HashSet<String>();
    public Vector<String> nToken = new Vector<String>();
  public QueryPhrase(String query) {
    super(query);
  }
  public Vector<String> getTokenNotInPhrase() {
      return nToken;
  }
  public Vector<String[]> getPhrases() {
      return _phrases;
  }
  @Override
  public void processQuery() {
    if (_query == null) {
      return;
    }
    System.out.println("Query:"+_query);
    _query = _query.replaceAll("%20"," ");
    _query = _query.replaceAll("%22","\"");
    System.out.println("process query?"+_query);
    String [] tokens = _query.split("[ \"]");
    System.out.println("Query:"+tokens.length);
    for (String token:tokens) 
        if (!token.equals("")){
            System.out.println("Query:"+token);
            _tokens.add(token);
        }
    tokens = _query.split("[\"]");
    for (int i = 1; i<tokens.length;i=i+2) {
        String[] phrase = tokens[i].split("[ ]");
        _phrases.add(phrase);
    }
    System.out.println("Phrase size:"+_phrases.size());
    for (int i = 0; i<_phrases.size();i++) {
        System.out.print("phrase:");
        for (int j = 0; j<_phrases.get(i).length;j++) {
            System.out.print(_phrases.get(i)[j]+" ");
            phraseSet.add(_phrases.get(i)[j]);
        }
        System.out.println();
    }
    System.out.print("Tokens:");
    for (int i = 0; i<_tokens.size();i++) {
        System.out.print(_tokens.get(i)+" ");
        if (!phraseSet.contains(_tokens.get(i)))
            nToken.add(_tokens.get(i));
    }
    System.out.println();
    System.out.print("nTokens:");
    for (int i = 0; i<nToken.size();i++)
        System.out.print(nToken.get(i)+" ");
    System.out.println();
  }
}
