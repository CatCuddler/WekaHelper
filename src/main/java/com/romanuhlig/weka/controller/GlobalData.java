package com.romanuhlig.weka.controller;

import java.util.ArrayList;
import java.util.List;

public class GlobalData {


    static List<String> allAvailableSensors;


    public static List<String> getAllAvailableSensors() {
        return allAvailableSensors;
    }

    public static void setAllAvailableSensors(List<String> allAvailableSensors) {
        GlobalData.allAvailableSensors = allAvailableSensors;
    }
}
