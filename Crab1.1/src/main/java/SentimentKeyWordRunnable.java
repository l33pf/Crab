/*
 ***LICENSE***
 Copyright (c) 2022 l33pf (https://github.com/l33pf)

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

                docTwo = Jsoup.connect(linkage.attr("abs:href")).get();

                for(String keyword : Crab.keyWords){
                    if(docTwo.title().contains(keyword)){
                        sentiment = SentimentAnalyser.analyse(doc.title()); //run sentiment analysis on the title

                        if(SentimentType.fromInt(sentiment) != SentimentType.NEGATIVE || SentimentType.fromInt(sentiment) != SentimentType.VERY_NEGATIVE){
                            //Messy way but works
                            ConcurrentHashMap<String,SentimentType> inner_map = new ConcurrentHashMap<>();
                            inner_map.put(link,SentimentType.fromInt(sentiment));
                            Crab.keywordDb.put(keyword,inner_map);
                        }

                        //add the link to the crawler stack to see if any further relevant links can be found
                        Crab.urlStack.safePush(linkage.attr("abs:href"));

                        //break out of the loop given a keyword has been found for this link
                        break;
                    }
                }
            }
        }catch(Exception e){

        }
    }
}