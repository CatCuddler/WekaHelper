package com.romanuhlig.weka.data;

import java.util.ArrayList;

public class OutputFeatureVector {

    ArrayList<String> features;
    String subject;

    public OutputFeatureVector(String subject, ArrayList<String> features) {
        this.features = features;
        this.subject = subject;
    }

    public OutputFeatureVector(String subject) {
        this.features = new ArrayList<>();
        this.subject = subject;
    }

    public ArrayList<String> getFeatures() {
        return features;
    }

    public String getSubject() {
        return subject;
    }

    public void addFeature(String newFeature) {
        features.add(newFeature);
    }

    public String[] getFeaturesAsArray() {
        return features.toArray(new String[features.size()]);
    }

    public String getClassValue() {
        return features.get(features.size() - 1);
    }
}
