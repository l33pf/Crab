package com.company;

import java.lang.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class CrawlRunnable implements Runnable {

    Stack links = new Stack();
    public List<String> keyWords = new ArrayList<String>();
    public TreeMap<Integer,String> linksToAnalyse = new TreeMap<Integer,String>();
    int threshold = 0, counter = 0, result = 0;

    public CrawlRunnable(Stack links, List<String> words, int matchThreshold){
            this.links = links;
            this.keyWords = words;
            this.threshold = matchThreshold;
    }

    //This function looks for the key word indicators set
    //Returns the overall score of the page looked at
    public int searchText(String body, List<String> keyWords ){

        int score = 0;

        for(String word : keyWords){
            if(body.contains(word)){
                score++;
            }
        }

        return score;
    }

    public void run(){

            while(links.size() != 0){

                String current = links.pop();

                System.out.println("Now Crawling: " + current + "\n" );
                counter++;
                result = 0;

                try{
                    Document doc = Jsoup.connect(current).get();
                    Element body = doc.body();

                    System.out.println("key word search commenced\n");

                    result = searchText(body.text().toString(), keyWords);

                    if(result>=threshold) {linksToAnalyse.put(counter,current); System.out.println("Match found");}

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
    }
}
