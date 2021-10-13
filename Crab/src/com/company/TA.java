package com.company;

import opennlp.tools.tokenize.SimpleTokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public final class TA
{
        private SimpleTokenizer tokenize = SimpleTokenizer.INSTANCE;
        private OverlapMeasures measure = new OverlapMeasures();
        private TreeMap<Integer,String> textMap = new TreeMap<Integer,String>();

        //Constructor for URL Seed
        TA(final TreeMap<Integer,String> textToAnalyse){
                Objects.requireNonNull(this.textMap = textToAnalyse);
        }

        //Takes the text of the page and tokenizes it, returns an array of tokens in string format
        public String [] tokenizePage(String text) {
                return tokenize.tokenize(text);
        }

        //Compute the Jaro distance of two strings
        private static double jaroDistance(String a, String b){
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

        //This function takes each indicator word and analyses with the extracted text
        //Uses both Jaccard and Jaro distance measures
        //Try to optimise this function, pushing over cubic run time currently.
        public float analyseReport(String [] tokens, List<String> list){

                float jaccardAvg = 0;

                float [] jaccardTotalIndicator = new float[list.size()];

                for(int i = 0; i < list.size(); i++){

                        String currentIndicator = list.get(i);
                        String [] current = tokenize.tokenize(currentIndicator);
                        float jaccardTotal = 0;

                        for(int j = 0; j < tokens.length; j++){
                                //Look for similiarity by doing Fuzzy String analysis
                                jaccardTotal += measure.jaccard(tokens[i].toCharArray(),currentIndicator.toCharArray());
                        }

                        jaccardTotalIndicator[i] = jaccardTotal;
                }

                //calculate the averages
                for(int i = 0; i < jaccardTotalIndicator.length; i++){
                        jaccardAvg += jaccardTotalIndicator[i];
                }

                return jaccardAvg = jaccardAvg/jaccardTotalIndicator.length;
        }
}
