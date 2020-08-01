package com.romanuhlig.weka.io;

import weka.core.Attribute;

import java.util.ArrayList;

/**
 * Represents and creates subsets of all possible sensors
 *
 * @author Roman Uhlig
 */
public class SensorSubset {

    // sensors included in this subset, and sensors that were in the source data, but are not in this subset
    private final ArrayList<String> includedSensors;
    private final ArrayList<String> excludedSensors;

    // string representations of the included sensors for logging and file output
    private final String folderStringRepresentation;
    private final String sensorListRepresentation;

    // general information about the included sensors
    private final boolean includesBothHandControllers;
    private final boolean includesAtLeastOneHandController;
    private final boolean includesHMD;
    private final int numberOfHeadControllersAndHMD;
    private final int numberOfTrackers;

    /**
     * Create a sensor subset with the given included and excluded sensors
     *
     * @param includedSensors
     * @param excludedSensors
     */
    public SensorSubset(ArrayList<String> includedSensors, ArrayList<String> excludedSensors) {

        this.includedSensors = includedSensors;
        this.excludedSensors = excludedSensors;

        // construct file name for this subset as "03-Sensor01-Sensor02-Sensor03"
        String newFolderStringRepresentation = new String();
        if (includedSensors.size() < 10) {
            newFolderStringRepresentation += "0";
        }
        newFolderStringRepresentation += includedSensors.size();
        for (String sensor : includedSensors) {
            newFolderStringRepresentation += "-" + sensor;
        }
        this.folderStringRepresentation = newFolderStringRepresentation;

        // construct sensor list representation for results as "Sensor01-Sensor02-Sensor03"
        String newSensorListRepresentation = includedSensors.get(0);
        for (int i = 1; i < includedSensors.size(); i++) {
            newSensorListRepresentation += "-" + includedSensors.get(i);
        }
        sensorListRepresentation = newSensorListRepresentation;


        // determine general information about the included sensors
        int tempNumberOfHeadControllersAndHMD = 0;
        includesAtLeastOneHandController
                = includedSensors.contains("lForeArm") || includedSensors.contains("rForeArm");
        includesBothHandControllers
                = includedSensors.contains("lForeArm") && includedSensors.contains("rForeArm");
        includesHMD
                = includedSensors.contains("head");
        if (includesHMD) {
            tempNumberOfHeadControllersAndHMD += 1;
        }
        if (includesBothHandControllers) {
            tempNumberOfHeadControllersAndHMD += 2;
        } else if (includesAtLeastOneHandController) {
            tempNumberOfHeadControllersAndHMD += 1;
        }
        numberOfHeadControllersAndHMD
                = tempNumberOfHeadControllersAndHMD;
        numberOfTrackers
                = getNumberOfSensors() - numberOfHeadControllersAndHMD;

    }

    /**
     * The sensors included in this subset
     *
     * @return
     */
    public ArrayList<String> getIncludedSensors() {
        return includedSensors;
    }

    /**
     * The sensors used in the source data, but not part of this subset
     *
     * @return
     */
    public ArrayList<String> getExcludedSensors() {
        return excludedSensors;
    }

    /**
     * A one line string representation of all sensors
     *
     * @return
     */
    public String getFolderStringRepresentation() {
        return folderStringRepresentation;
    }

    /**
     * The number of sensors included in this subset
     *
     * @return
     */
    public int getNumberOfSensors() {
        return includedSensors.size();
    }

    /**
     * Whether the given attribute is incompatible with this subset
     *
     * @param attribute
     * @return
     */
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

    /**
     * Whether this subset contains the given sensor
     *
     * @param sensor
     * @return
     */
    public boolean contains(String sensor) {
        return includedSensors.contains(sensor);
    }

    /**
     * Generate all possible, unique sensor subsets from the given list of sensor positions
     * <p>
     * Does not include the empty set
     *
     * @param sensorPositions
     * @return
     */
    public static ArrayList<SensorSubset> generateAllSubsets(ArrayList<String> sensorPositions) {

        // collect possible subsets recursively
        ArrayList<SensorSubset> allSubsets = new ArrayList<>();
        collectSensorSubsetsRecursively(allSubsets, sensorPositions, new ArrayList<>(), new ArrayList<>(), 0);

        return allSubsets;
    }

    /**
     * Collect all possible, unique sensor subsets which can be created for the given sensor positions
     * <p>
     * Does not include the empty set
     *
     * @param subsetCollector
     * @param originalSensorPositions
     * @param currentIncludedSensors
     * @param currentExcludedSensors
     * @param currentIndex
     */
    private static void collectSensorSubsetsRecursively
    (ArrayList<SensorSubset> subsetCollector, ArrayList<String> originalSensorPositions,
     ArrayList<String> currentIncludedSensors, ArrayList<String> currentExcludedSensors,
     int currentIndex) {

        // add new subset at end of recursion
        if (currentIndex >= originalSensorPositions.size()) {
            // only if at least one sensorPosition is in list
            if (!currentIncludedSensors.isEmpty()) {
                subsetCollector.add((new SensorSubset(currentIncludedSensors, currentExcludedSensors)));
            }
        } else {
            // otherwise, look at the current element
            // start one new step by adding the sensor string at this position to the subset
            ArrayList<String> includedSensorsWithCurrentIndex = new ArrayList<String>(currentIncludedSensors);
            includedSensorsWithCurrentIndex.add(originalSensorPositions.get(currentIndex));
            ArrayList<String> excludedSensorsWithCurrentIndex = new ArrayList<String>(currentExcludedSensors);
            collectSensorSubsetsRecursively(subsetCollector, originalSensorPositions,
                    includedSensorsWithCurrentIndex, excludedSensorsWithCurrentIndex, currentIndex + 1);

            // start one new step without adding the sensor string at this position to the subset
            ArrayList<String> includedSensorsWithoutCurrentIndex = new ArrayList<String>(currentIncludedSensors);
            ArrayList<String> excludedSensorsWithoutCurrentIndex = new ArrayList<String>(currentExcludedSensors);
            excludedSensorsWithoutCurrentIndex.add(originalSensorPositions.get(currentIndex));
            collectSensorSubsetsRecursively(subsetCollector, originalSensorPositions,
                    includedSensorsWithoutCurrentIndex, excludedSensorsWithoutCurrentIndex, currentIndex + 1);
        }
    }

    /**
     * A small, one line representation of all included sensors
     *
     * @return
     */
    public String getSensorListRepresentation() {
        return sensorListRepresentation;
    }

    /**
     * Whether both handcontrollers are included in this subset
     *
     * @return
     */
    public boolean includesBothHandControllers() {
        return includesBothHandControllers;
    }

    /**
     * Whether one or two handcontrollers are included in this subset
     *
     * @return
     */
    public boolean includesAtLeastOneHandController() {
        return includesAtLeastOneHandController;
    }

    /**
     * Whether the HMD is included in this subset
     *
     * @return
     */
    public boolean includesHMD() {
        return includesHMD;
    }

    /**
     * The number of trackers included in this subset
     *
     * @return
     */
    public int getNumberOfTrackers() {
        return numberOfTrackers;
    }
}