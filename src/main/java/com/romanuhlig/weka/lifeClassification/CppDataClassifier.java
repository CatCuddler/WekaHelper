package com.romanuhlig.weka.lifeClassification;

import com.romanuhlig.weka.data.FrameData;
import com.romanuhlig.weka.data.FrameDataSet;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

import java.util.ArrayList;

public class CppDataClassifier {

    private int numberOfDataPoints = 0;

    double timeBetweenCallbacks = 1;
    double timeOfLastCallback = 0;

    public CppDataClassifier() {
        //TODO: -pass on and save initial time?
    }

    public void addDataPoint(String sensorPosition, String subject, String activity,
                             double calPosX, double calPosY, double calPosZ,
                             double calRotX, double calRotY, double calRotZ, double calRotW,
                             double angVelX, double angVelY, double angVelZ,
                             double linVelX, double linVelY, double linVelZ,
                             double scale, double time) {
        numberOfDataPoints++;
        outputClassifierResultToCpp("classifier Result = " + numberOfDataPoints);

        FrameData newDataPoint = new FrameData(
                sensorPosition, subject, activity,
                calPosX, calPosY, calPosZ,
                calRotX, calRotY, calRotZ, calRotW,
                angVelX, angVelY, angVelZ,
                linVelX, linVelY, linVelZ,
                scale, time);


        outputClassifierResultToCpp(newDataPoint.getSensorPosition() + newDataPoint.getActivity() + newDataPoint.getSubject());


// ---------------- Instance creation -------------------------
//        // Create empty instance with three attribute values
//        Instance inst = new DenseInstance(3);
//        Attribute length = new Attribute("length");
//        Attribute height = new Attribute("height");
//        Attribute width = new Attribute("width");
//
//        Classifier classifier;
//
//// Set instance's values for the attributes "length", "weight", and "position"
//        inst.setValue(length, 5.3);
//        inst.setValue(height, 300);
//        inst.setValue(width, "first");
//
//// Set instance's dataset to be the dataset "race"
//        inst.setDataset(inst);
//
//// Print the instance
//        System.out.println("The instance: " + inst);
//
//        classifier.classifyInstance(inst);

    }

    // to be supplied in C++ through JNI
    public native void outputClassifierResultToCpp(String result);


}
