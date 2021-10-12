package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public static List<String> keyWords = new ArrayList<String>();

    public final static HashMap<Integer,String> indicatorWords = new HashMap<Integer,String>();

    boolean running = false;
    private final static Logger LOGGER = Logger.getLogger(Crab.class.getName());
    TreeMap<Integer,Stack> map = new TreeMap<Integer,Stack>();

    Stack urlStack = new Stack();

    public final String INDICATOR_FILE_PATH = "./Indicators.csv";
    public final String KEYWORD_FILE_PATH = "./KeyWords.csv";

    /* This is the key word threshold, Crab will only crawl further if a page hits this threshold */
    final int threshold = 3;

    void startCrawl() throws IOException, CsvException {
        int counter = 0;
        URLSeed.readIn(urlStack);

        Utility.readToCSV(KEYWORD_FILE_PATH,keyWords);

        Stack linkStack = new Stack();

        while(urlStack.size() > 1){

            String current = urlStack.pop();

            try{

                Document doc = Jsoup.connect(current).get();
                Element body = doc.body();
                Elements links = doc.select("a[href]");

                textData.put(current,body.text());

                for(Element link : links){
                    linkStack.push(link.attr("abs:href").toString());
                }

            } catch (Exception ex){
                ex.printStackTrace();
            }

            map.put(counter,linkStack);
            counter++;
        }

        //Submit a Text Analysis stack from the URLSeeds
        //TA textAnalysis = new TA(textData,indicatorWords);
        //

        //Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for(Stack stack : map.values()){
            executorService.execute(new CrawlRunnable(stack,keyWords,threshold));
        }

        executorService.shutdown();

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

    void testMethod(){
            List<String> keyIndicators = new ArrayList<>();
            Utility.readToCSV(INDICATOR_FILE_PATH,keyIndicators);

            for(String word : keyIndicators){
                System.out.println(word);
            }
    }


}
