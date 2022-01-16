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

/*
 * SentimentKeyWordRunnable
 * This class takes a URL and then looks if the title contains a keyword then runs sentiment analysis.
 * Otherwise it will return.
 *
 * Created: 16/1/2022

 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SentimentKeyWordRunnable implements Runnable {

    String link;

    SentimentKeyWordRunnable(String URL){
        this.link = URL;
    }

    public void run(){

        try{
            final Document doc = Jsoup.connect(link).get();

            for(String keyword : Crab.keyWords){
                if(doc.title().contains(keyword)){

                    Document docTwo;
                    final Elements links = doc.select("a[href]");




                    break; //don't loop again given we have already done the analysis
                }
            }




        }catch(Exception e){

        }







    }


}
