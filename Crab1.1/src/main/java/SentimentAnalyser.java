import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/* Class is used for Sentiment analysis using Stanford's CoreNLP package */

public final class SentimentAnalyser {

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final Lock w = rwl.writeLock();
    private static final Lock r = rwl.readLock();

    /**
     * We use this method to analyse a title from a webpage and give it a sentiment score.
     * @param title - Title from Webpage
     * @return - sentiment score from CoreNLP
     */
    public synchronized static int analyse(String title){

        Properties pp = new Properties();
        RedwoodConfiguration.current().clear().apply();
        pp.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(pp);
        Annotation annotation = pipeline.process(title);

        for(CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)){
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            return RNNCoreAnnotations.getPredictedClass(tree);
        }
        return 0; /* no sentiment */
    }

    public synchronized static HashMap<String,PriorityQueue<String>> pos_keywordTagger(final String title, final ArrayList<String> arr){
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        RedwoodConfiguration.current().clear().apply();
        props.setProperty("annotators", "tokenize,ssplit,pos");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = pipeline.processToCoreDocument(title);

        HashMap<String, PriorityQueue<String>> tagMap = new HashMap<>();

        for (CoreLabel tok : document.tokens()) {

            arr.forEach((String tg)->{ // filter out to find the tags we are interested in

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

            });
        }

        return tagMap;
    }



}