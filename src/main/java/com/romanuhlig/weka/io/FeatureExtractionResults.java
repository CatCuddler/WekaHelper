package com.romanuhlig.weka.io;

import java.util.ArrayList;

public class FeatureExtractionResults {

    private final ArrayList<TrainingAndTestFilePackage> trainingAndTestFilePackages = new ArrayList<>();
    private final ArrayList<String> sensorPositions;

    public FeatureExtractionResults(ArrayList<String> sensorPositions) {
        this.sensorPositions = sensorPositions;
    }

    public void addTrainingAndTestFilePackage(TrainingAndTestFilePackage newPackage) {
        trainingAndTestFilePackages.add(newPackage);
    }

    public ArrayList<TrainingAndTestFilePackage> getTrainingAndTestFilePackages() {
        return trainingAndTestFilePackages;
    }

    public ArrayList<String> getSensorPositions() {
        return sensorPositions;
    }
}