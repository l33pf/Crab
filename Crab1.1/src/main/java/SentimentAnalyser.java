import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;


import java.util.Properties;

/* Class is used for Sentiment analysis using Stanford's CoreNLP package */

public final class SentimentAnalyser {

    /**
     * We use this method to analyse a title from a webpage and give it a sentiment score.
     * @param title - Title from Webpage
     * @return - sentiment score from CoreNLP
     */
    public static int analyse(final String title){

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

    /**
     *
     * @return - sentiment average over a page
     * TODO
     */
    public int analysePage(){
        return 0;
    }

}