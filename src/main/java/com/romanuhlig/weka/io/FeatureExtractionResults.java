package com.romanuhlig.weka.io;

import java.util.ArrayList;

public class FeatureExtractionResults {

    private final ArrayList<TrainingAndTestFilePackage> trainingAndTestFilePackages = new ArrayList<>();
    private final ArrayList<String> allSensorPositions;
    private TrainingAndTestFilePackage completeFeatureSet;

    public FeatureExtractionResults(ArrayList<String> sensorPositions) {
        this.allSensorPositions = sensorPositions;
    }

    public void addTrainingAndTestFilePackage(TrainingAndTestFilePackage newPackage) {
        trainingAndTestFilePackages.add(newPackage);
    }

    public ArrayList<TrainingAndTestFilePackage> getIndividualTrainingAndTestFilePackages() {
        return trainingAndTestFilePackages;
    }


    public ArrayList<String> getAllSensorPositions() {
        return allSensorPositions;
    }

    public TrainingAndTestFilePackage getCompleteFeatureSet() {
        return completeFeatureSet;
    }

    public void setCompleteFeatureSet(TrainingAndTestFilePackage completeFeatureSet) {
        this.completeFeatureSet = completeFeatureSet;
    }
}
