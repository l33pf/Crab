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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import com.opencsv.exceptions.CsvException;

public final class Crab {

    public static final CrabStack urlStack = new CrabStack();

    public static final CrabStack_LF urlStack_LF = new CrabStack_LF();

    private static final int numOfThreads = (Runtime.getRuntime().availableProcessors())+1;
    private static final int CAPACITY = 10;

    public static boolean writeJson = true;

    public static ConcurrentLinkedQueue<String> v_list = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String,SentimentType> con_map = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String,SentimentType> full_sentiment_map = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String,Boolean> record_map = new ConcurrentHashMap<>();
    public static ConcurrentLinkedQueue<String> optimalURLrecord = new ConcurrentLinkedQueue<>();

    /* For Keyword article gathering */
    public static final ConcurrentHashMap<String,ConcurrentHashMap<String,SentimentType>> keywordDb = new ConcurrentHashMap<>();
    public static final HashSet<String> keyWords = new HashSet<>();
    public static boolean keyWordCrawl = false;

    public static ConcurrentLinkedQueue<String> b_list = new ConcurrentLinkedQueue<>();

    /* For Keyword Crawl */
    public static ConcurrentLinkedQueue<KeywordClass> keyWordQueue = new ConcurrentLinkedQueue<>();

    public static ThreadPoolExecutor exec = new ThreadPoolExecutor(numOfThreads, numOfThreads,
            1L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(CAPACITY),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    Crab() throws IOException {
        Utility.SerializeConMap(con_map,"con_map.ser");
        Utility.SerializeConMap(full_sentiment_map,"f_map_ser");
        Utility.SerializeQueue(v_list,"vlist.ser");
        Utility.SerializeRecordMap(record_map,"r_map.ser");
    }

    public static void CrabCrawl() throws IOException, CsvException, ClassNotFoundException {

        /* Read in the URL Seed set supplied into a stack */
        Utility.readIn(urlStack);
        Utility.readIn_LF(urlStack_LF);

        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        if(urlStack.size()==0){
            //logger.error("No URL seed set supplied to crawler");

            throw new IllegalArgumentException("no URL Seed set supplied. \n");
        }

        //Deserialize data structures
        v_list = Utility.DeserializeQueue("vlist.ser");
        con_map = Utility.DeserializeConMap("con_map.ser"); //bug in con map, not loading correctly
        record_map = Utility.DeserializeRecordMap("r_map.ser");

/*        if(keyWordCrawl){
            System.out.println("Doing Keyword Crawl. \n");
            while(!urlStack_LF.isEmpty()){
                exec.submit(new SentimentKeyWordRunnable(urlStack_LF.p op()));
            }*/
        //     }else{
        while(!urlStack_LF.isEmpty()){
            exec.submit(new SentimentBasisRunnable(urlStack_LF.pop()));
        }
        //     }

        exec.shutdown();

        Utility.SerializeConMap(con_map,"con_map.ser");
        Utility.SerializeQueue(v_list,"vlist.ser");
        Utility.SerializeRecordMap(record_map,"r_map.ser");

        if(keyWordCrawl){
            if(Crab.writeJson){
                Utility.writekeyWordResults_ToJSON(keywordDb);
            }
        }else{
            if(Crab.writeJson){
                Utility.writeResultsToJSON(con_map,Utility.RESULTS_JSON_PATH);
            }
        }
    }
}