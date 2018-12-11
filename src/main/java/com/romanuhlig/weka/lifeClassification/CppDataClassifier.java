package com.romanuhlig.weka.lifeClassification;

import com.romanuhlig.weka.controller.TestBenchSettings;
import com.romanuhlig.weka.data.FrameData;
import com.romanuhlig.weka.data.FrameDataSet;
import weka.classifiers.Classifier;

import java.io.File;
import java.net.URLDecoder;

public class CppDataClassifier {

    double timeOfLastFrameData = 0;
    double timeOfLastClassification = 0;

    final double timeBetweenClassifications = 1;

    private FrameDataSet frameDataSet;

    final String pathToClassifier;
    Classifier classifier;


    public CppDataClassifier() {
        frameDataSet = new FrameDataSet("", "");

        // load pre-trained weka model
        pathToClassifier = getFilePathToJar() + "/wekaModel.model";
        try {
            classifier = (Classifier) weka.core.SerializationHelper.read(pathToClassifier);
        } catch (Exception e) {
            outputClassifierResultToCpp("unable to load weka classifier from   " + pathToClassifier + "   !!!");
        }
    }

    public void addFrameData(String sensorPosition, String subject, String activity,
                             double calPosX, double calPosY, double calPosZ,
                             double calRotX, double calRotY, double calRotZ, double calRotW,
                             double angVelX, double angVelY, double angVelZ,
                             double linVelX, double linVelY, double linVelZ,
                             double scale, double time) {


//        if (frameDataSet.getAllFrameData().size() > 0) {
//            if (frameDataSet.getAllFrameData().get(0) != null) {
//                outputClassifierResultToCpp("" + frameDataSet.getAllFrameData().get(0).size());
//            }
//        }

//        outputClassifierResultToCpp("" + time);

        // only consider a new classification once a new frame has started
        // that is, not while different sensors for the current frame might still be incoming
        if ((time != timeOfLastFrameData) && (time - timeOfLastClassification > timeBetweenClassifications)) {

            // only consider a new classification if enough data was collected
            if (frameDataSet.enoughDataForWindowSize(TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion())) {

                timeOfLastClassification = time;

                FrameDataSet frameDataSetForWindow =
                        frameDataSet.getLatestDataForWindowSizeAndRemoveEarlierData(
                                TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion());

                outputClassifierResultToCpp("number of sensor lists = " + frameDataSetForWindow.getAllFrameData().size());
                outputClassifierResultToCpp("first list data = " + frameDataSetForWindow.getAllFrameData().get(0).size());
//                outputClassifierResultToCpp("classifier Result = " + frameDataSet.getAllFrameData().get(0).size());
//                outputClassifierResultToCpp(sensorPosition);

            }

        }


        // create and remember new frame data
        FrameData newFrameData = new FrameData(
                sensorPosition, subject, activity,
                calPosX, calPosY, calPosZ,
                calRotX, calRotY, calRotZ, calRotW,
                angVelX, angVelY, angVelZ,
                linVelX, linVelY, linVelZ,
                scale, time);
        frameDataSet.addFrameData(newFrameData);

        timeOfLastFrameData = newFrameData.getTime();


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

    String getFilePathToJar() {

        String path = CppDataClassifier.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = new File(path).getParentFile().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            return decodedPath;
        } catch (Exception e) {
            outputClassifierResultToCpp("Determining jar folder for weka model loading failed !!!");
            return "Determining jar folder for weka model loading failed !!!";
        }
    }

    // to be supplied in C++ through JNI
    public native void outputClassifierResultToCpp(String result);


}
