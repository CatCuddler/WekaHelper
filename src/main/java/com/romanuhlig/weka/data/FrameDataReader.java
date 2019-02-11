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
        for (int i = 0; i < listOfInputFiles.length; i++) {

            System.out.println("reading input file:   " + (i + 1) + " / " + listOfInputFiles.length);

            File inputFile = listOfInputFiles[i];
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

        // free up memory
        originalFrameDataSets = null;

        // prepare header for output file
        // identify sensors in original data
        ArrayList<String> sensorTypes = windows.get(0).getAllSensorPositions();
        ArrayList<String> headerFields =
                getHeaderForSensorTypes(
                        sensorTypes, true,
                        !TestBenchSettings.useIndividualFeatureFilesForEachSubject());

        ArrayList<OutputFeatureVector> outputFeatureVectors = new ArrayList<>();

        // extract features
        // for each window

        int numberOfWindows = windows.size();

        for (int i = windows.size() - 1; i >= 0; i--) {

            if ((numberOfWindows - i) % 100 == 0) {
                System.out.println("calculated features for window:   " + (numberOfWindows - i) + " / " + numberOfWindows);
            }

            // shorten list as we go, to save on memory
            FrameDataSet singleWindow = windows.get(i);
            windows.remove(i);

            // create new data line
            OutputFeatureVector currentOutputFeatureVector = getFeaturesForFrameDataSet(singleWindow);
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

        // create training and test file for each subject individually
        if (TestBenchSettings.useIndividualFeatureFilesForEachSubject()) {

            System.out.println("writing features to individual files for each subject");

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
//                    String o1Class = o1.getFeaturesWithClassValue().get(o1.getFeaturesWithClassValue().size() - 1);
//                    String o2Class = o2.getFeaturesWithClassValue().get(o2.getFeaturesWithClassValue().size() - 1);
//                    return o1Class.compareTo(o2Class);
//                }
//            });
//
//            allButSubjectVectors.sort(new Comparator<OutputFeatureVector>() {
//                @Override
//                public int compare(OutputFeatureVector o1, OutputFeatureVector o2) {
//                    String o1Class = o1.getFeaturesWithClassValue().get(o1.getFeaturesWithClassValue().size() - 1);
//                    String o2Class = o2.getFeaturesWithClassValue().get(o2.getFeaturesWithClassValue().size() - 1);
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
        } else {
            // create dummy file packages if they are only needed as a reference to the subjects
            for (String subject : subjectNameList) {
                featureExtractionResults.addTrainingAndTestFilePackage(new TrainingAndTestFilePackage(subject));
            }
        }

        System.out.println("writing all features into a single file");

        // write all features in a single data file
        String completeFeatureSetFilePath = outputFeaturesFilePath + "/allDataInOne.csv";
        writeOutputFeatureVectorToCSV(completeFeatureSetFilePath, headerFields, outputFeatureVectors);
        featureExtractionResults.setCompleteFeatureSet(new TrainingAndTestFilePackage(completeFeatureSetFilePath, completeFeatureSetFilePath, "completeFeatureSet"));

        return featureExtractionResults;

    }


    public static ArrayList<String> getHeaderForFrameDataSet(FrameDataSet frameDataSet,
                                                             boolean includeClassForTraining,
                                                             boolean includeSubjectForTraining) {

        ArrayList<String> sensorTypes = frameDataSet.getAllSensorPositions();
        return getHeaderForSensorTypes(sensorTypes, includeClassForTraining, includeSubjectForTraining);
    }


    public static OutputFeatureVector getFeaturesForFrameDataSet(FrameDataSet dataSource) {

        // create new data line
        OutputFeatureVector outputFeatureVector = new OutputFeatureVector(dataSource.getSubject(), dataSource.getActivity());

        ArrayList<List<FrameData>> frameDataSet = dataSource.getAllSensorLists();

        for (List<FrameData> singleSensor : frameDataSet) {

            // leave out blocked sensors
            if (TestBenchSettings.isSensorBlocked(singleSensor.get(0).getSensorPosition())) {
                continue;
            }

            // values that stay true for the whole window
            double overallTimePassed =
                    singleSensor.get(singleSensor.size() - 1).getTime()
                            - singleSensor.get(0).getTime();
            double bodySize = singleSensor.get(0).getScale();

            // calculate features for sensor
            // logic says that average velocity and height / range will be more affected by body size than acceleration,
            // testing shows that this corresponds to the "best" setting for detection
            SortingValueCollector Position_Height = new SortingValueCollector(true, true, overallTimePassed, bodySize);
            SortingValueCollector Position_X = new SortingValueCollector(true, true, overallTimePassed, bodySize);
            SortingValueCollector Position_Z = new SortingValueCollector(true, true, overallTimePassed, bodySize);
            SortingValueCollector Velocity_X = new SortingValueCollector(true, true, overallTimePassed, bodySize);
            SortingValueCollector Velocity_Z = new SortingValueCollector(true, true, overallTimePassed, bodySize);
            SortingValueCollector Velocity_Height = new SortingValueCollector(true, true, overallTimePassed, bodySize);
            SortingValueCollector Velocity_XZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
            SortingValueCollector Velocity_XYZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
            SortingValueCollector Acceleration_X = new SortingValueCollector(true, false, overallTimePassed, bodySize);
            SortingValueCollector Acceleration_Z = new SortingValueCollector(true, false, overallTimePassed, bodySize);
            SortingValueCollector Acceleration_Height = new SortingValueCollector(true, false, overallTimePassed, bodySize);
            SortingValueCollector Acceleration_XZ = new SortingValueCollector(true, false, overallTimePassed, bodySize);
            SortingValueCollector Acceleration_XYZ = new SortingValueCollector(true, false, overallTimePassed, bodySize);
            SortingValueCollector AngularVelocity = new SortingValueCollector(true, false, overallTimePassed, bodySize);
            SortingValueCollector AngularAcceleration = new SortingValueCollector(true, false, overallTimePassed, bodySize);


            for (int i = 0; i < singleSensor.size(); i++) {

                FrameData frameData = singleSensor.get(i);

                double timeSinceLastFrame;
                if (i > 0) {
                    timeSinceLastFrame = frameData.getTime() - singleSensor.get(i - 1).getTime();
                } else {
                    timeSinceLastFrame = 0;
                }

                Velocity_X.addValue(Math.abs(frameData.getLinVelX()), timeSinceLastFrame);
                Velocity_Z.addValue(Math.abs(frameData.getLinVelZ()), timeSinceLastFrame);
                Velocity_Height.addValue(Math.abs(frameData.getLinVelY()), timeSinceLastFrame);


                Velocity_XZ.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getLinVelX(),
                                frameData.getLinVelZ()),
                        timeSinceLastFrame);
                Velocity_XYZ.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getLinVelX(),
                                frameData.getLinVelY(),
                                frameData.getLinVelZ()),
                        timeSinceLastFrame);


                Acceleration_X.addValue(Math.abs(frameData.getLinAccelerationX()), timeSinceLastFrame);
                Acceleration_Height.addValue(Math.abs(frameData.getLinAccelerationY()), timeSinceLastFrame);
                Acceleration_Z.addValue(Math.abs(frameData.getLinAccelerationZ()), timeSinceLastFrame);


                Acceleration_XZ.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getLinAccelerationX(),
                                frameData.getLinAccelerationZ()),
                        timeSinceLastFrame);
                Acceleration_XYZ.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getLinAccelerationX(),
                                frameData.getLinAccelerationY(),
                                frameData.getLinAccelerationZ()),
                        timeSinceLastFrame);


                AngularVelocity.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getAngVelX(),
                                frameData.getAngVelY(),
                                frameData.getAngVelZ()),
                        timeSinceLastFrame);
                AngularAcceleration.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getAngAccelerationX(),
                                frameData.getAngAccelerationY(),
                                frameData.getAngAccelerationZ()),
                        timeSinceLastFrame);


                Position_X.addValue(frameData.getCalPosX(), timeSinceLastFrame);
                Position_Z.addValue(frameData.getCalPosZ(), timeSinceLastFrame);
                Position_Height.addValue(frameData.getCalPosY(), timeSinceLastFrame);
            }

            // output the calculated values
            outputFeatureVector.addFeature(Position_Height.getMax());
            outputFeatureVector.addFeature(Position_Height.getMin());
            outputFeatureVector.addFeature(Position_Height.getRange());

            addStandardFeatures(outputFeatureVector, Position_Height);

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                //TODO: - add rangeXZ and rangeXYZ, which is harder to compute but independent of orientation
                outputFeatureVector.addFeature(Position_X.getRange());
                outputFeatureVector.addFeature(Position_Z.getRange());

                addStandardFeatures(outputFeatureVector, Velocity_X);
                addStandardFeatures(outputFeatureVector, Velocity_Z);
            }
            addStandardFeatures(outputFeatureVector, Velocity_Height);
            addStandardFeatures(outputFeatureVector, Velocity_XZ);
            addStandardFeatures(outputFeatureVector, Velocity_XYZ);

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                addStandardFeatures(outputFeatureVector, Acceleration_X);
                addStandardFeatures(outputFeatureVector, Acceleration_Z);
            }
            addStandardFeatures(outputFeatureVector, Acceleration_Height);
            addStandardFeatures(outputFeatureVector, Acceleration_XZ);
            addStandardFeatures(outputFeatureVector, Acceleration_XYZ);

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Angular)) {
                addStandardFeatures(outputFeatureVector, AngularVelocity);
                addStandardFeatures(outputFeatureVector, AngularAcceleration);
            }

        }


        // collect features that depend on the relationship between two sensors
        if (TestBenchSettings.featureTagsAllowed(FeatureTag.DualSensorCombination)) {
            for (int ssA = 0; ssA < frameDataSet.size(); ssA++) {
                List<FrameData> singleSensorA = frameDataSet.get(ssA);

                // values that stay true for the whole window
                double overallTimePassed =
                        singleSensorA.get(singleSensorA.size() - 1).getTime()
                                - singleSensorA.get(0).getTime();
                double bodySize = singleSensorA.get(0).getScale();


                for (int ssB = ssA + 1; ssB < frameDataSet.size(); ssB++) {
                    List<FrameData> singleSensorB = frameDataSet.get(ssB);

                    SortingValueCollector averageDistanceX = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceChangeX = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceChangeZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceHeight = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceChangeHeight = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceXZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceChangeXZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceXYZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector averageDistanceChangeXYZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);

                    double averageDistanceLastFrameX = 0;
                    double averageDistanceLastFrameZ = 0;
                    double averageDistanceLastFrameHeight = 0;
                    double averageDistanceLastFrameXZ = 0;
                    double averageDistanceLastFrameXYZ = 0;

                    for (int i = 0; i < singleSensorA.size(); i++) {

                        // prepare current frame
                        FrameData frameDataA = singleSensorA.get(i);
                        FrameData frameDataB = singleSensorB.get(i);

                        double averageDistanceCurrentFrameX =
                                MathHelper.distance(
                                        frameDataA.getCalPosX(),
                                        frameDataB.getCalPosX());
                        double averageDistanceCurrentFrameZ =
                                MathHelper.distance(
                                        frameDataA.getCalPosZ(),
                                        frameDataB.getCalPosZ());
                        double averageDistanceCurrentFrameHeight =
                                MathHelper.distance(
                                        frameDataA.getCalPosY(),
                                        frameDataB.getCalPosY());
                        double averageDistanceCurrentFrameXZ =
                                MathHelper.distance(
                                        frameDataA.getCalPosX(),
                                        frameDataA.getCalPosZ(),
                                        frameDataB.getCalPosX(),
                                        frameDataB.getCalPosZ());
                        double averageDistanceCurrentFrameXYZ =
                                MathHelper.distance(
                                        frameDataA.getCalPosX(),
                                        frameDataA.getCalPosY(),
                                        frameDataA.getCalPosZ(),
                                        frameDataB.getCalPosX(),
                                        frameDataB.getCalPosY(),
                                        frameDataB.getCalPosZ());

                        // calculate current frame
                        if (i > 0) {
                            double timeSinceLastFrame = frameDataA.getTime() - singleSensorA.get(i - 1).getTime();

                            averageDistanceX.addValue(averageDistanceCurrentFrameX, timeSinceLastFrame);
                            averageDistanceChangeX.addValue(Math.abs(averageDistanceCurrentFrameX - averageDistanceLastFrameX), timeSinceLastFrame);
                            averageDistanceZ.addValue(averageDistanceCurrentFrameZ, timeSinceLastFrame);
                            averageDistanceChangeZ.addValue(Math.abs(averageDistanceCurrentFrameZ - averageDistanceLastFrameZ), timeSinceLastFrame);
                            averageDistanceHeight.addValue(averageDistanceCurrentFrameHeight, timeSinceLastFrame);
                            averageDistanceChangeHeight.addValue(Math.abs(averageDistanceCurrentFrameHeight - averageDistanceLastFrameHeight), timeSinceLastFrame);
                            averageDistanceXZ.addValue(averageDistanceCurrentFrameXZ, timeSinceLastFrame);
                            averageDistanceChangeXZ.addValue(Math.abs(averageDistanceCurrentFrameXZ - averageDistanceLastFrameXZ), timeSinceLastFrame);
                            averageDistanceXYZ.addValue(averageDistanceCurrentFrameXYZ, timeSinceLastFrame);
                            averageDistanceChangeXYZ.addValue(Math.abs(averageDistanceCurrentFrameXYZ - averageDistanceLastFrameXYZ), timeSinceLastFrame);
                        }

                        // prepare next frame
                        averageDistanceLastFrameX = averageDistanceCurrentFrameX;
                        averageDistanceLastFrameZ = averageDistanceCurrentFrameZ;
                        averageDistanceLastFrameHeight = averageDistanceCurrentFrameHeight;
                        averageDistanceLastFrameXZ = averageDistanceCurrentFrameXZ;
                        averageDistanceLastFrameXYZ = averageDistanceCurrentFrameXYZ;
                    }

                    addStandardFeatures(outputFeatureVector, averageDistanceX);
                    addStandardFeatures(outputFeatureVector, averageDistanceChangeX);
                    addStandardFeatures(outputFeatureVector, averageDistanceZ);
                    addStandardFeatures(outputFeatureVector, averageDistanceChangeZ);
                    addStandardFeatures(outputFeatureVector, averageDistanceHeight);
                    addStandardFeatures(outputFeatureVector, averageDistanceChangeHeight);
                    addStandardFeatures(outputFeatureVector, averageDistanceXZ);
                    addStandardFeatures(outputFeatureVector, averageDistanceChangeXZ);
                    addStandardFeatures(outputFeatureVector, averageDistanceXYZ);
                    addStandardFeatures(outputFeatureVector, averageDistanceChangeXYZ);

                }
            }
        }
        return outputFeatureVector;
    }


    public static ArrayList<String> getHeaderForSensorTypes(ArrayList<String> sensorTypes,
                                                            boolean includeClassForTraining,
                                                            boolean includeSubjectForTraining) {

        ArrayList<String> headerFields = new ArrayList<>();


        for (String sensorType : sensorTypes) {

            // leave out blocked sensors
            if (TestBenchSettings.isSensorBlocked(sensorType)) {
                continue;
            }

            headerFields.add(sensorType + "_maximum_Height");
            headerFields.add(sensorType + "_minimum_Height");
            headerFields.add(sensorType + "_range_Height");

            addStandardFeatures(headerFields, sensorType, "Height");

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                //TODO: - add rangeXZ and rangeXYZ, which is harder to compute but independent of orientation
                headerFields.add(sensorType + "_range_X");
                headerFields.add(sensorType + "_range_Z");

                addStandardFeatures(headerFields, sensorType, "Velocity_X");
                addStandardFeatures(headerFields, sensorType, "Velocity_Z");
            }
            addStandardFeatures(headerFields, sensorType, "Velocity_Height");
            addStandardFeatures(headerFields, sensorType, "Velocity_XZ");
            addStandardFeatures(headerFields, sensorType, "Velocity_XYZ");

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                addStandardFeatures(headerFields, sensorType, "Acceleration_X");
                addStandardFeatures(headerFields, sensorType, "Acceleration_Z");
            }
            addStandardFeatures(headerFields, sensorType, "Acceleration_Height");
            addStandardFeatures(headerFields, sensorType, "Acceleration_XZ");
            addStandardFeatures(headerFields, sensorType, "Acceleration_XYZ");

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Angular)) {
                addStandardFeatures(headerFields, sensorType, "Velocity_Angular");
                addStandardFeatures(headerFields, sensorType, "Acceleration_Angular");
            }

        }

        // collect features that depend on the relationship between two sensors
        if (TestBenchSettings.featureTagsAllowed(FeatureTag.DualSensorCombination)) {
            for (int ssA = 0; ssA < sensorTypes.size(); ssA++) {
                String singleSensorA = sensorTypes.get(ssA);
                for (int ssB = ssA + 1; ssB < sensorTypes.size(); ssB++) {
                    String singleSensorB = sensorTypes.get(ssB);

                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_X");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_X");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_Z");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_Z");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_Height");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_Height");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_XZ");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_XZ");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_XYZ");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_XYZ");

                }
            }
        }


        if (includeSubjectForTraining) {
            headerFields.add("subject");
        }
        if (includeClassForTraining) {
            headerFields.add("activity");
        }


        return headerFields;
    }

    private static void addStandardFeatures(OutputFeatureVector featureVector, SortingValueCollector valueCollector) {
        featureVector.addFeature(valueCollector.getAverage());
        featureVector.addFeature(valueCollector.getRootMeanSquare());
        featureVector.addFeature(valueCollector.getStandardDeviation());
        featureVector.addFeature(valueCollector.getVariance());
        featureVector.addFeature(valueCollector.getMeanAbsoluteDeviation());
        featureVector.addFeature(valueCollector.getInterquartileRange());

        for (int i = 25; i < 100; i += 25) {
            featureVector.addFeature(valueCollector.getPercentile(i / 100d));
        }


    }

    private static void addStandardFeatures(ArrayList<String> headerFields, String sensor, String attribute) {
        headerFields.add(sensor + "_average_" + attribute);
        headerFields.add(sensor + "_rootMeanSquare_" + attribute);
        headerFields.add(sensor + "_standardDeviation_" + attribute);
        headerFields.add(sensor + "_variance_" + attribute);
        headerFields.add(sensor + "_meanAbsoluteDeviation_" + attribute);
        headerFields.add(sensor + "_interquartileRange_" + attribute);

        for (int i = 25; i < 100; i += 25) {
            headerFields.add(sensor + "_percentile" + i + "_" + attribute);
        }
    }


    private static void writeOutputFeatureVectorToCSV(
            String filePath, ArrayList<String> headerFields, ArrayList<OutputFeatureVector> featureVectors) {

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
                csvWriter.writeNext(outputFeatureVector.getFeaturesAndClassAsArray());
            }

        } catch (Exception e) {
            System.err.println("unable to write file " + featureVectors);
        }
    }


}


































