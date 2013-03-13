package edu.nyu.cs.cs2580;

import java.util.Vector;
/**
 * @CS2580: implement this class for HW2 to incorporate any additional
 * information needed for your favorite ranker.
 */

public class DocumentIndexed extends Document {
    private static final long serialVersionUID = 9184892508124423115L;

    private Indexer _indexer = null;
    //private String _body = null;
    //    private Vector<Integer> _titleTokens = new Vector<Integer>();
    //    private Vector<Integer> _bodyTokens = new Vector<Integer>();
    /*    public String getBody() {
        return _body;
    }
    public void setBody(String body) {
        _body = body;
    }
    */
    public DocumentIndexed(int docid,Indexer indexer) {
        super(docid);
        _indexer = indexer;
    }
    public String toString() {
        String ret = getTitle() + "\n" +
            getUrl() + "\n" +
            Integer.toString(_docid)+" "+
            Float.toString(getPageRank())+" " +
            Integer.toString(getNumViews())+ " "+
            Integer.toString(getSize())+"\n";
        return ret;
    }
    public void load(String s) {
    }

    /*    public void setTitleTokens(Vector<Integer> titleTokens) {
        _titleTokens = titleTokens;
    }

    public Vector<Integer> getTitleTokens() {
        return _titleTokens;
    }

    public void setBodyTokens(Vector<Integer> bodyTokens) {
        _bodyTokens = bodyTokens;
    }

    public Vector<Integer> getBodyTokens() {
        return _bodyTokens;
        }*/
}
