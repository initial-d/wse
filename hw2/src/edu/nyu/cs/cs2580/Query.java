package edu.nyu.cs.cs2580;

import java.util.Scanner;
import java.util.Vector;

/**
 * Representation of a user query.
 * 
 * In HW1: instructors provide this simple implementation.
 * 
 * In HW2: students must implement {@link QueryPhrase} to handle phrases.
 * 
 * @author congyu
 * @auhtor fdiaz
 */
public class Query {
  public String _query = null;
  public Vector<String> _tokens = new Vector<String>();

  public Query(String query) {
    _query = query;
  }
  public Vector<String[]>getPhrases() {
      return new Vector<String[]>();
  }
  public void processQuery() {
    if (_query == null) {
      return;
    }
    _query = _query.replaceAll("%20"," ");
    System.out.println("process query?"+_query);
    String [] tokens = _query.split(" ");
    System.out.println("Query:"+tokens.length);
    for (String token:tokens) {
        System.out.println("Query:"+token);
        _tokens.add(token);
    }
  }
  public Vector<String> getTokens () {
      return _tokens;
  }
}
