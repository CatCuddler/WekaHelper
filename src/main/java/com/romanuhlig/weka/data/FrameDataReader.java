package com.romanuhlig.weka.data;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.romanuhlig.weka.ConvexHull.ConvexHull;
import com.romanuhlig.weka.ConvexHull.ConvexHullPoint;
import com.romanuhlig.weka.controller.TestBenchSettings;
import com.romanuhlig.weka.io.FeatureExtractionResults;
import com.romanuhlig.weka.io.TrainingAndTestFilePackage;
import com.romanuhlig.weka.math.MathHelper;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.romanuhlig.weka.controller.TestBenchSettings.FeatureTag;
import com.romanuhlig.weka.quickhull3d.Point3d;
import com.romanuhlig.weka.quickhull3d.QuickHull3D;


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
            ArrayList<FrameDataSet> dataSetWindows = frameDataSet.separateFrameDataIntoValidWindows(windowSize, timeBetweenWindows);
            windows.addAll(dataSetWindows);
        }

        return windows;
    }

//    public static FeatureExtractionResults readExistingFeatureSet(String existingFeaturesInputFolder) {
//        File inputFolder = new File(existingFeaturesInputFolder);
//        File[] listOfInputFiles = inputFolder.listFiles();
//        String filepath = listOfInputFiles[0].getPath();
//
//        TrainingAndTestFilePackage filePackage = new TrainingAndTestFilePackage(filepath, filepath, "");
//
//        ArrayList<String> sensors = TestBenchSettings.getSensorsInExistingFeatureFile();
//        Collections.sort(sensors);
//        FeatureExtractionResults results = new FeatureExtractionResults(sensors);
//
//        for (String sensor : sensors) {
//            results.addTrainingAndTestFilePackage(new TrainingAndTestFilePackage());
//        }
//
//
//    }

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

        ArrayList<List<FrameData>> allSensorLists = dataSource.getAllSensorLists();


//        // collect data that can be reused for dual sensor combinations
//        double[] rangeXforSensor = new double[allSensorLists.size()];
//        double[] rangeZforSensor = new double[allSensorLists.size()];
//        double[] rangeHeightforSensor = new double[allSensorLists.size()];
//        double[] rangeXZforSensor = new double[allSensorLists.size()];
//        double[] rangeXYZforSensor = new double[allSensorLists.size()];


        // collect features that are calculated from the data of a single sensor
        for (int sensorID = 0; sensorID < allSensorLists.size(); sensorID++) {

            List<FrameData> singleSensorList = allSensorLists.get(sensorID);

            // leave out blocked sensors
            if (TestBenchSettings.isSensorBlocked(singleSensorList.get(0).getSensorPosition())) {
                continue;
            }

            // values that stay true for the whole window
            double overallTimePassed =
                    singleSensorList.get(singleSensorList.size() - 1).getTime()
                            - singleSensorList.get(0).getTime();
            double bodySize = singleSensorList.get(0).getScale();

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

            // collect points in order to calculate maximum range in different dimensions
            ArrayList<ConvexHullPoint> rangePointsXZ = new ArrayList<>(singleSensorList.size());
            Point3d[] rangePointsXYZ = new Point3d[singleSensorList.size()];


            for (int i = 0; i < singleSensorList.size(); i++) {

                FrameData frameData = singleSensorList.get(i);

                // collect points in order to calculate maximum range in different dimensions
                rangePointsXZ.add(new ConvexHullPoint(frameData.getCalPosX(), frameData.getCalPosZ()));
                rangePointsXYZ[i] = new Point3d(frameData.getCalPosX(), frameData.getCalPosY(), frameData.getCalPosZ());

                // collect other values over time for future analysis
                double timeSinceLastFrame = frameData.getFrameDuration();

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


            // adjust individual values that do not use the collector class
            List<ConvexHullPoint> rangeXZouterPoints = ConvexHull.makeHull(rangePointsXZ);
            double rangeXZ = 0;
            for (int a = 0; a < rangeXZouterPoints.size(); a++) {
                ConvexHullPoint pointA = rangeXZouterPoints.get(a);
                for (int b = a + 1; b < rangeXZouterPoints.size(); b++) {
                    ConvexHullPoint pointB = rangeXZouterPoints.get(b);
                    double distance = MathHelper.distance(
                            pointA.x, pointA.y,
                            pointB.x, pointB.y);
                    if (distance > rangeXZ) {
                        rangeXZ = distance;
                    }
                }
            }
            rangeXZ /= bodySize;

            double rangeXYZ = 0;
            try {
                QuickHull3D hull = new QuickHull3D(rangePointsXYZ);
                Point3d[] rangeXYZouterPoints = hull.getVertices();
                for (int a = 0; a < rangeXYZouterPoints.length; a++) {
                    Point3d pointA = rangeXYZouterPoints[a];
                    for (int b = a + 1; b < rangeXYZouterPoints.length; b++) {
                        Point3d pointB = rangeXYZouterPoints[b];
                        double distance = MathHelper.distance(
                                pointA.x, pointA.y, pointA.z,
                                pointB.x, pointB.y, pointB.z);
                        if (distance > rangeXYZ) {
                            rangeXYZ = distance;
                        }
                    }
                }
            } catch (Exception e) {
                // if there is an exception, it is because the points were to similar
                // if this is the case, we can just keep the maximum distance value at 0
            }
            rangeXYZ /= bodySize;


            // output the calculated values
            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Position)) {
                addStandardFeatures(outputFeatureVector, Position_Height);
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
//                outputFeatureVector.addFeature(Position_X.sort_getRange());
//                outputFeatureVector.addFeature(Position_Z.sort_getRange());
                if (TestBenchSettings.featureTagsAllowed(FeatureTag.Position)) {
                    Position_X.adjustToLowestValueAsZero();
                    Position_Z.adjustToLowestValueAsZero();
                    addStandardFeatures(outputFeatureVector, Position_X, false, false);
                    addStandardFeatures(outputFeatureVector, Position_Z, false, false);
                }

                if (TestBenchSettings.featureTagsAllowed(FeatureTag.Velocity)) {
                    addStandardFeatures(outputFeatureVector, Velocity_X);
                    addStandardFeatures(outputFeatureVector, Velocity_Z);
                }
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Position)) {
                outputFeatureVector.addFeature(rangeXZ);
                outputFeatureVector.addFeature(rangeXYZ);
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Velocity)) {
                addStandardFeatures(outputFeatureVector, Velocity_Height);
                addStandardFeatures(outputFeatureVector, Velocity_XZ);
                addStandardFeatures(outputFeatureVector, Velocity_XYZ);
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                if (TestBenchSettings.featureTagsAllowed(FeatureTag.Acceleration)) {
                    addStandardFeatures(outputFeatureVector, Acceleration_X);
                    addStandardFeatures(outputFeatureVector, Acceleration_Z);
                }
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Acceleration)) {
                addStandardFeatures(outputFeatureVector, Acceleration_Height);
                addStandardFeatures(outputFeatureVector, Acceleration_XZ);
                addStandardFeatures(outputFeatureVector, Acceleration_XYZ);
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Angular)) {
                addStandardFeatures(outputFeatureVector, AngularVelocity);
                addStandardFeatures(outputFeatureVector, AngularAcceleration);
            }

////             collect data for dual sensor combination features
//            rangeXforSensor[sensorID] = Position_X.sort_getRange();
//            rangeZforSensor[sensorID] = Position_Z.sort_getRange();
//            rangeHeightforSensor[sensorID] = Position_Height.sort_getRange();
//            rangeXZforSensor[sensorID] = rangeXZ;
//            rangeXYZforSensor[sensorID] = rangeXYZ;


        }


        // collect features that depend on the relationship between two sensors
        if (TestBenchSettings.featureTagsAllowed(FeatureTag.DualSensorCombination)) {
            for (int ssA = 0; ssA < allSensorLists.size(); ssA++) {
                List<FrameData> singleSensorA = allSensorLists.get(ssA);

                if (TestBenchSettings.isSensorBlocked(singleSensorA.get(0).getSensorPosition())) {
                    continue;
                }

                // values that stay true for the whole window
                double overallTimePassed =
                        singleSensorA.get(singleSensorA.size() - 1).getTime()
                                - singleSensorA.get(0).getTime();
                double bodySize = singleSensorA.get(0).getScale();


                for (int ssB = ssA + 1; ssB < allSensorLists.size(); ssB++) {
                    List<FrameData> singleSensorB = allSensorLists.get(ssB);

                    if (TestBenchSettings.isSensorBlocked(singleSensorB.get(0).getSensorPosition())) {
                        continue;
                    }

                    SortingValueCollector distanceX = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector distanceZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector distanceHeight = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector distanceXZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector distanceXYZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);

//                    SortingValueCollector distanceChangeX = new SortingValueCollector(true, true, overallTimePassed, bodySize);
//                    SortingValueCollector distanceChangeZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
//                    SortingValueCollector distanceChangeHeight = new SortingValueCollector(true, true, overallTimePassed, bodySize);
//                    SortingValueCollector distanceChangeXZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
//                    SortingValueCollector distanceChangeXYZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);

                    SortingValueCollector differenceVelocityX = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector differenceVelocityZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector differenceVelocityHeight = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector differenceVelocityXZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);
                    SortingValueCollector differenceVelocityXYZ = new SortingValueCollector(true, true, overallTimePassed, bodySize);

//                    SortingValueCollector differenceAngularVelocityXYZ = new SortingValueCollector(true, false, overallTimePassed, bodySize);


//                    SortingValueCollector differenceAccelerationX = new SortingValueCollector(true, false, overallTimePassed, bodySize);
//                    SortingValueCollector differenceAccelerationZ = new SortingValueCollector(true, false, overallTimePassed, bodySize);
//                    SortingValueCollector differenceAccelerationHeight = new SortingValueCollector(true, false, overallTimePassed, bodySize);
//                    SortingValueCollector differenceAccelerationXZ = new SortingValueCollector(true, false, overallTimePassed, bodySize);
//                    SortingValueCollector differenceAccelerationXYZ = new SortingValueCollector(true, false, overallTimePassed, bodySize);

//                    double averageDistanceLastFrameX = 0;
//                    double averageDistanceLastFrameZ = 0;
//                    double averageDistanceLastFrameHeight = 0;
//                    double averageDistanceLastFrameXZ = 0;
//                    double averageDistanceLastFrameXYZ = 0;

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
                        double timeSinceLastFrame = frameDataA.getFrameDuration();


                        distanceX.addValue(averageDistanceCurrentFrameX, timeSinceLastFrame);
                        distanceZ.addValue(averageDistanceCurrentFrameZ, timeSinceLastFrame);
                        distanceHeight.addValue(averageDistanceCurrentFrameHeight, timeSinceLastFrame);
                        distanceXZ.addValue(averageDistanceCurrentFrameXZ, timeSinceLastFrame);
                        distanceXYZ.addValue(averageDistanceCurrentFrameXYZ, timeSinceLastFrame);

//                            distanceChangeX.addValue(Math.abs(averageDistanceCurrentFrameX - averageDistanceLastFrameX), timeSinceLastFrame);
//                            distanceChangeZ.addValue(Math.abs(averageDistanceCurrentFrameZ - averageDistanceLastFrameZ), timeSinceLastFrame);
//                            distanceChangeHeight.addValue(Math.abs(averageDistanceCurrentFrameHeight - averageDistanceLastFrameHeight), timeSinceLastFrame);
//                            distanceChangeXZ.addValue(Math.abs(averageDistanceCurrentFrameXZ - averageDistanceLastFrameXZ), timeSinceLastFrame);
//                            distanceChangeXYZ.addValue(Math.abs(averageDistanceCurrentFrameXYZ - averageDistanceLastFrameXYZ), timeSinceLastFrame);

                        differenceVelocityX.addValue(Math.abs(frameDataA.getLinVelX() - frameDataB.getLinVelX()), timeSinceLastFrame);
                        differenceVelocityZ.addValue(Math.abs(frameDataA.getLinVelZ() - frameDataB.getLinVelZ()), timeSinceLastFrame);
                        differenceVelocityHeight.addValue(Math.abs(frameDataA.getLinVelY() - frameDataB.getLinVelY()), timeSinceLastFrame);
                        double velocityXZa = MathHelper.EuclideanNorm(
                                frameDataA.getLinVelX(),
                                frameDataA.getLinVelZ());
                        double velocityXZb = MathHelper.EuclideanNorm(
                                frameDataB.getLinVelX(),
                                frameDataB.getLinVelZ());
                        double velocityXYZa = MathHelper.EuclideanNorm(
                                frameDataA.getLinVelX(),
                                frameDataA.getLinVelY(),
                                frameDataA.getLinVelZ());
                        double velocityXYZb = MathHelper.EuclideanNorm(
                                frameDataB.getLinVelX(),
                                frameDataB.getLinVelY(),
                                frameDataB.getLinVelZ());
                        differenceVelocityXZ.addValue(Math.abs(velocityXZa - velocityXZb), timeSinceLastFrame);
                        differenceVelocityXYZ.addValue(Math.abs(velocityXYZa - velocityXYZb), timeSinceLastFrame);


//                            differenceAccelerationX.addValue(Math.abs(frameDataA.getLinAccelerationX() - frameDataB.getLinAccelerationX()), timeSinceLastFrame);
//                            differenceAccelerationZ.addValue(Math.abs(frameDataA.getLinAccelerationZ() - frameDataB.getLinAccelerationZ()), timeSinceLastFrame);
//                            differenceAccelerationHeight.addValue(Math.abs(frameDataA.getLinAccelerationY() - frameDataB.getLinAccelerationY()), timeSinceLastFrame);
//                            double AccelerationXZa = MathHelper.EuclideanNorm(
//                                    frameDataA.getLinAccelerationX(),
//                                    frameDataA.getLinAccelerationZ());
//                            double AccelerationXZb = MathHelper.EuclideanNorm(
//                                    frameDataB.getLinAccelerationX(),
//                                    frameDataB.getLinAccelerationZ());
//                            double AccelerationXYZa = MathHelper.EuclideanNorm(
//                                    frameDataA.getLinAccelerationX(),
//                                    frameDataA.getLinAccelerationY(),
//                                    frameDataA.getLinAccelerationZ());
//                            double AccelerationXYZb = MathHelper.EuclideanNorm(
//                                    frameDataB.getLinAccelerationX(),
//                                    frameDataB.getLinAccelerationY(),
//                                    frameDataB.getLinAccelerationZ());
//                            differenceAccelerationXZ.addValue(Math.abs(AccelerationXZa - AccelerationXZb), timeSinceLastFrame);
//                            differenceAccelerationXYZ.addValue(Math.abs(AccelerationXYZa - AccelerationXYZb), timeSinceLastFrame);

//                            double AngularVelocityXYZa =
//                                    MathHelper.EuclideanNorm(
//                                            frameDataA.getAngVelX(),
//                                            frameDataA.getAngVelY(),
//                                            frameDataA.getAngVelZ());
//                            double AngularVelocityXYZb =
//                                    MathHelper.EuclideanNorm(
//                                            frameDataB.getAngVelX(),
//                                            frameDataB.getAngVelY(),
//                                            frameDataB.getAngVelZ());
//                            differenceAngularVelocityXYZ.addValue((Math.abs(AngularVelocityXYZa - AngularVelocityXYZb)), timeSinceLastFrame);


////                         prepare next frame
//                        averageDistanceLastFrameX = averageDistanceCurrentFrameX;
//                        averageDistanceLastFrameZ = averageDistanceCurrentFrameZ;
//                        averageDistanceLastFrameHeight = averageDistanceCurrentFrameHeight;
//                        averageDistanceLastFrameXZ = averageDistanceCurrentFrameXZ;
//                        averageDistanceLastFrameXYZ = averageDistanceCurrentFrameXYZ;
                    }

                    if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                        addStandardFeatures(outputFeatureVector, distanceX);
                        addStandardFeatures(outputFeatureVector, distanceZ);
                    }
                    addStandardFeatures(outputFeatureVector, distanceHeight);
                    addStandardFeatures(outputFeatureVector, distanceXZ);
                    addStandardFeatures(outputFeatureVector, distanceXYZ);

//                    if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
//                    addStandardFeatures(outputFeatureVector, distanceChangeX);
//                    addStandardFeatures(outputFeatureVector, distanceChangeZ);
//                    }
//                    addStandardFeatures(outputFeatureVector, distanceChangeHeight);
//                    addStandardFeatures(outputFeatureVector, distanceChangeXZ);
//                    addStandardFeatures(outputFeatureVector, distanceChangeXYZ);

                    if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                        addStandardFeatures(outputFeatureVector, differenceVelocityX);
                        addStandardFeatures(outputFeatureVector, differenceVelocityZ);
                    }
                    addStandardFeatures(outputFeatureVector, differenceVelocityHeight);
                    addStandardFeatures(outputFeatureVector, differenceVelocityXZ);
                    addStandardFeatures(outputFeatureVector, differenceVelocityXYZ);

//                    if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
//                    addStandardFeatures(outputFeatureVector, differenceAccelerationX);
//                    addStandardFeatures(outputFeatureVector, differenceAccelerationZ);
//                    }
//                    addStandardFeatures(outputFeatureVector, differenceAccelerationHeight);
//                    addStandardFeatures(outputFeatureVector, differenceAccelerationXZ);
//                    addStandardFeatures(outputFeatureVector, differenceAccelerationXYZ);

//                    addStandardFeatures(outputFeatureVector, differenceAngularVelocityXYZ);


//                    outputFeatureVector.addFeature(Math.abs(rangeXforSensor[ssA] - rangeXforSensor[ssB]));
//                    outputFeatureVector.addFeature(Math.abs(rangeZforSensor[ssA] - rangeZforSensor[ssB]));
//                    outputFeatureVector.addFeature(Math.abs(rangeHeightforSensor[ssA] - rangeHeightforSensor[ssB]));
//                    outputFeatureVector.addFeature(Math.abs(rangeXZforSensor[ssA] - rangeXZforSensor[ssB]));
//                    outputFeatureVector.addFeature(Math.abs(rangeXYZforSensor[ssA] - rangeXYZforSensor[ssB]));


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

//            headerFields.add(sensorType + "_maximum_Height");
//            headerFields.add(sensorType + "_minimum_Height");
//            headerFields.add(sensorType + "_range_Height");

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Position)) {
                addStandardFeatures(headerFields, sensorType, "Position_Height");
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
//                headerFields.add(sensorType + "_range_X");
//                headerFields.add(sensorType + "_range_Z");
                if (TestBenchSettings.featureTagsAllowed(FeatureTag.Position)) {
                    addStandardFeatures(headerFields, sensorType, "Position_X", false, false);
                    addStandardFeatures(headerFields, sensorType, "Position_Z", false, false);
                }

                if (TestBenchSettings.featureTagsAllowed(FeatureTag.Velocity)) {
                    addStandardFeatures(headerFields, sensorType, "Velocity_X");
                    addStandardFeatures(headerFields, sensorType, "Velocity_Z");
                }
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Position)) {
                headerFields.add(sensorType + "_range_XZ");
                headerFields.add(sensorType + "_range_XYZ");
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Velocity)) {
                addStandardFeatures(headerFields, sensorType, "Velocity_Height");
                addStandardFeatures(headerFields, sensorType, "Velocity_XZ");
                addStandardFeatures(headerFields, sensorType, "Velocity_XYZ");
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                if (TestBenchSettings.featureTagsAllowed(FeatureTag.Acceleration)) {
                    addStandardFeatures(headerFields, sensorType, "Acceleration_X");
                    addStandardFeatures(headerFields, sensorType, "Acceleration_Z");
                }
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Acceleration)) {
                addStandardFeatures(headerFields, sensorType, "Acceleration_Height");
                addStandardFeatures(headerFields, sensorType, "Acceleration_XZ");
                addStandardFeatures(headerFields, sensorType, "Acceleration_XYZ");
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureTag.Angular)) {
                addStandardFeatures(headerFields, sensorType, "Velocity_Angular");
                addStandardFeatures(headerFields, sensorType, "Acceleration_Angular");
            }

        }

        // collect features that depend on the relationship between two sensors
        if (TestBenchSettings.featureTagsAllowed(FeatureTag.DualSensorCombination)) {
            for (int ssA = 0; ssA < sensorTypes.size(); ssA++) {
                String singleSensorA = sensorTypes.get(ssA);

                if (TestBenchSettings.isSensorBlocked(singleSensorA)) {
                    continue;
                }

                for (int ssB = ssA + 1; ssB < sensorTypes.size(); ssB++) {
                    String singleSensorB = sensorTypes.get(ssB);

                    if (TestBenchSettings.isSensorBlocked(singleSensorB)) {
                        continue;
                    }

                    if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                        addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_X");
                        addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_Z");
                    }
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_Height");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_XZ");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_XYZ");

//                    if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_X");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_Z");
//                    }
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_Height");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_XZ");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_XYZ");

                    if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
                        addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_X");
                        addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_Z");
                    }
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_Height");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_XZ");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_XYZ");

//                    if (TestBenchSettings.featureTagsAllowed(FeatureTag.SubjectOrientationRelevant)) {
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceAcceleration_X");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceAcceleration_Z");
//                    }
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceAcceleration_Height");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceAcceleration_XZ");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceAcceleration_XYZ");

//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceAngularVelocity_XYZ");

//                    headerFields.add(singleSensorA + "_" + singleSensorB + "_rangeDifference_X");
//                    headerFields.add(singleSensorA + "_" + singleSensorB + "_rangeDifference_Z");
//                    headerFields.add(singleSensorA + "_" + singleSensorB + "_rangeDifference_Height");
//                    headerFields.add(singleSensorA + "_" + singleSensorB + "_rangeDifference_XZ");
//                    headerFields.add(singleSensorA + "_" + singleSensorB + "_rangeDifference_XYZ");


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

    private static void addStandardFeatures(
            OutputFeatureVector featureVector, SortingValueCollector valueCollector,
            boolean includeMin, boolean includeMax) {
        featureVector.addFeature(valueCollector.getAverage());
        featureVector.addFeature(valueCollector.getRootMeanSquare());
        featureVector.addFeature(valueCollector.getStandardDeviation());
        featureVector.addFeature(valueCollector.getVariance());
        featureVector.addFeature(valueCollector.getMeanAbsoluteDeviation());
        featureVector.addFeature(valueCollector.sort_getInterquartileRange());
        if (includeMax) {
            featureVector.addFeature(valueCollector.sort_getMax());
        }
        if (includeMin) {
            featureVector.addFeature(valueCollector.sort_getMin());
        }

        featureVector.addFeature(valueCollector.sort_getRange());
        featureVector.addFeature(valueCollector.getMedianCrossingRate());

        for (int i = 25; i < 100; i += 25) {
            featureVector.addFeature(valueCollector.sort_getPercentile(i / 100d));
        }


    }

    private static void addStandardFeatures(ArrayList<String> headerFields, String sensor, String attribute,
                                            boolean includeMin, boolean includeMax) {
        headerFields.add(sensor + "_average_" + attribute);
        headerFields.add(sensor + "_rootMeanSquare_" + attribute);
        headerFields.add(sensor + "_standardDeviation_" + attribute);
        headerFields.add(sensor + "_variance_" + attribute);
        headerFields.add(sensor + "_meanAbsoluteDeviation_" + attribute);
        headerFields.add(sensor + "_interquartileRange_" + attribute);
        if (includeMax) {
            headerFields.add(sensor + "_max_" + attribute);
        }
        if (includeMin) {
            headerFields.add(sensor + "_min_" + attribute);
        }
        headerFields.add(sensor + "_range_" + attribute);
        headerFields.add(sensor + "_medianCrossingRate_" + attribute);


        for (int i = 25; i < 100; i += 25) {
            headerFields.add(sensor + "_percentile" + i + "_" + attribute);
        }
    }

    private static void addStandardFeatures(ArrayList<String> headerFields, String sensor, String attribute) {
        addStandardFeatures(headerFields, sensor, attribute, true, true);
    }

    private static void addStandardFeatures(
            OutputFeatureVector featureVector, SortingValueCollector valueCollector) {
        addStandardFeatures(featureVector, valueCollector, true, true);
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


































