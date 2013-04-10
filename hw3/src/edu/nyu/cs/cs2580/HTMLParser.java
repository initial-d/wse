package edu.nyu.cs.cs2580;

import java.io.*;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
public class HTMLParser {
    public static String stemm(String b) {
        String a=b;
        Stemmer s = new Stemmer();
        for (int i = 0;i<b.length();i++)
            if (b.charAt(i)<'0'||b.charAt(i)>'9') {
                s.add(b.charAt(i));
            }
            else return b;
        s.stem();
        a = s.toString();
        return a;
    }
    public static void parse(String fileName,IndexerInverted indexer,ArrayList<Integer> tokens) {
        String current = "";
        int bracketCount = 0;
        char c;
        int script = 0;
        try {
            /*        InputStream in = new FileInputStream(fileName);
        Reader reader = new InputStreamReader(in);
        // buffer for efficiency
        Reader buffer = new BufferedReader(reader);*/
        BufferedReader reader = new BufferedReader( new FileReader (fileName));
        int r;
        while ((r = reader.read()) != -1) {
            //            System.out.println(current);
            c = (char) r;
            if (bracketCount>0) {
                // inside bracket, everything appended
                if (current.length()<=10)
                    current+=c;
                if (c=='>')
                    bracketCount --;
                if (c=='<')
                    bracketCount ++;
                // bracket terminates
                if (bracketCount ==0&&current.length()>0) {
                    if (current.charAt(0)=='<') {
                        if (current.length()>=7 && current.substring(0,7).equals("<script")){
                            script ++;
                        }
                        else if (current.equals("</script>")) {
                            script --;
                        }
                    current = "";
                    }
                }
            }
            else if ((c<='z'&&c>='a')||(c<='Z'&&c>='A')||(c<='9'&&c>='0')) {
                // not in bracket normal character, append
                if (script==0) {
                    current = current+c;
                }
            }
            else {
                /// not in bracket, meet special chars,
                /// put curret into vector
                if (script==0 && current.length()>0 && bracketCount == 0) {
                    // valid token
                    indexer.addToken(stemm(current),tokens);
                    //                    ret.add(stemm(current));
                }
                current = "";
                // check if start bracket
                if (c=='<') {
                    bracketCount++;
                    current="<";
                }
            }
        }
        reader.close();
        } catch (IOException e){
            System.out.println("readfile error!");
        }
    }
    public static Vector<String> parse(String fileName) {
        String current = "";
        int bracketCount = 0;
        char c;
        int script = 0;
        Vector<String> ret = new Vector<String>();
        try {
        BufferedReader reader = new BufferedReader( new FileReader (fileName));
        int r;
        while ((r = reader.read()) != -1) {
            c = (char) r;
            if (bracketCount>0) {
                if (current.length()<=10)
                    current+=c;
                if (c=='>')
                    bracketCount --;
                if (c=='<')
                    bracketCount ++;
                if (bracketCount ==0&&current.length()>0) {
                    if (current.charAt(0)=='<') {
                        if (current.length()>=7 && current.substring(0,7).equals("<script")){
                            script ++;
                        }
                        else if (current.equals("</script>")) {
                            script --;
                        }
                    current = "";
                    }
                }
            }
            else if ((c<='z'&&c>='a')||(c<='Z'&&c>='A')||(c<='9'&&c>='0')) {
                if (script==0) {
                    current = current+c;
                }
            }
            else {
                if (script==0 && current.length()>0 && bracketCount == 0) {
                    ret.add(stemm(current));
                }
                current = "";
                if (c=='<') {
                    bracketCount++;
                    current="<";
                }
            }
        }
        reader.close();
        } catch (IOException e){
            System.out.println("readfile error!");
        }
        return ret;
    }
}
