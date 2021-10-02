package com.company;

import opennlp.tools.tokenize.SimpleTokenizer;

public class TA
{
        SimpleTokenizer tokenize = SimpleTokenizer.INSTANCE;

        public TA(){
        }

        //Takes the text of the page and tokenizes it, returns an array of tokens in string format
        public String [] tokenizePage(String text) {
                 return tokenize.tokenize(text);
        }

        //Compute the Jaro distance of two strings
        public static double jaroDistance(String a, String b){
                int nMatches = 0;
                int t = 0; //number of transpositions

                int lengthOne = a.length();
                int lengthTwo = b.length();

                if(lengthOne == 0 || lengthTwo == 0){
                        return 0;
                }

                //Calculate match distance
                int md = Integer.max(lengthOne,lengthTwo)/2 -1;

                for(int i = 0; i < lengthOne; i++){
                        int start = Integer.max(0,i-md);
                        int end = Integer.min(i+md+1,lengthTwo);

                        for(int j = start; j < end; j++) {
                                if(a.charAt(i) != b.charAt(j)) continue;
                                nMatches++;
                        }
                }

                int k = 0;
                for(int i = 0; i < lengthOne; i++){
                        if(a.charAt(i) != b.charAt(k)){
                                t++;
                        }
                        k++;
                }

                double matches = (double)nMatches;
                return ((matches / lengthOne) + ((matches / lengthTwo) + ((matches - t/2.0) / matches))) / 3.0;
        }

        void analyseReport(String [] tokens, String indicatorWord){

        }

}
