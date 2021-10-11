package com.company;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

//This class is the Utility class for Crab
//I/O with CSV etc.

public final class Utility {

    public static void writeToCSV(final String file, String [] a){
    }

    public static void readToCSV(final String file, List<String> lst){

        int counter = 0;

        try{
            FileReader filereader = new FileReader(file);

            CSVReader csvReader = new CSVReader(filereader);
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                for (String cell : line) {
                    lst.add(counter,cell);
                    counter++;
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }





}
