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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Objects;

public class SentimentFullRunnable implements Runnable{

    String url;

    SentimentFullRunnable(String link){
        Objects.requireNonNull(this.url = link);
    }

    public void run(){

        int sentiment = 0;
        int headerSentiment = 0;
        int hSentiment;

        try{

            final Document doc = Jsoup.connect(url).get();
            final Elements content = doc.select("article");
            Elements contents = content.select("p");
            final String heading = doc.head().text();
            hSentiment = SentimentAnalyser.analyse(heading);

            if (content.size() == 0) {
                contents = doc.select("p");

                for(Element e : contents){
                    sentiment += SentimentAnalyser.analyse(e.text());
                }
            } else {
                for (Element e : contents) {
                    sentiment += SentimentAnalyser.analyse(e.text());
                }
            }

            //Look at header tags for any further info, May get better accuracy
            final Elements hTags = doc.select("h1, h2, h3, h4, h5, h6");

            for(Element h : hTags){
                headerSentiment += SentimentAnalyser.analyse(h.text());
            }

            if(headerSentiment > sentiment){
                Crab.con_map.put(url,SentimentType.fromInt(Math.max(headerSentiment,hSentiment)));
            }else if(headerSentiment < sentiment){
                if(headerSentiment < hSentiment){
                    Crab.con_map.putIfAbsent(url,SentimentType.fromInt(hSentiment));
                }
                Crab.con_map.putIfAbsent(url,SentimentType.fromInt(sentiment));
            }else{
                Crab.con_map.putIfAbsent(url,SentimentType.fromInt(sentiment));
            }

        }catch(Exception e){

        }

    }

}
