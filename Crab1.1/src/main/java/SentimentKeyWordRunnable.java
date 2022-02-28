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
 * Runnable class takes a URL and then searches for input-defined keywords looking for the individual links of the URL for
 * a keyword match. If a match is found sentiment analysis is applied and then the result is added to the keyword's
 * associated KeywordClass building up an overall sentiment profile for matched keyword links.
 *
 * Take note that whatever the sentiment result from a keyword matched page (i.e. positive/negative) will mean it is added
 * to the URL stack for crawling where as for SentimentBasisRunnable only positive/neutral pages are crawled.
 *
 * Created: 16/1/2022
 * Edited: 27/02/2022 - by l33pf

 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.util.Objects;

public class SentimentKeyWordRunnable implements Runnable {

    String link;
    int sentiment, m_sentiment;

    SentimentKeyWordRunnable(final String URL){
        Objects.requireNonNull(this.link = URL);
    }

    public void run(){

        try{
            final Document doc = Jsoup.connect(link).get();

            Document docTwo;

            final Elements links = doc.select("a[href]");

            for(Element e : links){

                //if no other thread has crawled this page
                if(!Crab.v_list.contains(e.attr("abs:href"))){

                    //connect to get the metadata
                    docTwo = Jsoup.connect(e.attr("abs:href")).get();

                    for(KeywordClass c : Crab.keyWordQueue){

                        //If the link title contains one of the keywords
                        if(docTwo.title().contains(c.keyword)){

                            System.out.println("Match found in: " + e.attr("abs:href"));

                            sentiment = SentimentAnalyser.analyse(doc.title()); //run sentiment analysis on the title

                            //get further content from the metadata and run sentiment analysis
                            m_sentiment = SentimentAnalyser.analyse(docTwo.select("meta[name=description]").get(0)
                                    .attr("content"));

                            if(sentiment >= m_sentiment){ //compare the two sentiments

                                switch (SentimentType.fromInt(sentiment)) {
                                    case VERY_POSITIVE, POSITIVE -> c.positiveSentiment.put(e.attr("abs:href"), LocalDate.now());
                                    case NEUTRAL -> c.neutralSentiment.put(e.attr("abs:href"), LocalDate.now());
                                    case VERY_NEGATIVE, NEGATIVE -> c.negativeSentiment.put(e.attr("abs:href"), LocalDate.now());
                                }
                            }else {

                                switch (SentimentType.fromInt(m_sentiment)) {
                                    case VERY_POSITIVE, POSITIVE -> c.positiveSentiment.put(e.attr("abs:href"), LocalDate.now());
                                    case NEUTRAL -> c.neutralSentiment.put(e.attr("abs:href"), LocalDate.now());
                                    case VERY_NEGATIVE, NEGATIVE -> c.negativeSentiment.put(e.attr("abs:href"), LocalDate.now());
                                }
                            }
                            //Further interested given the keyword match,
                            // so add to the stack to look at it's links to assess further
                            Crab.urlStack_LF.push(e.attr("abs:href"));

                            //debating whether to add break here
                            //given if we have articles where we may have multiple keywords its more likely to be a feature
                            //i.e. a recap of a bunch of assets or games over some period of time.
                            break;
                        }
                    }
                }
                //add to the visit list so we avoid re-visiting it regardless of matching state of the link
                Crab.v_list.add(e.attr("abs:href"));
            }
        }catch(Exception ex){
                //log4j
        }
    }
}