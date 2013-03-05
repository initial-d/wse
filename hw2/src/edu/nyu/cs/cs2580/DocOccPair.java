package edu.nyu.cs.cs2580;
import java.io.Serializable;

public class DocOccPair implements Serializable{
    private static final long serialVersionUID = 9084892508124423115L;
    private int _did;
    private int _occ;

    public DocOccPair (int did, int occ) {
        _did = did;
        _occ = occ;
    }
    public int getDid() {
        return _did;
    }
    public void setDid(int did) {
        _did = did;
    }
    public void setOcc(int occ) {
        _occ = occ;
    }
    public int getOcc() {
        return _occ;
    }
}