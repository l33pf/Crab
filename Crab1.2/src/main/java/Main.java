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

        //Example: Setting up a Keyword Crawl

        String [] blocked = new String[]{"t.me","www.facebook.com","www.twitter.com","www.linkedin.com"};
        String [] tags = new String[]{"NNP","JJ","NN","NNS","VB"};
        String [] words = new String[]{"Bitcoin","Ethereum","Algorand","Cardano","Solana"};

        Crab.readKwordCrawlInput(blocked,tags,words);

        Crab.KEYWORD_CRAWL = true;

        Crab.crabCrawl();
    }
}
