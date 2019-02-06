package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassifierFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class TestBenchSettings {


    // algorithms to test
    static ArrayList<ClassifierFactory.ClassifierType> classifiersToUse = new ArrayList<>(Arrays.asList(
//            ClassifierFactory.ClassifierType.J48
//          ,
//            ClassifierFactory.ClassifierType.RandomForest
//            ,
//            ClassifierFactory.ClassifierType.NaiveBayes
//            ,
            ClassifierFactory.ClassifierType.SMO
//            ,
//            ClassifierFactory.ClassifierType.OneR
    ));


    // folders
    private static String inputBaseFolder = "./inputFrameData/currentInput";
    private static String outputBaseFolder = "./outputResults/";
    private static String outputFolderTag = "";

    // sensor permutations to use during evaluation
    private static SensorUsage sensorUsageHMD = SensorUsage.CannotInclude;
    private static SensorUsage sensorUsageHandControllers = SensorUsage.CannotInclude;
    private static boolean allowSingleHandController = false;
    private static int minimumNumberOfTrackers = -1;
    private static int maximumNumberOfTrackers = 2;
    private static int minimumNumberOfSensors = -1;
    private static int maximumNumberOfSensors = -1;


    // feature types to disallow
    private static ArrayList<FeatureTag> forbiddenFeatureTags = new ArrayList<>(Arrays.asList(
//          FeatureTag.Angular
//          FeatureTag.SubjectOrientationRelevant
//          FeatureTag.DualSensorCombination
    ));


    // input frame data
    private static double windowSizeForFrameDataToFeatureConversion = 5;
    private static double windowSpacingForFrameDataToFeatureConversion = 1d;


    // result output
    private static boolean writeAllModelsToFolder = true;
    private static boolean useIndividualFeatureFilesForEachSubject = false;













    public enum SensorUsage {
        MayInclude, MustInclude, CannotInclude
    }

    public enum FeatureTag {
        Angular,
        SubjectOrientationRelevant,
        DualSensorCombination
    }

    public static String summarySingleLine() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("C");
        for (ClassifierFactory.ClassifierType classifier : classifiersToUse) {
            stringBuilder.append("_");
            stringBuilder.append(classifier.toString());
        }

        stringBuilder.append("   ws_" + windowSizeForFrameDataToFeatureConversion);
        stringBuilder.append("   wsp_" + windowSpacingForFrameDataToFeatureConversion);
        stringBuilder.append("   hmd_" + sensorUsageHMD.toString());
        stringBuilder.append("   hc_" + sensorUsageHandControllers.toString());
        stringBuilder.append("   shc_" + allowSingleHandController);
        stringBuilder.append("   t_" + minimumNumberOfTrackers + "-" + maximumNumberOfTrackers);
        stringBuilder.append("   s_" + minimumNumberOfSensors + "-" + maximumNumberOfSensors);

        return stringBuilder.toString();
    }


    public static String summaryBig() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Classifiers:   ");
        for (ClassifierFactory.ClassifierType classifier : classifiersToUse) {
            stringBuilder.append(classifier.toString());
            stringBuilder.append("   ");
        }
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("Window Size for frame data to feature conversion:   " + windowSizeForFrameDataToFeatureConversion);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("Window Spacing for frame data to feature conversion:   " + windowSpacingForFrameDataToFeatureConversion);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("sensor usage HMD:   " + sensorUsageHMD.toString());
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("sensor usage HandController:   " + sensorUsageHandControllers.toString());
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("disallow single HandController:   " + allowSingleHandController);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("maximum number Of Trackers:  " + maximumNumberOfTrackers);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("minimum number Of Trackers:  " + minimumNumberOfTrackers);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("maximum number of Sensors:   " + maximumNumberOfSensors);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("minimum number of Sensors:   " + minimumNumberOfSensors);
        stringBuilder.append(System.lineSeparator());

        return stringBuilder.toString();
    }


    public static ArrayList<ClassifierFactory.ClassifierType> getClassifiersToUse() {
        return classifiersToUse;
    }

    public static String getInputBaseFolder() {
        return inputBaseFolder;
    }

    public static String outputBaseFolder() {
        return outputBaseFolder;
    }

    public static String getOutputFolderTag() {
        return outputFolderTag;
    }

    public static double getWindowSizeForFrameDataToFeatureConversion() {
        return windowSizeForFrameDataToFeatureConversion;
    }

    public static double getWindowSpacingForFrameDataToFeatureConversion() {
        return windowSpacingForFrameDataToFeatureConversion;
    }

    public static SensorUsage getSensorUsageHMD() {
        return sensorUsageHMD;
    }

    public static SensorUsage getSensorUsageHandControllers() {
        return sensorUsageHandControllers;
    }


    public static int getMaximumNumberOfTrackers() {
        return maximumNumberOfTrackers;
    }

    public static int getMaximumNumberOfSensors() {
        return maximumNumberOfSensors;
    }

    public static boolean allowSingleHandController() {
        return allowSingleHandController;
    }

    public static int getMinimumNumberOfTrackers() {
        return minimumNumberOfTrackers;
    }

    public static int getMinimumNumberOfSensors() {
        return minimumNumberOfSensors;
    }

    public static boolean writeAllModelsToFolder() {
        return writeAllModelsToFolder;
    }


    public static boolean featureTagsAllowed(FeatureTag... tags) {

        for (FeatureTag tag : tags) {
            if (forbiddenFeatureTags.contains(tag)) {
                return false;
            }
        }

        return true;
    }

    public static boolean useIndividualFeatureFilesForEachSubject() {
        return useIndividualFeatureFilesForEachSubject;
    }
}
