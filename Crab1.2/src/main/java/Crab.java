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
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.tinylog.Logger;

public class Crab {
    public static final int DEFAULT_SIZE = 1000;

    public static ConcurrentLinkedQueue<String> blockedList = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> visitList = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String,Boolean> parentSetMap = new ConcurrentHashMap<>(DEFAULT_SIZE);
    public static ConcurrentLinkedQueue<String> optimalURLrecord = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<String> keywordVisitList = new ConcurrentLinkedQueue<>();
    public static ArrayList<String> cTags = new ArrayList<>(DEFAULT_SIZE);

    public static ConcurrentHashMap<String,KeywordClass> keywordMap = new ConcurrentHashMap<>(DEFAULT_SIZE);

    /* urlQueue is the frontier for both crawl types */
    public static ConcurrentLinkedQueue<String> urlQueue = new ConcurrentLinkedQueue<>();

    /* Comparator used for the frontier priority queue of keyword search, higher keywords appear at the top. */
    public static Comparator<CrabTag> tagSort = Comparator.comparingInt(CrabTag::getQuantity).reversed();
    public static PriorityBlockingQueue<CrabTag> frontierQueue = new PriorityBlockingQueue<>(1000,tagSort);

    private static final int NUM_OF_THREADS = (Runtime.getRuntime().availableProcessors())+1;
    private static final int CORE_SIZE = (NUM_OF_THREADS % 2 == 0) ? NUM_OF_THREADS /2 : Math.floorDiv(NUM_OF_THREADS,2);
    private static final int CAPACITY = 100;

    /* Can be changed, easy heuristic to avoiding spider traps */
    private static final int URL_CHAR_LIMIT = 256;

    /* HTTP-status codes that prevent the crawler */
    public static int [] status_codes = new int[]{900,0,400,404,999,403,503,451,429,500, 302};

    public static Utility.Serialization sr = new Utility.Serialization();

    public static ConcurrentHashMap<String, LocalDateTime> crawlHistory = new ConcurrentHashMap<>(1000);

    /**
     * @About setting this will configure Crab into Keyword Crawl mode
     * NOTE: you must supply keywords for the crawl to run.
     */
    public static boolean KEYWORD_CRAWL = false;

    /**
     * @About setting this flag will ensure every thread will push links
     * onto the URL stack for crawling within an optimal crawl.
     */
    public static boolean FULL_DEPTH = false;

    /**
     * @About setting this flag will run the thread pool at the maximum
     * number of available threads.
     */
    public static boolean FULL_UTILISATION = false;

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

    /**
     * @About if this flag is set Crab will use the defined crawl page limit and
     * will end the crawl once reached.
     */
    public static boolean USE_CRAWL_LIMIT = false;

    /**
     * @About amountCrawled is a counter which is updated on each page visit, crawlLimit is
     * a user-defined limit that allows to stop the crawl after visiting the defined amount of pages
     * specified by the user. By default we set this to 1000 (arbitrary value).
     */
    public static AtomicInteger amountCrawled = new AtomicInteger(1);
    public static int crawlLimit = 1000;

    public static  ThreadPoolExecutor exec = new ThreadPoolExecutor((FULL_UTILISATION) ? NUM_OF_THREADS : CORE_SIZE
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
     * @About Sentiment runnable is the class each thread uses to implement a sentiment-based crawl.
     */
    private static class SentimentRunnable implements Runnable{

        SentimentRunnable(String link){this.URL = link;}
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

                            if(Utility.urlTools.checkWhiteSpace(childLink)){
                                URI child_uri = new URI(childLink);

                                //resolves some of the HTTP Status Exceptions (i.e. linkedin is within the blocklist)
                                if(blockedList.stream().noneMatch(str->str.matches(child_uri.getHost()))){
                                    final String sanitised = childLink.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");

                                    if(visitList.stream().noneMatch(str->str.matches(sanitised))){

                                        Connection con = Jsoup.connect(childLink).ignoreHttpErrors(true);
                                        Connection.Response res = con.execute();
                                        int status = res.statusCode();

                                        if(Arrays.stream(status_codes).noneMatch(x->x==status)){
                                            visitList.add(sanitised);
                                            crawlHistory.putIfAbsent(sanitised,LocalDateTime.now());

                                            if(USE_CRAWL_LIMIT){
                                                amountCrawled.getAndIncrement();
                                            }
                                            Document docTwo = con.get();

                                            sentiment = Utility.SentimentAnalyser.analyse(docTwo.title());
                                            System.out.println("Visited: " + link.attr("abs:href") + " " +  "Parent: " + URL + "\n");

                                            Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_VL_RECORD,sanitised);
                                            if(!map.containsKey(childLink)){ map.put(childLink,sentiment);}

                                            if(FULL_DEPTH){
                                                frontierQueue.add(new CrabTag(childLink,sentiment));
                                            }
                                        }
                                    }
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

                            if(Utility.urlTools.checkWhiteSpace(childLink) && childLink.length() != URL_CHAR_LIMIT){
                                Connection con = Jsoup.connect(childLink).ignoreHttpErrors(true);
                                Connection.Response res = con.execute();
                                int status = res.statusCode();
                                String contentType = res.contentType();

                                if(Arrays.stream(status_codes).noneMatch(x -> x == status)) {
                                    assert contentType != null;
                                    if (contentType.contains("text/html")) {

                                        final Document docTwo = con.get();
                                        final String sanitisedLink = childLink.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)", "");
                                        URI linkURI = new URI(childLink);

                                        if (blockedList.stream().noneMatch(str -> str.matches(linkURI.getHost()))
                                                && keywordVisitList.stream().noneMatch(str -> str.matches(sanitisedLink))) {
                                            System.out.println("Visited: " + childLink + " Parent: " + URL + "\n");
                                            final HashMap<String, PriorityQueue<String>> tagMap;
                                            tagMap = Utility.SentimentAnalyser.pos_keywordTagger(docTwo.title(), tags);

                                            final Queue<String> matches = Utility.SentimentAnalyser.checkKword(tagMap, kword_map);

                                            if (!matches.isEmpty()) {
                                                System.out.println("Matches found for: " + childLink + "\n");
                                                if (keywordVisitList.stream().noneMatch(str -> str.matches(sanitisedLink))) {
                                                    keywordVisitList.add(sanitisedLink);

                                                    LocalDateTime record = LocalDateTime.now();
                                                    crawlHistory.putIfAbsent(sanitisedLink, record);

                                                    if (USE_CRAWL_LIMIT) { //only increment if the limit is set
                                                        amountCrawled.getAndIncrement();
                                                    }

                                                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_VL_RECORD, new writerObj(childLink));
                                                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_HISTORY, new writerObj(childLink, record));

                                                    /* Analyse the headline of the page for a keyword match
                                                     *  similiar to optimal crawl. */
                                                    Document linkDoc = Jsoup.connect(childLink).get();
                                                    String titleToAnalyse = linkDoc.title();
                                                    int sentiment = Utility.SentimentAnalyser.analyse(titleToAnalyse);

                                                    if (!parentSetMap.containsKey(childLink)) {
                                                        matches.forEach((String match) -> Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_MATCHES, new writerObj(childLink, match)));
                                                        Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_SENTIMENT_SPEC, new writerObj(childLink, matches, sentiment));
                                                    }

                                                    if (Crab.FULL_DEPTH) {
                                                        //Crab.urlQueue.add(childLink);
                                                        Crab.frontierQueue.add(new CrabTag(childLink,matches.size()));
                                                    }

                                                    //Builds a sentiment profile through the keyword class for a keyword by updating
                                                    if (Crab.FULL_PROFILE) {
                                                        matches.forEach((String str) -> {
                                                            if (keywordMap.keySet().stream().anyMatch(key -> (key.matches(str)))) {
                                                                Utility.SentimentAnalyser.detSentiment(childLink, titleToAnalyse, sentiment, keywordMap.get(str));
                                                            }
                                                        });
                                                    }
                                                }
                                            } else {
                                                if (keywordVisitList.stream().noneMatch(str -> str.matches(sanitisedLink))) {
                                                    keywordVisitList.add(sanitisedLink);
                                                    Utility.DataIO.writeOut(Utility.IO_LEVEL.WRITE_KWORD_VL_RECORD, sanitisedLink);
                                                }
                                            }
                                        }
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

    public static void deserializeAllObj() throws IOException, ClassNotFoundException {
        File f = new File("v_list.bin");
        if(f.exists()){
            visitList = (ConcurrentLinkedQueue<String>) sr.deserializeQueue(f.getName());
        } else { sr.serializeQueue(visitList,f.getName()); }
        f = new File("kw_v_list.bin");
        if(f.exists()){
            keywordVisitList = (ConcurrentLinkedQueue<String>) sr.deserializeQueue(f.getName());
        } else { sr.serializeQueue(keywordVisitList,f.getName()); // this could be the error, serializing an empty queue
        }
        f = new File("optimal_link_record.bin");
        if(f.exists()){
            optimalURLrecord  = (ConcurrentLinkedQueue<String>) sr.deserializeQueue(f.getName());
        }else { sr.serializeQueue(optimalURLrecord,f.getName()); }
    }

    /**
     *  allows to read in large amount of user inputted data for the keyword crawl, saves the user
     *  having to create single lines of adding data to the structures. Also useful for file input of lists.
     */
    public static void readKwordCrawlInput(String [] blist, String [] tags, String [] kwords){
                Collections.addAll(blockedList,blist);
                Collections.addAll(cTags,tags);
                ArrayList<KeywordClass> q = new ArrayList<>(100);
                for(String word : kwords){q.add(new KeywordClass(word));}
                q.forEach((KeywordClass k)-> keywordMap.put(k.keyword,k));
    }

    /**
     * @About main crawling function.
     * Checks which flag was set and then applies the appropriate settings for the crawl and runs.
     */
    public static void crabCrawl() throws IOException, ClassNotFoundException {
        Utility.DataIO.processURLSeedKW("./test.csv");

        Logger.info("Crawl Started.");

        deserializeAllObj();

        if(!KEYWORD_CRAWL){
            System.out.println("Optimal Crawl enabled.");
            if(FULL_UTILISATION){System.out.println("Full Utilisation of Threads configured.");}else {System.out.println("Full Utilisation off.\n");}
            if(!FULL_DEPTH){System.out.println("Optimal depth off.\n");}
            System.out.println("Start time of Crawl: " + java.time.LocalTime.now());

            if(USE_CRAWL_LIMIT){
                System.out.println("Crawl Limit Enabled" + " Page Crawl Limit: " + crawlLimit);
                while(!urlQueue.isEmpty() && !(amountCrawled.intValue() == crawlLimit)){
                    String urlToCrawl = urlQueue.poll();

                    if(!crawlHistory.containsKey(urlToCrawl)){
                        exec.submit(new SentimentRunnable(urlToCrawl));
                    }
                }
            }else{
                while(!urlQueue.isEmpty()){
                    String urlToCrawl = urlQueue.poll();

                    if(!crawlHistory.containsKey(urlToCrawl)){
                        exec.submit(new SentimentRunnable(urlToCrawl));
                    }
               }
            }
        }else{
            System.out.println("Keyword Crawl enabled\n");
            if(FULL_UTILISATION){System.out.println("Full Utilisation of Threads configured.");}else {System.out.println("Full Utilisation off.\n");}

            if(USE_CRAWL_LIMIT){
                System.out.println("Crawl Limit Enabled" + " Page Crawl Limit: " + crawlLimit);
                while(!frontierQueue.isEmpty() && amountCrawled.intValue() != crawlLimit){

                    CrabTag tag = frontierQueue.poll();
                    assert tag != null;
                    String urlToCrawl = tag.link;

                    if(!crawlHistory.containsKey(urlToCrawl)){
                        exec.submit(new KeyWordRunnable(urlToCrawl,cTags));
                    }
                }
            }else{
                while(!frontierQueue.isEmpty()){

                    CrabTag tag = frontierQueue.poll();
                    assert tag != null;

                    String urlToCrawl = tag.link;

                    if(!crawlHistory.containsKey(urlToCrawl)){
                        exec.submit(new KeyWordRunnable(urlToCrawl,cTags));
                    }
                }
            }
        }
        serializeAllObj();
    }
}