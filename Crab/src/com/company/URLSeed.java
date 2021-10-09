package com.company;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class URLSeed {

    //used to take a database and create a URL Seed Set
    //uses a stack to store URL's

    public static String SAMPLE_CSV_FILE_PATH = "./test.csv";

    public static Stack stack = new Stack();

    //Read in Database
    public static boolean readIn(Stack stack) throws IOException, CsvException {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH));
                CSVReader csvReader = new CSVReader(reader);
        ) {
            // Reading Records One by One in a String array
            String[] nextURL;
            int i = 0;
            while ((nextURL = csvReader.readNext()) != null) {
                stack.push(nextURL[0]);
            }
            return true;
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getURL(){
        return stack.pop();
    }

    public String lastURLStored(){
        return stack.peek();
    }

    public void addURL(String value){
        stack.push(value);
    }

}





