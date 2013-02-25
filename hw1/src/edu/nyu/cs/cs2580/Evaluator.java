package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.*;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Collections;

class Evaluator {

  public static void main(String[] args) throws IOException {
    HashMap < String , HashMap < Integer , Double > > relevance_judgments =
      new HashMap < String , HashMap < Integer , Double > >();
    if (args.length < 1){
      System.out.println("need to provide relevance_judgments");
      return;
    }
    String p = args[0];
    // first read the relevance judgments into the HashMap
    readRelevanceJudgments(p,relevance_judgments);

    String outputPath = "unknownRanker.tsv";
    if (args.length>1 ) {
        if (args[1].equals("cosine")) {
            outputPath = "hw1.3-vsm.tsv";
        } else if (args[1].equals("QL")) {
            outputPath = "hw1.3-ql.tsv";
        } else if (args[1].equals("phrase")) {
            outputPath = "hw1.3-phrase.tsv";
        } else if (args[1].equals("nviews")) {
            outputPath = "hw1.3-numviews.tsv";
        } else if (args[1].equals("linear")) {
            outputPath = "hw1.3-linear.tsv";
        }
    }
    outputPath = "../results/"+outputPath;
    evaluateStdin(relevance_judgments,outputPath);
  }

    /*  private static Vector<Double> calculatePrecision (Vector<Integer> level,
                                                    Vector<Integer> relevance_count) {
      Vector<Double> ret = new Vector<Double> ();
      for (int i = 0; i<level.size();i++) {
          ret.add(((double)relevance_count.get(i))/(double)level.get(i));
      }
      return ret;
  }

  private static Vector<Double> calculateRecall (Vector<Integer> relevance_count,
                                                    int num_Relevance) {
      Vector<Double> ret = new Vector<Double> ();
      for (int i = 0; i<relevance_count.size();i++) {
          ret.add(((double)relevance_count.get(i))/(double)num_Relevance);
      }
      return ret;
  }

  private static Vector<Double> calculateF (Vector<Double> precision,
                                                 Vector<Double> recall) {
      Vector<Double> ret = new Vector<Double> ();
      for (int i = 0; i<precision.size();i++) {
          double upper = precision.get(i)*recall.get(i)*2.0;
          double lower = precision.get(i)+recall.get(i);
          if (lower!=0)
              ret.add(upper/lower);
          else 
              ret.add(0.0);
      }
      return ret;
      }*/
    private static double calculateDCG(Vector<Double> reli) {
        double ret = reli.get(0);
        for (int i = 1; i<reli.size();i++)
            ret = ret + reli.get(i)*Math.log(2)/ Math.log(i+1);
        return ret;
    }


  public static void readRelevanceJudgments(
    String p,HashMap < String , HashMap < Integer , Double > > relevance_judgments){
    try {
      BufferedReader reader = new BufferedReader(new FileReader(p));
      try {
        String line = null;
        while ((line = reader.readLine()) != null){
          // parse the query,did,relevance line
          Scanner s = new Scanner(line).useDelimiter("\t");
          String query = s.next();
          int did = Integer.parseInt(s.next());
          String grade = s.next();
          double rel = 0.0;
          // convert to binary relevance
          if (grade.equals("Perfect"))
              rel = 10.0;
          else if (grade.equals("Excellent"))
              rel = 7.0;
          else if (grade.equals("Good"))
              rel = 5.0;
          else if (grade.equals("Fair"))
              rel = 1.0;
          else
              rel = 0.0;
          if (relevance_judgments.containsKey(query) == false){
            HashMap < Integer , Double > qr = new HashMap < Integer , Double >();
            relevance_judgments.put(query,qr);
          }
          HashMap < Integer , Double > qr = relevance_judgments.get(query);
          qr.put(did,rel);
        }
      } finally {
        reader.close();
      }
    } catch (IOException ioe){
      System.err.println("Oops " + ioe.getMessage());
    }
  }
  private static int countRelevant(HashMap < Integer , Double > qr) {
      int ret = 0;
      for (Double value : qr.values()) {
          if (value>1.0)
              ret++;
      }
      return ret;
  }

  public static void evaluateStdin(
                                   HashMap < String , HashMap < Integer , Double > > relevance_judgments,String outputPath){
    // only consider one query per call
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

      String line = null;
      double RR = 0.0;
      double N = 0.0;
      int lineCount = 0;

      Vector<Integer> level = new Vector<Integer>();
      level.add(1);
      level.add(5);
      level.add(10);

      int numRelevant = -1;
      Vector<Double>precision = new Vector<Double>();
      Vector<Double>recall = new Vector<Double>();
      Vector<Double>fmeasure = new Vector<Double>();
      Vector<Double> pAtR = new Vector<Double>();
      Vector<Double> ndcg = new Vector<Double>();
      Vector<Double> reli = new Vector<Double>();
      for (int i = 0; i<=10; i++)
          pAtR.add(0.0);
      //      pAtR.set(0,1.0);

      int relevance_count = 0;
      int levelReach = 0;
      double currentPrecision = 0.0;
      double currentRecall = 0.0;
      double sumForAveragePrecision = 0.0;
      Double averagePrecision = 0.0;
      String query = "";
      Double reciprocal = 0.0;
      while ((line = reader.readLine()) != null&&!line.equals("")){
          Scanner s = new Scanner(line).useDelimiter("\t");
          query = s.next();
          int did = Integer.parseInt(s.next());
          String title = s.next();
          double rel = Double.parseDouble(s.next());

          if (relevance_judgments.containsKey(query) == false) {
              throw new IOException("query not found");
          }
          HashMap < Integer , Double > qr = relevance_judgments.get(query);

          if (numRelevant == -1)
              numRelevant = countRelevant(qr);

          lineCount++;
          if (qr.containsKey(did)&&qr.get(did) > 1.0) {
              if (relevance_count == 0)
                  reciprocal = 1.0 / lineCount;
              relevance_count++;
          }
          currentPrecision = (double)relevance_count/(double)lineCount;
          if (numRelevant == 0)
              currentRecall = 0.0;
          else
              currentRecall = (double)relevance_count/(double)numRelevant;

          if (qr.containsKey(did)&&qr.get(did) > 1.0) {
              sumForAveragePrecision += currentPrecision;
          }
          for (int i = 0; i<=10; i++)
              if (currentRecall>=0.1*i) {
                  if (currentPrecision>pAtR.get(i))
                      pAtR.set(i,currentPrecision);
              }


          if (levelReach<level.size()) {
              if (qr.containsKey(did))
                  reli.add(qr.get(did));
              else
                  reli.add(0.0);

              if (level.get(levelReach) == lineCount) {

                  precision.add(currentPrecision);
                  recall.add(currentRecall);
                  double upper = precision.get(levelReach)*recall.get(levelReach)*2.0;
                  double lower = precision.get(levelReach) + recall.get(levelReach);
                  if (lower == 0)
                      fmeasure.add(0.0);
                  else
                      fmeasure.add(upper/lower);
                  levelReach++;
                  Vector<Double> sortedReli = new Vector<Double>();
                  for (int i = 0; i<reli.size();i++)
                      sortedReli.add(reli.get(i));
                  System.out.println("reli");
                  for (Integer i = 0; i<reli.size();i++) {
                      System.out.print(reli.get(i).toString()+" ");
                  }

                  Collections.sort(sortedReli);
                  Collections.reverse(sortedReli);
                  System.out.println("sortedReli");
                  for (int i = 0; i<reli.size();i++)
                  System.out.print(sortedReli.get(i).toString()+" ");
                  Double dcg = calculateDCG(reli);
                  Double idcg = calculateDCG(sortedReli);
                  System.out.println("\n");
                  System.out.println("dcg="+dcg.toString()+" idcg="+idcg.toString());
                  if (idcg!=0)
                      ndcg.add(dcg/idcg);
                  else
                      ndcg.add(0.0);
                  System.out.println("ndcg="+ndcg.get(ndcg.size()-1).toString());
              }
          }
      }

      String response = new String();
      response = query + "\t";
      for (int i = 0; i<precision.size();i++)
          response= response+precision.get(i).toString()+"\t";
      //      response = response + "\n";

      for (int i = 0; i<recall.size();i++)
          response= response+recall.get(i).toString()+"\t";
      //      response = response + "\n";

      for (int i = 0; i<fmeasure.size();i++)
          response= response+fmeasure.get(i).toString()+"\t";
      //      response = response + "\n";

      for (int i = 0; i<pAtR.size();i++)
          response= response+pAtR.get(i).toString()+"\t";
      //      response = response + "\n";
      if (relevance_count == 0)
          averagePrecision = 0.0;
      else
          averagePrecision = sumForAveragePrecision / relevance_count;
      response = response + averagePrecision.toString() + "\t";
      //      response = response + "\n";

      for (int i= 0; i<ndcg.size();i++)
          response = response + ndcg.get(i) + "\t";
      //response = response + "\n";

      response = response+reciprocal.toString() + "\n";
      System.out.println(response);
      FileWriter fstream = new FileWriter(outputPath,true);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(response);
      out.close();


    } catch (Exception e){
        System.err.println("Error:" + e.getMessage());
    }
  }
}
