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
/*
 * SentimentBasisRunnable:
 * This class takes a URL and then runs sentiment analysis on all child-links of the URL,
 * it will look for the optimally sentiment-best link and when finished will push the best
 * sentimentally link onto the URL stack.
 *
 * Created: 7/12/2021
 * Edited: 4/3/2022 - Added better Threading capabilities (i.e. to avoid race conditions)
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Objects;

import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public class SentimentBasisRunnable implements Runnable{

    String URL, bestLink;
    volatile int bestSentiment, sentiment;
    HashMap<String,Integer> map = new HashMap<>();
    final Queue<String> test = Crab.b_list;
    ConcurrentLinkedQueue<String> testTwo = Crab.parent_set;

    public SentimentBasisRunnable(final String link){
        Objects.requireNonNull(this.URL = link);
    }

    public boolean checkBlocked(final String link){

        try{
            URI uri = new URI(link);
            for(String b_url : test){
                //have very similiar URL's
                if(FuzzySearch.ratio(uri.getHost(),b_url) > 90){
                    return true;
                }
            }
        }catch(Exception ex){
            return false;
        }
        return false;
    }

    public void run(){

       //Thread thread = Thread.currentThread();

        boolean blocked_link;

        final String sanitised_url = URL.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");

        try{
            //if(!Crab.v_list.contains(sanitised_url) || (Crab.parent_set.contains(URL))){
            if(!Utility.checkValInQueue(Crab.v_list,sanitised_url) || Crab.parent_set.contains(URL)){
                final Document doc = Jsoup.connect(URL).get();
                System.out.println("Visiting: " + URL + "\n");
                Document docTwo;
                final Elements links = doc.select("a[href]");

                for(Element link : links){
                    blocked_link = false;

                    docTwo = Jsoup.connect(link.attr("abs:href")).get();

                    //check the URL isn't contained in the block list
                    if(checkBlocked(link.attr("abs:href"))){
                        blocked_link = true;
                    }

                    if(!blocked_link){

                        String title = docTwo.title();

                        sentiment = SentimentAnalyser.analyse(title);

                        if(!Utility.checkValInQueue(Crab.v_list,link.attr("abs:href").replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)",""))){
                            System.out.println("Visited: " + link.attr("abs:href") + " " +  "Parent: " + URL + "\n");
                            Crab.v_list.add(link.attr("abs:href").replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)",""));
                            Utility.writeVisitList(link.attr("abs:href"));
                        }

                        if(!map.containsKey(link.attr("abs:href"))){ //avoids adding a link more than once for the optimal calculation
                            map.put(link.attr("abs:href"),sentiment);
                        }

                        Utility.writeSentimentResult(link.attr("abs:href"),SentimentType.fromInt(sentiment));

                        if(Crab.optimalDepth){ //Note this is only done if the user wants to pursue an aggressive optimal crawl strategy
                            Crab.urlStack_LF.push(link.attr("abs:href"));
                        }

                    }
                }

                //Calculate the optimal link from the set of links
                for(final String link : map.keySet()){
                    if(map.get(link) > bestSentiment){
                        bestLink = link;
                        bestSentiment = map.get(link);
                    }
                    System.out.println("done");
                }

                System.out.println("Best Sentiment link for parent URL : " + URL + " " + bestLink);
                if(!Utility.checkValInQueue(Crab.optimalURLrecord,bestLink)){
                    Crab.optimalURLrecord.add(bestLink);
                    Utility.writeURLOptimalSentimentResult(bestLink,bestSentiment,Jsoup.connect(bestLink).get().title());
                }

            }
        }catch(Exception ex){

        }
    }
}