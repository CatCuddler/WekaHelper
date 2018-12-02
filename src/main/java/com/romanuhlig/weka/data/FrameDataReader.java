package com.romanuhlig.weka.data;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FrameDataReader {

    static final String outputBaseFolder = "features";

    public static FrameDataSet readFrameData(String filePath) {

        try (
                // reader in resource statement is automatically closed on failure
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
        ) {
            // read data directly into annotated fields of FrameData class
            CsvToBean<FrameData> csvToBean =
                    new CsvToBeanBuilder(reader)
                            .withType(FrameData.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .withSeparator(';')
                            .build();
            List<FrameData> frameDataFromFile = csvToBean.parse();

            FrameDataSet frameDataSet = new FrameDataSet(frameDataFromFile);

            for (int i = 0; i < frameDataSet.getAllFrameData().get(0).size(); i++) {
                System.out.println("linear acceleration " + frameDataSet.getAllFrameData().get(0).get(i).getLinAccelerationX());
                System.out.println("angular acceleration " + frameDataSet.getAllFrameData().get(0).get(i).getAngAccelerationX());
            }

            return frameDataSet;

        } catch (Exception e) {
            System.err.println("unable to read file " + filePath);
            return null;
        }
    }


    public static void createFeatureSets(String inputFilePath, String outputFilePath) {

        // create feature table output folder
        String outputFeaturesFilePath = outputFilePath + "/" + outputBaseFolder + "/";
        new File(outputFeaturesFilePath).mkdirs();

        // read existing data
        FrameDataSet dataSet = readFrameData(inputFilePath);

        // write data back out
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(outputFeaturesFilePath + "testFile"));
        ) {
            StatefulBeanToCsv<FrameData> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();


            beanToCsv.write(dataSet.composeFlatList());

        } catch (Exception e) {
            System.err.println("unable to write file " + outputFeaturesFilePath);
        }

    }

}


































