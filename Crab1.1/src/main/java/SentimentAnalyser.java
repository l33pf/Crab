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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import java.util.Properties;
import java.util.stream.Collectors;

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



}