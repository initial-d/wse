package edu.nyu.cs.cs2580;
import java.util.LinkedList;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Vector;
import java.io.UnsupportedEncodingException;


public class vbyte {
    public vbyte(){

    }
    public String toString(){//add 0 if not size 2X
    	int size=al.size();
    	boolean two=false;
    	if(size%2==0)
    		two=true;
    	if(!two)
    		size+=1;
    	byte bt[]=new byte[size];
    	int loc=0;
    	for(Byte b:al){
    		bt[loc++]=b;
    	}
    	if(!two)
    		bt[loc++]=0;
    	String s;
    	//s=new String(bt);
    	//return s;
    	
		try {
			s = new String(bt,"utf-16le");
			return s;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    	
    	
    }
    public void loadFromString(String s){
    	//System.out.println(s);
    	//byte[] bt=s.getBytes();
    	
    	byte[] bt=new byte[1];
		try {
			bt = s.getBytes("utf-16le");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	//for(Byte b:bt)
    	//	System.out.println(b);
    	int size=bt.length;
    	if (size==0)
    		return;
    	if(bt[size-1]==0){
    		size--;
    	}
    	
    	for(int i=0;i<size;i++){
    		int tmp=0;
            while(bt[i]>0){
            	al.add(bt[i]);
                tmp=tmp<<7;
                tmp+=bt[i];
                i++;
            }
            al.add(bt[i]);
            tmp=tmp<<7;
            tmp+=bt[i]&127;
            intal.add(tmp);
    	}

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
    public vbyte(ArrayList<Integer> ial){
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
    public vbyte(LinkedList<Byte> bal){
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
        }//don't let the last byte >0!
    }
}
