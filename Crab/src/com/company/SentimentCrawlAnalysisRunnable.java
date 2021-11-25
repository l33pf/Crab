package com.company;

/*
***LICENSE***
        Copyright (c) 2021 l33pf (https://github.com/l33pf)

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

import java.util.Objects;

/**
 * SentimentCrawlAnalysisRunnable.java
 * This class is used to do full sentiment analysis on Extracted text from a webpage.
 * Runs better on webpages which have <article> tags. Currently using CoreNLP note: this
 * may change based on results.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class SentimentCrawlAnalysisRunnable implements Runnable {

    String URL;
    String article = null;

    SentimentCrawlAnalysisRunnable(String linkToPage){
        Objects.requireNonNull(this.URL = linkToPage);
    }

    public void run(){

        try{

            Document doc = Jsoup.connect(URL).get();
            Elements content = doc.select("article");
            Elements contents = content.select("p");

            for(Element e : contents){
                article = article.concat(e.text());
            }

            switch(SentimentType.fromInt(SentimentAnalyser.analyse(article))){

                case POSITIVE :
                    Crab.put(URL,SentimentType.POSITIVE);
                    break;

                case VERY_POSITIVE:
                    Crab.put(URL,SentimentType.VERY_POSITIVE);
                    break;
            }

        }catch(Exception e){
                if(Crab.logging){

                }
        }


    }

}
