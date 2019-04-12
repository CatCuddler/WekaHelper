package com.romanuhlig.weka.io;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.Serializable;

/**
 * Stores the locations of the feature files for a single subject
 *
 * @author Roman Uhlig
 */
public class SubjectTrainingAndTestFilePackage implements Serializable {

    // file path and subject identifier
    private final String trainingFilePath;
    private final String testFilePath;
    private final String subject;

    // cached data, to avoid reloading the files
    private Instances trainingDataUnfiltered;
    private Instances testDataUnfiltered;

    /**
     * Create a SubjectTrainingAndTestFilePackage for the given files and subject
     *
     * @param trainingFilePath
     * @param testFilePath
     * @param subject
     */
    public SubjectTrainingAndTestFilePackage(String trainingFilePath, String testFilePath, String subject) {
        this.trainingFilePath = trainingFilePath;
        this.testFilePath = testFilePath;
        this.subject = subject;
    }

    /**
     * Create a SubjectTrainingAndTestFilePackage for the given subject, with missing training and test files
     * <p>
     * Used as placeholder, when a single feature file is used instead of individual files for each subject
     *
     * @param subject
     */
    public SubjectTrainingAndTestFilePackage(String subject) {
        this.subject = subject;
        this.testFilePath = "";
        this.trainingFilePath = "";
    }

    /**
     * The path of a feature file that contains all but the subject's data
     *
     * @return
     */
    public String getTrainingFilePath() {
        return trainingFilePath;
    }

    /**
     * The path of a feature file that contains only the subject's data
     *
     * @return
     */
    public String getTestFilePath() {
        return testFilePath;
    }

    /**
     * Load the training feature file for this subject
     * <p>
     * The training features contain all but the subject's data
     *
     * @return
     */
    public Instances getTrainingDataUnfiltered() {

        // load and cache the features
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

    /**
     * Load the test feature file for this subject
     * <p>
     * The test features contain only the subject's data
     *
     * @return
     */
    public Instances getTestDataUnfiltered() {

        // load and cache the features
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

    /**
     * The subject for the included training and test data
     *
     * @return
     */
    public String getSubject() {
        return subject;
    }

}
