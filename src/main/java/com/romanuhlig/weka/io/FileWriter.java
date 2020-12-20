package com.romanuhlig.weka.io;

import com.opencsv.CSVWriter;
import com.romanuhlig.weka.classification.ClassificationResult;
import weka.core.Instances;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes data to disk
 *
 * @author Roman Uhlig
 */
public class FileWriter {

    /**
     * Write a table containing a single classification result to disk
     *
     * @param result
     * @param folder
     * @param filename
     */
    public static void writeClassificationResult(ClassificationResult result, String folder, String filename) {

        ensureFolderExists(folder);

        // forward request to writer for multiple results
        List<ClassificationResult> results = new ArrayList<>();
        results.add(result);
        writeClassificationResults(results, folder, filename);
    }

    /**
     * Write table containing the given classification results to disk
     *
     * @param results
     * @param folder
     * @param filename
     */
    public static void writeClassificationResults(
            List<ClassificationResult> results, String folder, String filename) {

        ensureFolderExists(folder);

        String fullFilePath = folder + filename + ".csv";

        try (
                // create table writer
                Writer writer = Files.newBufferedWriter(Paths.get(fullFilePath));
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            // add header
            csvWriter.writeNext(ClassificationResult.getHeaderForCSV());

            // writer results line by line
            for (ClassificationResult result : results) {
                csvWriter.writeNext(result.getDataForCSV());
            }
        } catch (Exception e) {
            System.err.println("could not write classification result " + fullFilePath);
        }
    }

    /**
     * Write the given string to a simple text file
     *
     * @param text
     * @param folder
     * @param filename
     */
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

    /**
     * Write the names of the features used for the training and test data to disk
     *
     * @param trainingData
     * @param testData
     * @param folder
     * @param filename
     */
    public static void writeFeaturesUsed(Instances trainingData, Instances testData, String folder, String filename) {

        ensureFolderExists(folder);

        StringBuilder stringBuilder = new StringBuilder();

        // collect training data features
        stringBuilder.append("Training Data features:   ");
        for (int i = 0; i < trainingData.numAttributes(); i++) {
            stringBuilder.append(trainingData.attribute(i).name());
            stringBuilder.append("   ");
        }

        // formatting
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());

        // collect test data features
        stringBuilder.append("Test Data features:       ");
        for (int i = 0; i < testData.numAttributes(); i++) {
            stringBuilder.append(testData.attribute(i).name());
            stringBuilder.append("   ");
        }

        writeTextFile(stringBuilder.toString(), folder, filename);
    }

    /**
     * Delete all files within the existing features extraction result folder,
     * and place a new feature extraction result file within both folders
     *
     * @param results
     * @param currentFolder
     * @param existingFeaturesInputFolder
     * @param filename
     */
    public static void writeNewFeatureExtractionResults(SubjectsFeatureExtractionResults results, String currentFolder,
                                                        String existingFeaturesInputFolder, String filename) {

        // get rid of any existing feature extraction result
        File inputFolder = new File(existingFeaturesInputFolder);
        File[] listOfInputFiles = inputFolder.listFiles();
        if (listOfInputFiles != null) {
            for (int i = listOfInputFiles.length - 1; i >= 0; i--) {
                listOfInputFiles[i].delete();
            }
        }

        // save new file
        serializeFeatureExtractionResult(results, currentFolder, filename);
        serializeFeatureExtractionResult(results, existingFeaturesInputFolder, filename);
    }

    /**
     * Write a serialized version of the given feature extraction results to disk
     *
     * @param results
     * @param folder
     * @param filename
     */
    private static void serializeFeatureExtractionResult(
            SubjectsFeatureExtractionResults results, String folder, String filename) {

        ensureFolderExists(folder);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(folder + "/" + filename);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(results);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (Exception e) {
            System.out.println("unable to write serialized feature extraction results");
        }
    }

    /**
     * Load serialized feature extraction results from disk
     * <p>
     * Assumes that there is exactly one file within the given folder, which is a serialized version
     * of previously created feature extraction results
     *
     * @param existingFeaturesInputFolder
     * @return
     */
    public static SubjectsFeatureExtractionResults readExistingFeatureSet(String existingFeaturesInputFolder) {

        // get all files within the given folder
        File inputFolder = new File(existingFeaturesInputFolder);
        File[] listOfInputFiles = inputFolder.listFiles();

        try {
            // try to load the first file (there should be only one)
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

    /**
     * Create the given folder, if it does not exist yet
     *
     * @param folder
     */
    private static void ensureFolderExists(String folder) {
        new File(folder).mkdirs();
    }
}