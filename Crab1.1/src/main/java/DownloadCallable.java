import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DownloadCallable implements Callable<Boolean> {

    final String URL;

    DownloadCallable(String linkToDownload){
        this.URL = linkToDownload;
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

        //Download the page's content
        final String text = getPageContent(URL);

        //Send to the Sentiment Analyser callable
        Future<Integer> fut = Crab.exec.submit(new SentimentAnalyserCallable(text));

        while(!fut.isDone()){
            //Dependent on text length could be increased given more computationally expensive.
            Thread.sleep(1000);
        }

        int sentimentResult = fut.get();

        Utility.writeFullSentimentResult("CrabFullSentiment.csv",URL,sentimentResult);

        return sentimentResult > 0;
    }
}