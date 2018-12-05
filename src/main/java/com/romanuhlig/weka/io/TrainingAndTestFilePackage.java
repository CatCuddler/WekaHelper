package com.romanuhlig.weka.io;

import java.io.File;
import java.util.ArrayList;

public class TrainingAndTestFilePackage {

    private final String trainingFilePath;
    private final String testFilePath;
    private final String subject;

    public TrainingAndTestFilePackage(String trainingFilePath, String testFilePath, String subject) {
        this.trainingFilePath = trainingFilePath;
        this.testFilePath = testFilePath;
        this.subject = subject;
    }

    public String getTrainingFilePath() {
        return trainingFilePath;
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public String getSubject() {
        return subject;
    }

}
