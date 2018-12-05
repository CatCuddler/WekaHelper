package com.romanuhlig.weka.classification;

import com.opencsv.bean.CsvBindByName;
import com.romanuhlig.weka.io.SensorPermutation;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class ClassificationResult {


    @CsvBindByName
    private String classifier;
    @CsvBindByName
    private String testDataSubject;

    @CsvBindByName
    private int numberOfSensors;

    @CsvBindByName
    private double averageF1Score;
    @CsvBindByName
    private double weightedTruePositiveRate;

    public ClassificationResult(Evaluation evaluation, Classifier classifier, Instances instances, String subject,
                                SensorPermutation sensorPermutation) {
        this.classifier = classifier.getClass().getSimpleName();
        this.testDataSubject = subject;
        this.numberOfSensors = sensorPermutation.getNumberOfSensors();

        double averageF1 = 0;
        for (int i = 0; i < instances.numClasses(); i++) {
            averageF1 += evaluation.fMeasure(i);
        }
        averageF1Score = averageF1 / instances.numClasses();

        this.weightedTruePositiveRate = evaluation.weightedTruePositiveRate();
    }


    public String getClassifier() {
        return classifier;
    }

    public String getTestDataSubject() {
        return testDataSubject;
    }

    public int getNumberOfSensors() {
        return numberOfSensors;
    }

    public double getAverageF1Score() {
        return averageF1Score;
    }

    public double getWeightedTruePositiveRate() {
        return weightedTruePositiveRate;
    }
}
