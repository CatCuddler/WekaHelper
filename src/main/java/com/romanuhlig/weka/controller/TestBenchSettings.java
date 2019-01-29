package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassifierFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class TestBenchSettings {


    // algorithms to test
    static ArrayList<ClassifierFactory.ClassifierType> classifiersToUse = new ArrayList<>(Arrays.asList(
            ClassifierFactory.ClassifierType.J48,
           // ClassifierFactory.ClassifierType.RandomForest,
           // ClassifierFactory.ClassifierType.NaiveBayes,
           // ClassifierFactory.ClassifierType.SMO,
            ClassifierFactory.ClassifierType.OneR
    ));


    // folders
    static String inputBaseFolder = "./inputFrameData/currentInput";
    static String outputBaseFolder = "./outputResults/";
    static String outputFolderTag = "";

    // sensor permutations to use during evaluation
    // HH stands for Head + HandControllers
    static SensorUsage sensorUsageHMD = SensorUsage.MustInclude;
    static SensorUsage sensorUsageHandControllers = SensorUsage.CannotInclude;
    static boolean disAllowSingleHandController = true;
    static int maximumNumberOfTrackers = 1;
    static int maximumNumberOfSensors = 5;

    public enum SensorUsage {
        MayInclude, MustInclude, CannotInclude
    }


    // input frame data
    static double windowSizeForFrameDataToFeatureConversion = 5;
    static double windowSpacingForFrameDataToFeatureConversion = 1;


    // result output
    static boolean writeAllModelsToFolder = true;


    public static String summarySingleLine() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("C");
        for (ClassifierFactory.ClassifierType classifier : classifiersToUse) {
            stringBuilder.append("-");
            stringBuilder.append(classifier.toString());
        }

        stringBuilder.append("   WS-" + windowSizeForFrameDataToFeatureConversion);
        stringBuilder.append("   WSP-" + windowSpacingForFrameDataToFeatureConversion);
        stringBuilder.append("   HMD-" + sensorUsageHMD.toString());
        stringBuilder.append("   HC-" + sensorUsageHandControllers.toString());
        stringBuilder.append("   dSHC-" + disAllowSingleHandController);
        stringBuilder.append("   T-" + maximumNumberOfTrackers);
        stringBuilder.append("   S-" + maximumNumberOfSensors);

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

        stringBuilder.append("disallow single HandController:   " + disAllowSingleHandController);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("maximum number Of Trackers:  " + maximumNumberOfTrackers);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("maximum number of Sensors:   " + maximumNumberOfSensors);
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

    public static boolean disAllowSingleHandController() {
        return disAllowSingleHandController;
    }

    public static boolean writeAllModelsToFolder() {
        return writeAllModelsToFolder;
    }


}
