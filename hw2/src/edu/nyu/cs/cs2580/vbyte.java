package edu.nyu.cs.cs2580;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Vector;


public class vByte {
    public vByte(){

    }
    private LinkedList<Byte> al=new LinkedList<Byte>();
    private ArrayList<Integer> intal =new ArrayList<Integer>();
    public int isize(){
        return intal.size();
    }
    public int bsize(){
        return al.size();
    }
    public void push(int i){
        intal.add(i);
        update(i);
    }
    private void update(Integer i){
        if(i>=1<<21){
            al.add((byte)(i>>21));
        }
        i&=(1<<21)-1;
        if(i>=1<<14){
            al.add((byte)(i>>14));
        }
        i&=(1<<14)-1;
        if(i>=1<<7){
            al.add((byte)(i>>7));
        }
        i&=(1<<7)-1;
        i|=128;
        al.add(i.byteValue());
    }
    public vByte(ArrayList<Integer> ial){
        for(Integer i:intal){
            if(i>=1<<21){
                al.add((byte)(i>>21));
            }
            i&=(1<<21)-1;
            if(i>=1<<14){
                al.add((byte)(i>>14));
            }
            i&=(1<<14)-1;
            if(i>=1<<7){
                al.add((byte)(i>>7));
            }
            i&=(1<<7)-1;
            i|=128;
            al.add(i.byteValue());
        }
    }
    public LinkedList<Byte> getBytes(){
        return (LinkedList<Byte>) al.clone();
    }
    public ArrayList<Integer> getInts(){
        return (ArrayList<Integer>) intal.clone();
    }
    public Vector<Integer> getLists(){
        return new Vector<Integer>(intal);
    }
    public vByte(LinkedList<Byte> bal){
        for(int i=0;i<bal.size();i++){
            int tmp=0;
            while(bal.get(i)>0){
                tmp=tmp<<7;
                tmp+=bal.get(i);
                i++;
            }
            tmp=tmp<<7;
            tmp+=bal.get(i)&127;
            intal.add(tmp);
        }//don't let the last byte <0!
    }
}
