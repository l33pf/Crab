package com.company;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class URLSeed {

    //used to take a database and create a URL Seed Set
    //uses a stack to store URL's

    private static final String SAMPLE_CSV_FILE_PATH = "./test.csv";

    public static Stack stck = new Stack();

    public static boolean readIn() throws IOException, CsvException {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH));
                CSVReader csvReader = new CSVReader(reader);
        ) {
            // Reading Records One by One in a String array
            String[] nextURL;
            while ((nextURL = csvReader.readNext()) != null) {
                stck.push(nextURL[0]);
            }
            return true;
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getURL(){
        return stck.pop();
    }

    public String lastURLStored(){
        return stck.peek();
    }

    public void addURL(String value){
        stck.push(value);
    }

}





