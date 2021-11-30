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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;

/** SentimentCrawlBasis.java
 * This class is used to implement a basic Sentiment Crawl.
 * It takes a link from the URL Seed set provided as a String, then runs sentiment analysis on the associated links
 * If the sentiment is deemed good, Crab will store the link.
 */

public final class SentimentCrawlBasisRunnable implements Runnable {

    String current;

    SentimentCrawlBasisRunnable(final String link) {
        Objects.requireNonNull(this.current = link);
    }

   public void run(){

       try{
           Document doc = Jsoup.connect(current).get();
           Elements links = doc.select("a[href]");
           String link_title;

           for (Element link : links) {
               link_title = link.attr("abs:href");
               Crab.sentiment(link_title);
           }

       }catch(Exception e){

       }
   }
}
