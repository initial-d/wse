package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class LogMinerNumviews extends LogMiner {
    public LogMinerNumviews(Options options) {
        super(options);
    }

    /**
     * This function processes the logs within the log directory as specified by
     * the {@link _options}. The logs are obtained from Wikipedia dumps and have
     * the following format per line: [language]<space>[article]<space>[#views].
     * Those view information are to be extracted for documents in our corpus and
     * stored somewhere to be used during indexing.
     *
     * Note that the log contains view information for all articles in Wikipedia
     * and it is necessary to locate the information about articles within our
     * corpus.
     *
     * @throws IOException
     */
    @Override
        public void compute() throws IOException {
        System.out.println("Computing using " + this.getClass().getName());
        Map<String, Integer> numViews = new HashMap<String, Integer>();

        getDocNames(numViews);
        processLogs(numViews);
        outputToFile(numViews);
        return;
    }

    /**
     * During indexing mode, this function loads the NumViews values computed
     * during mining mode to be used by the indexer.
     *
     * @throws IOException
     */
    @Override
        public Object load() throws IOException {
        System.out.println("Loading using " + this.getClass().getName());
        String outputPath =  _options._indexPrefix +
            "/corpus_numViews.idx";
        return loadFromFile(outputPath);
    }
    public Object loadFromFile(String fileName) throws IOException{
        Map<String, Integer> numViews = new HashMap<String, Integer>();
        System.out.println("load numviews from:"+fileName);
        FileReader filereader = new FileReader(fileName);
        BufferedReader bufferedreader = new BufferedReader(filereader);
        String line = bufferedreader.readLine();
        String[] tmp;
        while (line!=null) {
            tmp = line.split(" ");
            numViews.put(tmp[0],Integer.parseInt(tmp[1]));
            line = bufferedreader.readLine();
        }
        bufferedreader.close();
        outputNumViews(numViews);
        return numViews;
    }
    private void getDocNames(Map<String, Integer> numViews) throws IOException {
        final File folder = new File(_options._corpusPrefix);
        int docId = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                numViews.put(fileEntry.getName(),0);
            }
        }
    }
    //    public Object load(String path) throws IO)
    private void processLogs(Map<String, Integer> numViews) throws IOException{
        final File folder = new File(_options._logPrefix);
        FileReader filereader;
        BufferedReader bufferedreader;
        String line ;
        String[] tmp;
        Integer oldValue;
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                filereader = new FileReader(fileEntry);
                bufferedreader = new BufferedReader(filereader);
                line = bufferedreader.readLine();
                while (line!=null) {
                    tmp = line.split(" ");
                    oldValue = numViews.get(tmp[1]);
                    if (oldValue!=null) {
                        numViews.put(tmp[1],
                                      oldValue+
                                      Integer.parseInt(tmp[2]));
                    }
                    line = bufferedreader.readLine();
                }
                bufferedreader.close();
            }
        }
    }
    private void outputNumViews(Map<String, Integer> numViews) {
        Iterator it = numViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            System.out.println((String)pairs.getKey() + " " +
                               Integer.toString((Integer)pairs.getValue()));
        }
    }
    private void outputToFile(Map<String, Integer> numViews) throws IOException{
        String outputPath =  _options._indexPrefix +
            "/corpus_numViews.idx";
        FileWriter fstream = new FileWriter(outputPath);
        BufferedWriter out = new BufferedWriter(fstream);
        Iterator it = numViews.entrySet().iterator();
        String fileName;
        int score;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            fileName = (String)pairs.getKey();
            score = (Integer)pairs.getValue();
            out.write(fileName + " " +Integer.toString(score));
            out.newLine();
        }
        out.close();
    }
}
