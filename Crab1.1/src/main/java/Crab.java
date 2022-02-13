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

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.opencsv.exceptions.CsvException;

public final class Crab {

    public static final CrabStack urlStack = new CrabStack();

    private static final int numOfThreads = (Runtime.getRuntime().availableProcessors())+1;
    private static final int CAPACITY = 10;

    public static boolean writeJson = true;

    public static final Queue<String> visitedList = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String,SentimentType> con_map = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String,SentimentType> full_sentiment_map = new ConcurrentHashMap<>();

    /* For Keyword article gathering */
    public static final ConcurrentHashMap<String,ConcurrentHashMap<String,SentimentType>> keywordDb = new ConcurrentHashMap<>();
    public static final HashSet<String> keyWords = new HashSet<>();
    public static boolean keyWordCrawl = true;

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

    public static void CrabCrawl() throws IOException, CsvException, ClassNotFoundException, InterruptedException {

        /* Read in the URL Seed set supplied into a stack */
        Utility.readIn(urlStack);

        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        if(urlStack.size()==0){
            throw new IllegalArgumentException("no URL Seed set supplied. \n");
        }

        con_map = Utility.DeserializeConMap();

        if(keyWordCrawl){
            System.out.println("Doing Keyword Crawl. \n");
            while(urlStack.size() != 0){
                exec.submit(new SentimentKeyWordRunnable(urlStack.safePop()));
            }
        }else{
            while(urlStack.size() != 0){
                exec.submit(new SentimentBasisRunnable(urlStack.safePop())); //need to add in full crawl to
            }
        }

        exec.shutdown();

        if(keyWordCrawl){
            if(Crab.writeJson){
                    Utility.writekeyWordResults_ToJSON(keywordDb);
            }
        }else{
            if(Crab.writeJson){
                Utility.writeResultsToJSON(con_map,Utility.RESULTS_JSON_PATH);
            }
        }

  //      Utility.SerializeConMap(con_map);
  //      Utility.SerializeConMap(full_sentiment_map,"f_map_ser");
    }
}