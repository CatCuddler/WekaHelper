package com.romanuhlig.weka.data;

/**
 * Represents a single Feature
 * @author Roman Uhlig
 */
public class Feature implements Comparable<Feature> {
    private String name;
    private double value;

    /**
     * Constructs a feature with the given name and value
     * @param name
     * @param value
     */
    public Feature(String name, double value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Compares two features based on the alphabetical order of their names
     * @param otherFeature
     * @return
     */
    public int compareTo(Feature otherFeature) {
        return this.name.compareTo(otherFeature.name);
    }

    /**
     * The feature's name
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * The feature's value
     * @return
     */
    public double getValue(){
        return value;
    }


}