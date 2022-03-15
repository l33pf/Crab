/*
 ***LICENSE***
 Copyright (c) 2022 l33pf (https://github.com/l33pf)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 **/
import java.util.Objects;

public final class SentimentFullRunnable implements  Runnable{
    String URL, pageContent, keyWord; int sentimentScore, provided;

    SentimentFullRunnable(final String link, final String content){
        Objects.requireNonNull(this.URL = link);
        Objects.requireNonNull(this.pageContent = content);
        provided = 0;
    }

    /* If more than one keyword is matched to a page*/
    SentimentFullRunnable(final String link, final String content, final String keyWord){
        Objects.requireNonNull(this.URL = link);
        Objects.requireNonNull(this.pageContent = content);
        Objects.requireNonNull(this.keyWord = keyWord);
        provided = 1;
    }

    @Override
    public void run(){
        sentimentScore = SentimentAnalyser.analyse(pageContent);

        switch(provided){
            case 0 -> Utility.writeFullSentimentResult("full_sentiment_results.csv",URL,sentimentScore);
            case 1 -> Utility.writeFullSentimentResult("full_sentiment_results.csv",URL,sentimentScore,keyWord);
        }
    }
}