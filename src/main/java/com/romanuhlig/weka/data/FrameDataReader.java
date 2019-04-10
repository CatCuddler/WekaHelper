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

import com.romanuhlig.weka.controller.TestBenchSettings.FeatureType;
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

            if (listOfInputFiles[i].getName().equals("desktop.ini")) {
                System.out.println("ignored desktop.ini");
                continue;
            }

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

        ArrayList<FeatureVector> featureVectors = new ArrayList<>();

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
            FeatureVector currentFeatureVector = getFeaturesForFrameDataSet(singleWindow);
            featureVectors.add(currentFeatureVector);

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
                ArrayList<FeatureVector> onlySubjectVectors = new ArrayList<>();
                ArrayList<FeatureVector> allButSubjectVectors = new ArrayList<>();
                for (FeatureVector originalVector : featureVectors) {
                    if (originalVector.subject.equals(subject)) {
                        onlySubjectVectors.add(originalVector);
                    } else {
                        allButSubjectVectors.add(originalVector);
                    }
                }


//            // sort data by class value
//            onlySubjectVectors.sort(new Comparator<FeatureVector>() {
//                @Override
//                public int compare(FeatureVector o1, FeatureVector o2) {
//                    String o1Class = o1.getFeaturesWithClassValue().get(o1.getFeaturesWithClassValue().size() - 1);
//                    String o2Class = o2.getFeaturesWithClassValue().get(o2.getFeaturesWithClassValue().size() - 1);
//                    return o1Class.compareTo(o2Class);
//                }
//            });
//
//            allButSubjectVectors.sort(new Comparator<FeatureVector>() {
//                @Override
//                public int compare(FeatureVector o1, FeatureVector o2) {
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
        writeOutputFeatureVectorToCSV(completeFeatureSetFilePath, headerFields, featureVectors);
        featureExtractionResults.setCompleteFeatureSet(new TrainingAndTestFilePackage(completeFeatureSetFilePath, completeFeatureSetFilePath, "completeFeatureSet"));

        return featureExtractionResults;

    }


    public static ArrayList<String> getHeaderForFrameDataSet(FrameDataSet frameDataSet,
                                                             boolean includeClassForTraining,
                                                             boolean includeSubjectForTraining) {

        ArrayList<String> sensorTypes = frameDataSet.getAllSensorPositions();
        return getHeaderForSensorTypes(sensorTypes, includeClassForTraining, includeSubjectForTraining);
    }


    public static FeatureVector getFeaturesForFrameDataSet(FrameDataSet dataSource) {

        // create new data line
        FeatureVector featureVector = new FeatureVector(dataSource.getSubject(), dataSource.getActivity());

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
            StatisticalValueCollector Position_Height = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Position_X = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Position_Z = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Velocity_X = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Velocity_Z = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Velocity_Height = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Velocity_XZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Velocity_XYZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Acceleration_X = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector Acceleration_Z = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector Acceleration_Height = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector Acceleration_XZ = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector Acceleration_XYZ = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector AngularVelocity = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector AngularAcceleration = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);

            // collect points in order to calculate maximum range in different dimensions
            ArrayList<ConvexHullPoint> rangePointsXZ = new ArrayList<>(singleSensorList.size());
            Point3d[] rangePointsXYZ = new Point3d[singleSensorList.size()];


            for (int i = 0; i < singleSensorList.size(); i++) {

                FrameData frameData = singleSensorList.get(i);

                // collect points in order to calculate maximum range in different dimensions
                rangePointsXZ.add(new ConvexHullPoint(frameData.getRawPosX(), frameData.getRawPosZ()));
                rangePointsXYZ[i] = new Point3d(frameData.getRawPosX(), frameData.getRawPosY(), frameData.getRawPosZ());

                // collect other values over time for future analysis
                double timeSinceLastFrame = frameData.getFrameDuration();

                Velocity_X.addValue(Math.abs(frameData.getRawLinVelX()), timeSinceLastFrame);
                Velocity_Z.addValue(Math.abs(frameData.getRawLinVelZ()), timeSinceLastFrame);
                Velocity_Height.addValue(Math.abs(frameData.getRawLinVelY()), timeSinceLastFrame);


                Velocity_XZ.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getRawLinVelX(),
                                frameData.getRawLinVelZ()),
                        timeSinceLastFrame);
                Velocity_XYZ.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getRawLinVelX(),
                                frameData.getRawLinVelY(),
                                frameData.getRawLinVelZ()),
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
                                frameData.getRawAngVelX(),
                                frameData.getRawAngVelY(),
                                frameData.getRawAngVelZ()),
                        timeSinceLastFrame);
                AngularAcceleration.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getAngAccelerationX(),
                                frameData.getAngAccelerationY(),
                                frameData.getAngAccelerationZ()),
                        timeSinceLastFrame);


                Position_X.addValue(frameData.getRawPosX(), timeSinceLastFrame);
                Position_Z.addValue(frameData.getRawPosZ(), timeSinceLastFrame);
                Position_Height.addValue(frameData.getRawPosY(), timeSinceLastFrame);
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
            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Position)) {
                addStandardFeatures(featureVector, Position_Height);
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.SubjectOrientationRelevant)) {
//                featureVector.addFeature(Position_X.sort_getRange());
//                featureVector.addFeature(Position_Z.sort_getRange());
                if (TestBenchSettings.featureTagsAllowed(FeatureType.Position)) {
                    Position_X.adjustToLowestValueAsZero();
                    Position_Z.adjustToLowestValueAsZero();
                    addStandardFeatures(featureVector, Position_X, false, false);
                    addStandardFeatures(featureVector, Position_Z, false, false);
                }

                if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Velocity)) {
                    addStandardFeatures(featureVector, Velocity_X);
                    addStandardFeatures(featureVector, Velocity_Z);
                }
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Position)) {
                featureVector.addFeature(rangeXZ);
                featureVector.addFeature(rangeXYZ);
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Velocity)) {
                addStandardFeatures(featureVector, Velocity_Height);
                addStandardFeatures(featureVector, Velocity_XZ);
                addStandardFeatures(featureVector, Velocity_XYZ);
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.SubjectOrientationRelevant)) {
                if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Acceleration)) {
                    addStandardFeatures(featureVector, Acceleration_X);
                    addStandardFeatures(featureVector, Acceleration_Z);
                }
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Acceleration)) {
                addStandardFeatures(featureVector, Acceleration_Height);
                addStandardFeatures(featureVector, Acceleration_XZ);
                addStandardFeatures(featureVector, Acceleration_XYZ);
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Angular)) {
                addStandardFeatures(featureVector, AngularVelocity);
                addStandardFeatures(featureVector, AngularAcceleration);
            }

////             collect data for dual sensor combination features
//            rangeXforSensor[sensorID] = Position_X.sort_getRange();
//            rangeZforSensor[sensorID] = Position_Z.sort_getRange();
//            rangeHeightforSensor[sensorID] = Position_Height.sort_getRange();
//            rangeXZforSensor[sensorID] = rangeXZ;
//            rangeXYZforSensor[sensorID] = rangeXYZ;


        }


        // collect features that depend on the relationship between two sensors
        if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.DualSensorCombination)) {
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

                    StatisticalValueCollector distanceX = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceHeight = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceXZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceXYZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);

//                    StatisticalValueCollector distanceChangeX = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
//                    StatisticalValueCollector distanceChangeZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
//                    StatisticalValueCollector distanceChangeHeight = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
//                    StatisticalValueCollector distanceChangeXZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
//                    StatisticalValueCollector distanceChangeXYZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);

                    StatisticalValueCollector differenceVelocityX = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector differenceVelocityZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector differenceVelocityHeight = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector differenceVelocityXZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector differenceVelocityXYZ = new StatisticalValueCollector(true, true, overallTimePassed, bodySize);

//                    StatisticalValueCollector differenceAngularVelocityXYZ = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);


//                    StatisticalValueCollector differenceAccelerationX = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
//                    StatisticalValueCollector differenceAccelerationZ = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
//                    StatisticalValueCollector differenceAccelerationHeight = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
//                    StatisticalValueCollector differenceAccelerationXZ = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
//                    StatisticalValueCollector differenceAccelerationXYZ = new StatisticalValueCollector(true, false, overallTimePassed, bodySize);

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
                                        frameDataA.getRawPosX(),
                                        frameDataB.getRawPosX());
                        double averageDistanceCurrentFrameZ =
                                MathHelper.distance(
                                        frameDataA.getRawPosZ(),
                                        frameDataB.getRawPosZ());
                        double averageDistanceCurrentFrameHeight =
                                MathHelper.distance(
                                        frameDataA.getRawPosY(),
                                        frameDataB.getRawPosY());
                        double averageDistanceCurrentFrameXZ =
                                MathHelper.distance(
                                        frameDataA.getRawPosX(),
                                        frameDataA.getRawPosZ(),
                                        frameDataB.getRawPosX(),
                                        frameDataB.getRawPosZ());
                        double averageDistanceCurrentFrameXYZ =
                                MathHelper.distance(
                                        frameDataA.getRawPosX(),
                                        frameDataA.getRawPosY(),
                                        frameDataA.getRawPosZ(),
                                        frameDataB.getRawPosX(),
                                        frameDataB.getRawPosY(),
                                        frameDataB.getRawPosZ());


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

                        differenceVelocityX.addValue(Math.abs(frameDataA.getRawLinVelX() - frameDataB.getRawLinVelX()), timeSinceLastFrame);
                        differenceVelocityZ.addValue(Math.abs(frameDataA.getRawLinVelZ() - frameDataB.getRawLinVelZ()), timeSinceLastFrame);
                        differenceVelocityHeight.addValue(Math.abs(frameDataA.getRawLinVelY() - frameDataB.getRawLinVelY()), timeSinceLastFrame);
                        double velocityXZa = MathHelper.EuclideanNorm(
                                frameDataA.getRawLinVelX(),
                                frameDataA.getRawLinVelZ());
                        double velocityXZb = MathHelper.EuclideanNorm(
                                frameDataB.getRawLinVelX(),
                                frameDataB.getRawLinVelZ());
                        double velocityXYZa = MathHelper.EuclideanNorm(
                                frameDataA.getRawLinVelX(),
                                frameDataA.getRawLinVelY(),
                                frameDataA.getRawLinVelZ());
                        double velocityXYZb = MathHelper.EuclideanNorm(
                                frameDataB.getRawLinVelX(),
                                frameDataB.getRawLinVelY(),
                                frameDataB.getRawLinVelZ());
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
//                                            frameDataA.getRawAngVelX(),
//                                            frameDataA.getRawAngVelY(),
//                                            frameDataA.getRawAngVelZ());
//                            double AngularVelocityXYZb =
//                                    MathHelper.EuclideanNorm(
//                                            frameDataB.getRawAngVelX(),
//                                            frameDataB.getRawAngVelY(),
//                                            frameDataB.getRawAngVelZ());
//                            differenceAngularVelocityXYZ.addValue((Math.abs(AngularVelocityXYZa - AngularVelocityXYZb)), timeSinceLastFrame);


////                         prepare next frame
//                        averageDistanceLastFrameX = averageDistanceCurrentFrameX;
//                        averageDistanceLastFrameZ = averageDistanceCurrentFrameZ;
//                        averageDistanceLastFrameHeight = averageDistanceCurrentFrameHeight;
//                        averageDistanceLastFrameXZ = averageDistanceCurrentFrameXZ;
//                        averageDistanceLastFrameXYZ = averageDistanceCurrentFrameXYZ;
                    }

                    if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.SubjectOrientationRelevant)) {
                        addStandardFeatures(featureVector, distanceX);
                        addStandardFeatures(featureVector, distanceZ);
                    }
                    addStandardFeatures(featureVector, distanceHeight);
                    addStandardFeatures(featureVector, distanceXZ);
                    addStandardFeatures(featureVector, distanceXYZ);

//                    if (TestBenchSettings.featureTagsAllowed(FeatureType.SubjectOrientationRelevant)) {
//                    addStandardFeatures(featureVector, distanceChangeX);
//                    addStandardFeatures(featureVector, distanceChangeZ);
//                    }
//                    addStandardFeatures(featureVector, distanceChangeHeight);
//                    addStandardFeatures(featureVector, distanceChangeXZ);
//                    addStandardFeatures(featureVector, distanceChangeXYZ);

                    if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.SubjectOrientationRelevant)) {
                        addStandardFeatures(featureVector, differenceVelocityX);
                        addStandardFeatures(featureVector, differenceVelocityZ);
                    }
                    addStandardFeatures(featureVector, differenceVelocityHeight);
                    addStandardFeatures(featureVector, differenceVelocityXZ);
                    addStandardFeatures(featureVector, differenceVelocityXYZ);

//                    if (TestBenchSettings.featureTagsAllowed(FeatureType.SubjectOrientationRelevant)) {
//                    addStandardFeatures(featureVector, differenceAccelerationX);
//                    addStandardFeatures(featureVector, differenceAccelerationZ);
//                    }
//                    addStandardFeatures(featureVector, differenceAccelerationHeight);
//                    addStandardFeatures(featureVector, differenceAccelerationXZ);
//                    addStandardFeatures(featureVector, differenceAccelerationXYZ);

//                    addStandardFeatures(featureVector, differenceAngularVelocityXYZ);


//                    featureVector.addFeature(Math.abs(rangeXforSensor[ssA] - rangeXforSensor[ssB]));
//                    featureVector.addFeature(Math.abs(rangeZforSensor[ssA] - rangeZforSensor[ssB]));
//                    featureVector.addFeature(Math.abs(rangeHeightforSensor[ssA] - rangeHeightforSensor[ssB]));
//                    featureVector.addFeature(Math.abs(rangeXZforSensor[ssA] - rangeXZforSensor[ssB]));
//                    featureVector.addFeature(Math.abs(rangeXYZforSensor[ssA] - rangeXYZforSensor[ssB]));


                }
            }
        }
        return featureVector;
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

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Position)) {
                addStandardFeatures(headerFields, sensorType, "Position_Height");
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureType.SubjectOrientationRelevant)) {
//                headerFields.add(sensorType + "_range_X");
//                headerFields.add(sensorType + "_range_Z");
                if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Position)) {
                    addStandardFeatures(headerFields, sensorType, "Position_X", false, false);
                    addStandardFeatures(headerFields, sensorType, "Position_Z", false, false);
                }

                if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Velocity)) {
                    addStandardFeatures(headerFields, sensorType, "Velocity_X");
                    addStandardFeatures(headerFields, sensorType, "Velocity_Z");
                }
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Position)) {
                headerFields.add(sensorType + "_range_XZ");
                headerFields.add(sensorType + "_range_XYZ");
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Velocity)) {
                addStandardFeatures(headerFields, sensorType, "Velocity_Height");
                addStandardFeatures(headerFields, sensorType, "Velocity_XZ");
                addStandardFeatures(headerFields, sensorType, "Velocity_XYZ");
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.SubjectOrientationRelevant)) {
                if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Acceleration)) {
                    addStandardFeatures(headerFields, sensorType, "Acceleration_X");
                    addStandardFeatures(headerFields, sensorType, "Acceleration_Z");
                }
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureType.Acceleration)) {
                addStandardFeatures(headerFields, sensorType, "Acceleration_Height");
                addStandardFeatures(headerFields, sensorType, "Acceleration_XZ");
                addStandardFeatures(headerFields, sensorType, "Acceleration_XYZ");
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Angular)) {
                addStandardFeatures(headerFields, sensorType, "Velocity_Angular");
                addStandardFeatures(headerFields, sensorType, "Acceleration_Angular");
            }

        }

        // collect features that depend on the relationship between two sensors
        if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.DualSensorCombination)) {
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

                    if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.SubjectOrientationRelevant)) {
                        addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_X");
                        addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_Z");
                    }
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_Height");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_XZ");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistance_XYZ");

//                    if (TestBenchSettings.featureTagsAllowed(FeatureType.SubjectOrientationRelevant)) {
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_X");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_Z");
//                    }
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_Height");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_XZ");
//                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "AverageDistanceChange_XYZ");

                    if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.SubjectOrientationRelevant)) {
                        addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_X");
                        addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_Z");
                    }
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_Height");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_XZ");
                    addStandardFeatures(headerFields, singleSensorA + "_" + singleSensorB, "differenceVelocity_XYZ");

//                    if (TestBenchSettings.featureTagsAllowed(FeatureType.SubjectOrientationRelevant)) {
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
            FeatureVector featureVector, StatisticalValueCollector valueCollector,
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
            FeatureVector featureVector, StatisticalValueCollector valueCollector) {
        addStandardFeatures(featureVector, valueCollector, true, true);
    }

    private static void writeOutputFeatureVectorToCSV(
            String filePath, ArrayList<String> headerFields, ArrayList<FeatureVector> featureVectors) {

        // sort output by class
        // The Weka Gui is unable to deal with unordered classes, which makes sanity checks difficult
        featureVectors.sort(new Comparator<FeatureVector>() {
            @Override
            public int compare(FeatureVector o1, FeatureVector o2) {
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

            for (FeatureVector featureVector : featureVectors) {
                csvWriter.writeNext(featureVector.getFeaturesWithClassAndSubject(
                        !TestBenchSettings.useIndividualFeatureFilesForEachSubject()));
            }

        } catch (Exception e) {
            System.err.println("unable to write file " + featureVectors);
        }
    }
}


































