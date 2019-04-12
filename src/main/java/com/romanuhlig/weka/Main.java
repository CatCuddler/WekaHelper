package com.romanuhlig.weka;

import com.romanuhlig.weka.controller.TestBench;

/**
 * Starts the feature extraction and classification process
 *
 * @author Roman Uhlig
 */
public class Main {

    public static void main(String[] args) {

        // use the TestBench as the central controller
        TestBench testBench = new TestBench();
        System.out.println("running test bench");
        testBench.run();
    }
}
