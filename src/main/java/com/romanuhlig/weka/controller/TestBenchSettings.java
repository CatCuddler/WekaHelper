package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassifierFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class TestBenchSettings {


    // algorithms to test
    static ArrayList<ClassifierFactory.ClassifierType> classifiersToUse = new ArrayList<>(Arrays.asList(
            //ClassifierFactory.ClassifierType.J48
            ClassifierFactory.ClassifierType.RandomForest
            //ClassifierFactory.ClassifierType.NaiveBayes,
            //ClassifierFactory.ClassifierType.SMO,
            //ClassifierFactory.ClassifierType.OneR
    ));


    // folders
    static String inputBaseFolder = "./inputFrameData/currentInput";
    static String outputBaseFolder = "./outputResults/";
    static String outputFolderTag = "";

    // sensor permutations to use during evaluation
    // HH stands for Head + HandControllers
    static SensorPermutationUsage sensorPermutationUsage = SensorPermutationUsage.HH_plus_Trackers_upTo;
    static int maximumNumberOfTrackers = 1;

    public enum SensorPermutationUsage {
        All, Only_HH, HH_plus_Trackers_upTo
    }


    // input frame data
    static double windowSizeForFrameDataToFeatureConversion = 5;
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

    public static SensorPermutationUsage getSensorPermutationUsage() {
        return sensorPermutationUsage;
    }

    public static int getMaximumNumberOfTrackers() {
        return maximumNumberOfTrackers;
    }

    public static boolean isWriteAllModelsToFolder() {
        return writeAllModelsToFolder;
    }
}
