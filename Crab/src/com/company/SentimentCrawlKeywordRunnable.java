package com.company;

import java.util.List;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class SentimentCrawlKeywordRunnable implements Runnable {

    List<String> keyWords;
    String URL;

    SentimentCrawlKeywordRunnable(String link){
        Objects.requireNonNull(this.URL = link);
    }

    public void run(){

        try{

            keyWords = Crab.keyWordsList;

            Document doc = Jsoup.connect(URL).get();
            Elements links = doc.select("a[href]");

            System.out.println("Analysing: +" + URL);

            for(Element link : links){

                Document linkDoc = Jsoup.connect(link.attr("abs:href")).get();

                for(String word : keyWords){

                    if(linkDoc.title().contains(word)){
                                Crab.sentiment(link.attr("abs:href"));
                    }
                }
            }

        }catch(Exception e){
                    if(Crab.logging){

                    }
        }
    }
}
