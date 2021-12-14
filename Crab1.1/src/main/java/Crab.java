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

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.opencsv.exceptions.CsvException;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public final class Crab {

    public static final CrabStack urlStack = new CrabStack();

    private static final int numOfThreads = (Runtime.getRuntime().availableProcessors())+1;
    private static final int CAPACITY = 10;

    public static boolean writeJson = true;

    public static final Queue<String> visitedList = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String,SentimentType> con_map = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String,SentimentType> full_sentiment_map = new ConcurrentHashMap<>();


    public static ThreadPoolExecutor exec = new ThreadPoolExecutor(numOfThreads, numOfThreads,
            10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(CAPACITY),
            Executors.defaultThreadFactory(),
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());


    Crab() throws IOException {
        Utility.SerializeConMap(con_map);
        Utility.SerializeConMap(full_sentiment_map,"f_map_ser");
    }

    public static int analyseFullPage(final String URL){

        int sentiment = 0;
        int headerSentiment = 0;
        int hSentiment;

        try{
            final Document doc = Jsoup.connect(URL).get();
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
                return Math.max(headerSentiment, hSentiment);
            }else if(headerSentiment < sentiment){
                if(headerSentiment < hSentiment){
                    return hSentiment;
                }
                return sentiment;
            }else{
                return sentiment;
            }

        }catch(Exception e){

        }
        return 0;
    }

    public static void CrabCrawl() throws IOException, CsvException, ClassNotFoundException, InterruptedException {

        /* Read in the URL Seed set supplied into a stack */
        Utility.readIn(urlStack);

        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        if(urlStack.size()==0){
            throw new IllegalArgumentException("no URL Seed set supplied. \n");
        }

        con_map = Utility.DeserializeConMap();

        while(urlStack.size() != 0){
            exec.submit(new SentimentBasisRunnable(urlStack.safePop())); //need to add in full crawl to
        }

        exec.shutdown();

        if(Crab.writeJson){
            Utility.writeResultsToJSON(con_map);
        }

  //      Utility.SerializeConMap(con_map);
  //      Utility.SerializeConMap(full_sentiment_map,"f_map_ser");
    }
}