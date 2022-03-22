import java.util.ArrayList;
import java.util.List;

public class Main {

    private final static int WARMUP_SIZE = 10000;
    static List<String> testList = new ArrayList<>(WARMUP_SIZE);

    /* Static portion that's used to warm the JVM up */
    static {
        for (int i = 0; i < WARMUP_SIZE; i++)
            testList.add(Integer.toString(i));
    }

    public static void main(String [] args){

        //Console call

        Crab.crabCrawl();

        /* EXAMPLE: Setting Up a Keyword Crawl
        Crab.blockedList.add("t.me");
        Crab.blockedList.add("www.facebook.com");
        Crab.blockedList.add("www.twitter.com");
        Crab.blockedList.add("www.linkedin.com");

        Crab.cTags.add("NNP");
        Crab.cTags.add("JJ");
        Crab.cTags.add("NN");
        Crab.cTags.add("NNS");
        Crab.cTags.add("VB");

        Crab.KeywordClass one = new Crab.KeywordClass("Bitcoin");
        Crab.KeywordClass two = new Crab.KeywordClass("Ethereum");
        Crab.KeywordClass thr = new Crab.KeywordClass("DeFi");

        Crab.keywordMap.put(one.keyword,one);
        Crab.keywordMap.put(two.keyword,two);
        Crab.keywordMap.put(thr.keyword,thr);

        Crab.KEYWORD_CRAWL = true;
        
        Crab.crabCrawl();
        */

    }
}
