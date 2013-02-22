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

class QueryHandler implements HttpHandler {
  private static String plainResponse =
      "Request received, but I am not smart enough to echo yet!\n";

  private Ranker _ranker;

  public QueryHandler(Ranker ranker){
    _ranker = ranker;
  }

  public static Map<String, String> getQueryMap(String query){
    String[] params = query.split("&");
    Map<String, String> map = new HashMap<String, String>();
    for (String param : params){
      String name = param.split("=")[0];
      String value = param.split("=")[1];
      value = value.replace('+',' ');
      System.out.println(name+':'+value);
      map.put(name, value);
    }
    return map;
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
        Map<String,String> query_map = getQueryMap(uriQuery);
        Set<String> keys = query_map.keySet();
        if (keys.contains("query")){
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
                } else if (ranker_type.equals("nviews")) {
                    sds = _ranker.runqueryWithViews(query_map.get("query"));
                    outputPath = "hw1.1-numviews.tsv";
                    System.out.println("Num views ranker");
                } else if (ranker_type.equals("linear")){
                    sds = _ranker.runqueryWithViews(query_map.get("query"));
                    outputPath = "hw1.2-linear.tsv";
                    System.out.println("Num views ranker");
                } else {
                    queryResponse = (ranker_type+" not implemented.");
                }
            } else {
                sds = _ranker.runquery(query_map.get("query"));
            }
            Iterator < ScoredDocument > itr = sds.iterator();
            while (itr.hasNext()){
                ScoredDocument sd = itr.next();
                if (queryResponse.length() > 0){
                    queryResponse = queryResponse + "\n";
                }
                queryResponse = queryResponse + query_map.get("query") + "\t" + sd.asString();
            }
            if (queryResponse.length() > 0){
                queryResponse = queryResponse + "\n";
            }
            FileWriter fstream = new FileWriter(outputPath);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(queryResponse);
            //Close the output stream
            out.close();
        }
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
