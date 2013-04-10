package edu.nyu.cs.cs2580;
import java.util.Vector;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

class RPF {
  protected Options _options;
  protected CgiArguments _arguments;
  protected Indexer _indexer;
    public RPF (CgiArguments argument,Options options, Indexer indexer) {
        _options = options;
        _arguments = argument;
        _indexer = indexer;
    }
    private class Term implements Comparable<Term> {
        public Term (String t,int i) {
            _term = t;
            _count = i;
        }
        public String _term;
        public int _count;
        @Override
            public int compareTo(Term t) {
            if (this._count>t._count) return -1;
            if (this._count<t._count) return 1;
            return this._term.compareTo(t._term);
        }
    }

    public void rpf (Vector<ScoredDocument> docs, Vector<String> terms, Vector<Double> probs) {
        try {
            Vector<Term> prf = expand(docs);
            store(prf,terms,probs);
        } catch (IOException e) {
        }
    }
    private  void store(Vector<Term> prf, Vector<String> terms, Vector<Double> prob) {
        int sum = 0;
        for (int i = 0; i<prf.size();i++)
            sum += prf.get(i)._count;
        double p;
        for (int i = 0; i<prf.size();i++) {
            p = ((double)prf.get(i)._count)/(double) sum;
            terms.add(prf.get(i)._term);
            prob.add(p);
        }
    }
    private  Vector<Term>  expand(Vector<ScoredDocument> docs) throws IOException{
        Map<String,Term> terms = new HashMap<String,Term> ();
        for (int i = 0; i<docs.size(); i++) {
            handleFile(_options._corpusPrefix+"/"+docs.get(i).getDoc().getTitle(),
                       terms);
        }
        // working on terms
        Iterator it = terms.entrySet().iterator();
        Vector<Term> tV= new Vector<Term>();

        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            tV.add((Term) pairs.getValue());
        }
        Collections.sort(tV);
        StopWords stopWords = new StopWords();
        stopWords.load();
        int j = _arguments.getNumTerms();
        Vector<Term> result = new Vector<Term> ();
        for (int i = 0; i<tV.size();i++)
            if (!stopWords.isStopWord(tV.get(i)._term)&&j>0&&tV.get(i)._term.length()>2) {
                /*                System.out.println(tV.get(i)._term+" "+
                                   tV.get(i)._count+" "+
                                   _indexer.corpusTermFrequency(tV.get(i)._term)+" "+
                                   _indexer.corpusDocFrequencyByTerm(tV.get(i)._term));*/
                result.add(tV.get(i));
                j--;
            }
        return result;
    }
    private  void handleFile(String filePath,Map<String,Term> terms) {
        Vector<String> words = HTMLParser.parse(filePath);
        for (int i = 0; i<words.size();i++) {
            Term t = terms.get(words.get(i));
            if (t==null) {
                t = new Term(words.get(i),1);
                terms.put(words.get(i),t);
            } else {
                t._count++;
            }
        }
    }
}