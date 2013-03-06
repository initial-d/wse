package edu.nyu.cs.cs2580;
import java.util.ArrayList;
import java.util.LinkedList;


public class vByte {
  private ArrayList<Byte> al=new ArrayList<Byte>();//dunno which list is byte better...
	//private StringBuilder sb=new StringBuilder();
	private LinkedList<Integer> intal =new LinkedList<Integer>();
	public vByte(LinkedList<Integer> ial){
		intal=new LinkedList<Integer>(ial);
		for(Integer i:intal){
			//System.out.println(i);
			if(i>=1<<21){
				al.add((byte)(i>>21));
			}
			i&=(1<<21)-1;
			//System.out.println(i);
			if(i>=1<<14){
				al.add((byte)(i>>14));
			}
			i&=(1<<14)-1;
			//System.out.println(i);
			if(i>=1<<7){
				al.add((byte)(i>>7));
			}
			i&=(1<<7)-1;
			//System.out.println(i);
			//System.out.println(7);
			i|=128;
			al.add(i.byteValue());
			
		}
	}
	public ArrayList<Byte> getBytes(){
		return (ArrayList<Byte>) al.clone();
	}
	public LinkedList<Integer> getInts(){
		return (LinkedList<Integer>) intal.clone();
	}
	public vByte(ArrayList<Byte> bal){
		intal.clear();
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
