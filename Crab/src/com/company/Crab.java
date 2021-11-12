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
import com.opencsv.exceptions.CsvException;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.apache.log4j.Logger;

public final class Crab {

    public static List<String> nonVisitURLs = new ArrayList<>();

    public final static Stack urlStack = new Stack();

    public final String INDICATOR_FILE_PATH = "./Indicators.csv";
    public final String KEYWORD_FILE_PATH = "./KeyWords.csv";

    private static final int numOfThreads = (Runtime.getRuntime().availableProcessors())+1;
    private static final int CAPACITY = 5;

    public static final List<String> keyWordsList = new ArrayList<>();

    public static ConcurrentHashMap<String,SentimentType> con_map = new ConcurrentHashMap<>();

     /* Setting this will record logging in Crab */
     public static final boolean logging = false;
     public final static Logger logger = Logger.getLogger(Crab.class);

     Crab() throws IOException {
         Utility.SerializeConMap(con_map);
     }

    public static void sentiment(final String URL){

        try{
            Document doc = Jsoup.connect(URL).get();
            String toAnalyse = doc.title();

            switch(SentimentType.fromInt(SentimentAnalyser.analyse(toAnalyse))){

                case VERY_POSITIVE:
                        if(con_map.get(URL) == null){
                            System.out.println("Added: " + URL + "\n");
                            con_map.put(URL,SentimentType.VERY_POSITIVE);
                        }
                    break;

                case POSITIVE:
                    if(con_map.get(URL) == null){
                        System.out.println("Added: " + URL + "\n");
                        con_map.put(URL,SentimentType.POSITIVE);
                    }
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

                    case VERY_POSITIVE:
                        if(con_map.get(URL) == null){
                            con_map.put(URL,SentimentType.VERY_POSITIVE);
                            urlStack.safePush(URL);
                        }
                        break;

                    case POSITIVE:
                        if(con_map.get(URL) == null){
                            con_map.put(URL,SentimentType.POSITIVE);
                            urlStack.safePush(URL);
                        }
                        break;
                }

            } catch (Exception e){
                    if(Crab.logging){

                    }
            }
    }

    public static void CrabCrawl(final Crawl_Type crawl) throws IOException, CsvException, ClassNotFoundException {

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

      //          con_map = Utility.DeserializeConMap();

                while(urlStack.size() != 0){
                    exec.submit(new SentimentCrawlBasisRunnable(urlStack.safePop()));
                }

                break;

                /* This option does a full sentiment crawl */
            case FullSentiment:

                con_map = Utility.DeserializeConMap();

                while(urlStack.size() != 0){
                    exec.submit(new SentimentCrawlRunnable(urlStack.safePop()));
                }

                break;

                /* This option does a single sentiment crawl based on keywords provided  */
            case keyWordSentiment:

                con_map = Utility.DeserializeConMap();

                while(urlStack.size() != 0){
                    /* Each thread reads the key words defined by the user */
                    exec.submit(new SentimentCrawlKeywordRunnable(urlStack.safePop()));
                }

                break;
        }

        exec.shutdown();

        Utility.writeToCSV(con_map);
//        Utility.SerializeConMap(con_map);
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
        keyWordSentiment
    }

    /**
     * Assigns a job to the Crawler based on User input
     * @param opt
     */
    private static void jobCentre(OPTIONS opt) throws IOException, CsvException, ClassNotFoundException {

        Scanner reader = new Scanner(System.in);
        Crawl_Type crawl;

        /* Default Crawl Type */
        crawl = Crawl_Type.Sentiment;

        switch (opt) {
            case Crawl -> CrabCrawl(crawl);
            case Info -> info();
            case ChangeCandidate -> {
                System.out.println("Please enter the file path to the candidate CSV file \n");
                URLSeed.SAMPLE_CSV_FILE_PATH = reader.next();
                if (URLSeed.readIn(URLSeed.stack)) {
                    System.out.println("Candidate CSV changed");
                } else {
                    System.out.println("Unable to change candidate CSV");
                }
            }

            case Blocked -> {
                System.out.println("The following is the URL's not visited or analysed: \n");
                for (String nonVisitURL : nonVisitURLs) {
                    System.out.println(nonVisitURL);
                }
                System.out.println("to update the list please press u, or to exit press q");
                if (reader.next().contains("u")) {
                    updateNonVisitList(reader);
                } else {
                    reader.close();
                    UI();
                }
            }

            case CrawlType -> {
                System.out.println("The current crawl type is: " + crawl);
                System.out.println("Do you want to change the crawl type (y/n) ?\n");
                if (reader.next().contains("y")) {
                    switch (crawl) {

                        case Sentiment -> {
                            System.out.println("currently the crawl is a Sentiment Crawl\n");
                            System.out.println("the remaining crawl options to choose from are: \n");
                            System.out.println("Full Sentiment\n");
                            System.out.println("keyword Sentiment\n");
                            System.out.print("'fs' for Full sentiment, 'ks' for keyword sentiment \n");
                            if (reader.next().contains("fs")) {
                                crawl = Crawl_Type.FullSentiment;
                            } else if (reader.next().contains("ks")) {
                                crawl = Crawl_Type.keyWordSentiment;
                            } else {
                                UI();
                            }
                        }

                        case FullSentiment -> {
                            System.out.println("currently the crawl is a Full Sentiment Crawl\n");
                            System.out.println("the remaining crawl options to choose from are: \n");
                            System.out.println("Sentiment\n");
                            System.out.println("keyword Sentiment\n");
                            System.out.print("'s' for sentiment, 'ks' for keyword sentiment \n");
                            if (reader.next().contains("s")) {
                                crawl = Crawl_Type.Sentiment;
                            } else if (reader.next().contains("ks")) {
                                crawl = Crawl_Type.keyWordSentiment;
                            } else {
                                UI();
                            }
                        }

                        case keyWordSentiment -> {
                            System.out.println("currently the crawl is a Key word Sentiment Crawl\n");
                            System.out.println("the remaining crawl options to choose from are: \n");
                            System.out.println("Sentiment\n");
                            System.out.println("Full Sentiment\n");
                            System.out.print("'s' for sentiment, 'fs' for full sentiment \n");
                            if (reader.next().contains("s")) {
                                crawl = Crawl_Type.Sentiment;
                            } else if (reader.next().contains("fs")) {
                                crawl = Crawl_Type.FullSentiment;
                            } else {
                                UI();
                            }
                        }
                    }
                } else {
                    reader.close();
                    UI();
                }
            }
        }

    }

    private static void UI() throws IOException, CsvException, ClassNotFoundException {

        info();

        OPTIONS opt;
        opt = OPTIONS.Stall;

        Scanner reader = new Scanner(System.in);
        String userInput = reader.next();

        opt = switch (userInput) {
            case "C", "c" -> OPTIONS.Crawl;
            case "I", "i" -> OPTIONS.Info;
            case "CH", "ch" -> OPTIONS.ChangeCandidate;
            case "B", "b" -> OPTIONS.Blocked;
            case "CT", "ct" -> OPTIONS.CrawlType;
            case "H", "h" -> OPTIONS.Help;
            default -> opt;
        };

        reader.close();
        jobCentre(opt);

    }

    /**
     Method prints the info page out to console.
     */
    private static void info(){
        try (BufferedReader br = new BufferedReader(new FileReader("info.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            if(logging){
                logger.error("error in info()",e);
            }
        }
        System.out.println("\n");
    }

    /**
     * This method allows the user to update the Non-Visit list (i.e. Youtube) through the console.
     * @param reader
     */
    private static void updateNonVisitList(final Scanner reader){
        System.out.println("Type/paste in URL to add to the Non-Visit URL list. \n");
        System.out.println("to quit entering type qq \n");
        while(!reader.next().contains("qq")){
            nonVisitURLs.add(reader.next());
        }
        reader.close();
    }

}
