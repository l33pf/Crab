package com.company;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

public class TA {
        InputStream modelIn, modelInNm = null;
        SimpleTokenizer tokenize = SimpleTokenizer.INSTANCE;

        public TA(){

                try{
                        modelIn = new FileInputStream("en-token.bin");

                        TokenizerModel model = new TokenizerModel(modelIn);
                        TokenizerME tokenizer = new TokenizerME(model);
                }catch(IOException e){
                        e.printStackTrace();
                }

        }

        //Takes the text of the page and tokenizes it, returns an array of tokens in string format
        public String [] tokenizePage(String text) {
                 return tokenize.tokenize(text);
        }

        //Finds Duplicates in two strings
        //Credit to: Grant Ingersoll, Thomas Morton and Drew Farris
        //https://github.com/tamingtext/book/blob/master/src/main/java/com/tamingtext/fuzzy/OverlapMeasures.java
        private int findDuplicates(char[] s, boolean[] sdup) {
                int ndup =0;
                for (int si=0;si<s.length;si++) {
                        if (sdup[si]) {
                                ndup++;
                        }
                        else {
                                for (int si2=si+1;si2<s.length;si2++) {
                                        if (!sdup[si2]) {
                                                sdup[si2] = s[si] == s[si2];
                                        }
                                }
                        }
                }
                return ndup;
        }

        //Finds Duplicates in two strings
        //Credit to: Grant Ingersoll, Thomas Morton and Drew Farris
        //https://github.com/tamingtext/book/blob/master/src/main/java/com/tamingtext/fuzzy/OverlapMeasures.java
        public float jaccard(char[] s, char[] t) {
                int intersection = 0;
                int union = s.length+t.length;
                boolean[] sdup = new boolean[s.length];
                union -= findDuplicates(s,sdup);   //<co id="co_fuzzy_jaccard_dups1"/>
                boolean[] tdup = new boolean[t.length];
                union -= findDuplicates(t,tdup);
                for (int si=0;si<s.length;si++) {
                        if (!sdup[si]) {   //<co id="co_fuzzy_jaccard_skip1"/>
                                for (int ti=0;ti<t.length;ti++) {
                                        if (!tdup[ti]) {
                                                if (s[si] == t[ti]) {   //<co id="co_fuzzy_jaccard_intersection" />
                                                        intersection++;
                                                        break;
                                                }
                                        }
                                }
                        }
                }
                union-=intersection;
                return (float) intersection/union; //<co id="co_fuzzy_jaccard_return"/>
        }
}
