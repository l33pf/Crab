/*
 ***LICENSE***
 Copyright (c) 2021 l33pf (https://github.com/l33pf)

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

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.nodes.Element;

public class SentimentKeyWordRunnable implements  Runnable{

    //Used as local storage for POS tags
    ArrayList<String> tags;

    //Used to store keyword objects for search locally
    Queue<KeywordClass> keywords;

    String URL;
    final Queue<String> test = Crab.b_list;
    ConcurrentLinkedQueue<String> testTwo = Crab.parent_set;
    HashMap<String,Boolean> map = new HashMap<>();

    SentimentKeyWordRunnable(final ArrayList<String> POS_Tags, final Queue<KeywordClass> kw_obj){
        Objects.requireNonNull(this.tags = POS_Tags);
        Objects.requireNonNull(this.keywords = kw_obj);
    }

    SentimentKeyWordRunnable(final ArrayList<String> POS_Tags, final Queue<KeywordClass> kw_obj, String link){
        Objects.requireNonNull(this.tags = POS_Tags);
        Objects.requireNonNull(this.keywords = kw_obj);
        Objects.requireNonNull(this.URL = link);
    }

    public boolean checkBlocked(final String link){

        try{
            URI uri = new URI(link);
            for(String b_url : test){
                //have very similiar URL's
                if(FuzzySearch.ratio(uri.getHost(),b_url) > 90){
                    return true;
                }
            }
        }catch(Exception ex){
            return false;
        }
        return false;
    }

    /*
        This method looks for keywords then returns a list of matches by searching through the extracted tag map
        Can be replaced for boolean if needed, the queue is merely for documenting.
     */
    public Queue<String> checkForKeyword(final HashMap<String,PriorityQueue<String>> t_map, final Queue<KeywordClass> q_keywrds){
            Queue<String> matches = new ArrayDeque<>();

            q_keywrds.forEach((KeywordClass obj)->{
                for(String tag : t_map.keySet()){
                    PriorityQueue<String> p_queue = t_map.get(tag);

                    p_queue.forEach((String word)->{
                        if(word.matches(obj.keyword)){ //contains
                            matches.add(obj.keyword);
                        }
                    });
                }
            });

            return matches;
    }

    @Override
    public void run() {

        boolean blocked_link;

        final String sanitised_url = URL.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");

        try{

            if(!Utility.checkValInQueue(Crab.keywordVisitList,sanitised_url) || Crab.parent_set.contains(URL)){

                final Document doc = Jsoup.connect(URL).get();
                final Elements links = doc.select("a[href]");
                Document docTwo;

                for(Element link : links){
                    blocked_link = false;

                    docTwo = Jsoup.connect(link.attr("abs:href")).get();

                    //check the URL isn't contained in the block list
                    if(checkBlocked(link.attr("abs:href"))){
                        blocked_link = true;
                    }

                    String sanitisedLink = link.attr("abs:href").replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");

                    if(!blocked_link && !Utility.checkValInQueue(Crab.keywordVisitList,sanitisedLink)){

                        boolean matchesFound;

                        String title = docTwo.title();

                        HashMap<String,PriorityQueue<String>> tagMap;

                        //extract the tags from the text
                        tagMap = SentimentAnalyser.pos_keywordTagger(title,tags);

                        Queue<String> matches = checkForKeyword(tagMap,keywords);

                        //if we have matches found, add it to the download queue to store the content
                        if(!matches.isEmpty()){

                            System.out.println("Matches found for: " + link.attr("abs:href") + "\n");

                            if(!Utility.checkValInQueue(Crab.keywordVisitList,sanitisedLink)){
                                Crab.keywordVisitList.add(sanitisedLink);
                                Utility.writeVisitList_kw(link.attr("abs:href"),"CrabKWVisitList.csv");
                                Utility.writeURLKeywordMatches(matches,link.attr("abs:href"),"CrabURLKeywordMatches.csv");
                            }

                            //send out for download
                            Crab.fullSentimentTasks.add(new DownloadCallable(link.attr("abs:href")));

                            matchesFound = true;
                        }else{
                            matchesFound = false;

                            if(!Utility.checkValInQueue(Crab.keywordVisitList,sanitisedLink)){
                                Crab.keywordVisitList.add(sanitisedLink);
                                Utility.writeVisitList_kw(link.attr("abs:href"),"CrabKWVisitList.csv");
                            }
                        }

                        map.put(URL,matchesFound);
                    }
                }
            }
        }catch(Exception ex){

        }
    }
}