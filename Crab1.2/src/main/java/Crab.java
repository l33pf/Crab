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
import org.tinylog.Logger;

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

    private static final int NUM_OF_THREADS = (Runtime.getRuntime().availableProcessors())+1;
    private static final int CORE_SIZE = (NUM_OF_THREADS % 2 == 0) ? NUM_OF_THREADS /2 : Math.floorDiv(NUM_OF_THREADS,2);
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
    public static boolean FULL_UTILISATION;

    /**
     * @About setting this flag will record all previous 'optimal' links
     *  with the overall optimal link from a page in optimal crawl.
     */
    public static boolean RECORD_ALL = false;

    /**
     * @About setting this flag will use the keyword class to build up
     * a sentiment distribution contained in three data structures for
     * a given keyword.
     */
    public static boolean FULL_PROFILE;

    public static ThreadPoolExecutor exec = new ThreadPoolExecutor((FULL_UTILISATION) ? NUM_OF_THREADS : CORE_SIZE
            , NUM_OF_THREADS,
            1L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(CAPACITY),
            Executors.defaultThreadFactory(),
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * @About a class for storing articles and their sentiments based upon the keyword given.
     */
    public static class KeywordClass{
        final String keyword;

        ConcurrentHashMap<String,String> negativeSent = new ConcurrentHashMap<>(DEFAULT_SIZE);
        ConcurrentHashMap<String,String> neutralSent = new ConcurrentHashMap<>(DEFAULT_SIZE);
        ConcurrentHashMap<String,String> positiveSent = new ConcurrentHashMap<>(DEFAULT_SIZE);

        KeywordClass(String keyWord){
            this.keyword = keyWord;
        }
    }

    /**
     * @About Optimal runnable is the class each thread uses to implement an optimal crawl.
     */
    private static class OptimalRunnable implements Runnable{

        OptimalRunnable(String link){this.URL = link;}
        String URL, bestLink;
        int bestSentiment, sentiment;
        HashMap<String,Integer> map = new HashMap<>();
        Queue<String> blockedList = Crab.blockedList;
        HashMap<String,Integer> optMap = new HashMap<>();

        public void run(){

            final String sanitised_url = URL.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
            try{
                URI uri = new URI(URL);
                if(visitList.stream().noneMatch(str->str.matches(sanitised_url)) ||
                        parentSetMap.containsKey(URL) || blockedList.stream().noneMatch(str->str.matches(uri.getHost()))){

                    visitList.add(sanitised_url);
                    final Document doc = Jsoup.connect(URL).get(); System.out.println("Visiting: " + URL + "\n");
                    final Elements links = doc.select("a[href]");

                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_VL_RECORD,new writerObj(sanitised_url));

                    links.forEach((Element link)->{
                        try{
                            final String childLink = link.attr("abs:href");
                            URI child_uri = new URI(childLink);

                            //resolves some of the HTTP Status Exceptions (i.e. linkedin is within the blocklist)
                            if(blockedList.stream().noneMatch(str->str.matches(child_uri.getHost()))){
                                final String sanitised = childLink.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");

                                if(visitList.stream().noneMatch(str->str.matches(sanitised))){

                                    visitList.add(sanitised);
                                    final Document docTwo =  Jsoup.connect(childLink).get();
                                    sentiment = Utility.SentimentAnalyser.analyse(docTwo.title());
                                    System.out.println("Visited: " + link.attr("abs:href") + " " +  "Parent: " + URL + "\n");

                                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_VL_RECORD,sanitised);
                                    if(!map.containsKey(childLink)){ map.put(childLink,sentiment);}
                                    if(OPTIMAL_DEPTH){Crab.urlQueue.add(childLink);}
                                }
                            }
                        }catch(Exception ex){Logger.error(ex);}
                    });

                    map.keySet().forEach((String key)->{
                        int x = map.get(key);
                        if(x>bestSentiment){
                            bestLink = key;
                            bestSentiment = x;
                            if(Crab.RECORD_ALL){ optMap.putIfAbsent(bestLink,bestSentiment); }
                        }
                    });

                    if(optimalURLrecord.stream().noneMatch(str->str.matches(bestLink))){
                        if(Crab.RECORD_ALL){
                            optMap.keySet().forEach((String str)-> Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_OPT_SENTIMENT, new writerObj(str,optMap.get(str))));
                        }else{
                            optimalURLrecord.add(bestLink);
                            System.out.println("Best Link for: " + URL + "is" + bestLink + "\n");
                            Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_OPT_SENTIMENT, new writerObj(bestLink,bestSentiment));
                        }
                    }
                }
            }catch(Exception ex){Logger.error(ex);}
        }
    }

    /**
     * @About KeyWordRunnable is the class every thread uses to implement a keyword crawl.
     */
    private static class KeyWordRunnable implements Runnable{

        private static final Queue<String> bList = blockedList;
        String URL; ArrayList<String> tags;
        ConcurrentHashMap<String,KeywordClass> kword_map = keywordMap;

        KeyWordRunnable(String link, final ArrayList<String> POS_Tags){
            this.URL = link;
            this.tags = POS_Tags;
            tags.ensureCapacity(POS_Tags.size());
        }

        public void run(){
            final String sanitised_url = URL.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");
            try{
                URI uri = new URI(URL);
                if(keywordVisitList.stream().noneMatch(str->str.matches(uri.getHost())) || parentSetMap.containsKey(URL) && bList.stream().noneMatch(str->str.matches(uri.getHost()))){

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

                            if(blockedList.stream().noneMatch(str->str.matches(linkURI.getHost()))
                                    && keywordVisitList.stream().noneMatch(str->str.matches(sanitisedLink))){
                                System.out.println("Visited: " + childLink +  " Parent: " + URL + "\n");
                                final HashMap<String, PriorityQueue<String>> tagMap;
                                tagMap = Utility.SentimentAnalyser.pos_keywordTagger(docTwo.title(),tags);

                                final Queue<String> matches = Utility.SentimentAnalyser.checkKword(tagMap,kword_map);

                                if(!matches.isEmpty()){
                                    System.out.println("Matches found for: " + childLink + "\n");
                                    if(keywordVisitList.stream().noneMatch(str->str.matches(sanitisedLink))){
                                        keywordVisitList.add(sanitisedLink);
                                        Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_VL_RECORD,new writerObj(childLink));

                                        /* Analyse the headline of the page for a keyword match
                                        *  similiar to optimal crawl. */
                                        Document linkDoc = Jsoup.connect(childLink).get();
                                        String titleToAnalyse = linkDoc.title();
                                        int sentiment = Utility.SentimentAnalyser.analyse(titleToAnalyse);

                                        if(parentSetMap.keySet().stream().noneMatch(str->str.matches(childLink))){
                                            matches.forEach((String match)-> Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_MATCHES,new writerObj(childLink,match)));
                                            //Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_SENTIMENT_MATCHES,new writerObj(childLink,matches,sentiment));
                                            Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_SENTIMENT_SPEC, new writerObj(childLink,matches,sentiment));
                                        }

                                        if(Crab.OPTIMAL_DEPTH){ Crab.urlQueue.add(childLink);}

                                        //Builds a sentiment profile through the keyword class for a keyword by updating
                                        if(Crab.FULL_PROFILE){
                                            matches.forEach((String str)->{
                                                 if(keywordMap.keySet().stream().anyMatch(key->(key.matches(str)))){
                                                     Utility.SentimentAnalyser.detSentiment(childLink,titleToAnalyse,sentiment,keywordMap.get(str));
                                                 }
                                            });
                                        }
                                    }
                                }else{
                                    if(keywordVisitList.stream().noneMatch(str->str.matches(sanitisedLink))){
                                        keywordVisitList.add(sanitisedLink);
                                        Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_VL_RECORD,sanitisedLink);
                                    }
                                }
                            }
                        }catch(Exception ex){Logger.error(ex);}
                    });
                }
            }catch(Exception ex){Logger.error(ex);}
        }
    }

    public static void serializeAllObj() throws IOException {
        sr.serializeQueue(visitList,"v_list.bin");
        sr.serializeQueue(keywordVisitList,"kw_v_list.bin");
        sr.serializeQueue(optimalURLrecord,"optimal_link_record.bin");
        sr.serializeMap(parentSetMap,"parent_set_map.bin");
    }

    public static void deserializeAllObj() throws IOException {
        File f = new File("v_list.bin");
        if(f.exists()){
            visitList = (ConcurrentLinkedQueue<String>) sr.deserializeQueue(f.getName());
        } else { sr.serializeQueue(visitList,f.getName()); }
        f = new File("kw_v_list.bin");
        if(f.exists()){
            keywordVisitList = (ConcurrentLinkedQueue<String>) sr.deserializeQueue(f.getName());
        } else { sr.serializeQueue(keywordVisitList,f.getName()); }
        f = new File("optimal_link_record.bin");
        if(f.exists()){
            optimalURLrecord  = (ConcurrentLinkedQueue<String>) sr.deserializeQueue(f.getName());
        }else { sr.serializeQueue(optimalURLrecord,f.getName()); }
    }

    /**
     * @About main crawling function.
     * Checks which flag was set and then applies the appropriate settings for the crawl.
     */
    public static void crabCrawl() throws IOException {
        Utility.DataIO.readInURLSeed("./test.csv");

        Logger.info("Crawl Started.");

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
            System.out.println("Keyword Crawl enabled");
            if(FULL_UTILISATION){System.out.println("Full Utilisation of Threads configured.");}else {System.out.println("Full Utilisation off.\n");}

            while(!urlQueue.isEmpty()){
                String urlToCrawl = urlQueue.poll();
                exec.submit(new KeyWordRunnable(urlToCrawl,cTags));
            }
        }
        serializeAllObj();
    }
}