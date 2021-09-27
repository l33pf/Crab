package com.company;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;




public class Engine {

    HashMap<String,Boolean> crawlStatus = new HashMap<String,Boolean>();
    HashMap<String,String> textData = new HashMap<String,String>();

    Stack linkStack = new Stack();
    boolean running = false;

    void startCrawl() throws IOException {

        while(URLSeed.stck.size() > 1){

            if(!crawlStatus.containsKey(URLSeed.stck.peek())){
                running = true;
                crawlStatus.put(URLSeed.stck.peek(),true);
                String current = URLSeed.stck.pop();

                try{
                    Document doc = Jsoup.connect(current).get();
                    Element body = doc.body();
                    Elements links = doc.select("a[href]");

                    //Need to remove artifacts from extracted URL (regex)
                    for(Element link : links){
                        linkStack.push(link.attr("abs:href").toString());
                    }

                    textData.put(current,body.text());

                } catch(Exception ex){

                }
            }
        }
    }

    // Here we'll utilise weka and look for specifics that may indicate trading signals
    void textualAnalysis(HashMap<String,String> textData){

    }

    //Used to Submit Jobs/Thread Pool
    void jobCentre(OPTIONS opt){

      if(opt == OPTIONS.Crawl){

      }
      else if(opt == OPTIONS.Search){

      }
      else if(opt == OPTIONS.Add){

      }

    }

    public enum OPTIONS{
        Crawl,
        Search,
        LastRan,
        Info,
        Add,
        Stall
    }

    void UI(){

        OPTIONS opt;
        opt = OPTIONS.Stall;

        Scanner reader = new Scanner(System.in);
        String userInput = reader.next();

        switch(userInput){

            case "C" : case "c" :
                opt = OPTIONS.Crawl;
                break;

            case "S" : case "s":
                opt = OPTIONS.Search;
                break;

            case "L" : case "l":
                opt = OPTIONS.LastRan;
                break;

            case "I" : case "i":
                opt = OPTIONS.Info;
                break;

            case "A" : case "a":
                opt = OPTIONS.Add;
                break;
        }

        reader.close();
    }







}
