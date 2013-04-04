package edu.nyu.cs.cs2580;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collections;
import java.lang.Math;
import edu.nyu.cs.cs2580.SearchEngine.Options;
import java.io.IOException;

public class Spearman {
    private static class DocInfo {
        public DocInfo () {}
        public int _numView=0;
        public Double _pageRank=0.0;
        public Double _pR=0.0;
        public Double _nR=0.0;
    }
    public static class pageRankComparator implements Comparator<DocInfo> {
        public int compare(DocInfo doc1, DocInfo doc2) {
            if (doc1._pageRank>doc2._pageRank) return -1;
            if (doc1._pageRank<doc2._pageRank) return 1;
            return 0;
        }
    }
    public static class numViewComparator implements Comparator<DocInfo> {
        public int compare(DocInfo doc1, DocInfo doc2) {
            if (doc1._numView>doc2._numView) return -1;
            if (doc1._numView<doc2._numView) return 1;
            return 0;
        }
    }
    private static Vector<DocInfo> _infosV = new Vector<DocInfo>();
    private static void buildM(Map<String,Double> pageRanks,Map<String,Integer> numViews) {
        Map<String, DocInfo> infosM = new HashMap<String, DocInfo>();

        Iterator it = pageRanks.entrySet().iterator();
        String name;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            name = (String) pairs.getKey();
            if (infosM.get(name)==null) {
                DocInfo di = new DocInfo();
                di._pageRank = (Double) pairs.getValue();
                infosM.put(name,di);
            }
        }
        it = numViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            name = (String) pairs.getKey();
            if (infosM.get(name)==null) {
                DocInfo di = new DocInfo();
                di._numView = (Integer) pairs.getValue();
                infosM.put(name,di);
            } else {
                DocInfo di = infosM.get(name);
                di._numView = (Integer) pairs.getValue();
            }
        }
        it = infosM.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            name = (String) pairs.getKey();
            DocInfo di = (DocInfo) pairs.getValue();
            _infosV.add(di);
            //System.out.println(name+" "+di._pageRank+" "+di._numView);
        }
    }
    private static void loadData(String pageRankPath, String numViewPath) throws IOException{
        Options opt = new Options ("conf/engine.conf");
        CorpusAnalyzer analyzer = CorpusAnalyzer.Factory.getCorpusAnalyzerByOption(opt);
        LogMiner miner = LogMiner.Factory.getLogMinerByOption(opt);
        Map<String,Double> rankScores =
            (HashMap<String,Double>)
            ((CorpusAnalyzerPagerank)analyzer).loadFromFile(pageRankPath);
        Map<String,Integer> numViews =
            (HashMap<String,Integer>)
            ((LogMinerNumviews)miner).loadFromFile(numViewPath);
        buildM(rankScores,numViews);
    }
    private static boolean equal(Double a, Double b) {
        if (Double.toString(a).compareTo(Double.toString(b))==0)
            return true;
        return false;
        /*        double c= (a-b);
        if (c<0) c = -c;
        if (c/a<0.00000000001&&c/b<0.00000000001)
            return true;
            return false;*/
    }
    private static void calculatePr() {
        System.out.println("PR:");
        Collections.sort(_infosV, new pageRankComparator());
        int head=0,tail=0;
        double sum;
        while (head<_infosV.size()) {
            tail = head;
            while (tail+1<_infosV.size()&&
                   equal(_infosV.get(tail+1)._pageRank,
                         _infosV.get(head)._pageRank)) {
                tail ++;
            }
            sum =((double) (head+tail+2))/2.0;
            for (int i = head;i<=tail;i++)
                _infosV.get(i)._pR = sum;
            head = tail + 1;
        }
        for (int i = 0; i<_infosV.size();i++)
            System.out.println(_infosV.get(i)._numView+" "+
                               _infosV.get(i)._pageRank +" " +
                               _infosV.get(i)._nR + " " +
                               _infosV.get(i)._pR);
    }
    private static void calculateNr() {
        System.out.println("NR:");
        Collections.sort(_infosV, new numViewComparator());
        int head=0,tail=0;
        double sum;
        while (head<_infosV.size()) {
            tail = head;
            while (tail+1<_infosV.size()&&
                   _infosV.get(tail+1)._numView == _infosV.get(head)._numView) {
                tail ++;
            }
            sum =((double) (head+tail+2))/2.0;
            for (int i = head;i<=tail;i++)
                _infosV.get(i)._nR = sum;
            head = tail + 1;
        }
        for (int i = 0; i<_infosV.size();i++)
            System.out.println(_infosV.get(i)._numView+" "+
                               _infosV.get(i)._pageRank +" " +
                               _infosV.get(i)._nR + " " +
                               _infosV.get(i)._pR);
    }
    private static void computeRanks() {
        calculatePr();
        calculateNr();
        double pz=0,nz=0;
        pz = ((double)(1+_infosV.size()))/2.0;
        nz = pz;
        double up=0,down1=0,down2=0;
        double xx,yy;
        for (int i = 0; i<_infosV.size();i++) {
            xx = _infosV.get(i)._pR - pz;
            yy = _infosV.get(i)._nR - nz;
            up = up + xx*yy;
            down1 = down1 + xx*xx;
            down2 = down2 + yy*yy;
        }
        down1 = Math.sqrt(down1);
        down2 = Math.sqrt(down2);
        double tao = up/(down1*down2);
        System.out.println(tao);
    }
    //    private static void computeTao() {
    //    }
    public static void main (String [] args) throws IOException{
        Spearman.loadData(args[0],args[1]);
        Spearman.computeRanks();
    }
}