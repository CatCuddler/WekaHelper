package com.romanuhlig.weka.data;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.romanuhlig.weka.controller.TestBenchSettings;
import com.romanuhlig.weka.io.FeatureExtractionResults;
import com.romanuhlig.weka.io.TrainingAndTestFilePackage;
import com.romanuhlig.weka.math.MathHelper;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.romanuhlig.weka.controller.TestBenchSettings.FeatureTag;

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

/*            for (int i = 0; i < frameDataSet.getAllSensorLists().get(0).size(); i++) {
                System.out.println("linear acceleration " + frameDataSet.getAllSensorLists().get(0).get(i).getLinAccelerationX());
                System.out.println("angular acceleration " + frameDataSet.getAllSensorLists().get(0).get(i).getAngAccelerationX());
 }
 */

            //    System.out.println(frameDataSet.getSubject());
            //   System.out.println(frameDataSet.getActivity());

            return frameDataSet;

        } catch (
                Exception e) {
            System.err.println("unable to read file " + filePath);
            return null;
        }

    }


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
        ArrayList<String> headerFields = getHeaderForSensorTypes(sensorTypes, true);

        ArrayList<OutputFeatureVector> outputFeatureVectors = new ArrayList<>();

        // extract features
        // for each window
        for (FrameDataSet singleWindow : windows) {

            // create new data line
            OutputFeatureVector currentOutputFeatureVector = getFeaturesForFrameDataSet(singleWindow, true);
            outputFeatureVectors.add(currentOutputFeatureVector);

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


//            // sort data by class value
//            onlySubjectVectors.sort(new Comparator<OutputFeatureVector>() {
//                @Override
//                public int compare(OutputFeatureVector o1, OutputFeatureVector o2) {
//                    String o1Class = o1.getFeatures().get(o1.getFeatures().size() - 1);
//                    String o2Class = o2.getFeatures().get(o2.getFeatures().size() - 1);
//                    return o1Class.compareTo(o2Class);
//                }
//            });
//
//            allButSubjectVectors.sort(new Comparator<OutputFeatureVector>() {
//                @Override
//                public int compare(OutputFeatureVector o1, OutputFeatureVector o2) {
//                    String o1Class = o1.getFeatures().get(o1.getFeatures().size() - 1);
//                    String o2Class = o2.getFeatures().get(o2.getFeatures().size() - 1);
//                    return o1Class.compareTo(o2Class);
//                }
//            });


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


    public static ArrayList<String> getHeaderForFrameDataSet(FrameDataSet frameDataSet, boolean includeClassForTraining) {

        ArrayList<String> sensorTypes = frameDataSet.getAllSensorPositions();
        return getHeaderForSensorTypes(sensorTypes, includeClassForTraining);
    }


    public static OutputFeatureVector getFeaturesForFrameDataSet(FrameDataSet dataSource, boolean includeClassForTraining) {

        // create new data line
        OutputFeatureVector outputFeatureVector = new OutputFeatureVector(dataSource.getSubject());

        ArrayList<ArrayList<FrameData>> frameDataSet = dataSource.getAllSensorLists();

        for (ArrayList<FrameData> singleSensor : frameDataSet) {

            // calculate features for sensor
            double maximumHeight = Double.NEGATIVE_INFINITY;
            double averageHeight = 0;
            double minimumHeight = Double.POSITIVE_INFINITY;
            double minimumX = Double.POSITIVE_INFINITY;
            double minimumZ = Double.POSITIVE_INFINITY;
            double maximumX = Double.NEGATIVE_INFINITY;
            double maximumZ = Double.NEGATIVE_INFINITY;
            double averageVelocityX = 0;
            double averageVelocityHeight = 0;
            double averageVelocityZ = 0;
            double averageVelocityXZ = 0;
            double averageVelocityXYZ = 0;
            double averageAccelerationX = 0;
            double averageAccelerationHeight = 0;
            double averageAccelerationZ = 0;
            double averageAccelerationXZ = 0;
            double averageAccelerationXYZ = 0;
            double averageAngularVelocity = 0;
            double averageAngularAcceleration = 0;


            for (int i = 0; i < singleSensor.size(); i++) {

                FrameData frameData = singleSensor.get(i);

                if (i > 0) {
                    double timeSinceLastFrame = frameData.getTime() - singleSensor.get(i - 1).getTime();

                    averageVelocityX += (Math.abs(frameData.getLinVelX()) * timeSinceLastFrame);
                    averageVelocityHeight += (Math.abs(frameData.getLinVelY()) * timeSinceLastFrame);
                    averageVelocityZ += (Math.abs(frameData.getLinVelZ()) * timeSinceLastFrame);

                    averageVelocityXZ += MathHelper.EuclideanNorm(
                            frameData.getLinVelX(), frameData.getLinVelZ());
                    averageVelocityXYZ += MathHelper.EuclideanNorm(
                            frameData.getLinVelX(), frameData.getLinVelY(), frameData.getLinVelZ());

                    averageAccelerationX += (Math.abs(frameData.getLinAccelerationX()) * timeSinceLastFrame);
                    averageAccelerationHeight += (Math.abs(frameData.getLinAccelerationY()) * timeSinceLastFrame);
                    averageAccelerationZ += (Math.abs(frameData.getLinAccelerationZ()) * timeSinceLastFrame);

                    averageAccelerationXZ += MathHelper.EuclideanNorm(
                            frameData.getLinAccelerationX(), frameData.getLinAccelerationZ());
                    averageAccelerationXYZ += MathHelper.EuclideanNorm(
                            frameData.getLinAccelerationX(), frameData.getLinAccelerationY(), frameData.getLinAccelerationZ());

                    averageHeight += frameData.getCalPosY() * timeSinceLastFrame;

                    averageAngularVelocity +=
                            timeSinceLastFrame * MathHelper.EuclideanNorm(
                                    frameData.getAngVelX(),
                                    frameData.getAngVelY(),
                                    frameData.getAngVelZ());
                    averageAngularAcceleration +=
                            timeSinceLastFrame * MathHelper.EuclideanNorm(
                                    frameData.getAngAccelerationX(),
                                    frameData.getAngAccelerationY(),
                                    frameData.getAngAccelerationZ());


                }


                // if (frameData.getSensorPosition().equals("head"))
                //     System.out.println(frameData.getCalPosZ()); // correctly shows different values from original file

                if (frameData.getCalPosY() > maximumHeight) {
                    maximumHeight = frameData.getCalPosY();
                }
                if (frameData.getCalPosY() < minimumHeight) {
                    minimumHeight = frameData.getCalPosY();
                }


                if (frameData.getCalPosX() > maximumX) {
                    maximumX = frameData.getCalPosX();
                } else if (frameData.getCalPosX() < minimumX) {
                    minimumX = frameData.getCalPosX();
                }

                if (frameData.getCalPosZ() > maximumZ) {
                    maximumZ = frameData.getCalPosZ();
                } else if (frameData.getCalPosZ() < minimumZ) {
                    minimumZ = frameData.getCalPosZ();
                }
            }


            // adjust average values to overall time passed
            double overallTimePassed =
                    singleSensor.get(singleSensor.size() - 1).getTime()
                            - singleSensor.get(0).getTime();
            averageHeight /= overallTimePassed;
            averageVelocityX /= overallTimePassed;
            averageVelocityHeight /= overallTimePassed;
            averageVelocityZ /= overallTimePassed;
            averageVelocityXZ /= overallTimePassed;
            averageVelocityXYZ /= overallTimePassed;
            averageAccelerationX /= overallTimePassed;
            averageAccelerationHeight /= overallTimePassed;
            averageAccelerationXZ /= overallTimePassed;
            averageAccelerationXYZ /= overallTimePassed;
            averageAccelerationZ /= overallTimePassed;
            averageAngularVelocity /= overallTimePassed;
            averageAngularAcceleration /= overallTimePassed;

            double rangeX = maximumX - minimumX;
            double rangeZ = maximumZ - minimumZ;

            // adjust to subject height
            maximumHeight /= singleSensor.get(0).getScale();
            minimumHeight /= singleSensor.get(0).getScale();
            averageHeight /= singleSensor.get(0).getScale();
            rangeX /= singleSensor.get(0).getScale();
            rangeZ /= singleSensor.get(0).getScale();
            averageVelocityX /= singleSensor.get(0).getScale();
            averageVelocityHeight /= singleSensor.get(0).getScale();
            averageVelocityZ /= singleSensor.get(0).getScale();
            averageVelocityXZ /= singleSensor.get(0).getScale();
            averageVelocityXYZ /= singleSensor.get(0).getScale();
            averageAccelerationX /= singleSensor.get(0).getScale();
            averageAccelerationHeight /= singleSensor.get(0).getScale();
            averageAccelerationZ /= singleSensor.get(0).getScale();
            averageAccelerationXZ /= singleSensor.get(0).getScale();
            averageAccelerationXYZ /= singleSensor.get(0).getScale();


            // add features to data line
            outputFeatureVector.addFeature(Double.toString(maximumHeight));
            outputFeatureVector.addFeature(Double.toString(minimumHeight));
            outputFeatureVector.addFeature(Double.toString(averageHeight));
            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                outputFeatureVector.addFeature(Double.toString(rangeX));
                outputFeatureVector.addFeature(Double.toString(rangeZ));
                outputFeatureVector.addFeature(Double.toString(averageVelocityX));
                outputFeatureVector.addFeature(Double.toString(averageVelocityZ));
            }
            outputFeatureVector.addFeature(Double.toString(averageVelocityHeight));
            outputFeatureVector.addFeature(Double.toString(averageVelocityXZ));
            outputFeatureVector.addFeature(Double.toString(averageVelocityXYZ));
            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                outputFeatureVector.addFeature(Double.toString(averageAccelerationX));
                outputFeatureVector.addFeature(Double.toString(averageAccelerationZ));
            }
            outputFeatureVector.addFeature(Double.toString(averageAccelerationHeight));
            outputFeatureVector.addFeature(Double.toString(averageAccelerationXZ));
            outputFeatureVector.addFeature(Double.toString(averageAccelerationXYZ));
            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Angular)) {
                outputFeatureVector.addFeature(Double.toString(averageAngularVelocity));
                outputFeatureVector.addFeature(Double.toString(averageAngularAcceleration));
            }


            //  System.out.println("max          " + maximumHeight);
            //  System.out.println("avg          " + averageHeight);

        }

        if (includeClassForTraining) {
            outputFeatureVector.addFeature(dataSource.getActivity());
        }

        return outputFeatureVector;
    }


    public static ArrayList<String> getHeaderForSensorTypes(ArrayList<String> sensorTypes, boolean includeClassForTraining) {

        ArrayList<String> headerFields = new ArrayList<>();

        for (String sensorType : sensorTypes) {
            headerFields.add(sensorType + "_maximumHeight");
            headerFields.add(sensorType + "_minimumHeight");
            headerFields.add(sensorType + "_averageHeight");
            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                //TODO: - add rangeXZ and rangeXYZ, which is harder to compute but independent of orientation
                headerFields.add(sensorType + "_rangeX");
                headerFields.add(sensorType + "_rangeZ");
                headerFields.add(sensorType + "_averageVelocityX");
                headerFields.add(sensorType + "_averageVelocityZ");
            }
            headerFields.add(sensorType + "_averageVelocityHeight");
            headerFields.add(sensorType + "_averageVelocityXZ");
            headerFields.add(sensorType + "_averageVelocityXYZ");
            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                headerFields.add(sensorType + "_averageAccelerationX");
                headerFields.add(sensorType + "_averageAccelerationZ");
            }
            headerFields.add(sensorType + "_averageAccelerationHeight");
            headerFields.add(sensorType + "_averageAccelerationXZ");
            headerFields.add(sensorType + "_averageAccelerationXYZ");
            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Angular)) {
                headerFields.add(sensorType + "_averageAngularVelocity");
                headerFields.add(sensorType + "_averageAngularAcceleration");
            }

        }

        if (includeClassForTraining) {
            headerFields.add("activity");
        }

        return headerFields;
    }


    private static void writeOutputFeatureVectorToCSV(String
                                                              filePath, ArrayList<String> headerFields, ArrayList<OutputFeatureVector> featureVectors) {

        // sort output by class
        // The Weka Gui is unable to deal with unordered classes, which makes sanity checks difficult
        featureVectors.sort(new Comparator<OutputFeatureVector>() {
            @Override
            public int compare(OutputFeatureVector o1, OutputFeatureVector o2) {
                return o1.getClassValue().compareTo(o2.getClassValue());
            }
        });

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


































