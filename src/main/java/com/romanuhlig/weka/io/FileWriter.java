package com.romanuhlig.weka.io;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.romanuhlig.weka.classification.ClassificationResult;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileWriter {


    public static void writeClassificationResult(ClassificationResult result, String folder, String filename) {

        List<ClassificationResult> results = new ArrayList<>();
        results.add(result);

        writeClassificationResults(results, folder, filename);

    }

    public static void writeClassificationResults(List<ClassificationResult> results, String folder, String
            filename) {

        // make sure folder exists
        new File(folder).mkdirs();

        String fullFilePath = folder + filename + ".csv";

        try (
                Writer writer = Files.newBufferedWriter(Paths.get(fullFilePath));
        ) {
            StatefulBeanToCsv<ClassificationResult> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            beanToCsv.write(results);

        } catch (Exception e) {
            System.err.println("could not write classification result " + fullFilePath);
        }

    }


}
