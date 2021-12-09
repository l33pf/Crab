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

/**
 * SentimentBasisRunnable:
 * This class takes a URL and then runs sentiment analysis on all child-links of the URL,
 * it will look for the optimally sentiment-best link and when finished will push the best
 * sentimentally link onto the URL stack.
 *
 * Created: 7/12/2021

 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Objects;

public final class SentimentBasisRunnable implements Runnable {

    String URL, bestLink;
    int bestSentiment, sentiment;

    public SentimentBasisRunnable(String link){
        Objects.requireNonNull(this.URL = link);
    }

    public void run(){

        try{
            final Document doc = Jsoup.connect(URL).get();
            System.out.println("Doing analysis on: " + URL + "\n");
            Document docTwo;
            final Elements links = doc.select("a[href]");

            for(Element link : links){
                docTwo = Jsoup.connect(link.attr("abs:href")).get();

                    final String linkTitle = docTwo.title();

                    if(Crab.visitedList.contains(link.attr("abs:href"))){
                        //do nothing - want to avoid this as we are wasting cycles
                    }else{
                        System.out.println("Visited: " + link.attr("abs:href") + "\n");

                        Crab.visitedList.add(link.attr("abs:href"));

                        sentiment = SentimentAnalyser.analyse(linkTitle);

                        Utility.writeURLSentimentResult(link.attr("abs:href"),sentiment,linkTitle);

                        if(sentiment > bestSentiment){
                            bestSentiment = sentiment; //change the optimal
                            bestLink = link.attr("abs:href");
                            Utility.writeURLOptimalSentimentResult(bestLink,bestSentiment,Jsoup.connect(bestLink).get().title()); //test
                            System.out.println("Best Sentiment Link for: " + URL + " currently: " + bestLink + "\n" );
                        }
                    }
            }
            final String title = (Jsoup.connect(bestLink).get()).title();
            Utility.writeURLOptimalSentimentResult(bestLink,bestSentiment,title);

            Crab.urlStack.safePush(bestLink);

        }catch(Exception e){

        }
    }
}
