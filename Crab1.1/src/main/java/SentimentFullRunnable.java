/*
 ***LICENSE***
Copyright 2022 l33pf (https://github.com/l33pf)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
