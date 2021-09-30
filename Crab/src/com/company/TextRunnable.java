package com.company;

//This class will be used as a thread to instantiate Weka's textual analysis on the text extracted


public class TextRunnable implements Runnable{

    String textToAnalyse;

    public TextRunnable(String txtA){
        this.textToAnalyse = txtA;
    }

    public void run(){

    }

}
