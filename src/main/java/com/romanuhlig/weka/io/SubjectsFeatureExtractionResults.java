package com.romanuhlig.weka.io;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Packages the data created during the feature extraction process for all subjects
 *
 * @author Roman Uhlig
 */
public class SubjectsFeatureExtractionResults implements Serializable {

    // the feature file location if a single file was created (for all subjects, see TestBenchSettings)
    private SubjectTrainingAndTestFilePackage completeFeatureSet;
    // the feature file locations if individual files were created (no longer used aside from debugging)
    private final ArrayList<SubjectTrainingAndTestFilePackage> subjectTrainingAndTestFilePackages = new ArrayList<>();

    private final ArrayList<String> allSensorPositions;

    /**
     * Create feature extraction results for the given sensor positions
     * <p>
     * At least one training and test file package needs to be added or set for later usage
     *
     * @param sensorPositions
     */
    public SubjectsFeatureExtractionResults(ArrayList<String> sensorPositions) {
        this.allSensorPositions = sensorPositions;
    }

    /**
     * Add a new training and test file package for a single subject
     *
     * @param newPackage
     */
    public void addTrainingAndTestFilePackage(SubjectTrainingAndTestFilePackage newPackage) {
        subjectTrainingAndTestFilePackages.add(newPackage);
    }

    /**
     * Get multiple training and test file packages used for individual subjects
     * <p>
     * See TestBenchSettings to change whether a single file or multiple files are used
     *
     * @return
     */
    public ArrayList<SubjectTrainingAndTestFilePackage> getSubjectTrainingAndTestFilePackages() {
        return subjectTrainingAndTestFilePackages;
    }

    /**
     * The sensor positions used to create the source data of the included features
     *
     * @return
     */
    public ArrayList<String> getAllSensorPositions() {
        return allSensorPositions;
    }

    /**
     * All features for all subjects as a single file
     * <p>
     * See TestBenchSettings to change whether a single file or multiple files are used
     *
     * @return
     */
    public SubjectTrainingAndTestFilePackage getCompleteFeatureSet() {
        return completeFeatureSet;
    }

    /**
     * Set a single file in which all features for all subjects are saved
     *
     * @param completeFeatureSet
     */
    public void setCompleteFeatureSet(SubjectTrainingAndTestFilePackage completeFeatureSet) {
        this.completeFeatureSet = completeFeatureSet;
    }
}
