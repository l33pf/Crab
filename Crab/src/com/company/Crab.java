package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.opencsv.exceptions.CsvException;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class Crab {

    HashMap<String,Boolean> crawlStatus = new HashMap<String,Boolean>();
    HashMap<String,String> textData = new HashMap<String,String>();
    public List<String> keyWords = new ArrayList<String>();

    public final static HashMap<Integer,String> indicatorWords = new HashMap<Integer,String>();

    Stack linkStack = new Stack();
    boolean running = false;
    private final static Logger LOGGER = Logger.getLogger(Crab.class.getName());
    TreeMap<Integer,Stack> map = new TreeMap<Integer,Stack>();

    Stack urlStack = new Stack();

    void startCrawl() throws IOException, CsvException {
        int counter = 0;
        URLSeed.readIn(urlStack);

        while(urlStack.size() > 1){

            String current = urlStack.pop();

            try{

                Document doc = Jsoup.connect(current).get();
                Element body = doc.body();
                Elements links = doc.select("a[href]");

                textData.put(current,body.text());

                System.out.println(current);
                System.out.println(textData.size());

                for(Element link : links){
                    linkStack.push(link.attr("abs:href").toString());
                }

            } catch (Exception ex){
                ex.printStackTrace();
            }

            map.put(counter,linkStack);
            linkStack.flushStack(linkStack); //clear the link stack
            counter++;
        }

        //Submit a Text Analysis stack from the URLSeeds
        TA textAnalysis = new TA(textData,indicatorWords);

        //Create a thread pool
     //   ExecutorService executorService = Executors.newFixedThreadPool(10);

  //      for(Stack stack : map.values()){
    //        executorService.execute(new CrawlRunnable(stack,keyWords));
     //   }

      //  executorService.shutdown();
    }

    void jobCentre(OPTIONS opt) throws IOException, CsvException {

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

          case ChangeCandidate:
/*                System.out.println("Please enter the file path to the candidate CSV file");
                Scanner reader = new Scanner(System.in);
                URLSeed.SAMPLE_CSV_FILE_PATH = reader.next();
                URLSeed replacement = new URLSeed();
                if(replacement.readIn()){
                    System.out.println("Candidate CSV changed");
                }else{
                    System.out.println("Unable to change Candidate CSV");
                    LOGGER.setLevel(Level.WARNING);
                }*/
      }
    }

    public enum OPTIONS{
        Crawl,
        Info,
        Add,
        Stall,
        ChangeCandidate
    }

    void UI() throws IOException, CsvException {

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

            case "CH" : case "ch":
                opt = OPTIONS.ChangeCandidate;
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
