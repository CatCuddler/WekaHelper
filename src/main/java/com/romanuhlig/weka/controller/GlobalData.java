package com.romanuhlig.weka.controller;

import java.util.List;

/**
 * Haven for data that need to be globally accessible
 *
 * @author Roman Uhlig
 */
public class GlobalData {


    private static List<String> allAvailableSensors;
    private static List<String> allActivities;

    /**
     * Get all sensors that are used within the current data set
     * @return
     */
    public static List<String> getAllAvailableSensors() {
        return allAvailableSensors;
    }

    /**
     * Set all sensors that are used within the current data set
     * @param allAvailableSensors
     */
    public static void setAllAvailableSensors(List<String> allAvailableSensors) {
        GlobalData.allAvailableSensors = allAvailableSensors;
    }

    /**
     * Get all activities that are used within the current data set
     * @return
     */
    public static List<String> getAllActivities() {
        return allActivities;
    }

    /**
     * Set all activities that are used within the current data set
     * @param allActivities
     */
    public static void setAllActivities(List<String> allActivities) {
        GlobalData.allActivities = allActivities;
    }
}
