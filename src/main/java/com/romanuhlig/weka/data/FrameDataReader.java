package com.romanuhlig.weka.data;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.romanuhlig.weka.controller.TestBenchSettings;
import com.romanuhlig.weka.io.FeatureExtractionResults;
import com.romanuhlig.weka.io.TrainingAndTestFilePackage;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FrameDataReader {

    static final String outputBaseFolder = "features";

    public static FrameDataSet readFrameDataSet(String filePath) {

        try (
                // reader in resource statement is automatically closed on failure
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
        ) {
            // read data directly into annotated fields of FrameData class
            @SuppressWarnings("unchecked")
            CsvToBean<FrameData> csvToBean =
                    new CsvToBeanBuilder(reader)
                            .withType(FrameData.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .withSeparator(';')
                            .build();
            List<FrameData> frameDataFromFile = csvToBean.parse();

            FrameDataSet frameDataSet = new FrameDataSet(frameDataFromFile);

/*            for (int i = 0; i < frameDataSet.getAllFrameData().get(0).size(); i++) {
                System.out.println("linear acceleration " + frameDataSet.getAllFrameData().get(0).get(i).getLinAccelerationX());
                System.out.println("angular acceleration " + frameDataSet.getAllFrameData().get(0).get(i).getAngAccelerationX());
 }
 */

            System.out.println(frameDataSet.getSubject());
            System.out.println(frameDataSet.getActivity());

            return frameDataSet;

        } catch (
                Exception e) {
            System.err.println("unable to read file " + filePath);
            return null;
        }

    }

    // the test subject name is not a feature, but including it in the output data can be useful
    // when checking if the data is written correctly
    static boolean includeSubjectNameInFeatureOutput = false;

    public static ArrayList<FrameDataSet> readAllFrameDataSets(String inputFilePath) {

        // read existing data
        // get files in path
        File inputFolder = new File(inputFilePath);
        File[] listOfInputFiles = inputFolder.listFiles();

        ArrayList<FrameDataSet> allFrameDataSets = new ArrayList<>();
        for (File inputFile : listOfInputFiles) {
            // exclude lock files
            if (inputFile.getName().startsWith(".~")) {
                continue;
            }

            FrameDataSet frameDataSet = readFrameDataSet(inputFile.getPath());
            allFrameDataSets.add(frameDataSet);
        }

        return allFrameDataSets;

    }

    public static ArrayList<FrameDataSet> separateFrameDataSetsIntoWindows(ArrayList<FrameDataSet> originalFrameDataSets, double windowSize, double timeBetweenWindows) {

        ArrayList<FrameDataSet> windows = new ArrayList<>();
        for (FrameDataSet frameDataSet : originalFrameDataSets) {
            ArrayList<FrameDataSet> dataSetWindows = frameDataSet.separateFrameDataIntoWindows(windowSize, timeBetweenWindows);
            windows.addAll(dataSetWindows);
        }

        return windows;
    }

    public static FeatureExtractionResults createFeatureSets(String inputFilePath, String outputFilePath) {


        // read original recorded data, and separate into windows
        ArrayList<FrameDataSet> originalFrameDataSets = readAllFrameDataSets(inputFilePath);
        ArrayList<FrameDataSet> windows = separateFrameDataSetsIntoWindows(
                originalFrameDataSets,
                TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion(),
                TestBenchSettings.getWindowSpacingForFrameDataToFeatureConversion());

        // collect all subject names
        HashSet<String> subjectNames = new HashSet<>();
        for (FrameDataSet originalFrameDataSet : originalFrameDataSets) {
            subjectNames.add(originalFrameDataSet.getSubject());
        }

        // prepare header for output file
        // identify sensors in original data
        ArrayList<String> sensorTypes = windows.get(0).getAllSensorPositions();
        ArrayList<String> headerFields = new ArrayList<>();
        // each feature for every sensor
        for (String sensorType : sensorTypes) {
            headerFields.add(sensorType + "_maximumHeight");
            headerFields.add(sensorType + "_averageHeight");
        }
        if (includeSubjectNameInFeatureOutput) {
            headerFields.add("subject");
        }
        headerFields.add("activity");

        ArrayList<OutputFeatureVector> outputFeatureVectors = new ArrayList<>();

        // extract features
        // for each window
        for (FrameDataSet singleWindow : windows) {

            // create new data line
            OutputFeatureVector currentOutputFeatureVector = new OutputFeatureVector(singleWindow.getSubject());
            outputFeatureVectors.add(currentOutputFeatureVector);
            ArrayList<ArrayList<FrameData>> frameDataSet = singleWindow.getAllFrameData();

            for (ArrayList<FrameData> singleSensor : frameDataSet) {

                // calculate features for sensor
                double maximumHeight = 0;
                double averageHeight = 0;

                for (FrameData frameData : singleSensor) {

                    // if (frameData.getSensorPosition().equals("head"))
                    //     System.out.println(frameData.getCalPosZ()); // correctly shows different values from original file

                    if (frameData.getCalPosZ() > maximumHeight) {
                        maximumHeight = frameData.getCalPosZ();
                    }
                    averageHeight += frameData.getCalPosZ();
                }
                averageHeight /= singleSensor.size();

                // add features to data line
                currentOutputFeatureVector.addFeature(Double.toString(maximumHeight));
                currentOutputFeatureVector.addFeature(Double.toString(averageHeight));

                //  System.out.println("max          " + maximumHeight);
                //  System.out.println("avg          " + averageHeight);

            }

            if (includeSubjectNameInFeatureOutput) {
                currentOutputFeatureVector.addFeature(singleWindow.getSubject());
            }
            currentOutputFeatureVector.addFeature(singleWindow.getActivity());

        }


        // create folder for feature output file
        String outputFeaturesFilePath = outputFilePath + "/" + outputBaseFolder + "/";
        new File(outputFeaturesFilePath).mkdirs();

        // create folder for each subject
        ArrayList<String> subjectNameList = new ArrayList<>(subjectNames);
        for (String subject : subjectNameList) {
            String subjectFolderPath = outputFilePath + "/" + outputBaseFolder + "/" + subject + "/";
            new File(subjectFolderPath).mkdirs();
        }

        FeatureExtractionResults featureExtractionResults = new FeatureExtractionResults(sensorTypes);

        // create training and test file for each subject
        for (String subject : subjectNameList) {

            // collect training and test examples
            ArrayList<OutputFeatureVector> onlySubjectVectors = new ArrayList<>();
            ArrayList<OutputFeatureVector> allButSubjectVectors = new ArrayList<>();
            for (OutputFeatureVector originalVector : outputFeatureVectors) {
                if (originalVector.subject.equals(subject)) {
                    onlySubjectVectors.add(originalVector);
                } else {
                    allButSubjectVectors.add(originalVector);
                }
            }

            // write and collect files
            String trainingFilePath = outputFeaturesFilePath + subject + "/trainingDataSet.csv";
            String testFilePath = outputFeaturesFilePath + subject + "/testDataSet.csv";

            writeOutputFeatureVectorToCSV(trainingFilePath, headerFields, allButSubjectVectors);
            writeOutputFeatureVectorToCSV(testFilePath, headerFields, onlySubjectVectors);

            featureExtractionResults.addTrainingAndTestFilePackage(new TrainingAndTestFilePackage(trainingFilePath, testFilePath, subject));

        }

        // write feature data in file
        writeOutputFeatureVectorToCSV(outputFeaturesFilePath + "/allDataInOne.csv", headerFields, outputFeatureVectors);

        return featureExtractionResults;

    }


    private static void writeOutputFeatureVectorToCSV(String filePath, ArrayList<String> headerFields, ArrayList<OutputFeatureVector> featureVectors) {
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(filePath));

                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            //  String[] headerRecord = {"Name", "Email", "Phone", "Country"};
            csvWriter.writeNext(headerFields.toArray(new String[headerFields.size()]));

            for (OutputFeatureVector outputFeatureVector : featureVectors) {
                csvWriter.writeNext(outputFeatureVector.getFeaturesAsArray());
            }

        } catch (Exception e) {
            System.err.println("unable to write file " + featureVectors);
        }
    }


}


































