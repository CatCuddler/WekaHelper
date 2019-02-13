package com.romanuhlig.weka.io;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class TrainingAndTestFilePackage implements Serializable {

    private final String trainingFilePath;
    private final String testFilePath;
    private final String subject;

    private Instances trainingDataUnfiltered;
    private Instances testDataUnfiltered;

    public TrainingAndTestFilePackage(String trainingFilePath, String testFilePath, String subject) {
        this.trainingFilePath = trainingFilePath;
        this.testFilePath = testFilePath;
        this.subject = subject;
    }

    public TrainingAndTestFilePackage(String subject) {
        this.subject = subject;
        this.testFilePath = "";
        this.trainingFilePath = "";
    }

    public String getTrainingFilePath() {
        return trainingFilePath;
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public Instances getTrainingDataUnfiltered() {

        if (trainingDataUnfiltered == null) {
            try {
                DataSource trainingSource = new DataSource(getTrainingFilePath());
                trainingDataUnfiltered = trainingSource.getDataSet();
                trainingDataUnfiltered.setClassIndex(trainingDataUnfiltered.numAttributes() - 1);
            } catch (Exception e) {
                System.err.println("could not open training file " + getTrainingFilePath());
            }
        }

        return trainingDataUnfiltered;

    }

    public Instances getTestDataUnfiltered() {
        if (testDataUnfiltered == null) {
            try {
                DataSource testSource = new DataSource(getTestFilePath());
                testDataUnfiltered = testSource.getDataSet();
                testDataUnfiltered.setClassIndex(testDataUnfiltered.numAttributes() - 1);
            } catch (Exception e) {
                System.err.println("could not open test file " + getTestFilePath());
            }
        }
        return testDataUnfiltered;
    }

    public String getSubject() {
        return subject;
    }

}
