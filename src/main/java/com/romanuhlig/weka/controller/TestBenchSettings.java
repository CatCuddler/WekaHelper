package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassifierFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class TestBenchSettings {


    // algorithms to test
    static ArrayList<ClassifierFactory.ClassifierType> classifiersToUse = new ArrayList<>(Arrays.asList(
            ClassifierFactory.ClassifierType.RandomForest,
            ClassifierFactory.ClassifierType.NaiveBayes,
            ClassifierFactory.ClassifierType.SMO
    ));


    // folders
    static String inputBaseFolder = "./inputFrameData/";
    static String outputBaseFolder = "./outputResults/";
    static String outputFolderTag = "";


    // input frame data
    static double windowSizeForFrameDataToFeatureConversion = 1;
    static double windowSpacingForFrameDataToFeatureConversion = 0.1;


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
}
