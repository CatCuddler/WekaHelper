package com.romanuhlig.weka.lifeClassification;

import com.romanuhlig.weka.controller.TestBenchSettings;
import com.romanuhlig.weka.data.FrameData;
import com.romanuhlig.weka.data.FrameDataReader;
import com.romanuhlig.weka.data.FrameDataSet;
import com.romanuhlig.weka.data.OutputFeatureVector;
import org.apache.commons.lang3.time.StopWatch;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CppDataClassifier {

    double timeOfLastFrameData = 0;
    double timeOfLastClassification = 0;

    final double timeBetweenClassifications = 1.2;

    private FrameDataSet frameDataSet;

    final String pathToClassifier;
    Classifier classifier;

    ArrayList<String> instanceHeader;

    // data required for any classifier and any run
    ArrayList<String> classVal;
    Attribute classAttribute;
    ArrayList<Attribute> attributes;
    ExecutorService threadPool = Executors.newCachedThreadPool();

    public CppDataClassifier() {
        frameDataSet = new FrameDataSet("", "");

        // load pre-trained weka model
        pathToClassifier = getFilePathToJar() + "/wekaModel.model";
        try {
            classifier = (Classifier) weka.core.SerializationHelper.read(pathToClassifier);
            outputClassifierResultToCpp("weka model successfully loaded from file");
        } catch (Exception e) {
            outputClassifierResultToCpp("unable to load weka classifier from   " + pathToClassifier + "   !!!");
        }


        classVal = new ArrayList<>();
        // add class attribute
        classVal.add("jogging");
        classVal.add("kick");
        classVal.add("kickPunch");
        classVal.add("lateralBounding");
        classVal.add("lunges");
        classVal.add("punch");
        classVal.add("sitting");
        classVal.add("squats");
        classVal.add("standing");
        classVal.add("walking");
        classAttribute = new Attribute("activity", classVal);
    }

    public void addFrameData(String sensorPosition, String subject, String activity,
                             double calPosX, double calPosY, double calPosZ,
                             double calRotX, double calRotY, double calRotZ, double calRotW,
                             double angVelX, double angVelY, double angVelZ,
                             double linVelX, double linVelY, double linVelZ,
                             double scale, double time) {


//        if (frameDataSet.getAllSensorLists().size() > 0) {
//            if (frameDataSet.getAllSensorLists().get(0) != null) {
//                outputClassifierResultToCpp("" + frameDataSet.getAllSensorLists().get(0).size());
//            }
//        }

//        outputClassifierResultToCpp("" + time);

        // only consider a new classification once a new frame has started
        // that is, not while different sensors for the current frame might still be incoming
        if ((time != timeOfLastFrameData) && (time - timeOfLastClassification > timeBetweenClassifications)) {

//            outputClassifierResultToCpp("enough time passed");

            // only consider a new classification if enough data was collected
            if (frameDataSet.enoughDataForWindowSize(TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion())) {

//                outputClassifierResultToCpp("enough data collected, trying classification");

                timeOfLastClassification = time;

//                outputClassifierResultToCpp("a");


//                StopWatch fullProcessStopWatch = new StopWatch();
//                fullProcessStopWatch.start();

                FrameDataSet frameDataSetForWindow =
                        frameDataSet.getLatestDataForWindowSizeAndRemoveEarlierData(
                                TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion());


//                outputClassifierResultToCpp("b");


                // run the feature computation and execution within a separate thread, to avoid slowdowns
                Runnable recognitionThread = new Runnable() {
                    public void run() {


//                        outputClassifierResultToCpp("starting classification thread");

                        // create features
                        OutputFeatureVector features = FrameDataReader.getFeaturesForFrameDataSet(frameDataSetForWindow);

                        // create header and attribute types
                        if (instanceHeader == null) {
                            instanceHeader = FrameDataReader.getHeaderForFrameDataSet(frameDataSetForWindow, false, false);

                            //                        outputClassifierResultToCpp("c");

                            // create weka data instance from features
                            // ---------------- Instance creation -------------------------
                            // Create empty instance with three attribute values
                            int numberOfFeatures = instanceHeader.size();
                            attributes = new ArrayList<>(numberOfFeatures);
                            for (int i = 0; i < numberOfFeatures; i++) {
                                Attribute newAttribute = new Attribute(instanceHeader.get(i));
                                attributes.add(newAttribute);
//                              outputClassifierResultToCpp("attribute added:   " + header.get(i));
                            }
                            attributes.add(classAttribute);


                        }


                        int numberOfFeatures = instanceHeader.size();
                        Instance instance = new DenseInstance(numberOfFeatures);
//                        outputClassifierResultToCpp("d");

                        // create an Instances Object to house the instance (as its single entry)
                        Instances instances = new Instances("LifeInstances", attributes, 0);
                        instances.setClass(classAttribute);

                        // make them known to each other
                        instances.add(instance);
                        instance.setDataset(instances);

//                        outputClassifierResultToCpp("e");

                        ArrayList<Double> featureValues = features.getFeaturesWithoutClassAndSubject();

                        // fill the attribute values for the instance
                        for (int i = 0; i < numberOfFeatures; i++) {
//                    outputClassifierResultToCpp("trying to set value:   ");
//                    outputClassifierResultToCpp("" + features.getFeaturesWithoutClassAndSubject().get(i));
//                    outputClassifierResultToCpp("" + attributes.get(i).name());


                            try {
//                                instance.setValue(attributes.get(i), featureValues.get(i));
                                instance.setValue(i, featureValues.get(i));
                                // outputClassifierResultToCpp("success setting value:   " + i + "   " + attributes.get(i).name());
                            } catch (Exception e) {
                                outputClassifierResultToCpp(e.getLocalizedMessage());
                                outputClassifierResultToCpp("error setting value:   " + i + "   " + attributes.get(i).name());
                            }
                        }


//                        outputClassifierResultToCpp("f");


////              output state for debugging
//                outputClassifierResultToCpp("final attributes in instance:");
//                for (int i = 0; i < instance.numAttributes(); i++) {
//                    outputClassifierResultToCpp(instance.attribute(i).name() + "   " + instance.value(i));
//                }

//                        outputClassifierResultToCpp("gonna start actual classification");


                        // predict the class
                        try {
                            double prediction = classifier.classifyInstance(instance);
                            // outputClassifierResultToCpp("prediction:   " + prediction + "   " + classVal.get((int) prediction));
                            outputClassifierResultToCpp(classVal.get((int) prediction));
                        } catch (Exception e) {
                            outputClassifierResultToCpp(e.getLocalizedMessage());
                            outputClassifierResultToCpp("classification failed !!!");
                        }

//                        outputClassifierResultToCpp("g");

//                outputClassifierResultToCpp("number of sensor lists = " + frameDataSetForWindow.getAllSensorLists().size());
//                outputClassifierResultToCpp("first list data = " + frameDataSetForWindow.getAllSensorLists().get(0).size());
//                outputClassifierResultToCpp("classifier Result = " + frameDataSet.getAllSensorLists().get(0).size());
//                outputClassifierResultToCpp(sensorPosition);

//                        fullProcessStopWatch.stop();
//                        outputClassifierResultToCpp("total time:   " + fullProcessStopWatch.getTime(TimeUnit.MILLISECONDS));
                    }

                };

                threadPool.execute(recognitionThread);

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
