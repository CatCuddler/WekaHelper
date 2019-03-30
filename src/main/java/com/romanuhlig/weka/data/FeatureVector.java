package com.romanuhlig.weka.data;

import com.romanuhlig.weka.controller.TestBenchSettings;

import java.util.ArrayList;

public class FeatureVector {

    private final ArrayList<Double> features;
    final String classValue;
    final String subject;

    public FeatureVector(String subject, String classValue) {
        this.features = new ArrayList<>();
        this.subject = subject;
        this.classValue = classValue;
    }

    public ArrayList<Double> getFeaturesWithoutClassAndSubject() {

//        ArrayList<String> allFeatures = new ArrayList<>(features.size());
//        for (Double value : features) {
//            allFeatures.add(Double.toString(value));
//        }

//        return allFeatures;
        return features;
    }

    public String getSubject() {
        return subject;
    }

    public void addFeature(Double newFeature) {
        features.add(newFeature * TestBenchSettings.scaleAllFeaturesBy());
    }

    public String[] getFeaturesAndClassAsArray() {
        String[] allFeaturesWithClass;
        if (TestBenchSettings.useIndividualFeatureFilesForEachSubject()) {
            allFeaturesWithClass = new String[features.size() + 1];
        } else {
            allFeaturesWithClass = new String[features.size() + 2];
        }

        for (int i = 0; i < features.size(); i++) {
            allFeaturesWithClass[i] = Double.toString(features.get(i));
        }

        if (TestBenchSettings.useIndividualFeatureFilesForEachSubject()) {
            allFeaturesWithClass[allFeaturesWithClass.length - 1] = classValue;
        } else {
            allFeaturesWithClass[allFeaturesWithClass.length - 2] = subject;
            allFeaturesWithClass[allFeaturesWithClass.length - 1] = classValue;
        }

        return allFeaturesWithClass;
    }

    public String getClassValue() {
        return classValue;
    }
}
