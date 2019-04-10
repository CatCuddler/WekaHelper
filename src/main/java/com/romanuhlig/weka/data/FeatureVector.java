package com.romanuhlig.weka.data;

import com.romanuhlig.weka.controller.TestBenchSettings;

import java.util.ArrayList;

/**
 * Represents the computed featureValues for a given time window
 *
 * @author Roman Uhlig
 */
public class FeatureVector {

    private final ArrayList<Double> featureValues;
    final String classValue;
    final String subject;

    /**
     * Create a new feature vector for the given subject and class
     * <p>
     * Feature values are added individually
     *
     * @param subject
     * @param classValue
     */
    public FeatureVector(String subject, String classValue) {
        this.featureValues = new ArrayList<>();
        this.subject = subject;
        this.classValue = classValue;
    }

    /**
     * Only the feature values
     *
     * @return
     */
    public ArrayList<Double> getFeaturesWithoutClassAndSubject() {
        return featureValues;
    }

    /**
     * The subject that recorded the data this feature vector was extracted from
     *
     * @return
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Add a single feature value
     * <p>
     * Adding order is relevant, and needs to stay consistent
     *
     * @param newFeature
     */
    public void addFeature(Double newFeature) {
        featureValues.add(newFeature * TestBenchSettings.scaleAllFeaturesBy());
    }

    /**
     * The feature values, followed by the subject (optional) and class value
     *
     * @return
     */
    public String[] getFeaturesWithClassAndSubject(boolean includeSubject) {

        // create longer array if subject included
        String[] allFeaturesWithClass;
        if (includeSubject) {
            allFeaturesWithClass = new String[featureValues.size() + 1];
        } else {
            allFeaturesWithClass = new String[featureValues.size() + 2];
        }

        // convert feature values to strings
        for (int i = 0; i < featureValues.size(); i++) {
            allFeaturesWithClass[i] = Double.toString(featureValues.get(i));
        }

        // add subject (optional) and class
        if (!includeSubject) {
            allFeaturesWithClass[allFeaturesWithClass.length - 1] = classValue;
        } else {
            allFeaturesWithClass[allFeaturesWithClass.length - 2] = subject;
            allFeaturesWithClass[allFeaturesWithClass.length - 1] = classValue;
        }

        return allFeaturesWithClass;
    }

    /**
     * The actual class value associated with the generated features
     *
     * @return
     */
    public String getClassValue() {
        return classValue;
    }
}
