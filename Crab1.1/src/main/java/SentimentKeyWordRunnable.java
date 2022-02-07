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

/*
 * SentimentKeyWordRunnable
 * This runnable class takes a URL and then looks if the titles of its links contains a keyword then runs sentiment analysis and
 * based on the result will then add the link to the URL onto the crawler's stack. Note that the Crawl only goes for neutral-positive
 * links it doesn't look at any displaying negative sentiment (this can be changed).
 *
 * Created: 16/1/2022

 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentHashMap;

public class SentimentKeyWordRunnable implements Runnable {

    String link;
    int sentiment;

    SentimentKeyWordRunnable(String URL){
        this.link = URL;
    }

    public void run(){

        try{
            final Document doc = Jsoup.connect(link).get();
            Document docTwo;

            final Elements links = doc.select("a[href]");

            for(Element linkage : links){

                if(!Crab.visitedList.contains(linkage.attr("abs:href"))){

                    docTwo = Jsoup.connect(linkage.attr("abs:href")).get();

                    for(String keyword : Crab.keyWords){
                        if(docTwo.title().contains(keyword)){

                            System.out.println("Now visiting: " + linkage.attr("abs:href")+ "\n");

                            sentiment = SentimentAnalyser.analyse(doc.title()); //run sentiment analysis on the title

                            if(SentimentType.fromInt(sentiment) == SentimentType.POSITIVE || SentimentType.fromInt(sentiment) == SentimentType.VERY_POSITIVE){
                                //Messy way but works
                                ConcurrentHashMap<String,SentimentType> inner_map = new ConcurrentHashMap<>();
                                inner_map.put(link,SentimentType.fromInt(sentiment));
                                Crab.keywordDb.put(keyword,inner_map);
                                System.out.println("Positive Sentiment detected on Link: " + linkage.attr("abs:href") + "\n");
                            }

                            Utility.writeKeywordSentimentResult(keyword,linkage.attr("abs:href"),SentimentType.fromInt(sentiment));

                            //add the link to the crawler stack to see if any further relevant links can be found
                            Crab.urlStack.safePush(linkage.attr("abs:href"));

                            //break out of the loop given a keyword has been found for this link
                            break;
                        }
                    }
                }
                Crab.visitedList.add(linkage.attr("abs:href"));
            }
        }catch(Exception e){

        }
    }
}