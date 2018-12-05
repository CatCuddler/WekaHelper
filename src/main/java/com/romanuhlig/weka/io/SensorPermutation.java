package com.romanuhlig.weka.io;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SensorPermutation {

    ArrayList<String> includedSensors;
    ArrayList<String> excludedSensors;


    public SensorPermutation(ArrayList<String> includedSensors, ArrayList<String> excludedSensors) {
        this.includedSensors = includedSensors;
        this.excludedSensors = excludedSensors;
    }

    public static ArrayList<SensorPermutation> generateAllPermutations(ArrayList<String> sensorPositions) {

        ArrayList<SensorPermutation> allPermutations = new ArrayList<>();

        collectSensorPermutationsRecursively(allPermutations, sensorPositions, new ArrayList<>(), new ArrayList<>(), 0);

        return allPermutations;
    }

    public ArrayList<String> getIncludedSensors() {
        return includedSensors;
    }

    public ArrayList<String> getExcludedSensors() {
        return excludedSensors;
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

            // otherwise, look at the current element
        } else {

            // start one new step with the string at this position
            ArrayList<String> includedSensorsWithCurrentIndex = new ArrayList<String>(currentIncludedSensors);
            includedSensorsWithCurrentIndex.add(originalSensorPositions.get(currentIndex));
            ArrayList<String> excludedSensorsWithCurrentIndex = new ArrayList<String>(currentIncludedSensors);
            collectSensorPermutationsRecursively(allPermutations, originalSensorPositions,
                    includedSensorsWithCurrentIndex, excludedSensorsWithCurrentIndex, currentIndex + 1);

            // start one new step without the string at this position
            ArrayList<String> includedSensorsWithoutCurrentIndex = new ArrayList<String>(currentIncludedSensors);
            ArrayList<String> excludedSensorsWithoutCurrentIndex = new ArrayList<String>(currentIncludedSensors);
            excludedSensorsWithoutCurrentIndex.add(originalSensorPositions.get(currentIndex));
            collectSensorPermutationsRecursively(allPermutations, originalSensorPositions,
                    includedSensorsWithoutCurrentIndex, excludedSensorsWithoutCurrentIndex, currentIndex + 1);

        }

    }


}
