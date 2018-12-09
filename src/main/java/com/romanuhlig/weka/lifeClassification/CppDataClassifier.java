package com.romanuhlig.weka.lifeClassification;

public class CppDataClassifier {

    private int numberOfDataPoints = 0;


    public CppDataClassifier() {
    }

    public void addDataPoint() {
        numberOfDataPoints++;
        outputClassifierResultToCpp("classifier Result = " + numberOfDataPoints);
    }

    // to be supplied in C++ through JNI
    public native void outputClassifierResultToCpp(String result);


}
