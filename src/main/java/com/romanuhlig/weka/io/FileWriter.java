package com.romanuhlig.weka.io;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.romanuhlig.weka.classification.ClassificationResult;
import weka.core.Instances;

import java.io.BufferedWriter;
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
//
//    public static void writeClassificationResults(List<ClassificationResult> results, String folder, String
//            filename) {
//
//        // make sure folder exists
//        new File(folder).mkdirs();
//
//        String fullFilePath = folder + filename + ".csv";
//
//        try (
//                Writer writer = Files.newBufferedWriter(Paths.get(fullFilePath));
//        ) {
//            StatefulBeanToCsv<ClassificationResult> beanToCsv = new StatefulBeanToCsvBuilder(writer)
//                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
//                    .build();
//
//            beanToCsv.write(results);
//
//        } catch (Exception e) {
//            System.err.println("could not write classification result " + fullFilePath);
//        }
//
//    }

    public static void writeClassificationResults(List<ClassificationResult> results, String folder, String
            filename) {

        // make sure folder exists
        new File(folder).mkdirs();

        String fullFilePath = folder + filename + ".csv";

        try (
                Writer writer = Files.newBufferedWriter(Paths.get(fullFilePath));

                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {

            csvWriter.writeNext(ClassificationResult.getHeaderForCSV());

            for (ClassificationResult result : results) {
                csvWriter.writeNext(result.getDataForCSV());
            }

        } catch (Exception e) {
            System.err.println("could not write classification result " + fullFilePath);
        }

    }

    public static void writeTextFile(String text, String folder, String filename) {

        File logFile = new File(folder + filename);

        try (
                BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(logFile));
        ) {
            writer.write(text);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void writeFeaturesUsed(Instances trainingData, Instances testData, String folder, String filename) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Training Data features:   ");
        for (int i = 0; i < trainingData.numAttributes(); i++) {
            stringBuilder.append(trainingData.attribute(i).name());
            stringBuilder.append("   ");
        }

        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("Test Data features:       ");
        for (int i = 0; i < trainingData.numAttributes(); i++) {
            stringBuilder.append(trainingData.attribute(i).name());
            stringBuilder.append("   ");
        }

        writeTextFile(stringBuilder.toString(), folder, filename);

    }


}
