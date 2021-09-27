package com.company;

import java.lang.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CrawlRunnable implements Runnable {

    Stack links = new Stack();

    public CrawlRunnable(Stack links){
            this.links = links;
    }

    public void run(){

    }


}
