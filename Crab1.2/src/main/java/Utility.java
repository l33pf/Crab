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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class Utility{

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock w = rwl.writeLock();

    public enum IO_LEVEL{
        WRITE_VL_RECORD,
        WRITE_SENTIMENT_RESULT,
        WRITE_KWORD_VL_RECORD,
        WRITE_KWORD_SENTIMENT_RESULT,
        WRITE_KWORD_MATCHES,
        WRITE_KWORD_SENTIMENT_MATCHES,
        WRITE_OPT_SENTIMENT,
        WRITE_KWORD_SENTIMENT_SPEC
    }

    public enum SentimentType {
        VERY_POSITIVE,
        POSITIVE,
        NEUTRAL,
        NEGATIVE,
        VERY_NEGATIVE,
        ;

        /**
         * Get the sentiment class from the Stanford Sentiment Treebank
         * integer encoding. That is, an integer between 0 and 4 (inclusive)
         *
         * @param sentiment The Integer representation of a sentiment.
         *
         * @return The sentiment class associated with that integer.
         */
        public static SentimentType fromInt(final int sentiment) {
            return switch (sentiment) {
                case 0 -> VERY_NEGATIVE;
                case 1 -> NEGATIVE;
                case 2 -> NEUTRAL;
                case 3 -> POSITIVE;
                case 4 -> VERY_POSITIVE;
                default -> throw new NoSuchElementException("No sentiment value for integer: " + sentiment);
            };
        }
    }

    /**
     * @About Serialization class for Serializing/Deserializing
     * persistent objects in Crab.
     *
     */
    public final static class Serialization {

        public void serializeQueue(final Collection<String> q, final String fileName) throws IOException{
            try(Output out = new Output(new FileOutputStream(fileName))){
                Kryo kry = new Kryo(); kry.register(ConcurrentLinkedQueue.class);
                kry.writeClassAndObject(out,q);
            }
        }

        public void serializeMap(final Map map, final String fileName) throws IOException{
            try(Output out = new Output(new FileOutputStream(fileName))){
                Kryo kry = new Kryo(); kry.register(ConcurrentHashMap.class);
                kry.writeClassAndObject(out,map);
            }
        }

        public Collection<String> deserializeQueue(final String fileName){
                Collection<String> q = new ConcurrentLinkedQueue<>();
                try{
                    Input in = new Input(new FileInputStream(fileName)); Kryo kry = new Kryo();
                    kry.register(ConcurrentLinkedQueue.class);
                    q = (Collection<String>) kry.readClassAndObject(in); in.close();
                }catch(Exception ex){ex.printStackTrace();} return  q;
        }


        public Map deserializeMap(final String fileName){
                Map<String,Integer> map = new ConcurrentHashMap<>(); //check
                try{
                    Input in = new Input(new FileInputStream(fileName)); Kryo kry = new Kryo();
                    kry.register(ConcurrentHashMap.class);
                    map = (Map<String, Integer>) kry.readClassAndObject(in); in.close();
                }catch(Exception ex){ex.printStackTrace();} return map;
        }
    }

    /**
     * @About I/O Class for writing results into given file format
     *
     */
    public final static class DataIO {

        public static void readInURLSeed(final String fileName){
            try (
                    Reader reader = Files.newBufferedReader(Paths.get(fileName));
                    CSVReader csvReader = new CSVReader(reader)
            ) {
                String[] nextURL;
                while ((nextURL = csvReader.readNext()) != null) {
                    Crab.urlQueue.add(nextURL[0]);
                    Crab.parentSetMap.put(nextURL[0],true);
                }
            } catch (IOException | CsvValidationException e){e.printStackTrace();} {
            }

        }

        public static void writeOut(IO_LEVEL lvl,writerObj obj){
            w.lock();
            try{
                try{
                    FileWriter fw;
                    CSVWriter cs;
                    switch (lvl) {
                        case WRITE_SENTIMENT_RESULT -> {
                            fw = new FileWriter("crab_sentiment_results.csv", true);
                            cs = new CSVWriter(fw);
                            cs.writeNext(new String[]{obj.url, String.valueOf(obj.sentimentLevel)});
                            cs.close();
                            fw.close();
                        }
                        case WRITE_KWORD_VL_RECORD -> {
                            fw = new FileWriter("crab_kword_visit_list.csv", true);
                            cs = new CSVWriter(fw);
                            cs.writeNext(new String[]{obj.url});
                            cs.close();
                            fw.close();
                        }
                        case WRITE_KWORD_SENTIMENT_RESULT -> {
                            fw = new FileWriter("crab_kword_sentiment_results.csv", true);
                            cs = new CSVWriter(fw);
                            cs.writeNext(new String[]{obj.url, String.valueOf(obj.sentimentLevel), obj.keyword});
                            cs.close();
                            fw.close();
                        }
                        case WRITE_KWORD_MATCHES -> {
                            fw = new FileWriter("crab_kword_matches.csv", true);
                            cs = new CSVWriter(fw);
                            cs.writeNext(new String[]{obj.url, obj.keyword});
                            cs.close();
                            fw.close();
                        }
                        case WRITE_OPT_SENTIMENT -> {
                            fw = new FileWriter("crab_opt_sentiment_results.csv", true);
                            cs = new CSVWriter(fw);
                            cs.writeNext(new String[]{obj.url, String.valueOf(SentimentType.fromInt(obj.sentimentLevel))});
                            cs.close();
                            fw.close();
                        }
                        case WRITE_KWORD_SENTIMENT_MATCHES -> {
                            fw = new FileWriter("crab_kword_sentiment_results.csv", true);
                            cs = new CSVWriter(fw);
                            obj.keywordMatches.forEach((String match)->{
                                cs.writeNext(new String[]{obj.url,match, String.valueOf(SentimentType.fromInt(obj.sentimentLevel))});
                            });
                            cs.close();
                            fw.close();
                        }

                        case WRITE_KWORD_SENTIMENT_SPEC -> {
                            for(String match : obj.keywordMatches){
                                FileWriter f_w = new FileWriter(match + ".csv",true);
                                CSVWriter c = new CSVWriter(f_w);
                                c.writeNext(new String[]{obj.url, String.valueOf(SentimentType.fromInt(obj.sentimentLevel))});
                                c.close();
                                f_w.close();
                            }
                        }
                    }
                }catch(Exception ex){ex.printStackTrace();}
            }finally{
                w.unlock();
            }
        }

        public static void writeOut(IO_LEVEL lvl, String link){
            w.lock();
            try{
                FileWriter fw;
                CSVWriter cs;

                try{
                    switch (lvl) {
                        case WRITE_VL_RECORD -> {
                            fw = new FileWriter("crab_visit_list.csv", true);
                            cs = new CSVWriter(fw);
                            cs.writeNext(new String[]{link});
                            cs.close();
                            fw.close();
                        }
                        case WRITE_KWORD_VL_RECORD -> {
                            fw = new FileWriter("crab_kword_visit_list.csv", true);
                            cs = new CSVWriter(fw);
                            cs.writeNext(new String[]{link});
                            cs.close();
                            fw.close();
                        }
                    }
                }catch(Exception ex){ex.printStackTrace();}
            }finally {
                w.unlock();
            }
        }
    }

    /**
     * @About Class for interacting with CoreNLP methods
     */
    public static class SentimentAnalyser{
        private static final ReentrantReadWriteLock rwl_sentiment = new ReentrantReadWriteLock();
        private static final Lock w_sentiment = rwl_sentiment.writeLock();

        private static final ReentrantReadWriteLock rSentiment = new ReentrantReadWriteLock();
        private static final Lock r_sentiment = rSentiment.readLock();

        public static int analyse(final String title){
                    try{
                        w_sentiment.lock();
                        final Properties pp = new Properties();
                        RedwoodConfiguration.current().clear().apply();
                        pp.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
                        final StanfordCoreNLP pipeline = new StanfordCoreNLP(pp);
                        final Annotation annotation = pipeline.process(title);

                        for(final CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)){
                            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                            return RNNCoreAnnotations.getPredictedClass(tree);
                        }
                        return 0;
                    }finally {
                        w_sentiment.unlock();
                    }
        }

        public static HashMap<String,PriorityQueue<String>> pos_keywordTagger(final String title, final ArrayList<String> arr){
                try{
                    w_sentiment.lock();

                    final Properties props = new Properties();
                    RedwoodConfiguration.current().clear().apply();
                    props.setProperty("annotators", "tokenize,ssplit,pos");
                    final StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                    final CoreDocument document = pipeline.processToCoreDocument(title);

                    final HashMap<String, PriorityQueue<String>> tagMap = new HashMap<>();
                    document.tokens().forEach((final CoreLabel tok)-> arr.forEach((final String tg)->{
                        if(tagMap.containsKey(tok.tag())){
                            PriorityQueue q = tagMap.get(tok.tag());

                            if(!q.contains(tok.word())){
                                q.add(tok.word());
                                tagMap.put(tok.tag(),q);
                            }
                        }else{
                            PriorityQueue<String> q = new PriorityQueue<>();
                            q.add(tok.word());
                            tagMap.put(tok.tag(),q);
                        }
                    }));

                    return tagMap;


                }finally{
                    w_sentiment.unlock();
                }
        }

        public static Queue<String> checkKword(final HashMap<String,PriorityQueue<String>> t_map, final ConcurrentHashMap<String, Crab.KeywordClass> keyWordMap) throws URISyntaxException {
            try{
                r_sentiment.lock();
                Queue<String> matches = new ArrayDeque<>();
                for(String keyword : keyWordMap.keySet()){
                    for(String tag : t_map.keySet()){
                        PriorityQueue<String> p_queue = t_map.get(tag);

                        p_queue.forEach((String word)->{
                            if(word.matches(keyword)){
                                matches.add(keyword);
                            }
                        });
                    }
                }
                return matches;
            }finally {
                r_sentiment.unlock();
            }
        }
    }
}
