package com.company;

import opennlp.tools.tokenize.SimpleTokenizer;

import java.util.HashMap;

public final class TA
{
        SimpleTokenizer tokenize = SimpleTokenizer.INSTANCE;
        OverlapMeasures measure = new OverlapMeasures();
        boolean finished = false;
        float jaccardAvg = 0;
        double jaroAvg = 0.0;

        HashMap<Integer,String> indicatorWords = new HashMap<Integer,String>();
        HashMap<String,String> textData = new HashMap<String,String>();

        //Constructor for URL Seed
        TA(final HashMap<String,String> txtData, final HashMap<Integer,String> words){
                this.textData = txtData;
                this.indicatorWords = words;
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

        //This function takes each indicator word and analyses with the extracted text
        //Uses both Jaccard and Jaro distance measures
        //Try to optimise this function, pushing over cubic run time currently.
        boolean analyseReport(String [] tokens, HashMap<Integer,String> indicatorWords){

                float [] jaccardTotalIndicator = new float[indicatorWords.size()];
                double [] jaroTotalIndicator = new double[indicatorWords.size()];

                for(int i = 0; i < indicatorWords.size(); i++){

                        String currentIndicator = indicatorWords.get(i);
                        String [] current = tokenize.tokenize(currentIndicator);
                        float jaccardTotal = 0;
                        double jaroTotal = 0;

                        for(int j = 0; j < tokens.length; j++){
                                //Look for similiarity by doing Fuzzy String analysis
                                jaccardTotal += measure.jaccard(tokens[i].toCharArray(),currentIndicator.toCharArray());
                                jaroTotal += jaroDistance(tokens[i],currentIndicator);
                        }

                        jaccardTotalIndicator[i] = jaccardTotal;
                        jaroTotalIndicator[i] = jaroTotal;
                }

                //calculate the averages
                for(int i = 0; i < jaccardTotalIndicator.length; i++){
                        jaccardAvg += jaccardTotalIndicator[i];
                        for(int j = 0; i < jaroTotalIndicator.length; i++){
                                jaroAvg += jaroTotalIndicator[j];
                        }
                }

                jaccardAvg = jaccardAvg/jaccardTotalIndicator.length;
                jaroAvg = jaroAvg/jaroTotalIndicator.length;

                return finished = true;
        }
}
