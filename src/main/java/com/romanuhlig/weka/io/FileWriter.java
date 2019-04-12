package com.romanuhlig.weka.io;

import com.opencsv.CSVWriter;
import com.romanuhlig.weka.classification.ClassificationResult;
import weka.core.Instances;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileWriter {


    public static void writeClassificationResult(ClassificationResult result, String folder, String filename) {

        ensureFolderExists(folder);

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

        ensureFolderExists(folder);

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
        ensureFolderExists(folder);

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

        ensureFolderExists(folder);

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

    public static void writeNewFeatureExtractionResults(SubjectsFeatureExtractionResults results, String currentFolder, String existingFeaturesInputFolder, String filename) {

        // get rid of any existing feature extraction result
        File inputFolder = new File(existingFeaturesInputFolder);
        File[] listOfInputFiles = inputFolder.listFiles();
        if (listOfInputFiles != null) {
            for (int i = listOfInputFiles.length - 1; i >= 0; i--) {
                listOfInputFiles[i].delete();
            }
        }


        // save new file
        serializeObject(results, currentFolder, filename);
        serializeObject(results, existingFeaturesInputFolder, filename);
    }

    private static void serializeObject(SubjectsFeatureExtractionResults results, String folder, String filename) {

        ensureFolderExists(folder);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(folder + "/" + filename);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(results);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (Exception e) {
            System.out.println("unable to write serialize object");
        }

    }


    public static SubjectsFeatureExtractionResults readExistingFeatureSet(String existingFeaturesInputFolder) {

        File inputFolder = new File(existingFeaturesInputFolder);
        File[] listOfInputFiles = inputFolder.listFiles();

        try {
            FileInputStream fileInputStream = new FileInputStream(listOfInputFiles[0].getPath());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            SubjectsFeatureExtractionResults result = (SubjectsFeatureExtractionResults) objectInputStream.readObject();
            objectInputStream.close();
            return result;
        } catch (Exception e) {
            System.out.println("unable to load existing feature extraction result");
            e.printStackTrace();
            return new SubjectsFeatureExtractionResults(new ArrayList<>());
        }


    }

    private static void ensureFolderExists(String folder){
        new File(folder).mkdirs();
    }
}
