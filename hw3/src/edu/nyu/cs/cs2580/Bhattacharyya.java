package edu.nyu.cs.cs2580;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.lang.Math;
public class Bhattacharyya {
    private static Vector<PRF> _prfs = new Vector<PRF>();
    private static class PRF {
        public String _name;
        public Map<String,Double> _prob = new HashMap<String,Double>();
        public void load(File file,String term) throws IOException{
            _name = term;
            FileReader filereader = new FileReader(file);
            BufferedReader bufferedreader = new BufferedReader(filereader);
            String line = bufferedreader.readLine();
            String[] tmp;
            while (line!=null) {
                tmp = line.split("[\t]");
                if (tmp.length>1) {
                    _prob.put(tmp[0],Double.parseDouble(tmp[1]));
                }
                line = bufferedreader.readLine();
            }
            bufferedreader.close();
        }
        public double similarity(PRF p2) {
            double ret = 0;
            Iterator it = _prob.entrySet().iterator();
            String term;
            Double prob;
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                term = (String)pairs.getKey();
                prob = (Double) pairs.getValue();
                if (p2._prob.get(term)!=null) {
                    ret = ret + prob *(Double) p2._prob.get(term);
                }
            }
            return ret;
        }
    }
    private static void loadData (String path) throws IOException{
        System.out.println(path);
        FileReader filereader = new FileReader(path);
        BufferedReader bufferedreader = new BufferedReader(filereader);
        String line = bufferedreader.readLine();
        String[] tmp;
        while (line!=null) {
            tmp = line.split(":");
            File fileEntry = new File(tmp[1]);
            PRF prf = new PRF();
            prf.load(fileEntry,tmp[0]);
            _prfs.add(prf);
            line = bufferedreader.readLine();
        }
        bufferedreader.close();
    }
    private static void compute(String path) throws IOException {
        FileWriter fstream = new FileWriter(path);
        BufferedWriter out = new BufferedWriter(fstream);
        for (int i = 0; i<_prfs.size();i++)
            for (int j = 0; j<_prfs.size();j++) 
                if (i!=j){
                double simi = _prfs.get(i).similarity(_prfs.get(j));
                out.write(_prfs.get(i)._name+"\t"+
                          _prfs.get(j)._name+"\t"+
                          simi+"\n");
            }
        out.close();
    }
    public static void main (String [] args) throws IOException{
        Bhattacharyya.loadData(args[0]);
        Bhattacharyya.compute(args[1]);
    }
}