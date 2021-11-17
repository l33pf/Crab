package com.company;

/*
***LICENSE***
        Copyright (c) 2021 l33pf (https://github.com/l33pf) & jelph (https://github.com/jelph)

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
**/

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Objects;

public class SentimentCrawlAverageRunnable implements Runnable {

    String URL;
    int totalLinks;
    String link_title;
    int counter = 0;
    int sentiment_score = 0;

    SentimentCrawlAverageRunnable(String link){
        Objects.requireNonNull(this.URL = link);
    }

    public void run() {

        try{

            Document doc = Jsoup.connect(URL).get();
            Elements links = doc.select("a[href]");
            totalLinks = links.size();

            for(Element link : links){
                link_title = link.attr("abs:href");
                sentiment_score += SentimentAnalyser.analyse(link_title);
                counter++;
            }

            // If The overall sentiment of the links is better than the current optimal score recorded
            // set that as the new local optimum, and record the parent URL for later
            if(sentiment_score > Crab.bestSentiment.get()){
                Crab.bestSentiment.set(sentiment_score);
                Crab.avg_sentiment.put(URL,sentiment_score);
            }

        }catch(Exception e){
            if(Crab.logging){
            }
        }
    }
}
