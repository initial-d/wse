package edu.nyu.cs.cs2580;
import java.io.IOException;
import java.io.OutputStream;
import java.io.*;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.HashMap;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import java.util.Comparator;
import java.util.Collections;

import java.util.Date;
import java.lang.System;  
import java.text.SimpleDateFormat;

class QueryHandler implements HttpHandler {
  private static String plainResponse =
      "Request received, but I am not smart enough to echo yet!\n";

  private Ranker _ranker;
  private Integer _sessionID;
  private Map<Integer,String> _sessions;
  static class SdComparator implements Comparator<ScoredDocument> {
      @Override
          public int compare(ScoredDocument arg0, ScoredDocument arg1) {
          if(arg0._score < arg1._score)  return 1;
          else if(arg0._score > arg1._score)  return -1;
          else  return 0;
      }
  }

  public QueryHandler(Ranker ranker){
    _ranker = ranker;
    _sessionID = 0;
    _sessions = new HashMap<Integer,String> ();
  }

  public static Map<String, String> getQueryMap(String query){
    String[] params = query.split("&");
    Map<String, String> map = new HashMap<String, String>();
    for (String param : params){
      String name = param.split("=")[0];
      String value = param.split("=")[1];
      value = value.replaceAll("%20"," ");
      System.out.println(name+':'+value);
      map.put(name, value);
    }
    return map;
  }
  private String handleSearch (String uriQuery) throws IOException{
      String queryResponse = "";
      Map<String,String> query_map = getQueryMap(uriQuery);
      Set<String> keys = query_map.keySet();

      if (keys.contains("query")){
          Boolean implementedQuery = true;
          String outputPath = "defaultRanker.tsv";
          Vector < ScoredDocument > sds = new Vector<ScoredDocument>();
          if (keys.contains("ranker")){
              String ranker_type = query_map.get("ranker");
              if (ranker_type.equals("cosine")){
                  sds = _ranker.runqueryWithCosine(query_map.get("query"));
                  outputPath = "hw1.1-vsm.tsv";
                  System.out.println("cosie ranker");
              } else if (ranker_type.equals("QL")){
                  sds = _ranker.runqueryWithJMS(query_map.get("query"));
                  outputPath = "hw1.1-ql.tsv";
                  System.out.println("QL ranker");
              } else if (ranker_type.equals("phrase")){
                  sds = _ranker.runqueryWithPhrase(query_map.get("query"));
                  outputPath = "hw1.1-phrase.tsv";
                  System.out.println("Phrase ranker");
              } else if (ranker_type.equals("numviews")) {
                  sds = _ranker.runqueryWithViews(query_map.get("query"));
                  outputPath = "hw1.1-numviews.tsv";
                  System.out.println("Num views ranker");
              } else if (ranker_type.equals("linear")){
                  sds = _ranker.runqueryWithLinear(query_map.get("query"));
                  outputPath = "hw1.2-linear.tsv";
                  System.out.println("Num views ranker");
              } else {
                  implementedQuery = false;
                  queryResponse = (ranker_type+" not implemented.Please use cosine | QL | phrase | numviews | linear\n");
              }
          } else {
              sds = _ranker.runquery(query_map.get("query"));
          }
          if (implementedQuery) {
              Collections.sort(sds, new SdComparator());
              queryResponse = generateQueryResponse(sds,query_map.get("query"));

              if (keys.contains("format")&&query_map.get("format").equals("html")) {
                  writeToHTML(outputPath,queryResponse);
              }
              /// write result to text file
              FileWriter fstream = new FileWriter("../results/"+outputPath,true);
              BufferedWriter out = new BufferedWriter(fstream);
              out.write(queryResponse);
              out.close();
              /// update session id
              Integer sid = _sessionID++;
              _sessions.put(sid,query_map.get("query"));
              System.out.println("sessionID="+sid);
              queryResponse = queryResponse + "\nsessionID="+sid.toString()+"\n";
              /// make top 10 to render the log
              for (int i = 0; i<10 && i<sds.size();i++)
                  makeLog(sid,query_map.get("query"),sds.get(i)._did,"render");
          }
          else {
              System.out.println(queryResponse);
          }
        }
      return queryResponse;
  }
  private String generateQueryResponse(Vector<ScoredDocument> sds,String query) {
      String queryResponse = "";
      Iterator < ScoredDocument > itr = sds.iterator();
      while (itr.hasNext()){
          ScoredDocument sd = itr.next();
          if (queryResponse.length() > 0){
              queryResponse = queryResponse + "\n";
          }
          queryResponse = queryResponse + query + "\t" + sd.asString();
      }
      if (queryResponse.length() > 0){
          queryResponse = queryResponse + "\n";
      }
      return queryResponse; 
  }
  private void writeToHTML (String outputPath,String queryResponse) throws IOException {
      FileWriter fstream = new FileWriter("../results/"+outputPath+".html");
      BufferedWriter out = new BufferedWriter(fstream);
      String tmp =queryResponse.substring(0);
      tmp=tmp.replaceAll("\n","</p><p>");
      String output = "<!DOCTYPE html><html><body><p>"+tmp+"</p></body></html>";
      out.write(output);
      out.close();
  }
  private String makeLog(Integer sid,String query,Integer did,String action) throws IOException{
      String ret;
      long nowmills = System.currentTimeMillis();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String time = sdf.format(new Date(nowmills));
      ret = sid.toString()+"\t"
          +_sessions.get(sid)+"\t"
          +did.toString()+"\t"
          +action+"\t"
          +time+"\n";

      FileWriter fstream = new FileWriter("../results/hw1.4-log.tsv",true);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(ret);
      out.close();
      return ret;
  }
  private String handleClick(String uriQuery) throws IOException{
      String queryResponse = "";
      Map<String,String> query_map = getQueryMap(uriQuery);
      Set<String> keys = query_map.keySet();
      if (!keys.contains("did")||(!keys.contains("sid"))) {
          queryResponse = "click format is 'click?did=<did>&sid=<sessionID>'\n";
          return queryResponse;
      }
      Integer did = Integer.valueOf(query_map.get("did"));
      Integer sid = Integer.valueOf(query_map.get("sid"));
      if (!_sessions.containsKey(sid)) {
          queryResponse = "invalid sid\n";
          return queryResponse;
      }
      queryResponse = makeLog(sid,_sessions.get(sid),did,"click");
      return queryResponse;
  }
  public void handle(HttpExchange exchange) throws IOException {
    String requestMethod = exchange.getRequestMethod();
    if (!requestMethod.equalsIgnoreCase("GET")){  // GET requests only.
      return;
    }

    // Print the user request header.
    Headers requestHeaders = exchange.getRequestHeaders();
    System.out.print("Incoming request: ");
    for (String key : requestHeaders.keySet()){
      System.out.print(key + ":" + requestHeaders.get(key) + "; ");
    }
    System.out.println();
    String queryResponse = "";
    String uriQuery = exchange.getRequestURI().getQuery();
    String uriPath = exchange.getRequestURI().getPath();

    if ((uriPath != null) && (uriQuery != null)){
      if (uriPath.equals("/search")){
          queryResponse = handleSearch(uriQuery);
      }
      if (uriPath.equals("/click")) {
          queryResponse = handleClick(uriQuery);
      }
    }
    // Construct a simple response.
    Headers responseHeaders = exchange.getResponseHeaders();
    responseHeaders.set("Content-Type", "text/plain");
    exchange.sendResponseHeaders(200, 0);  // arbitrary number of bytes
    OutputStream responseBody = exchange.getResponseBody();
    responseBody.write(queryResponse.getBytes());
    responseBody.close();
  }
}
