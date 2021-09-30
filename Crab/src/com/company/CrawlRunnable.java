package com.company;

import java.lang.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CrawlRunnable implements Runnable {

    Stack links = new Stack();
    public List<String> keyWords = new ArrayList<String>();

    public CrawlRunnable(Stack links, List<String> words){
            this.links = links;
            this.keyWords = words;
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

                try{
                    Document doc = Jsoup.connect(current).get();
                    Element body = doc.body();

                    if(searchText(body.text().toString(),keyWords) > 3){
                        // Call a Text Analysis Thread
                        TextRunnable txtAnalysis = new TextRunnable(body.text().toString());
                        Thread t1 = new Thread();
                        t1.start();
                        // add log message
                        t1.join(); //wait for the thread to finish
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
    }
}
