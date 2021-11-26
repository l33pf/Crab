package com.company;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.opencsv.exceptions.CsvException;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.apache.log4j.Logger;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public final class Crab {

    public static List<String> nonVisitURLs = new ArrayList<>();

    public final static Stack urlStack = new Stack();

    public final String INDICATOR_FILE_PATH = "./Indicators.csv";
    public final String KEYWORD_FILE_PATH = "./KeyWords.csv";

    private static final int numOfThreads = (Runtime.getRuntime().availableProcessors())+1;
    private static final int CAPACITY = 5;

    public static final Queue<String> keyWordsList = new ConcurrentLinkedQueue<String>();
    public static final Queue<String> visitedList = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String,SentimentType> con_map = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String,SentimentType> full_doc_map = new ConcurrentHashMap<>();

    /* Setting this will record logging in Crab */
    public static final boolean logging = false;
    public final static Logger logger = Logger.getLogger(Crab.class);

    final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    final static Lock r = rwl.readLock();
    final static Lock w = rwl.writeLock();

    /* Variables for Average Sentiment Crawl */
    public static final AtomicInteger bestSentiment = new AtomicInteger(0);
    public static ConcurrentHashMap<String,Integer> avg_sentiment = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String,Integer> data = new ConcurrentHashMap<>();

    public static Crawl_Type crab_crawl_type;

    Crab() throws IOException {
        Utility.SerializeConMap(con_map);
    }

    /*
        Re-entrant lock method for placing sentiment results into data structure.
     */
    public static void put(String val, SentimentType sentiment){
        r.lock();
        try{
            switch (crab_crawl_type) {
                case Sentiment -> con_map.putIfAbsent(val, sentiment);
                case FullSentiment -> full_doc_map.putIfAbsent(val, sentiment);
            }
        }finally {
            r.unlock();
        }
    }

    public static void sentiment(final String URL){

        try{

            Document doc = Jsoup.connect(URL).get();
            String toAnalyse = doc.title();

            switch(SentimentType.fromInt(SentimentAnalyser.analyse(toAnalyse))){

                case NEUTRAL:
                    System.out.println("Added: " + URL + "\n");
                    put(URL,SentimentType.NEUTRAL);
                    break;

                case POSITIVE:
                    System.out.println("Added: " + URL + "\n");
                    put(URL,SentimentType.POSITIVE);
                    break;
            }
        } catch (IOException e) {
            if(Crab.logging){

            }
        }
    }

    public static void full_sentimentKeyword(final String URL){

        try{

            Document doc = Jsoup.connect(URL).get();
            String toAnalyse = doc.title();

            switch(SentimentType.fromInt(SentimentAnalyser.analyse(toAnalyse))){

                case POSITIVE:
                    put(URL,SentimentType.POSITIVE);
                    if(!visitedList.contains(URL))
                    {
                        urlStack.safePush(URL);
                        System.out.println("Added: " + URL + "\n");
                    }
                    visitedList.add(URL);
                    break;

                case NEGATIVE:
                    put(URL,SentimentType.NEGATIVE);
                    if(!visitedList.contains(URL)){
                        urlStack.safePush(URL);
                        System.out.println("Added: " + URL + "\n");
                    }
                    visitedList.add(URL);
                    break;
            }

        } catch (Exception e){
            if(Crab.logging){

            }
        }
    }

    public static void avg_crawl_rec(final Stack urlStack) throws InterruptedException, IOException, ClassNotFoundException, CsvException {
            int optimal = 0;
            String link_title;
            int sentiment_score = 0;
            String optimal_page = null;

            while(urlStack.size() != 0){

                String URL = urlStack.pop();

                try{
                    Document doc = Jsoup.connect(URL).get();
                    Elements links = doc.select("a[href]");

                    for(Element link : links){
                        link_title = link.attr("abs:href");
                        sentiment_score += SentimentAnalyser.analyse(link_title);
                    }

                    if(sentiment_score > optimal){
                        optimal = sentiment_score;
                        optimal_page = URL;
                        System.out.println("New Optimal URL: " + optimal_page);
                    }
                }catch(Exception e){
                    if(logging){
                    }
                }
            }

            /* Push the optimal URL onto the stack */
            urlStack.push(optimal_page);
            Crab.Crawl_Type c = Crawl_Type.Sentiment;
            CrabCrawl(c); /* Move onto a normal sentiment  */
    }

    public static void CrabCrawl(Crawl_Type crawl) throws IOException, CsvException, ClassNotFoundException, InterruptedException {

        /* Read in the URL Seed set supplied into a stack */
        URLSeed.readIn(urlStack);

        if(urlStack.size()==0){
            if(Crab.logging){
                logger.error("URL stack size given is size zero");
            }

            throw new IllegalArgumentException("no URL Seed set supplied. \n");
        }

        ThreadPoolExecutor exec = new ThreadPoolExecutor(numOfThreads/2, numOfThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(CAPACITY),
                Executors.defaultThreadFactory(),
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        switch(crawl){

            /* This option will do a single sentiment crawl based on what is within the URL seed set */
            case Sentiment:
                crab_crawl_type = Crawl_Type.Sentiment;

                con_map = Utility.DeserializeConMap();

                while(urlStack.size() != 0){
                    exec.submit(new SentimentCrawlBasisRunnable(urlStack.safePop()));
                }

                break;

            /* This option does a full sentiment crawl */
            case FullSentiment:
                crab_crawl_type = Crawl_Type.FullSentiment;

                con_map = Utility.DeserializeConMap();

                while(urlStack.size() != 0){
                    exec.submit(new SentimentCrawlRunnable(urlStack.safePop()));
                }

                break;

            /* This option does a single sentiment crawl based on keywords provided  */
            case keyWordSentiment:

             //   con_map = Utility.DeserializeConMap();

                while(urlStack.size() != 0){
                    /* Each thread reads the key words defined by the user */
                    exec.submit(new SentimentCrawlKeywordRunnable(urlStack.safePop()));
                }

                break;

            case AverageSentiment:

                avg_crawl_rec(urlStack);


                break;
        }

        if((crawl == Crawl_Type.Sentiment) || (crawl == Crawl_Type.FullSentiment) || (crawl == Crawl_Type.keyWordSentiment)){
            exec.shutdown();
        }

        Utility.writeToCSV(con_map);
        Utility.SerializeConMap(con_map);
    }

    /**
     * This enum lists the options available in Crab.
     * used to reflect user choices and start the job centre.
     * can be amended if new features are added.
     */
    public enum OPTIONS{
        Crawl,
        Info,
        ChangeCandidate,
        Blocked,
        Stall,
        CrawlType,
        Help
    }

    /**
     * Enum for the Crawl Type used
     */
    public enum Crawl_Type {
        Sentiment,
        FullSentiment,
        keyWordSentiment,
        AverageSentiment
    }

}
