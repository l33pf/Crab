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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import com.fasterxml.jackson.core.JsonFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//This class is the Utility class for Crab, Provides I/O etc.

public final class Utility {

    public static final String RESULTS_FILE_PATH = "./CrabResults.csv";
    public static final String FULL_RESULTS_FILE_PATH = "./CrabFullAnalysisSentimentCrawl.csv";
    public static final String OPTIMAL_RESULTS_FILE_PATH = "./CrabOptimalCrawl.csv";
    public static String SAMPLE_CSV_FILE_PATH = "./test.csv";
    public static final String SENTIMENT_DISTRIBUTION_PATH = "./CrabParentSentimentDistribution.csv";

    public static final String RESULTS_JSON_PATH = "./CrabResults.json";
    public static final String OPTIMAL_RESULTS_JSON_PATH = "./CrabOptimalCrawlResults.json";

    public static ConcurrentHashMap map = new ConcurrentHashMap<>();

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock w = rwl.writeLock();

    //Read in Database
    public static boolean readIn(CrabStack crabStack) throws IOException, CsvException {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH));
                CSVReader csvReader = new CSVReader(reader)
        ) {
            // Reading Records One by One in a String array
            String[] nextURL;
            while ((nextURL = csvReader.readNext()) != null) {
                crabStack.push(nextURL[0]);
            }
            return true;
        } catch (IOException | CsvValidationException e) {

            return false;
        }
    }

    /**
     Writes crawl data to a JSON file
     */
    public static void writeResultsToJSON(final ConcurrentHashMap<String,SentimentType> con_map, final String file_addr) throws IOException {

        final JsonFactory factory = new JsonFactory();

        final JsonGenerator gen = factory.createGenerator(
                new File(file_addr), JsonEncoding.UTF8);

        gen.setPrettyPrinter(new MinimalPrettyPrinter(""));

        for(final String url : con_map.keySet()){
            //We'll use Jsoup in here just for testing at the moment to extract the title
                final Document doc = Jsoup.connect(url).get();

                gen.writeStartObject();
                gen.writeStringField("URL",url);
                gen.writeStringField("Sentiment",String.valueOf(con_map.get(url)));
                gen.writeStringField("Title",doc.title());
                gen.writeEndObject();
                gen.writeRaw('\n');
        }
        gen.close();
    }

    /**
        Serialize the Sentiment map
     */
    public static void SerializeConMap(final ConcurrentHashMap<String,SentimentType> con_map) throws IOException {
        FileOutputStream fs =
                new FileOutputStream("con_map.ser");
        ObjectOutputStream os = new ObjectOutputStream(fs);
        os.writeObject(con_map);
        os.close();
        fs.close();
    }

    /**
     Serialize the Sentiment map
     */
    public static void SerializeConMap(final ConcurrentHashMap<String,SentimentType> con_map, String fname) throws IOException {
        FileOutputStream fs =
                new FileOutputStream(fname);
        ObjectOutputStream os = new ObjectOutputStream(fs);
        os.writeObject(con_map);
        os.close();
        fs.close();
    }

    /**
        Deserialize the Sentiment map
     */
    public static ConcurrentHashMap<String,SentimentType> DeserializeConMap() throws IOException, ClassNotFoundException {
        FileInputStream fs = new FileInputStream("con_map.ser");
        ObjectInputStream os = new ObjectInputStream(fs);
        map = (ConcurrentHashMap)os.readObject();
        os.close();
        fs.close();
        return map;
    }

    /**
     Deserialize the Sentiment map
     */
    public static ConcurrentHashMap DeserializeConMap(String fname) throws IOException, ClassNotFoundException {
        FileInputStream fs = new FileInputStream(fname);
        ObjectInputStream os = new ObjectInputStream(fs);
        map = (ConcurrentHashMap)os.readObject();
        os.close();
        fs.close();
        return map;
    }


    public static void writeSentimentResult(final String url, final SentimentType sentiment){
        w.lock();

        try{
            try{
                FileWriter fileWriter = new FileWriter(FULL_RESULTS_FILE_PATH,true);

                CSVWriter writer = new CSVWriter(fileWriter);

                String [] record = {url,String.valueOf(sentiment)};

                writer.writeNext(record);

                writer.close();

            }
            catch(Exception e){

            }
        }finally {
            w.unlock();
        }
    }

    public static void writeSentimentDistribution(final String url, final double neg, final double ntrl, final double pos){
        w.lock();
        try{
            FileWriter fileWriter = new FileWriter(SENTIMENT_DISTRIBUTION_PATH,true);

            CSVWriter writer = new CSVWriter(fileWriter);

            String [] record = {url,String.valueOf(neg),String.valueOf(ntrl),String.valueOf(pos)};

            writer.writeNext(record);

            writer.close();

        }catch(Exception e){

        }
        finally{
            w.unlock();
        }
    }


    public synchronized static void writeURLSentimentResult(final String url, final int sentiment, final String title){
        w.lock();

        try{
            try{
                FileWriter fileWriter = new FileWriter(RESULTS_FILE_PATH,true);

                CSVWriter writer = new CSVWriter(fileWriter);

                String [] record = {url,String.valueOf(SentimentType.fromInt(sentiment)),title};

                writer.writeNext(record);

                writer.close();

            }
            catch(Exception e){

            }
        }finally {
            w.unlock();
        }
    }

    public synchronized static void writeURLOptimalSentimentResult(final String url, final int sentiment, final String title){
        w.lock();

        try{
            try{
                FileWriter fileWriter = new FileWriter(OPTIMAL_RESULTS_FILE_PATH,true);

                CSVWriter writer = new CSVWriter(fileWriter);

                String [] record = {url,String.valueOf(SentimentType.fromInt(sentiment)),title};

                writer.writeNext(record);

                writer.close();

            }
            catch(Exception e){

            }
        }finally {
            w.unlock();
        }
    }

    public synchronized static void writeKeywordSentimentResult(final String keyword, final String URL, final SentimentType sentiment){
        w.lock();

        final String KEYWORD_FILE_PATH = "./" + keyword + ".csv";

        try{
            try{
                FileWriter fileWriter = new FileWriter(KEYWORD_FILE_PATH,true);

                CSVWriter writer = new CSVWriter(fileWriter);

                String [] record = {keyword,URL, String.valueOf(sentiment)};

                writer.writeNext(record);

                writer.close();

            }catch(Exception e){

            }
        }finally{
            w.unlock();
        }
    }
}