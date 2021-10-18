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

    HashMap<String,String> textData = new HashMap<String,String>();
    public static List<String> keyWords = new ArrayList<String>();
    private final static Logger LOGGER = Logger.getLogger(Crab.class.getName());
    TreeMap<Integer,Stack> map = new TreeMap<Integer,Stack>();
    public static List<String> nonVisitURLs = new ArrayList<String>();

    Stack urlStack = new Stack();

    public final String INDICATOR_FILE_PATH = "./Indicators.csv";
    public final String KEYWORD_FILE_PATH = "./KeyWords.csv";

    /* This is the key word threshold, Crab will only crawl further if a page hits this threshold */
    final int threshold = 3;

    private static int numOfThreads = 65;

    /**
     * Starts The crawl process. The method utilises a Thread pool to analyse links and the extracted text
     * from the URLSeed.

     @throws IllegalArgumentException if urlStack or keyWords is set to zero.
     @throws IOException if files can't be read
     @throws CsvException if CSV file can't be obtained.

    */
    void startCrawl() throws IOException, CsvException {
        int counter = 0;

        URLSeed.readIn(urlStack);

        Utility.readToCSV(KEYWORD_FILE_PATH,keyWords);

        Stack linkStack = new Stack();

        if(urlStack.size() <= 0){
            throw new IllegalArgumentException("URLSeed is empty\n");
        } else if(keyWords.size() <= 0) { throw new IllegalArgumentException("No Keywords set.\n");}

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

        //Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);

        for(Stack stack : map.values()){
            executorService.execute(new CrawlRunnable(stack,keyWords,threshold));
        }

        executorService.shutdown();
    }

    /**
    * This method assigns a job based on user input from the UI.

     */
    void jobCentre(OPTIONS opt) throws IOException, CsvException {
        Scanner reader = new Scanner(System.in);
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

                    keyWords.add(reader.next());
                    if(reader.next().contains("/")){
                        reader.close();
                        break;
                    }
                    reader.close();
                    UI();
              }
              break;

          case ChangeCandidate:
              System.out.println("Please enter the file path to the candidate CSV file");
              URLSeed.SAMPLE_CSV_FILE_PATH = reader.next();

              if(URLSeed.readIn(URLSeed.stack)){
                  System.out.println("Candidate CSV changed");
              } else {
                  System.out.println("Unable to change candidate CSV");
              }
              break;

          case Blocked:
              System.out.println("The following is the URL's not visited or analysed: \n");
              Iterator<String> iterator = nonVisitURLs.iterator();
              while(iterator.hasNext()){
                  System.out.println(iterator.next());
              }

              System.out.println("to update the list please press u, or to exit press q");
              if(reader.next().contains("u")){
                    updateNonVisitList(reader);
              }else {
                    reader.close();
                    UI();
              }
              break;

          case Threads:
              System.out.println("The current number of threads used is: " + " " + numOfThreads + "\n");
              System.out.println("Do you want to update the number of threads used in crab ? (y/n)  \n");
              if(reader.next().contains("y")){
                  System.out.println("Enter the number of threads you wish to use. \n");
                  System.out.println("NOTE: performance will vary. \n");
                  numOfThreads = Integer.parseInt(reader.next());
                  System.out.println("Number of threads used is now: " + numOfThreads + "\n");
              }
              else{
                  reader.close();
                  UI();
              }
              reader.close();
              UI();
              break;
      }
    }

    /**
     * This enum lists the options available in Crab.
     * used to reflect user choices and start the job centre.
     * can be amended if new features are added.

     */
    public enum OPTIONS{
        Crawl,
        Info,
        Add,
        ChangeCandidate,
        Blocked,
        Stall,
        Threads
    }

    void UI() throws IOException, CsvException {

        info();

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

            case "B" : case "b":
                opt = OPTIONS.Blocked;
                break;
        }

        reader.close();
        jobCentre(opt);
    }

    /**
        Method prints the info page out.
     */
    private static void info(){
        try (BufferedReader br = new BufferedReader(new FileReader("info.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.setLevel(Level.WARNING);
        }
        System.out.println("\n");
    }

    private static void updateNonVisitList(final Scanner reader){
        System.out.println("Type/paste in URL to add to the Non-Visit URL list. \n");
        System.out.println("to quit entering type qq \n");
        while(!reader.next().contains("qq")){
            nonVisitURLs.add(reader.next());
        }
        reader.close();
    }
}