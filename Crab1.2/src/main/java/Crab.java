/*
 ***LICENSE***
Copyright 2022 https://github.com/l33pf

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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public final class Crab {
    public static final int DEFAULT_SIZE = 1000;

    public static ConcurrentLinkedQueue<String> blockedList = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> visitList = new ConcurrentLinkedQueue<>();
    public static ConcurrentHashMap<String,Boolean> parentSetMap = new ConcurrentHashMap<>(DEFAULT_SIZE);
    public static ConcurrentLinkedQueue<String> optimalURLrecord = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> keywordVisitList = new ConcurrentLinkedQueue<>();
    public static ArrayList<String> cTags = new ArrayList<>(DEFAULT_SIZE);

    public static ConcurrentHashMap<String,KeywordClass> keywordMap = new ConcurrentHashMap<>(DEFAULT_SIZE);

    public static ConcurrentLinkedQueue<String> urlQueue = new ConcurrentLinkedQueue<>();

    private static final int numOfThreads = (Runtime.getRuntime().availableProcessors())+1;
    private static final int coreSize = (numOfThreads % 2 == 0) ? numOfThreads/2 : (int)Math.floor(numOfThreads/2);
    private static final int CAPACITY = 100;

    public static Utility.Serialization sr = new Utility.Serialization();

    /**
     * @About setting this will configure Crab into Keyword Crawl mode
     * NOTE: you must supply keywords for the crawl to run.
     */
    public static boolean KEYWORD_CRAWL = false;

    /**
     * @About setting this flag will ensure every thread will push links
     * onto the URL stack for crawling within an optimal crawl.
     */
    public static boolean OPTIMAL_DEPTH = false;

    /**
     * @About setting this flag will run the thread pool at the maximum
     * number of available threads.
     */
    public static boolean FULL_UTILISATION = false;
    /********************************************************************\
    */

    public static ThreadPoolExecutor exec = new ThreadPoolExecutor((FULL_UTILISATION) ? numOfThreads : coreSize
            ,numOfThreads,
            1L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(CAPACITY), // Bounded
            Executors.defaultThreadFactory(),
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * @About Class for storing crawled info for a given keyword
     */
    public static class KeywordClass{
        final String keyword;

        /* Storage for articles based on their sentiment, with the date they were accessed */
        ConcurrentHashMap<String,Integer> negativeSentiment = new ConcurrentHashMap<>(DEFAULT_SIZE);
        ConcurrentHashMap<String,Integer> neutralSentiment = new ConcurrentHashMap<>(DEFAULT_SIZE);
        ConcurrentHashMap<String,Integer> positiveSentiment = new ConcurrentHashMap<>(DEFAULT_SIZE);

        /* Overall sentiment average of articles for the keyword in question */
        int average;

        KeywordClass(String keyWord){
            this.keyword = keyWord;
        }

        int calcAverageSentiment(){
            return average = (positiveSentiment.size()+neutralSentiment.size()+neutralSentiment.size())/3;
        }
    }

    private static class OptimalRunnable implements Runnable{

        //make blockList thread local ?
        OptimalRunnable(String link){this.URL = link;}
        String URL, bestLink;
        int bestSentiment, sentiment;
        HashMap<String,Integer> map = new HashMap<>();
        Queue<String> blockedList = Crab.blockedList;


        public void run(){

            final String sanitised_url = URL.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
            try{
                URI uri = new URI(URL);
                if(visitList.stream().noneMatch(str->str.matches(sanitised_url)) ||
                parentSetMap.contains(URL) || blockedList.stream().noneMatch(str->str.matches(uri.getHost()))){

                    visitList.add(sanitised_url);
                    final Document doc = Jsoup.connect(URL).get(); System.out.println("Visiting: " + URL + "\n");
                    final Elements links = doc.select("a[href]");

                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_VL_RECORD,new writerObj(sanitised_url));

                    links.forEach((Element link)->{
                        try{
                            final String childLink = link.attr("abs:href");
                            final Document docTwo =  Jsoup.connect(childLink).get();
                            URI child_uri = new URI(childLink);

                            if(blockedList.stream().noneMatch(str->str.matches(child_uri.getHost()))){
                                sentiment = Utility.SentimentAnalyser.analyse(docTwo.title());

                                final String sanitised = childLink.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
                                if(visitList.stream().noneMatch(str->str.matches(sanitised))){
                                    System.out.println("Visited: " + link.attr("abs:href") + " " +  "Parent: " + URL + "\n");
                                    visitList.add(sanitised);
                                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_VL_RECORD,sanitised);
                                    if(!map.containsKey(childLink)){ map.put(childLink,sentiment); }
                                }

                                if(OPTIMAL_DEPTH){Crab.urlQueue.add(childLink);}
                            }
                        }catch(Exception ex){ex.printStackTrace();}
                    });

                    map.keySet().forEach((String key)->{
                        int x = map.get(key);
                        if(x>bestSentiment){
                            bestLink = key;
                            bestSentiment = x;
                        }
                    });

                    if(optimalURLrecord.stream().noneMatch(str->str.matches(bestLink))){
                        optimalURLrecord.add(bestLink);
                        System.out.println("Best Link for: " + URL + "is" + bestLink + "\n");
                        Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_OPT_SENTIMENT, new writerObj(bestLink,bestSentiment));
                    }
                }
            }catch(Exception ex){ex.printStackTrace();}
        }
    }

    private static class KeyWordRunnable implements Runnable{

        private static final Queue<String> bList = blockedList;
        String URL; ArrayList<String> tags;
        HashMap<String,Boolean> map = new HashMap<>(DEFAULT_SIZE);
        ConcurrentHashMap<String,KeywordClass> kword_map = keywordMap;

        KeyWordRunnable(String link, final ArrayList<String> POS_Tags){
                this.URL = link;
                this.tags = POS_Tags;
                tags.ensureCapacity(POS_Tags.size());
        }

        public Queue<String> checkKword(final HashMap<String,PriorityQueue<String>> t_map, final ConcurrentHashMap<String,KeywordClass> keyWordMap, final String link){
                final Queue<String> matches = new ArrayDeque<>();
                for(String keyword : keyWordMap.keySet()){
                    for(final String tag : t_map.keySet()){
                        final PriorityQueue<String> p_queue = t_map.get(tag);

                        p_queue.forEach((final String word)->{
                            if(word.matches(keyword)){
                                exec.submit(new fullSentimentRunnable(link,keyWordMap.get(keyword)));
                                matches.add(keyword);
                            }
                        });
                    }
                }
                return matches;
        }

        public void run(){
            final String sanitised_url = URL.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
            try{
                URI uri = new URI(URL);
                    if(keywordVisitList.stream().noneMatch(str->str.matches(uri.getHost())) || parentSetMap.contains(URL) && bList.stream().noneMatch(str->str.matches(uri.getHost()))){

                    final Document doc = Jsoup.connect(URL).get();
                    final Elements links = doc.select("a[href]");

                    keywordVisitList.add(sanitised_url);
                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_VL_RECORD,new writerObj(URL));

                    links.forEach((Element link)->{
                        try{
                            final String childLink = link.attr("abs:href");
                            final Document docTwo = Jsoup.connect(childLink).get();
                            final String sanitisedLink = childLink.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
                            final URI linkURI = new URI(childLink);
                            boolean matchesFound;

                            if(blockedList.stream().noneMatch(str->str.matches(linkURI.getHost()))
                            && keywordVisitList.stream().noneMatch(str->str.matches(sanitisedLink))){
                                System.out.println("Visited: " + childLink +  " Parent: " + URL + "\n");
                                final HashMap<String, PriorityQueue<String>> tagMap;
                                tagMap = Utility.SentimentAnalyser.pos_keywordTagger(docTwo.title(),tags);

                                final Queue<String> matches = checkKword(tagMap,kword_map,childLink);

                                if(!matches.isEmpty()){
                                    System.out.println("Matches found for: " + childLink + "\n");
                                    if(keywordVisitList.stream().noneMatch(str->str.matches(sanitisedLink))){
                                        keywordVisitList.add(sanitisedLink);
                                        Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_VL_RECORD,new writerObj(childLink));

                                        matches.forEach((String match)-> Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_MATCHES,new writerObj(childLink,match)));

                                    }
                                    matchesFound = true;
                                }else{
                                    matchesFound = false;

                                    if(keywordVisitList.stream().noneMatch(str->str.matches(sanitisedLink))){
                                            keywordVisitList.add(sanitisedLink);
                                            Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_VL_RECORD,sanitisedLink);
                                    }
                                }
                                map.put(URL,matchesFound);
                            }
                        }catch(Exception ex){ex.printStackTrace();}
                    });
                }
            }catch(Exception ex){ex.printStackTrace();}
        }
    }

     private static class fullSentimentRunnable implements Runnable{
        private String URL;
        String keyword;
        KeywordClass obj; boolean obj_recv; int sentiment;

        public String getPageContent(String link){
            StringBuilder sb = new StringBuilder();
            try{
                Document doc = Jsoup.connect(link).get();
                Elements contents = doc.select("p");
                contents.forEach((Element para)-> sb.append(para.text()));
                return sb.toString();
            }catch(Exception ex){ex.printStackTrace();} return null;
        }

        fullSentimentRunnable(String link, KeywordClass keywordObj){
            this.URL = link;
            this.obj = keywordObj;
            this.keyword = keywordObj.keyword;
            obj_recv = true;
        }

        public void run(){
            System.out.println("Test\n");
            String content = getPageContent(URL);
            sentiment = Utility.SentimentAnalyser.analyse(content);

            if(obj_recv){
                if(keywordMap.contains(keyword)){
                    KeywordClass o = keywordMap.get(keyword);

                    switch (Utility.SentimentType.fromInt(sentiment)) {
                        case VERY_POSITIVE, POSITIVE -> o.positiveSentiment.put(URL, sentiment);
                        case NEUTRAL -> o.neutralSentiment.put(URL, sentiment);
                        case VERY_NEGATIVE, NEGATIVE -> o.negativeSentiment.put(URL, sentiment);
                    }
                    keywordMap.put(keyword,o);
                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_SENTIMENT_RESULT,new writerObj(URL,sentiment,keyword));
                }
            }else{
                Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_SENTIMENT_RESULT,new writerObj(URL,sentiment,keyword));
            }
        }
    }

    public static void serializeAllObj() throws IOException {
        sr.serializeQueue(visitList,"v_list.bin");
        sr.serializeQueue(keywordVisitList,"kw_v_list.bin");
        sr.serializeQueue(optimalURLrecord,"optimal_link_record.bin");
    }

    public static void deserializeAllObj() throws IOException {
        File f = new File("v_list.bin");
        if(f.exists()){
            visitList = (ConcurrentLinkedQueue<String>) sr.deserializeQueue("v_list.bin");
        } else { sr.serializeQueue(visitList,"v_list.bin"); }
        f = new File("kw_v_list.bin");
        if(f.exists()){
            keywordVisitList = (ConcurrentLinkedQueue<String>) sr.deserializeQueue("kw_v_list.bin");
        } else { sr.serializeQueue(keywordVisitList,"kw_v_list.bin"); }
        f = new File("optimal_link_record.bin");
        if(f.exists()){
            optimalURLrecord  = (ConcurrentLinkedQueue<String>) sr.deserializeQueue("optimal_link_record.bin");
        }else { sr.serializeQueue(optimalURLrecord,"optimal_link_record.bin"); }
    }

    Crab() throws IOException {
                sr.serializeQueue(visitList,"v_list.bin");
                sr.serializeQueue(optimalURLrecord,"optimal_link_record.bin");
                sr.serializeQueue(keywordVisitList,"kw_v_list.bin");
    }

    public static void crabCrawl() throws IOException {
        Utility.DataIO.readInURLSeed("./test.csv");

        deserializeAllObj();

        if(!KEYWORD_CRAWL){
                    System.out.println("Optimal Crawl enabled.");
                    if(FULL_UTILISATION){System.out.println("Full Utilisation of Threads configured.");}else {System.out.println("Full Utilisation off.\n");}
                    if(!OPTIMAL_DEPTH){System.out.println("Optimal depth off.\n");}
                    System.out.println("Start time of Crawl: " + java.time.LocalTime.now());

                    while(!urlQueue.isEmpty()){
                            String urlToCrawl = urlQueue.poll();
                            exec.submit(new OptimalRunnable(urlToCrawl));
                    }
        }else{
                    while(!urlQueue.isEmpty()){
                        String urlToCrawl = urlQueue.poll();
                        exec.submit(new KeyWordRunnable(urlToCrawl,cTags));
                    }
        }

        serializeAllObj();

        keywordMap.values().forEach(Utility.DataIO::writeObjData);
    }
    }