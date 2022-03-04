import java.util.Objects;
import java.util.concurrent.Callable;

public class SentimentAnalyserCallable implements Callable<Integer> {

    String linkTitle;

    SentimentAnalyserCallable(String title){
        Objects.requireNonNull(this.linkTitle = title);
    }

    @Override
    public Integer call() throws Exception {
        return SentimentAnalyser.analyse(linkTitle);
    }
}
