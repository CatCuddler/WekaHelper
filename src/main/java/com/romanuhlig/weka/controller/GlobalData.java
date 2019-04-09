package com.romanuhlig.weka.controller;

import java.util.List;

/**
 * Haven for data that need to be globally accessible
 *
 * @author Roman Uhlig
 */
public class GlobalData {


    static List<String> allAvailableSensors;

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
}
