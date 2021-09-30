package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Engine {

    HashMap<String,Boolean> crawlStatus = new HashMap<String,Boolean>();
    HashMap<String,String> textData = new HashMap<String,String>();
    public List<String> keyWords = new ArrayList<String>();
    Stack linkStack = new Stack();
    boolean running = false;
    private final static Logger LOGGER = Logger.getLogger(Engine.class.getName());

    void startCrawl() throws IOException {

        while(URLSeed.stck.size() > 1){
            int counter = 0;
            TreeMap<Integer,Stack> map = new TreeMap<Integer,Stack>();

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
                        ex.printStackTrace();
                        LOGGER.setLevel(Level.WARNING);
                }
            }
            map.put(counter,linkStack);
            linkStack.flushStack(linkStack); //clear the link stack
            counter++;
        }
    }

    //Used to Submit Jobs/Thread Pool
    void jobCentre(OPTIONS opt) throws IOException {

      switch(opt){

          case Crawl:
              startCrawl();
              break;

          case Info:
              info();
              break;

          case Add:
              while(true){
                    System.out.println("Please enter key word indicators\n");
                    System.out.println("Enter '/' to quit adding indicators.\n");
                    Scanner reader = new Scanner(System.in);
                    keyWords.add(reader.next());
                    if(reader.next().contains("/")){
                        reader.close();
                        break;
                    }
              }
              break;
      }
    }

    public enum OPTIONS{
        Crawl,
        Info,
        Add,
        Stall
    }

    void UI() throws IOException {

        OPTIONS opt;
        opt = OPTIONS.Stall;

        Scanner reader = new Scanner(System.in);
        String userInput = reader.next();

        switch(userInput){

            case "C" : case "c" :
                opt = OPTIONS.Crawl;
                break;

            case "I" : case "i":
                opt = OPTIONS.Info;
                break;

            case "A" : case "a":
                opt = OPTIONS.Add;
                break;
        }

        reader.close();
        jobCentre(opt);
    }

    //prints out the info page
    void info(){
        try (BufferedReader br = new BufferedReader(new FileReader("info.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.setLevel(Level.WARNING);
        }
    }
}
