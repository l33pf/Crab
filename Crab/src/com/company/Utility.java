package com.company;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

//This class is the Utility class for Crab
//I/O with CSV etc.

public final class Utility {

    private static final String RESULTS_FILE_PATH = "./CrabResults.csv";

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

    public static synchronized void writeResults(TreeMap<String,Float> resultMap){

        int counter = 0;
        String[] x = new String[resultMap.size()];
        String[] y = new String[resultMap.size()];

        String [][] xy = new String[resultMap.size()][resultMap.size()];

        for (Map.Entry<String, Float> entry : resultMap.entrySet()){
/*            x[counter] = entry.getKey();
            y[counter] = Float.toString(entry.getValue());
            counter++;*/
            xy[counter][0] = entry.getKey();
            xy[0][counter] = Float.toString(entry.getValue());
            counter++;
        }

        counter = 0;
        while(counter != resultMap.size()){

            try{
                FileWriter fileWriter = new FileWriter(RESULTS_FILE_PATH);

                CSVWriter writer = new CSVWriter(fileWriter);

                counter++;
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }

    }






}
