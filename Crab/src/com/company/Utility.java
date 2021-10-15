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

    private static final String [] writeOut = new String[1];

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

            try{
                FileWriter fileWriter = new FileWriter(RESULTS_FILE_PATH);

                CSVWriter writer = new CSVWriter(fileWriter);

                for(Map.Entry<String,Float>entry : resultMap.entrySet()){
                        String xx = entry.getKey();
                        String yy = Float.toString(entry.getValue());
                        writeOut[0] = xx;
                        writeOut[1] = yy;
                        writer.writeNext(writeOut);
                }

            }
            catch(Exception e){
                e.printStackTrace();
            }

    }






}
