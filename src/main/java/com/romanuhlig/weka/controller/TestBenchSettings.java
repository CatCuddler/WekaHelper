package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassifierFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class TestBenchSettings {


    // algorithms to test
    static ArrayList<ClassifierFactory.ClassifierType> classifiersToUse = new ArrayList<>(Arrays.asList(
            ClassifierFactory.ClassifierType.J48,
            ClassifierFactory.ClassifierType.RandomForest,
            ClassifierFactory.ClassifierType.NaiveBayes,
            ClassifierFactory.ClassifierType.SMO,
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
    static double windowSizeForFrameDataToFeatureConversion = 1;
    static double windowSpacingForFrameDataToFeatureConversion = 1;


    // result output
    static boolean writeAllModelsToFolder = true;


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

    public static boolean isWriteAllModelsToFolder() {
        return writeAllModelsToFolder;
    }
}
