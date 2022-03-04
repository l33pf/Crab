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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.*;
import java.nio.file.*;

//This class is the Utility class for Crab, Provides I/O etc.

public final class Utility {

    public static final String RESULTS_FILE_PATH = "./CrabResults.csv";
    public static final String FULL_RESULTS_FILE_PATH = "./CrabFullAnalysisSentimentCrawl.csv";
    public static final String OPTIMAL_RESULTS_FILE_PATH = "./CrabOptimalCrawl.csv";
    public static String SAMPLE_CSV_FILE_PATH = "./test.csv";
    public static final String SENTIMENT_DISTRIBUTION_PATH = "./CrabParentSentimentDistribution.csv";
    public static final String VISIT_LIST_PATH = "./CrabVisitList.csv";

    public static final String RESULTS_JSON_PATH = "./CrabResults.json";
    public static final String JSON_KEYWORD_JSON_PATH = "./CrabKeyWordCrawlResults.json";
    public static final String CSV_KEYWORD_PATH = "./CrabKeyWordCrawlResults.csv";

    public static ConcurrentHashMap map = new ConcurrentHashMap<>();

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock w = rwl.writeLock();

    public static final Gson gson = new Gson();

    //Read in Database
    public static boolean readIn(CrabStack crabStack) {
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
        }
        return false;
    }

    public static boolean readIn_LF(CrabStack_LF crabStack){
        try(
                Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH));
                CSVReader csvReader = new CSVReader(reader)
        ){
            String[] nextUrl;
            while((nextUrl = csvReader.readNext()) != null){
                crabStack.push(nextUrl[0]);
                //Crab.record_map.put(nextUrl[0],true); //indicate all these values are part of the URL seed set
                Crab.parent_set.add(nextUrl[0]);
            }
            return true;
        }catch(IOException | CsvValidationException e){
        }
        return false;
    }

    /**
     Writes crawl data to a JSON file
     */
    public static void writeResultsToJSON(final ConcurrentHashMap<String,SentimentType> con_map, final String file_addr) {

        try{
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
        }catch(IOException ex){

        }
    }

    public static void writekeyWordResults_ToJSON(final ConcurrentHashMap<String,ConcurrentHashMap<String,SentimentType>> keywordDb){
        try{
            final JsonFactory factory = new JsonFactory();

            ConcurrentHashMap<String, SentimentType> dataMap;

            for(String keyword : keywordDb.keySet()){
                String fname = "./" + keyword + ".json";

                JsonGenerator gen = factory.createGenerator(
                        new File(fname), JsonEncoding.UTF8
                );

                dataMap = keywordDb.get(keyword);

                gen.setPrettyPrinter(new MinimalPrettyPrinter(""));

                gen.writeStartObject();

                for(String key : dataMap.keySet()){
                    gen.writeStringField("URL",key);
                    gen.writeStringField("Sentiment",String.valueOf(dataMap.get(key)));
                    gen.writeEndObject();
                    gen.writeRaw('\n');
                }

                gen.close();
            }
        }catch(IOException ex){

        }
    }

    /**
     Serialize the Sentiment map
     */
    public static void SerializeConMap(final ConcurrentHashMap<String,SentimentType> con_map, String fname) throws IOException {
        try{
            FileOutputStream fs =
                    new FileOutputStream(fname);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(con_map);
            os.close();
            fs.close();

        }catch(Exception ex){

        }
    }

    public static void SerializeRecordMap(final ConcurrentHashMap<String,Boolean>r_map, final String fname){
        try{
            FileOutputStream fs = new FileOutputStream(fname);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(r_map);
            os.close();
            fs.close();
        }catch(Exception ex){
        }
    }

    public static ConcurrentHashMap<String,Boolean> DeserializeRecordMap(final String fname){
            ConcurrentHashMap<String,Boolean> map = new ConcurrentHashMap<>();
            try{
                FileInputStream fs = new FileInputStream(fname);
                ObjectInputStream os = new ObjectInputStream(fs);
                map = (ConcurrentHashMap<String, Boolean>) os.readObject();
                os.close();
                fs.close();
            }catch(Exception ex){

            }
            return map;
    }


    public static void SerializeQueue(final ConcurrentLinkedQueue<String> q, final String fname){
        try{
            FileOutputStream fs =
                    new FileOutputStream(fname);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(q);
            os.close();
            fs.close();
        }catch(IOException ex){

        }
    }

    public static ConcurrentLinkedQueue<String> DeserializeQueue(final String fname){
        ConcurrentLinkedQueue<String> vList = new ConcurrentLinkedQueue<>();

        try{
            FileInputStream fs = new FileInputStream(fname);
            ObjectInputStream os = new ObjectInputStream(fs);
            vList = (ConcurrentLinkedQueue<String>) os.readObject();
            os.close();
            fs.close();
        }catch(Exception ex){

        }
        return vList;
    }

    /**
     Deserialize the Sentiment map
     */
    public static ConcurrentHashMap<String,SentimentType> DeserializeConMap(String fname) throws IOException, ClassNotFoundException {
        ConcurrentHashMap<String,SentimentType> c_map = new ConcurrentHashMap<>();
        try{
            FileInputStream fs = new FileInputStream(fname);
            ObjectInputStream os = new ObjectInputStream(fs);
            c_map = (ConcurrentHashMap) os.readObject();
            os.close();
            fs.close();
        }catch(Exception ex){

        }
        return c_map;
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

    public static void writeVisitList(final String url){
        w.lock();
        try{
            FileWriter fileWriter = new FileWriter(VISIT_LIST_PATH,true);

            CSVWriter writer = new CSVWriter(fileWriter);

            String [] record = {url};

            writer.writeNext(record);

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
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
}