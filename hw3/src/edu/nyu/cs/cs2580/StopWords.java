package edu.nyu.cs.cs2580;

import java.util.HashSet;
import java.util.Set;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public  class StopWords {
    private  Set<String> _stopWords = new HashSet<String>();
    public void add(String s) {
        _stopWords.add(s);
    }
    public void writeToFile() throws IOException{
        FileWriter fstream = new FileWriter("data/index/stopwords.idx");
        BufferedWriter out = new BufferedWriter(fstream);
        for (String word: _stopWords) {
            out.write(word+"\n");
        }
        out.close();
    }
    public void load() throws IOException {
        BufferedReader reader = 
            new BufferedReader( new FileReader ("data/index/stopwords.idx"));
        String line;
        while( ( line = reader.readLine() ) != null ) {
            _stopWords.add(line);
        }
        reader.close();
    }
    public boolean isStopWord(String term) {
        return _stopWords.contains(term);
    }
}