import java.util.*;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    private final static int WARMUP_SIZE = 10000;
    static List<String> testList = new ArrayList<>(WARMUP_SIZE);

    /* Static portion that's used to warm the JVM up */
    static {
        for (int i = 0; i < WARMUP_SIZE; i++)
            testList.add(Integer.toString(i));
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, CsvException {
        Crab c = new Crab();

        c.keyWords.add("Bitcoin");
        c.keyWords.add("bitcoin");
        c.keyWords.add("Cardano");
        c.keyWords.add("cardano");

        Crab.blockList.add("https://facebook.com/");
        Crab.blockList.add("https://twitter.com/");

        Crab.visitedList.add("blank"); //add blank value
        Utility.SerializeQueue(Crab.visitedList);


        Crab.CrabCrawl();
    }
}
