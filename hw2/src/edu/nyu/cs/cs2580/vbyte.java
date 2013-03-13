package edu.nyu.cs.cs2580;
import java.util.ArrayList;
import java.util.Vector;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

public class vByte {


    static class Byte {
        int[] abyte;
        Byte(){
            abyte = new int[8];
        }

        public void readInt(int n) {
            // n must be less than 128 !!
            String bin = Integer.toBinaryString(n);
            for(int i = 0 ; i < (8 - bin.length()) ; i++) {
                abyte[i] = 0;
            }
            for(int i = 0 ; i < bin.length() ; i++){
                abyte[i + (8 - bin.length())] = bin.charAt(i) - 48; // ASCII code for '0' is 48
            }
            //System.out.println(" Byte ***** " + this.toString());
        }

        public void switchFirst(){
            abyte[0] = 1;
        }

        public int toInt(){
            //System.out.println(" Byte ***** " + this.toString());
            int res = 0;
            for (int i = 0 ; i < 8 ; i++){
                res += abyte[i] * Math.pow(2, (7 - i));
            }
            //System.out.println(" Value ***** " + res);
            return res;
        }

        public String toString(){
            String res ="";
            //            System.out.println("BYTES:");
            for(int i = 0 ; i < 8 ; i++) {
                System.out.print(abyte[i]);
                res += abyte[i];
            }
            return res;
        }
        public void loadFromChar(char c) {
            int x = c;
            for (int i = 0; i<8; i++) {
                abyte[i] =x / (int)Math.pow(2,(7-i));
                x = x % (int)Math.pow(2,(7-i));
            }
        }

    }


    public static LinkedList<Byte> vbEncode(ArrayList<Integer> numbers) {
        LinkedList<Byte> code = new LinkedList<Byte>();
        for (int i = 0; i<numbers.size();i++) {
            int n = numbers.get(i);
            code.addAll(vbEncodeNumber(n));
        }
        return code;
    }

    public static LinkedList<Byte> vbEncodeNumber(int n) {
        LinkedList<Byte> bytestream = new LinkedList<Byte>();
        int num = n;
        while (true) {
            Byte b = new Byte();
            b.readInt (num%128);
            bytestream.addFirst(b);
            if (num < 128) {
                break;
            }
            num /= 128;     //right-shift of length 7 (128 = 2^7)
        }
        Byte last = bytestream.get(bytestream.size() - 1); //retrieving the last byte
        last.switchFirst(); //setting the continuation bit to 1
        return bytestream;
    }

    public static ArrayList<Integer> vbDecode(LinkedList<Byte> code){

        ArrayList<Integer> numbers = new ArrayList<Integer>();
        int n = 0;
        for(int i = 0 ; !(code.isEmpty()) ; i++){
            Byte b = code.poll(); // read leading byte
            //System.out.println(" Reading byte " + b.toString() );
            int  bi = b.toInt();  // decimal value of this byte
            if (bi < 128) {
                //continuation bit is set to 0
                n = 128 * n + bi;
            } else {
                // continuation bit is set to 1
                n = 128 * n + (bi - 128);
                numbers.add(n);   // number is stored
                n = 0;            // reset
            }
        }
        return numbers;
    }
    public static String encodeToString (ArrayList<Integer> msg) {
        LinkedList<Byte> list = vbEncode(msg);
        String ret="";
        for (int i = 0; i<list.size();i++){
            ret = ret+(char)list.get(i).toInt();
            //            System.out.println(ret);
        }
        return ret;
    }
    public static ArrayList<Integer> decodeFromString(String s) {
        byte[] bs = s.getBytes();
        LinkedList<Byte> code = new LinkedList<Byte>();
        for (int i = 0; i<s.length();i++) {
            Byte b = new Byte();
            b.loadFromChar(s.charAt(i));
            code.add(b);
        }
        ArrayList<Integer> ret = vbDecode(code);
        return ret;
    }

}


