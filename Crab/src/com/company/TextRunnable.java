package com.company;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public final class TextRunnable implements Runnable{

    private final String textToAnalyse;
    private TreeMap<Integer,String> textMap = new TreeMap<Integer,String>();
    private List<String> indicatorWords = new ArrayList<String>();
    final TreeMap<String,Float> resultMap = new TreeMap<String,Float>();

    public TextRunnable(String txtA, TreeMap<Integer,String> map, List<String> keyIndicatorWords){
        Objects.requireNonNull(this.textToAnalyse = txtA);
        Objects.requireNonNull(this.textMap = map);
        Objects.requireNonNull(this.indicatorWords = keyIndicatorWords);
    }

    public void run(){

        //Create a Text Analysis objects
        final TA textAnalysis = new TA(textMap);

        String current;

        float [] results = new float[textMap.size()];

        for(int i = 0; i < textMap.size(); i++){
            current = textMap.get(i);

            String [] add = textAnalysis.tokenizePage(current);

            results[i] = textAnalysis.analyseReport(add,indicatorWords);

            resultMap.put(current,results[i]);
        }

        /*Write results to CSV */


    }
}
