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
*
 **/

/*
 * SentimentBasisRunnable:
 * This class takes a URL and then runs sentiment analysis on all child-links of the URL,
 * it will look for the optimally sentiment-best link and when finished will push the best
 * sentimentally link onto the URL stack.
 *
 * Created: 7/12/2021
 * Edited: 28/02/2022

 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Objects;

import java.net.URI;
import java.util.concurrent.Future;

public final class SentimentBasisRunnable implements Runnable {

    String URL, bestLink;
    int bestSentiment, sentiment;
    HashMap<String,SentimentType> map = new HashMap<>();
    Future fut;

    /**
     Used to calculate the sentiment distribution of all links associated to the parent URL
     */
    public void calculatePercentages(){
        int neg =0,pos =0,ntrl = 0;
        int N = map.size();

        for(String key : map.keySet()){

            switch (map.get(key)) {
                case POSITIVE -> pos++;
                case NEUTRAL -> ntrl++;
                case NEGATIVE -> neg++;
            }
        }

        double negPercent = ((double)neg/N)*100;
        double ntrlPercent = ((double)ntrl/N)*100;
        double posPercent = Math.floor((double)(pos/N)*100);

        Utility.writeSentimentDistribution(URL,Math.floor(negPercent),Math.floor(ntrlPercent),Math.floor(posPercent));
    }

    public SentimentBasisRunnable(String link){
        if(Crab.record_map.contains(link)){
            if(Crab.record_map.get(link)){ //if it's a parent set URL
                Objects.requireNonNull(this.URL = link);
            }
        }else{
            Objects.requireNonNull(this.URL = link);
        }
    }

    public void run(){

        boolean blocked_link = false;

        String sanitised_url = URL.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");

        try{
            if(!Crab.v_list.contains(sanitised_url)){

                final Document doc = Jsoup.connect(URL).get();
                System.out.println("Doing analysis on: " + URL + "\n");
                Document docTwo;

                final Elements links = doc.select("a[href]");

                for(Element link : links){

                    URI uri = new URI(link.attr("abs:href"));

                    docTwo = Jsoup.connect(link.attr("abs:href")).get();

                    if(!Crab.v_list.contains(link.attr("abs:href"))){

                        for(String b_url : Crab.b_list){

                            if(uri.getHost().contains(b_url)){
                                blocked_link = true;
                                Thread.currentThread().interrupt();
                            }
                        }

                        if(!blocked_link){

                            //if(!Crab.record_map.contains(link.attr("abs:href"))){
                                Crab.v_list.add(link.attr("abs:href").replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)",""));

                                System.out.println("Visited: " + link.attr("abs:href") + "\n");

                                Utility.writeVisitList(link.attr("abs:href"));
                                Crab.record_map.put(link.attr("abs:href"),false);
                          //  }

                            sentiment = SentimentAnalyser.analyse_th(docTwo.title());
                                    //SentimentAnalyser.analyse(docTwo.title());

                            if(sentiment > bestSentiment){

                                bestSentiment = sentiment;

                                String link_sanitised = link.attr("abs:href").replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");

                                if(!link.attr("abs:href").equals(bestLink)){
                                    bestLink = link.attr("abs:href");

                                    Crab.exec.submit(new SentimentFullRunnable(bestLink));
                                }

                                if(!Crab.optimalURLrecord.contains(link_sanitised)){ //Check another Thread hasn't already found this optimal link
                                    Crab.optimalURLrecord.add(link_sanitised);

                                    //Catches early parts of the crawl from writing out base URLs with very low sentiment scores as optimals
                                    if(SentimentType.fromInt(bestSentiment) != SentimentType.NEGATIVE || SentimentType.fromInt(bestSentiment) != SentimentType.VERY_NEGATIVE){

                                        if(!Crab.con_map.contains(bestLink)){
                                            Crab.con_map.put(bestLink,SentimentType.fromInt(bestSentiment));
                                            Utility.writeURLOptimalSentimentResult(bestLink,bestSentiment,Jsoup.connect(bestLink).get().title());
                                        }
                                    }
                                }
                                System.out.println("Best Sentiment Link for: " + URL + " currently: " + bestLink + "\n" );
                            }
                            Crab.urlStack_LF.push(bestLink);
                        }
                        final String title = (Jsoup.connect(bestLink).get()).title();
                        //Utility.writeURLOptimalSentimentResult(bestLink,bestSentiment,title);

                        //Crab.urlStack_LF.push(bestLink);
                    }
                }
            }
        }catch(Exception e){
        }
    }
}