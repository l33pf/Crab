import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DownloadCallable implements Callable<Boolean> {

    String URL;
    Queue<String> keywords;
    boolean provided = false;

    DownloadCallable(String linkToDownload){
        this.URL = linkToDownload;
    }

    DownloadCallable(String linkToDownload, Queue<String> keywordMatches){
        Objects.requireNonNull(this.URL = linkToDownload);
        Objects.requireNonNull(this.keywords = keywordMatches);
        this.provided = true;
    }

    String getPageContent(final String link){
        String toAnalyse = "";
        try{
            final Document doc = Jsoup.connect(link).get();
            Elements contents = doc.select("p");

            for(Element para : contents){
                toAnalyse = toAnalyse.concat(para.text());
            }

        }catch(Exception ex){
            return null;
        }
        return  toAnalyse;
    }

    @Override
    public Boolean call() throws Exception {

        System.out.println("Doing full sentiment analysis");

        //Download the page's content
        final String text = getPageContent(URL);

        //Send to the Sentiment Analyser callable
        Future<Integer> fut = Crab.exec.submit(new SentimentAnalyserCallable(text));

        while(!fut.isDone()){
            //Dependent on text length could be increased given more computationally expensive.
            Thread.sleep(1000);
        }

        int sentimentResult = fut.get();

        System.out.println("Full sentiment done on: " + URL + "\n");

        Utility.writeFullSentimentResult("full_sentiment_results.csv",URL,sentimentResult);

        if(provided){ //if a list of matching keywords has been provided
            //Update the structs in the keyword objects

            for(KeywordClass obj : Crab.keyWordQueue){
                keywords.forEach((String keyword)->{
                        if(keyword.matches(obj.keyword)){

                            switch (SentimentType.fromInt(sentimentResult)) {
                                case VERY_POSITIVE, POSITIVE -> obj.positiveSentiment.putIfAbsent(URL, sentimentResult);
                                case NEUTRAL -> obj.neutralSentiment.putIfAbsent(URL, sentimentResult);
                                case VERY_NEGATIVE, NEGATIVE -> obj.negativeSentiment.putIfAbsent(URL, sentimentResult);
                            }
                        }
                });
            }

        }

        return sentimentResult > 0;
    }
}