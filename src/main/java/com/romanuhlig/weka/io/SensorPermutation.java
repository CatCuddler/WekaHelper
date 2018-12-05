package com.romanuhlig.weka.io;

import weka.core.Attribute;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SensorPermutation {

    private final ArrayList<String> includedSensors;
    private final ArrayList<String> excludedSensors;
    private final String folderStringRepresentation;


    public SensorPermutation(ArrayList<String> includedSensors, ArrayList<String> excludedSensors) {
        this.includedSensors = includedSensors;
        this.excludedSensors = excludedSensors;

        // construct file name for this permutation as "03-Sensor01-Sensor02-Sensor03"
        String newFolderStringRepresentation = new String();
        if (includedSensors.size() < 10) {
            newFolderStringRepresentation += "0";
        }
        newFolderStringRepresentation += includedSensors.size();
        for (String sensor : includedSensors) {

            newFolderStringRepresentation += "-" + sensor;
        }
        this.folderStringRepresentation = newFolderStringRepresentation;

    }

    public ArrayList<String> getIncludedSensors() {
        return includedSensors;
    }

    public ArrayList<String> getExcludedSensors() {
        return excludedSensors;
    }

    public String getFolderStringRepresentation() {
        return folderStringRepresentation;
    }

    public int getNumberOfSensors() {
        return includedSensors.size();
    }

    public boolean attributeForbidden(Attribute attribute) {

        // test whether the attribute name contains an excluded sensor name
        for (String forbiddenSensor : excludedSensors) {
            if (attribute.name().contains(forbiddenSensor)) {
                return true;
            }
        }

        // no reason to forbid attribute was found
        return false;
    }

    public boolean contains(String sensor) {
        return includedSensors.contains(sensor);
    }

    public static ArrayList<SensorPermutation> generateAllPermutations(ArrayList<String> sensorPositions) {

        ArrayList<SensorPermutation> allPermutations = new ArrayList<>();

        collectSensorPermutationsRecursively(allPermutations, sensorPositions, new ArrayList<>(), new ArrayList<>(), 0);

        return allPermutations;
    }

    private static void collectSensorPermutationsRecursively
            (ArrayList<SensorPermutation> allPermutations, ArrayList<String> originalSensorPositions,
             ArrayList<String> currentIncludedSensors, ArrayList<String> currentExcludedSensors,
             int currentIndex) {

        // add new permutation at end of recursion
        if (currentIndex >= originalSensorPositions.size()) {
            // only if at least one sensorPosition is in list
            if (!currentIncludedSensors.isEmpty())
                allPermutations.add((new SensorPermutation(currentIncludedSensors, currentExcludedSensors)));
            System.out.println("permutation sizes   " + currentIncludedSensors.size() + "   " + currentExcludedSensors.size()
                    + "   " + (currentIncludedSensors.size() + currentExcludedSensors.size()));

            // otherwise, look at the current element
        } else {

            // start one new step with the string at this position
            ArrayList<String> includedSensorsWithCurrentIndex = new ArrayList<String>(currentIncludedSensors);
            includedSensorsWithCurrentIndex.add(originalSensorPositions.get(currentIndex));
            ArrayList<String> excludedSensorsWithCurrentIndex = new ArrayList<String>(currentExcludedSensors);
            collectSensorPermutationsRecursively(allPermutations, originalSensorPositions,
                    includedSensorsWithCurrentIndex, excludedSensorsWithCurrentIndex, currentIndex + 1);

            // start one new step without the string at this position
            ArrayList<String> includedSensorsWithoutCurrentIndex = new ArrayList<String>(currentIncludedSensors);
            ArrayList<String> excludedSensorsWithoutCurrentIndex = new ArrayList<String>(currentExcludedSensors);
            excludedSensorsWithoutCurrentIndex.add(originalSensorPositions.get(currentIndex));
            collectSensorPermutationsRecursively(allPermutations, originalSensorPositions,
                    includedSensorsWithoutCurrentIndex, excludedSensorsWithoutCurrentIndex, currentIndex + 1);

        }

    }


}
