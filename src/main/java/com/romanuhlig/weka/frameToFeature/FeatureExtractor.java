package com.romanuhlig.weka.frameToFeature;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.romanuhlig.weka.ConvexHull.ConvexHull;
import com.romanuhlig.weka.ConvexHull.ConvexHullPoint;
import com.romanuhlig.weka.controller.TestBenchSettings;
import com.romanuhlig.weka.io.SubjectsFeatureExtractionResults;
import com.romanuhlig.weka.io.SubjectTrainingAndTestFilePackage;
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

/**
 * Reads and processes sensor readings produced by the BodyTrackingFramework
 *
 * @author Roman Uhlig
 */
public class FeatureExtractor {

    // the feature folder name to be added to the overall base output folder
    private static final String outputBaseFolder = "features";

    /**
     * Read and process all sensor readings in the given file
     *
     * @param filePath
     * @return
     */
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

            // package all frame data
            FrameDataSet frameDataSet = new FrameDataSet(frameDataFromFile);
            return frameDataSet;

        } catch (Exception e) {
            System.err.println("unable to read file " + filePath);
            return null;
        }
    }

    /**
     * Read and process all sensor readings within files in the given folder
     *
     * @param inputFilePath
     * @return
     */
    public static ArrayList<FrameDataSet> readAllFrameDataSets(String inputFilePath) {

        // read existing data
        // get files in path
        File inputFolder = new File(inputFilePath);
        File[] listOfInputFiles = inputFolder.listFiles();

        // read and collect the frame data from all files
        ArrayList<FrameDataSet> allFrameDataSets = new ArrayList<>();
        for (int i = 0; i < listOfInputFiles.length; i++) {

            // windows likes to place a desktop.ini file wherever it feels like, so we have to ignore it
            if (listOfInputFiles[i].getName().equals("desktop.ini") || listOfInputFiles[i].getName().equals(".DS_Store")) {
                System.out.println("ignored desktop.ini or .DS_Store");
                continue;
            }

            // progression log output
            System.out.println("reading input file:   " + (i + 1) + " / " + listOfInputFiles.length);

            File inputFile = listOfInputFiles[i];

            // files may be opened while debugging, so exclude lock files
            if (inputFile.getName().startsWith(".~")) {
                continue;
            }

            // read and collect the data
            FrameDataSet frameDataSet = readFrameDataSet(inputFile.getPath());
            allFrameDataSets.add(frameDataSet);
        }

        return allFrameDataSets;

    }

    /**
     * Create smaller FrameDataSets based on a sliding window
     *
     * @param originalFrameDataSets
     * @param windowSize
     * @param timeBetweenWindows
     * @return
     */
    public static ArrayList<FrameDataSet> separateFrameDataSetsIntoWindows(
            ArrayList<FrameDataSet> originalFrameDataSets, double windowSize, double timeBetweenWindows) {

        ArrayList<FrameDataSet> windows = new ArrayList<>();
        for (FrameDataSet frameDataSet : originalFrameDataSets) {
            ArrayList<FrameDataSet> dataSetWindows =
                    frameDataSet.separateFrameDataIntoValidWindows(windowSize, timeBetweenWindows);
            windows.addAll(dataSetWindows);
        }

        return windows;
    }

    /**
     * Create feature vectors based on the given input files, and save them
     *
     * @param inputFilePath
     * @param outputFilePath
     * @return
     */
    public static SubjectsFeatureExtractionResults createFeatureFiles(String inputFilePath, String outputFilePath) {

        ArrayList<FrameDataSet> windows = new ArrayList<>();

        ArrayList<FrameDataSet> originalFrameDataSets = readAllFrameDataSets(inputFilePath);

        for (FrameDataSet originalFrameDataSet : originalFrameDataSets) {
            if (originalFrameDataSet.getActivity().contains("Krieger")) {
                // read original recorded data and separate for each movement (one window == movement)
                windows.add(originalFrameDataSet);
            } else {
                // read original recorded data, and separate into windows
                ArrayList<FrameDataSet> dataSetWindows =
                        originalFrameDataSet.separateFrameDataIntoValidWindows(TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion(),
                                TestBenchSettings.getWindowSpacingForFrameDataToFeatureConversion());
                // add only the first 10 windows (because we also have only 10 trials for each yoga pose)
                for (int i = 0; i < 10; i++) {
                    if (i < dataSetWindows.size()) {
                        windows.add(dataSetWindows.get(i));
                    }
                }
                //windows.addAll(dataSetWindows);
            }
        }

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
                getFeatureHeaderForSensorTypes(
                        sensorTypes, true,
                        !TestBenchSettings.useIndividualFeatureFilesForEachSubject());

        ArrayList<FeatureVector> featureVectors = new ArrayList<>();

        // extract features for each window
        int numberOfWindows = windows.size();
        for (int i = windows.size() - 1; i >= 0; i--) {

            if ((numberOfWindows - i) % 100 == 0) {
                System.out.println("calculated features for window:   "
                        + (numberOfWindows - i) + " / " + numberOfWindows);
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

        SubjectsFeatureExtractionResults subjectsFeatureExtractionResults
                = new SubjectsFeatureExtractionResults(sensorTypes);

        // create training and test file for each subject individually
        if (TestBenchSettings.useIndividualFeatureFilesForEachSubject()) {

            System.out.println("writing features to individual files for each subject");

            for (String subject : subjectNameList) {

                // collect training and test examples
                ArrayList<FeatureVector> onlySubjectVectors = new ArrayList<>();
                ArrayList<FeatureVector> allButSubjectVectors = new ArrayList<>();
                for (FeatureVector originalVector : featureVectors) {
                    if (originalVector.getSubject().equals(subject)) {
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

                subjectsFeatureExtractionResults.addTrainingAndTestFilePackage(new SubjectTrainingAndTestFilePackage(
                        trainingFilePath, testFilePath, subject));
            }
        } else {
            // create dummy file packages if they are only needed as a reference to the subjects
            for (String subject : subjectNameList) {
                subjectsFeatureExtractionResults.addTrainingAndTestFilePackage(
                        new SubjectTrainingAndTestFilePackage(subject));
            }
        }

        System.out.println("writing all features into a single file");

        // write all features in a single data file
        String completeFeatureSetFilePath = outputFeaturesFilePath + "/allDataInOne.csv";
        writeOutputFeatureVectorToCSV(completeFeatureSetFilePath, headerFields, featureVectors);
        subjectsFeatureExtractionResults.setCompleteFeatureSet(new SubjectTrainingAndTestFilePackage(
                completeFeatureSetFilePath, completeFeatureSetFilePath, "completeFeatureSet"));

        return subjectsFeatureExtractionResults;

    }

    /**
     * Get the feature headers required for the sensors contained in the FrameDataSet
     *
     * @param frameDataSet
     * @param includeClassForTraining
     * @param includeSubjectForTraining
     * @return
     */
    public static ArrayList<String> getFeatureHeaderForFrameDataSet(FrameDataSet frameDataSet,
                                                                    boolean includeClassForTraining,
                                                                    boolean includeSubjectForTraining) {

        ArrayList<String> sensorTypes = frameDataSet.getAllSensorPositions();
        return getFeatureHeaderForSensorTypes(sensorTypes, includeClassForTraining, includeSubjectForTraining);
    }

    /**
     * Calculate the features for the given FrameDataSet
     * <p>
     * The FrameDataSet should represent one window of the original sensor data
     *
     * @param dataSource
     * @return
     */
    public static FeatureVector getFeaturesForFrameDataSet(FrameDataSet dataSource) {

        // create new data line
        FeatureVector featureVector = new FeatureVector(dataSource.getSubject(), dataSource.getActivity());

        ArrayList<List<FrameData>> allSensorLists = dataSource.getAllSensorLists();

        // collect features that are calculated from the data of a single sensor
        for (int sensorID = 0; sensorID < allSensorLists.size(); sensorID++) {

            List<FrameData> singleSensorList = allSensorLists.get(sensorID);

            // leave out blocked sensors
            if (TestBenchSettings.isSensorBlocked(singleSensorList.get(0).getSensorPosition())) {
                continue;
            }

            // determine values that stay true for the whole window
            double overallTimePassed =
                    singleSensorList.get(singleSensorList.size() - 1).getTime()
                            - singleSensorList.get(0).getTime();
            double bodySize = singleSensorList.get(0).getScale();

            // Calculate features for sensor.
            // Intuition suggests that average velocity and height / range will be more affected by body size
            // than acceleration, testing shows that this corresponds to the "best" setting for detection.
            StatisticalValueCollector Position_X =
                    new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Position_Y =
                    new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Position_Z =
                    new StatisticalValueCollector(true, true, overallTimePassed, bodySize);

            StatisticalValueCollector Rotation_X =
                    new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Rotation_Y =
                    new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Rotation_Z =
                    new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
            StatisticalValueCollector Rotation_W =
                    new StatisticalValueCollector(true, true, overallTimePassed, bodySize);


            StatisticalValueCollector Velocity_Y =
                    new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector Acceleration_Y =
                    new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector Velocity_XYZ =
                    new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            StatisticalValueCollector Acceleration_XYZ =
                    new StatisticalValueCollector(true, false, overallTimePassed, bodySize);

            //StatisticalValueCollector AngularVelocity =
            //        new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
            //StatisticalValueCollector AngularAcceleration =
            //        new StatisticalValueCollector(true, false, overallTimePassed, bodySize);

            // collect points in order to calculate maximum range in different dimensions
            //ArrayList<ConvexHullPoint> rangePointsXZ = new ArrayList<>(singleSensorList.size());
            Point3d[] rangePointsXYZ = new Point3d[singleSensorList.size()];

            // collect values of this sensor over time
            for (int i = 0; i < singleSensorList.size(); i++) {

                FrameData frameData = singleSensorList.get(i);

                // collect points in order to calculate maximum range in different dimensions
                rangePointsXYZ[i] = new Point3d(frameData.getPosX(), frameData.getPosY(), frameData.getPosZ());

                // collect other values over time for future analysis
                double timeSinceLastFrame = frameData.getFrameDuration();

                // velocity
                Velocity_Y.addValue(frameData.getLinVelZ(), timeSinceLastFrame);
                Velocity_XYZ.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getLinVelX(),
                                frameData.getLinVelY(),
                                frameData.getLinVelZ()),
                        timeSinceLastFrame);

                // acceleration
                Acceleration_Y.addValue(frameData.getLinAccelerationZ(), timeSinceLastFrame);
                Acceleration_XYZ.addValue(
                        MathHelper.EuclideanNorm(
                                frameData.getLinAccelerationX(),
                                frameData.getLinAccelerationY(),
                                frameData.getLinAccelerationZ()),
                        timeSinceLastFrame);

                // angular velocity
                /*AngularVelocity.addValue(
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
                        timeSinceLastFrame);*/

                // position
                Position_X.addValue(frameData.getPosX(), timeSinceLastFrame);
                Position_Y.addValue(frameData.getPosY(), timeSinceLastFrame);
                Position_Z.addValue(frameData.getPosZ(), timeSinceLastFrame);

                // rotation
                Rotation_X.addValue(frameData.getRotX(), timeSinceLastFrame);
                Rotation_Y.addValue(frameData.getRotY(), timeSinceLastFrame);
                Rotation_Z.addValue(frameData.getRotZ(), timeSinceLastFrame);
                Rotation_W.addValue(frameData.getRotW(), timeSinceLastFrame);
            }

            // determine individual values that do not use the collector class
            // compute convex hull in 2D, and test for maximum distance among hull points
            /*List<ConvexHullPoint> rangeXZouterPoints = ConvexHull.makeHull(rangePointsXZ);
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
            rangeXZ /= bodySize;*/
            // compute convex hull in 3D, and test for maximum distance among hull points
            /*double rangeXYZ = 0;
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
                // if there is an exception, it is because all points were too similar
                // if this is the case, we can just keep the maximum distance value at 0
            }
            rangeXYZ /= bodySize;*/


            // collect the calculated single-sensor values for output
            // the order and type-based selection here has to be consistent with the generated header
            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Position) && !TestBenchSettings.featureTagsAllowed(FeatureType.DualSensorOnly)) {
                //Position_X.adjustToLowestValueAsZero();
                //Position_Y.adjustToLowestValueAsZero();
                addStandardFeatures(featureVector, Position_X);
                addStandardFeatures(featureVector, Position_Y);
                addStandardFeatures(featureVector, Position_Z);

                //featureVector.addFeature(rangeXYZ);
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Rotation)) {
                addStandardFeatures(featureVector, Rotation_X);
                addStandardFeatures(featureVector, Rotation_Y);
                addStandardFeatures(featureVector, Rotation_Z);
                addStandardFeatures(featureVector, Rotation_W);
            }


            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Velocity) && !TestBenchSettings.featureTagsAllowed(FeatureType.DualSensorOnly)) {
                //addStandardFeatures(featureVector, Velocity_Y);
                addStandardFeatures(featureVector, Velocity_XYZ);
                //addStandardFeatures(featureVector, AngularVelocity);
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Acceleration)) {
                //addStandardFeatures(featureVector, Acceleration_Y);
                addStandardFeatures(featureVector, Acceleration_XYZ);
                //addStandardFeatures(featureVector, AngularAcceleration);
            }
        }


        // collect features that depend on the relationship between two sensors
        if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.DualSensorCombination) || TestBenchSettings.featureTagsAllowed(FeatureType.DualSensorOnly)) {
            for (int ssA = 0; ssA < allSensorLists.size(); ssA++) {
                List<FrameData> singleSensorA = allSensorLists.get(ssA);

                if (TestBenchSettings.isSensorBlocked(singleSensorA.get(0).getSensorPosition())) {
                    continue;
                }

                // determine values that stay true for the whole window
                double overallTimePassed =
                        singleSensorA.get(singleSensorA.size() - 1).getTime()
                                - singleSensorA.get(0).getTime();
                double bodySize = singleSensorA.get(0).getScale();


                for (int ssB = ssA + 1; ssB < allSensorLists.size(); ssB++) {
                    List<FrameData> singleSensorB = allSensorLists.get(ssB);

                    if (TestBenchSettings.isSensorBlocked(singleSensorB.get(0).getSensorPosition())) {
                        continue;
                    }

                    StatisticalValueCollector distanceX =
                            new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceY =
                            new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceZ =
                            new StatisticalValueCollector(true, true, overallTimePassed, bodySize);

                    StatisticalValueCollector distanceXY =
                            new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceXZ =
                            new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceYZ =
                            new StatisticalValueCollector(true, true, overallTimePassed, bodySize);
                    StatisticalValueCollector distanceXYZ =
                            new StatisticalValueCollector(true, true, overallTimePassed, bodySize);

                    StatisticalValueCollector differenceVelocityY =
                            new StatisticalValueCollector(true, false, overallTimePassed, bodySize);
                    StatisticalValueCollector differenceVelocityXYZ =
                            new StatisticalValueCollector(true, false, overallTimePassed, bodySize);

                    for (int i = 0; i < singleSensorA.size(); i++) {

                        // prepare current frame
                        FrameData frameDataA = singleSensorA.get(i);
                        FrameData frameDataB = singleSensorB.get(i);
                        double timeSinceLastFrame = frameDataA.getFrameDuration();

                        // distance
                        double positionDistanceCurrentFrameX =
                                MathHelper.distance(
                                        frameDataA.getPosX(),
                                        frameDataB.getPosX());
                        double positionDistanceCurrentFrameY =
                                MathHelper.distance(
                                        frameDataA.getPosY(),
                                        frameDataB.getPosY());
                        double positionDistanceCurrentFrameZ =
                                MathHelper.distance(
                                        frameDataA.getPosZ(),
                                        frameDataB.getPosZ());
                        double positionDistanceCurrentFrameXY =
                                MathHelper.distance(
                                        frameDataA.getPosX(),
                                        frameDataA.getPosY(),
                                        frameDataB.getPosX(),
                                        frameDataB.getPosY());
                        double positionDistanceCurrentFrameXZ =
                                MathHelper.distance(
                                        frameDataA.getPosX(),
                                        frameDataA.getPosZ(),
                                        frameDataB.getPosX(),
                                        frameDataB.getPosZ());
                        double positionDistanceCurrentFrameYZ =
                                MathHelper.distance(
                                        frameDataA.getPosY(),
                                        frameDataA.getPosZ(),
                                        frameDataB.getPosY(),
                                        frameDataB.getPosZ());
                        double positionDistanceCurrentFrameXYZ =
                                MathHelper.distance(
                                        frameDataA.getPosX(),
                                        frameDataA.getPosY(),
                                        frameDataA.getPosZ(),
                                        frameDataB.getPosX(),
                                        frameDataB.getPosY(),
                                        frameDataB.getPosZ());

                        // distance in position
                        distanceX.addValue(positionDistanceCurrentFrameX, timeSinceLastFrame);
                        distanceY.addValue(positionDistanceCurrentFrameY, timeSinceLastFrame);
                        distanceZ.addValue(positionDistanceCurrentFrameZ, timeSinceLastFrame);
                        distanceXY.addValue(positionDistanceCurrentFrameXY, timeSinceLastFrame);
                        distanceXZ.addValue(positionDistanceCurrentFrameXZ, timeSinceLastFrame);
                        distanceYZ.addValue(positionDistanceCurrentFrameYZ, timeSinceLastFrame);
                        distanceXYZ.addValue(positionDistanceCurrentFrameXYZ, timeSinceLastFrame);

                        // difference in velocity
                        differenceVelocityY.addValue(Math.abs(frameDataA.getLinVelY() - frameDataB.getLinVelY()), timeSinceLastFrame);
                        double velocityXYZa = MathHelper.EuclideanNorm(
                                frameDataA.getLinVelX(),
                                frameDataA.getLinVelY(),
                                frameDataA.getLinVelZ());
                        double velocityXYZb = MathHelper.EuclideanNorm(
                                frameDataB.getLinVelX(),
                                frameDataB.getLinVelY(),
                                frameDataB.getLinVelZ());
                        differenceVelocityXYZ.addValue(Math.abs(velocityXYZa - velocityXYZb), timeSinceLastFrame);
                    }

                    // collect the calculated dual-sensor values for output
                    // the order and type-based selection here has to be consistent with the generated header
                    if (TestBenchSettings.featureTagsAllowed(FeatureType.Position)) {
                        addStandardFeatures(featureVector, distanceX, true, true, false);
                        addStandardFeatures(featureVector, distanceY, true, true, false);
                        addStandardFeatures(featureVector, distanceZ, true, true, false);
                        /*addStandardFeatures(featureVector, distanceXY, true, true, false);
                        addStandardFeatures(featureVector, distanceXZ, true, true, false);
                        addStandardFeatures(featureVector, distanceYZ, true, true, false);*/
                        addStandardFeatures(featureVector, distanceXYZ, true, true, false);
                    }

                    if (TestBenchSettings.featureTagsAllowed(FeatureType.Velocity)) {
                        //addStandardFeatures(featureVector, differenceVelocityY, true, true, false);
                        addStandardFeatures(featureVector, differenceVelocityXYZ, true, true, false);
                    }

                }
            }
        }
        return featureVector;
    }

    /**
     * Assemble the header for the features produced by the given sensors
     *
     * @param sensorTypes
     * @param includeClassForTraining
     * @param includeSubjectForTraining
     * @return
     */
    public static ArrayList<String> getFeatureHeaderForSensorTypes(ArrayList<String> sensorTypes,
                                                                   boolean includeClassForTraining,
                                                                   boolean includeSubjectForTraining) {

        ArrayList<String> headerFields = new ArrayList<>();

        // collect the single-sensor header fields
        // the order and type-based selection here has to be consistent with the generated features
        for (String sensorType : sensorTypes) {

            // leave out blocked sensors
            if (TestBenchSettings.isSensorBlocked(sensorType)) {
                continue;
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Position) && !TestBenchSettings.featureTagsAllowed(FeatureType.DualSensorOnly)) {
                addStandardFeatureHeader(headerFields, sensorType, "Position_X");
                addStandardFeatureHeader(headerFields, sensorType, "Position_Y");
                addStandardFeatureHeader(headerFields, sensorType, "Position_Z");

                //headerFields.add(sensorType + "_Range_XYZ");
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Rotation)) {
                addStandardFeatureHeader(headerFields, sensorType, "Rotation_X");
                addStandardFeatureHeader(headerFields, sensorType, "Rotation_Y");
                addStandardFeatureHeader(headerFields, sensorType, "Rotation_Z");
                addStandardFeatureHeader(headerFields, sensorType, "Rotation_W");
            }

            if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.Velocity) && !TestBenchSettings.featureTagsAllowed(FeatureType.DualSensorOnly)) {
                //addStandardFeatureHeader(headerFields, sensorType, "Velocity_Y");
                addStandardFeatureHeader(headerFields, sensorType, "Velocity_XYZ");
            }

            if (TestBenchSettings.featureTagsAllowed(FeatureType.Acceleration)) {
                //addStandardFeatureHeader(headerFields, sensorType, "Acceleration_Y");
                addStandardFeatureHeader(headerFields, sensorType, "Acceleration_XYZ");
            }
        }

        // collect the dual-sensor header fields
        // the order and type-based selection here has to be consistent with the generated features
        if (TestBenchSettings.featureTagsAllowed(TestBenchSettings.FeatureType.DualSensorCombination) || TestBenchSettings.featureTagsAllowed(FeatureType.DualSensorOnly)) {
            for (int ssA = 0; ssA < sensorTypes.size(); ssA++) {
                String singleSensorA = sensorTypes.get(ssA);

                // ignore blocked sensors
                if (TestBenchSettings.isSensorBlocked(singleSensorA)) {
                    continue;
                }

                for (int ssB = ssA + 1; ssB < sensorTypes.size(); ssB++) {
                    String singleSensorB = sensorTypes.get(ssB);

                    // ignore blocked sensors
                    if (TestBenchSettings.isSensorBlocked(singleSensorB)) {
                        continue;
                    }

                    if (TestBenchSettings.featureTagsAllowed(FeatureType.Position)) {
                        addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "PositionDistance_X", true, true, false);
                        addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "PositionDistance_Y", true, true, false);
                        addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "PositionDistance_Z", true, true, false);
                        /*addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "PositionDistance_XY", true, true, false);
                        addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "PositionDistance_XZ", true, true, false);
                        addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "PositionDistance_YZ", true, true, false);*/
                        addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "PositionDistance_XYZ", true, true, false);
                    }

                    if (TestBenchSettings.featureTagsAllowed(FeatureType.Velocity)) {
                        //addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "DifferenceVelocity_Y", true, true, false);
                        addStandardFeatureHeader(headerFields, singleSensorA + "_" + singleSensorB, "DifferenceVelocity_XYZ", true, true, false);
                    }
                }
            }
        }

        // add optional fields
        if (includeSubjectForTraining) {
            headerFields.add("subject");
        }
        if (includeClassForTraining) {
            headerFields.add("activity");
        }

        return headerFields;
    }

    /**
     * Add the standard features of the given ValueCollector to the given FeatureVector
     *
     * @param featureVector
     * @param valueCollector
     * @param includeMin
     * @param includeMax
     */
    private static void addStandardFeatures(
            FeatureVector featureVector, StatisticalValueCollector valueCollector,
            boolean includeMin, boolean includeMax, boolean mean_crossing_rate) {

        // the order and option-based selection here has to be consistent with the standard feature header
        featureVector.addFeature(valueCollector.getMeanScaledByTime());

        //featureVector.addFeature(valueCollector.getRootMeanSquare());
        featureVector.addFeature(valueCollector.getStandardDeviation());
        featureVector.addFeature(valueCollector.getMeanAbsoluteDeviation());

        featureVector.addFeature(valueCollector.sort_getInterquartileRange());

        if (includeMax) {
            featureVector.addFeature(valueCollector.sort_getMax());
        }
        if (includeMin) {
            featureVector.addFeature(valueCollector.sort_getMin());
        }

        featureVector.addFeature(valueCollector.sort_getRange());

        if (mean_crossing_rate) {
            featureVector.addFeature(valueCollector.sort_getMeanCrossingRate());
        }

        featureVector.addFeature(valueCollector.sort_getPercentile(0.25));
        featureVector.addFeature(valueCollector.sort_getPercentile(0.75));
    }

    /**
     * Add the standard set of features
     *
     * @param featureVector
     * @param valueCollector
     */
    private static void addStandardFeatures(
            FeatureVector featureVector, StatisticalValueCollector valueCollector) {
        addStandardFeatures(featureVector, valueCollector, true, true, true);
    }

    /**
     * Add the standard features for the given sensor and attribute to the given header fields
     *
     * @param headerFields
     * @param sensor
     * @param attribute
     * @param includeMin
     * @param includeMax
     */
    private static void addStandardFeatureHeader(ArrayList<String> headerFields, String sensor, String attribute,
                                                 boolean includeMin, boolean includeMax,
                                                 boolean mean_crossing_rate) {

        // the order and option-based selection here has to be consistent with the standard features
        headerFields.add(sensor + "_mean_" + attribute);

        //headerFields.add(sensor + "_rootMeanSquare_" + attribute);
        headerFields.add(sensor + "_standardDeviation_" + attribute);
        headerFields.add(sensor + "_meanAbsoluteDeviation_" + attribute);

        headerFields.add(sensor + "_interquartileRange_" + attribute);

        if (includeMax) {
            headerFields.add(sensor + "_max_" + attribute);
        }
        if (includeMin) {
            headerFields.add(sensor + "_min_" + attribute);
        }

        headerFields.add(sensor + "_range_" + attribute);

        if (mean_crossing_rate) {
            headerFields.add(sensor + "_meanCrossingRate_" + attribute);
        }

        headerFields.add(sensor + "_percentile25_" + attribute);
        headerFields.add(sensor + "_percentile75_" + attribute);
    }

    /**
     * Add headed for the standard set of features
     *
     * @param headerFields
     * @param sensor
     * @param attribute
     */
    private static void addStandardFeatureHeader(ArrayList<String> headerFields, String sensor, String attribute) {
        addStandardFeatureHeader(headerFields, sensor, attribute, true, true, true);
    }

    /**
     * Save the feature vectors into a table with the given header, at the given path location
     *
     * @param filePath
     * @param headerFields
     * @param featureVectors
     */
    private static void writeOutputFeatureVectorToCSV(
            String filePath, ArrayList<String> headerFields, ArrayList<FeatureVector> featureVectors) {

        // sort output by class
        // The Weka Gui is unable to deal with unordered classes, which makes sanity checks difficult otherwise
        featureVectors.sort(new Comparator<FeatureVector>() {
            @Override
            public int compare(FeatureVector o1, FeatureVector o2) {
                return o1.getClassValue().compareTo(o2.getClassValue());
            }
        });

        try (
                // setup writer
                Writer writer = Files.newBufferedWriter(Paths.get(filePath));
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            // write header
            csvWriter.writeNext(headerFields.toArray(new String[headerFields.size()]));
            // write features line by line
            for (FeatureVector featureVector : featureVectors) {
                csvWriter.writeNext(featureVector.getFeaturesWithClassAndSubject());
            }
        } catch (Exception e) {
            System.err.println("unable to write file " + featureVectors);
        }
    }
}