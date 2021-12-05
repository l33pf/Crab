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

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//This class is the Utility class for Crab, Provides I/O etc.

public final class Utility {

    public static final String RESULTS_FILE_PATH = "./CrabResults.csv";
    public static final String AVERAGE_CRAWL_RESULTS_FILE_PATH = "./AverageOptimal.csv";
    public static final String FULL_RESULTS_FILE_PATH = "./CrabFullAnalysisSentimentCrawl.csv";

    private static final String [] writeOut = new String[2];

    public static ConcurrentHashMap map = new ConcurrentHashMap<>();

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock r = rwl.readLock();
    private static final Lock w = rwl.writeLock();

    public Utility() throws IOException {
    }

    /**
        Read a CSV file into a list
     */
    public static void readToCSV(final String file, List<String> lst){

        int counter = 0;

        try{
            FileReader filereader = new FileReader(file);

            CSVReader csvReader = new CSVReader(filereader);
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                for (String cell : line) {
                    lst.add(counter,cell);
                    counter++;
                }
            }

        }catch(Exception e){

        }
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
     * @return
     */
    public static ConcurrentHashMap DeserializeConMap(String fname) throws IOException, ClassNotFoundException {
        FileInputStream fs = new FileInputStream(fname);
        ObjectInputStream os = new ObjectInputStream(fs);
        map = (ConcurrentHashMap)os.readObject();
        os.close();
        fs.close();
        return map;
    }

    /**
       Write Sentiment results to CSV file
     */
    public static void writeToCSV(final ConcurrentHashMap<String,SentimentType> con_map){

        try{
            FileWriter fileWriter = new FileWriter(RESULTS_FILE_PATH);

            CSVWriter writer = new CSVWriter(fileWriter);

           for(Map.Entry<String,SentimentType> entry : con_map.entrySet()){
                        String[] data = {entry.getKey(),String.valueOf(entry.getValue())};
                        writer.writeNext(data);
            }

            writer.close();

        }catch(Exception e){

        }
    }

    public final static void writeSentimentResult(final String url, final SentimentType sentiment){
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

    public final static void writeURLSentimentResult(final String url, final SentimentType sentiment, final String title){
        w.lock();

        try{
            try{
                FileWriter fileWriter = new FileWriter(RESULTS_FILE_PATH,true);

                CSVWriter writer = new CSVWriter(fileWriter);

                String [] record = {url,String.valueOf(sentiment),title};

                writer.writeNext(record);

                writer.close();

            }
            catch(Exception e){

            }
        }finally {
            w.unlock();
        }
    }



    public static void writeToCSV_FullAnalysis(final ConcurrentHashMap<String,SentimentType> con_map){

        try{
            FileWriter fileWriter = new FileWriter(FULL_RESULTS_FILE_PATH,true);

            CSVWriter writer = new CSVWriter(fileWriter);

            for(Map.Entry<String,SentimentType> entry : con_map.entrySet()){
                String[] data = {entry.getKey(),String.valueOf(entry.getValue())};
                writer.writeNext(data);
            }

            writer.close();

        }catch(Exception e){

        }
    }








}
