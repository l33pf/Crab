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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.ArrayList;

import com.opencsv.exceptions.CsvException;

public final class Crab {

    public static final CrabStack_LF urlStack_LF = new CrabStack_LF();

    private static final int numOfThreads = (Runtime.getRuntime().availableProcessors())+1;
    private static final int coreSize = (numOfThreads % 2 == 0) ? numOfThreads/2 : (int)Math.floor(numOfThreads/2);
    private static final int CAPACITY = 100;
    private static final int EXECUTION_THRESHOLD = 5;

    public static boolean writeJson = true;
    public static boolean optimalDepth = false;

    public static  ConcurrentLinkedQueue<String> v_list = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> parent_set = new ConcurrentLinkedQueue<>();

    public static  ConcurrentHashMap<String,SentimentType> con_map = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String,SentimentType> full_sentiment_map = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String,Boolean> record_map = new ConcurrentHashMap<>();
    public static ConcurrentLinkedQueue<String> optimalURLrecord = new ConcurrentLinkedQueue<>();

    /* For Keyword article gathering */
    public static final ConcurrentHashMap<String,ConcurrentHashMap<String,SentimentType>> keywordDb = new ConcurrentHashMap<>();
    public static boolean keyWordCrawl = false;
    public static final ConcurrentLinkedQueue<String> b_list = new ConcurrentLinkedQueue<>();

    public static final ArrayList<String> tagsToSearch = new ArrayList<>();
    public static volatile ConcurrentLinkedQueue<String> downloadQueue = new ConcurrentLinkedQueue<>(); //Take note: will be replaced

    public static ConcurrentLinkedQueue<String> keywordVisitList = new ConcurrentLinkedQueue<>();

    public static ConcurrentLinkedQueue<Callable<Boolean>> fullSentimentTasks = new ConcurrentLinkedQueue<>();

    public static Queue<Future<Boolean>> q = new LinkedBlockingQueue<>();

    public static boolean runFullSentiment = true;
    
    /* For Keyword Crawl */
    public static ConcurrentLinkedQueue<KeywordClass> keyWordQueue = new ConcurrentLinkedQueue<>();

    public static ThreadPoolExecutor exec = new ThreadPoolExecutor(coreSize, numOfThreads,
            1L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(CAPACITY), // Bounded
            Executors.defaultThreadFactory(),
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

    Crab() throws IOException {

        if(!keyWordCrawl){
            Utility.SerializeConMap(con_map,"con_map.ser");
            Utility.SerializeConMap(full_sentiment_map,"f_map_ser");
            Utility.SerializeQueue(v_list,"vlist.ser");
            Utility.SerializeRecordMap(record_map,"r_map.ser");
        }else{
            Utility.SerializeQueue(keywordVisitList,"kw_v_list.ser");
        }
    }

    //uses a completion service to execute a queue of full sentiment tasks for keyword crawl
    public static void runFullSentimentTasks(ThreadPoolExecutor exec, ConcurrentLinkedQueue<Callable<Boolean>> tasks) throws InterruptedException, ExecutionException{
            CompletionService<Boolean> taskService = new ExecutorCompletionService<>(exec);
            int sz = tasks.size();

            tasks.forEach((Callable<Boolean> task)->{
                taskService.submit(task);
            });

            //We are checking each for the status of the tasks
            for(int i = 0; i < sz; ++i){
                  Future<Boolean> fut = taskService.take();
                  boolean result = fut.get();
                  if(!result){
                        //add to failed tasks queue
                        q.add(fut);
                  }
            }
    }

    public static void CrabCrawl() throws IOException, CsvException, ClassNotFoundException, URISyntaxException {

        /* Read in the URL Seed set supplied into a stack */
        Utility.readIn_LF(urlStack_LF);

       // exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        if(urlStack_LF.isEmpty()){
            //logger.error("No URL seed set supplied to crawler");
            throw new IllegalArgumentException("no URL Seed set supplied. \n");
        }

        //Deserialize data structures
        if(!keyWordCrawl){
            v_list = Utility.DeserializeQueue("vlist.ser");
            con_map = Utility.DeserializeConMap("con_map.ser");
            record_map = Utility.DeserializeRecordMap("r_map.ser");
            optimalURLrecord = Utility.DeserializeQueue("opt_url_list.ser");
        }else{
            keywordVisitList = Utility.DeserializeQueue("kw_v_list.ser");
        }

        URI uri;
        if(keyWordCrawl){

            while(!urlStack_LF.isEmpty()){
                String url = urlStack_LF.pop();

                try{
                    uri = new URI(url);

                    if(!b_list.contains(uri.getHost())){
                        exec.submit(new SentimentKeyWordRunnable(tagsToSearch,keyWordQueue,url));
                    }

                    if(fullSentimentTasks.size() >= EXECUTION_THRESHOLD){
                        runFullSentimentTasks(exec,fullSentimentTasks);
                    }

                }catch(Exception e){

                }
            }
        }else{

            while(!urlStack_LF.isEmpty()){
                String url = urlStack_LF.pop();

                try{
                    uri = new URI(url);

                    if(!b_list.contains(uri.getHost())){
                        exec.submit(new SentimentBasisRunnable(url));
                    }
                }catch(Exception e){

                }
            }
        }

        exec.shutdown();

        if(!keyWordCrawl){
            Utility.SerializeConMap(con_map,"con_map.ser");
            Utility.SerializeQueue(v_list,"vlist.ser");
            Utility.SerializeRecordMap(record_map,"r_map.ser");
            Utility.SerializeQueue(optimalURLrecord,"opt_url_list.ser");
        }else{
            Utility.SerializeQueue(keywordVisitList,"kw_v_list.ser");
        }

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