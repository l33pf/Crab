package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public final class TextRunnable implements Runnable{

    String textToAnalyse;
    TreeMap<Integer,String> textMap = new TreeMap<Integer,String>();
    ArrayList<String> test = new ArrayList<>();
    List<String> indicatorWords = new ArrayList<String>();

    public TextRunnable(String txtA, TreeMap<Integer,String> map, List<String> keyIndicatorWords){
        this.textToAnalyse = txtA;
        this.textMap = map;
        this.indicatorWords = keyIndicatorWords;
    }

    public void run(){

        //Create a Text Analysis objects
        TA textAnalysis = new TA(textMap);

        String current;

        float [] results = new float[textMap.size()];

        for(int i = 0; i < textMap.size(); i++){
            current = textMap.get(i);

            String [] add = textAnalysis.tokenizePage(current);

            results[i] = textAnalysis.analyseReport(add,indicatorWords);
        }




    }
}
