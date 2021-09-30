package com.company;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

//Use this class to Tokenize extracted text and then run Named-Entity Recognition

public class TA {
        InputStream modelIn = null;
        SimpleTokenizer tokenize = SimpleTokenizer.INSTANCE;

        public TA(){

                try{
                        modelIn = new FileInputStream("en-token.bin");
                        TokenizerModel model = new TokenizerModel(modelIn);
                        TokenizerME tokenizer = new TokenizerME(model);
                }catch(IOException e){
                        e.printStackTrace();
                }

        }

        //Takes the text of the page and tokenizes it, returns an array of tokens in string format
        public String [] tokenizePage(String text) {
                 return tokenize.tokenize(text);
        }









}
