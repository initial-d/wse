package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {
    private Map<String, Integer> _docs = new HashMap<String, Integer>();
    private ArrayList<Integer> _docOutLinkCount = new ArrayList<Integer>();
    private ArrayList<Set<Integer> > _docInLink = new ArrayList<Set<Integer> >();
    private ArrayList<Double> _pageRank = new ArrayList<Double> ();
    private double lambda = 0.9;
    private int ite = 1;
    public CorpusAnalyzerPagerank(Options options) {
        super(options);
    }

    /**
     * This function processes the corpus as specified inside {@link _options}
     * and extracts the "internal" graph structure from the pages inside the
     * corpus. Internal means we only store links between two pages that are both
     * inside the corpus.
     *
     * Note that you will not be implementing a real crawler. Instead, the corpus
     * you are processing can be simply read from the disk. All you need to do is
     * reading the files one by one, parsing them, extracting the links for them,
     * and computing the graph composed of all and only links that connect two
     * pages that are both in the corpus.
     *
     * Note that you will need to design the data structure for storing the
     * resulting graph, which will be used by the {@link compute} function. Since
     * the graph may be large, it may be necessary to store partial graphs to
     * disk before producing the final graph.
     *
     * @throws IOException
     */
    @Override
        public void prepare() throws IOException {
        System.out.println("Preparing " + this.getClass().getName());
        getDocNames();
        final File folder = new File(_options._corpusPrefix);
        int docCount = 0;
        for (final File fileEntry : folder.listFiles()) {
            docCount++;
            if (docCount%1000 == 0 ) System.out.println(docCount);

            if (!fileEntry.isDirectory()) {
                handleFile(fileEntry.getName());
            }
        }
        //        outputInternalGraph();
        return;
    }

    /**
     * This function computes the PageRank based on the internal graph generated
     * by the {@link prepare} function, and stores the PageRank to be used for
     * ranking.
     *
     * Note that you will have to store the computed PageRank with each document
     * the same way you do the indexing for HW2. I.e., the PageRank information
     * becomes part of the index and can be used for ranking in serve mode. Thus,
     * you should store the whatever is needed inside the same directory as
     * specified by _indexPrefix inside {@link _options}.
     *
     * @throws IOException
     */
    @Override
        public void compute() throws IOException {
        System.out.println("Computing " + this.getClass().getName());
        double score = 1.0 / _docOutLinkCount.size();
        for (int i = 0; i<_docOutLinkCount.size();i++) {
            _pageRank.add(score);
        }
        //        outputePageRank();
        for (int i = 0; i<ite; i++) {
            ArrayList<Double> newRank = new ArrayList<Double> ();
            for (int j = 0; j<_pageRank.size();j++)
                newRank.add(newScore(j));
            _pageRank = newRank;
        }
        outputePageRank();
        return;
    }

    /**
     * During indexing mode, this function loads the PageRank values computed
     * during mining mode to be used by the indexer.
     *
     * @throws IOException
     */
    @Override
        public Object load() throws IOException {
        String outputPath =  _options._indexPrefix +
            "/corpus_pageRank.idx";
        return loadFromFile(outputPath);
    }
    public Object loadFromFile(String fileName) throws IOException {
        System.out.println("load pagerank from:"+fileName);
        Map<String, Double> rankScores = new HashMap<String, Double>();
        FileReader filereader = new FileReader(fileName);
        BufferedReader bufferedreader = new BufferedReader(filereader);
        String line = bufferedreader.readLine();
        String[] tmp;
        while (line!=null) {
            tmp = line.split(" ");
            rankScores.put(tmp[0],Double.parseDouble(tmp[1]));
            line = bufferedreader.readLine();
        }
        bufferedreader.close();
        //        printPageRank(rankScores);
        return rankScores;
    }
    private void handleFile(String fileName) throws IOException{
        int sourceId = _docs.get(fileName);
        int destId = 0;
        String wholePath = _options._corpusPrefix + "/" + fileName;
        File file = new File(wholePath);
        HeuristicLinkExtractor hle = new HeuristicLinkExtractor(file);
        String link = hle.getNextInCorpusLinkTarget();
        while (link!=null) {
            if (_docs.get(link)!=null) {
                destId = _docs.get(link);
                if (!_docInLink.get(destId).contains(sourceId)) {
                    _docOutLinkCount.set(sourceId,_docOutLinkCount.get(sourceId)+1);
                    _docInLink.get(destId).add(sourceId);
                }
            }
            link = hle.getNextInCorpusLinkTarget();
        }
    }
    private void getDocNames() throws IOException {
        final File folder = new File(_options._corpusPrefix);
        int docId = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                _docs.put(fileEntry.getName(),docId++);
                _docOutLinkCount.add(0);
                _docInLink.add( new HashSet<Integer>());
            }
        }
    }
    private double newScore(int inx) {
        double ret=1.0-lambda;
        for (Integer source: _docInLink.get(inx)) {
            ret = ret + lambda*_pageRank.get(source)
                /_docOutLinkCount.get(source);
        }
        return ret;
    }
    private void outputInternalGraph() {
        System.out.print("Out link count:");
        for (int i = 0; i<_docOutLinkCount.size();i++) {
            System.out.print(_docOutLinkCount.get(i)+" ");
        }
        System.out.println();
        for (int i = 0; i<_docInLink.size();i++) {
            for (Integer source: _docInLink.get(i)) {
                System.out.print(source+" ");
            }
            System.out.println();
        }
    }
    private void printPageRank(Map<String,Double> rankScores) {
        Iterator it = rankScores.entrySet().iterator();
        System.out.println("PageRank Load:");
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            System.out.println((String)pairs.getKey() + " " +
                               Double.toString((Double)pairs.getValue()));
        }
    }
    private void outputePageRank() throws IOException{
        String outputPath =  _options._indexPrefix +
            "/corpus_pageRank.idx";
        FileWriter fstream = new FileWriter(outputPath);
        BufferedWriter out = new BufferedWriter(fstream);
        //        System.out.println("PageRank:");
        Iterator it = _docs.entrySet().iterator();
        String fileName;
        int idx;
        double score;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            fileName = (String)pairs.getKey();
            idx = (Integer) pairs.getValue();
            score = _pageRank.get(idx);
            out.write(fileName + " " +Double.toString(score));
            out.newLine();
        }
        out.close();
    }
}
