package com.company;

import java.lang.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/** CrawlRunnable.java
 *  This is the thread class which applies the top-level search on the links gathered from the URL Seed set.
 *  From here, links are stored which match the user-defined threshold and then sent for further textual analysis.

 */
public final class CrawlRunnable implements Runnable {

    Stack links = new Stack();
    private List<String> keyWords = new ArrayList<String>();
    public TreeMap<Integer,String> linksToAnalyse = new TreeMap<Integer,String>();
    int threshold = 0, counter = 0, result = 0;

    public CrawlRunnable(Stack links, List<String> words, int matchThreshold){
        Objects.requireNonNull(this.links = links);
        Objects.requireNonNull(this.keyWords = words);
        Objects.requireNonNull(this.threshold = matchThreshold);
    }

    //This function looks for the key word indicators set
    //Returns the overall score of the page looked at
    private final int searchText(String body, final List<String> keyWords ){

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
