package com.romanuhlig.weka.lifeClassification;

import com.romanuhlig.weka.controller.TestBenchSettings;
import com.romanuhlig.weka.frameToFeature.FrameData;
import com.romanuhlig.weka.frameToFeature.FeatureExtractor;
import com.romanuhlig.weka.frameToFeature.FrameDataSet;
import com.romanuhlig.weka.frameToFeature.FeatureVector;
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

/**
 * Responsible for communicating with a C++ environment through the JNI interface
 * <p>
 * Loads a pre-trained model to regularly predict the class of incoming sensor readings
 *
 * @author Roman Uhlig
 */
public class CppDataClassifier {

    // time when the last sensor reading was registered, in seconds
    private double timeOfLastFrameData = 0;

    // time of the last prediction attempt, in seconds
    private double timeOfLastClassification = 0;
    // pause between two prediction attempts, in seconds
    private final double timeBetweenClassifications = 1.2;

    // collector for incoming sensor readings
    private FrameDataSet frameDataSet;

    // pre-trained model
    private final String pathToClassifier;
    private Classifier classifier;

    // the header for all instances, generated the first time it is needed
    private ArrayList<String> instanceHeader;

    // data required for any classifier and any run
    private ArrayList<String> classVal;
    private Attribute classAttribute;
    private ArrayList<Attribute> attributes;
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Creates a new CppDataClassifier
     * <p>
     * Requires a wekaModel.model file within the same folder as the jar file
     */
    public CppDataClassifier() {

        frameDataSet = new FrameDataSet("", "");

        // load pre-trained weka model from the same folder as the jar file
        pathToClassifier = getFolderPathToJar() + "/currentModel.model";
        try {
            classifier = (Classifier) weka.core.SerializationHelper.read(pathToClassifier);
            outputClassifierResultToCpp("weka model successfully loaded from file " + pathToClassifier);
        } catch (Exception e) {
            outputClassifierResultToCpp("unable to load weka classifier from " + pathToClassifier + "!!!");
        }

        // initialize class names
        classVal = new ArrayList<>();
        /*classVal.add("jogging");
        classVal.add("kick");
        classVal.add("kickPunch");
        classVal.add("lateralBounding");
        classVal.add("lunges");
        classVal.add("punch");
        classVal.add("sitting");
        classVal.add("squats");
        classVal.add("standing");
        classVal.add("walking");*/
        classVal.add("Krieger_1");
        classVal.add("Krieger_2");
        classVal.add("Krieger_3");
        classVal.add("sitting");
        classVal.add("standing");
        classVal.add("walking");
        classAttribute = new Attribute("activity", classVal);
    }

    /**
     * Add a new sensor reading, and predict the class of recent readings if enough time has passed
     * <p>
     * Meant to be called by a C++ environment through JNI
     *
     * @param sensorPosition
     * @param subject
     * @param activity
     * @param calPosX
     * @param calPosY
     * @param calPosZ
     * @param calRotX
     * @param calRotY
     * @param calRotZ
     * @param calRotW
     * @param angVelX
     * @param angVelY
     * @param angVelZ
     * @param linVelX
     * @param linVelY
     * @param linVelZ
     * @param scale
     * @param time
     */
    public void addFrameData(String sensorPosition, String subject, String activity,
                             double calPosX, double calPosY, double calPosZ,
                             double calRotX, double calRotY, double calRotZ, double calRotW,
                             double angVelX, double angVelY, double angVelZ,
                             double linVelX, double linVelY, double linVelZ,
                             double scale, double time) {

        // only consider a new classification once a new frame has started
        // that is, not while different sensors for the current frame might still be incoming
        /*if ((time != timeOfLastFrameData) && (time - timeOfLastClassification > timeBetweenClassifications)) {

            // if enough data was collected, start a new classification attempt
            if (frameDataSet.enoughDataForWindowSize(TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion())) {

                timeOfLastClassification = time;

                // copy the data required for a classification attempt, and get rid of older data
                FrameDataSet frameDataSetForWindow =
                        frameDataSet.getLatestDataForWindowSizeAndRemoveEarlierData(
                                TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion());

                // run the feature computation and execution within a separate thread, to avoid slowdowns
                Runnable recognitionThread = new Runnable() {
                    public void run() {

                        // create features
                        FeatureVector features = FeatureExtractor.getFeaturesForFrameDataSet(frameDataSetForWindow);

                        // if this is the first classification attempt, create header and attribute types
                        if (instanceHeader == null) {

                            // header
                            instanceHeader = FeatureExtractor.getFeatureHeaderForFrameDataSet(
                                    frameDataSetForWindow, false, false);

                            // attribute types
                            int numberOfFeatures = instanceHeader.size();
                            attributes = new ArrayList<>(numberOfFeatures);
                            for (int i = 0; i < numberOfFeatures; i++) {
                                Attribute newAttribute = new Attribute(instanceHeader.get(i));
                                attributes.add(newAttribute);
                            }
                            attributes.add(classAttribute);
                        }

                        // create a new instance
                        int numberOfFeatures = instanceHeader.size();
                        Instance instance = new DenseInstance(numberOfFeatures);

                        // create an InstanceS Object to house the Instance (as its single entry)
                        Instances instances = new Instances("LifeInstances", attributes, 0);
                        instances.setClass(classAttribute);

                        // make them known to each other
                        instances.add(instance);
                        instance.setDataset(instances);

                        // create the feature values for the instance
                        ArrayList<Double> featureValues = features.getFeaturesWithoutClassAndSubject();

                        // fill the attribute values for the instance with the feature values
                        for (int i = 0; i < numberOfFeatures; i++) {
                            try {
                                instance.setValue(i, featureValues.get(i));
                            } catch (Exception e) {
                                outputClassifierResultToCpp(e.getLocalizedMessage());
                                outputClassifierResultToCpp(
                                        "error setting value:   " + i + "   " + attributes.get(i).name());
                            }
                        }

                        // predict the class of the created instance
                        try {
                            double prediction = classifier.classifyInstance(instance);
                            outputClassifierResultToCpp(classVal.get((int) prediction));
                        } catch (Exception e) {
                            outputClassifierResultToCpp(e.getLocalizedMessage());
                            outputClassifierResultToCpp("classification failed !!!");
                        }
                    }
                };

                threadPool.execute(recognitionThread);
            }
        }*/

        // create and remember new frame data
        FrameData newFrameData = new FrameData(
                sensorPosition, subject, activity,
                calPosX, calPosY, calPosZ,
                calRotX, calRotY, calRotZ, calRotW,
                angVelX, angVelY, angVelZ,
                linVelX, linVelY, linVelZ,
                scale, time);
        frameDataSet.addFrameData(newFrameData);

        //timeOfLastFrameData = newFrameData.getTime();
    }

    public void recognizeLastMovement() {
        // copy the data required for a classification attempt, and get rid of older data
        FrameDataSet frameDataSetForWindow =
                frameDataSet.getLatestDataForWindowSizeAndRemoveEarlierData(
                        TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion());

        // run the feature computation and execution within a separate thread, to avoid slowdowns
        Runnable recognitionThread = new Runnable() {
            public void run() {

                // create features
                FeatureVector features = FeatureExtractor.getFeaturesForFrameDataSet(frameDataSetForWindow);

                // if this is the first classification attempt, create header and attribute types
                if (instanceHeader == null) {

                    // header
                    instanceHeader = FeatureExtractor.getFeatureHeaderForFrameDataSet(
                            frameDataSetForWindow, false, false);

                    // attribute types
                    int numberOfFeatures = instanceHeader.size();
                    attributes = new ArrayList<>(numberOfFeatures);
                    for (int i = 0; i < numberOfFeatures; i++) {
                        Attribute newAttribute = new Attribute(instanceHeader.get(i));
                        attributes.add(newAttribute);
                    }
                    attributes.add(classAttribute);
                }

                // create a new instance
                int numberOfFeatures = instanceHeader.size();
                Instance instance = new DenseInstance(numberOfFeatures);

                // create an InstanceS Object to house the Instance (as its single entry)
                Instances instances = new Instances("LifeInstances", attributes, 0);
                instances.setClass(classAttribute);

                // make them known to each other
                instances.add(instance);
                instance.setDataset(instances);

                // create the feature values for the instance
                ArrayList<Double> featureValues = features.getFeaturesWithoutClassAndSubject();

                // fill the attribute values for the instance with the feature values
                for (int i = 0; i < numberOfFeatures; i++) {
                    try {
                        instance.setValue(i, featureValues.get(i));
                    } catch (Exception e) {
                        outputClassifierResultToCpp(e.getLocalizedMessage());
                        outputClassifierResultToCpp(
                                "error setting value:   " + i + "   " + attributes.get(i).name());
                    }
                }

                // predict the class of the created instance
                try {
                    double prediction = classifier.classifyInstance(instance);
                    outputClassifierResultToCpp(classVal.get((int) prediction));
                } catch (Exception e) {
                    outputClassifierResultToCpp(e.getLocalizedMessage());
                    outputClassifierResultToCpp("classification failed !!!");
                }
            }
        };
        threadPool.execute(recognitionThread);
    }

    /**
     * Determine the folder path of the jar file containing this class
     *
     * @return
     */
    String getFolderPathToJar() {

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

    /**
     * A callback function to output results to a C++ environment through the JNI interface
     * <p>
     * Meant to be set by the C++ environment
     *
     * @param result
     */
    public native void outputClassifierResultToCpp(String result);
}
