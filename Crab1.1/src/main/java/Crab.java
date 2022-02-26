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

public final class Crab {

    public static final CrabStack_LF urlStack_LF = new CrabStack_LF();

    private static final int numOfThreads = (Runtime.getRuntime().availableProcessors())+1;
    private static final int CAPACITY = 10;

    public static boolean writeJson = true;

    public static Queue<String> visitedList = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String,SentimentType> con_map = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String,SentimentType> full_sentiment_map = new ConcurrentHashMap<>();

    /* For Keyword article gathering */
    public static final ConcurrentHashMap<String,ConcurrentHashMap<String,SentimentType>> keywordDb = new ConcurrentHashMap<>();
    public static final HashSet<String> keyWords = new HashSet<>();
    public static boolean keyWordCrawl = false;

    /* Used to store URL's that do not want/needed to be crawled */
    public static HashSet<String> blockList = new HashSet<>();

    public static ThreadPoolExecutor exec = new ThreadPoolExecutor(numOfThreads, numOfThreads,
            10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(CAPACITY),
            Executors.defaultThreadFactory(),
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

    Crab() throws IOException {
        Utility.SerializeConMap(con_map);
        Utility.SerializeConMap(full_sentiment_map,"f_map_ser");
    }

    public static void CrabCrawl() throws IOException, CsvException, ClassNotFoundException {

        /* Read in the URL Seed set supplied into a stack */
        Utility.readIn(urlStack_LF);

        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        if(urlStack_LF.isEmpty()){
            //logger.error("No URL seed set supplied to crawler");

            throw new IllegalArgumentException("no URL Seed set supplied. \n");
        }

        //Deserialize data structures
        visitedList = Utility.DeserializeQueue();
        con_map = Utility.DeserializeConMap();

      //  con_map = Utility.DeserializeConMap_json("./con_map_ser.json");

        if(keyWordCrawl){
            System.out.println("Doing Keyword Crawl. \n");
            while(!urlStack_LF.isEmpty()){
                exec.submit(new SentimentKeyWordRunnable(urlStack_LF.pop()));
            }
        }else{
            while(!urlStack_LF.isEmpty()){
                exec.submit(new SentimentBasisRunnable(urlStack_LF.pop()));
            }
        }

        exec.shutdown();

       // Utility.SerializeConMap_json(con_map,"con_map_ser.json");
        Utility.SerializeConMap(con_map);

        if(keyWordCrawl){
            if(Crab.writeJson){
                    Utility.writekeyWordResults_ToJSON(keywordDb);
            }
        }else{
            if(Crab.writeJson){
                Utility.writeResultsToJSON(con_map,Utility.RESULTS_JSON_PATH);
            }
        }

          Utility.SerializeQueue(visitedList);

    }
}