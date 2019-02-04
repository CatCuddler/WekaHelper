package com.romanuhlig.weka.data;

import java.util.ArrayList;

public class OutputFeatureVector {

    ArrayList<Double> features;
    final String classValue;
    final String subject;

    public OutputFeatureVector(String subject, String classValue) {
        this.features = new ArrayList<>();
        this.subject = subject;
        this.classValue = classValue;
    }

    public ArrayList<String> getFeaturesWithoutClassValue() {

        ArrayList<String> allFeatures = new ArrayList<>(features.size());
        for (Double value : features) {
            allFeatures.add(Double.toString(value));
        }

        return allFeatures;
    }

    public String getSubject() {
        return subject;
    }

    public void addFeature(Double newFeature) {
        features.add(newFeature);
    }

    public String[] getFeaturesAndClassAsArray() {
        String[] allFeaturesWithClass = new String[features.size() + 1];

        for (int i = 0; i < features.size(); i++) {
            allFeaturesWithClass[i] = Double.toString(features.get(i));
        }
        allFeaturesWithClass[allFeaturesWithClass.length - 1] = classValue;

        return allFeaturesWithClass;
    }

    public String getClassValue() {
        return classValue;
    }
}
