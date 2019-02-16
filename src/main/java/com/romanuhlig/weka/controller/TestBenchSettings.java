package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassifierFactory;
import com.romanuhlig.weka.classification.ClassifierFactory.ClassifierType;
import com.romanuhlig.weka.io.SensorPermutation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


public class TestBenchSettings {


    private static boolean useExistingFeatureFile = true;

    // value scaling required by some classifiers
    private static double scaleAllFeaturesBy = 1;

    private static String[][] onlyAllowSensorPermutations = new String[][]{
            {"rForeArm", "lLeg"}
//            ,
//            {"rForeArm", "rLeg"}
//            ,
//            {"rArm", "lLeg"}
//            ,
//            {"rArm", "rLeg"}
//            ,
//            {"lForeArm", "lLeg"}
//            ,
//            {"lForeArm", "rLeg"}
    };

    // feature types to disallow
    private static ArrayList<FeatureTag> forbiddenFeatureTags = new ArrayList<>(Arrays.asList(
//            FeatureTag.Angular
//            FeatureTag.SubjectOrientationRelevant
//            FeatureTag.DualSensorCombination
    ));

    // sensor permutations to use during evaluation
    private static SensorUsage sensorUsageHMD = SensorUsage.CannotInclude;
    private static SensorUsage sensorUsageHandControllers = SensorUsage.CannotInclude;
    private static boolean allowSingleHandController = false;
    private static int minimumNumberOfTrackers = -111;
    private static int maximumNumberOfTrackers = 2;
    private static int minimumNumberOfSensors = -111;
    private static int maximumNumberOfSensors = -111;

    // input frame data
    private static double windowSizeForFrameDataToFeatureConversion = 5;
    private static double windowSpacingForFrameDataToFeatureConversion = 1;


    // algorithms to test
    private static ArrayList<ClassifierFactory.ClassifierType> classifiersToUse = new ArrayList<>(Arrays.asList(
//            ClassifierType.J48
//            ,
//            ClassifierType.RandomForest,
//            ,
//            ClassifierType.NaiveBayes
//            ,
            ClassifierType.SMO
//            ,
//            ClassifierType.OneR
//            ,
//            ClassifierType.ZeroR
//            ,
//            ClassifierType.LMT                      // slow (345 / 6p), unsuited for brute forcing all permutations
//            , // results average, all < 1.0
//            ClassifierType.JRip
//            , // all NAN or 0, not fast (ca. 2h / 6p)
//            ClassifierType.SimpleLogistic
//            , // all with values, but none great and none 1, not fast (ca. 2h / 6p)
//            ClassifierType.VotedPerceptron        // (originally for binary class, adapted via multiclass classifier)
//            , // terrible results (all < .4 average, NAN min), slow (5h / 6p)
//            ClassifierType.SGD                    // (originally for binary class, adapted via multiclass classifier)
//            , // 3h / 6p, results .8-.95
//            ClassifierType.Logistic               // preeeetty slow
//            ,
//            ClassifierType.REPTree
//            ,
//            ClassifierType.RandomTree
//            ,
//            ClassifierType.IBk                    // lazy -> slow + needs to keep data -> not viable for actual usage
//            ,
//            ClassifierType.KStar                  // lazy -> slow + needs to keep data -> not viable for actual usage
//            ,
//            ClassifierType.MultilayerPerceptron   // very, very slow in training (deep neural network)
//            ,
//            ClassifierType.BayesNet               // - bin problems (too similar, scaling does not always help)
//            ,
//            ClassifierType.DecisionTable          // - bin problems (too similar, scaling does not always help)
//            ,
//            ClassifierType.LinearRegression       // - regression
//            ,
//            ClassifierType.SMOreg                 // - regression
//            ,
//            ClassifierType.GaussianProcess        // - regression
//            ,
//            ClassifierType.M5P                    // - regression
    ));


    // folders
    private static String inputBaseFolder = "./inputFrameData/currentInput";
    private static String existingFeaturesInputFolder = "./inputFrameData/existingFeatures";
    private static String outputBaseFolder = "./outputResults/";
    private static String outputFolderTag = "";


    // result output
    private static boolean writeAllModelsToFolder = true;
    private static boolean useIndividualFeatureFilesForEachSubject = false;


    public enum SensorUsage {
        MayInclude, MustInclude, CannotInclude
    }

    public enum FeatureTag {
        Angular,
        SubjectOrientationRelevant,
        DualSensorCombination
    }

    public static String summarySingleLine() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("C");
        for (ClassifierFactory.ClassifierType classifier : classifiersToUse) {
            stringBuilder.append("_");
            stringBuilder.append(classifier.toString());
        }

        stringBuilder.append("   s_" + scaleAllFeaturesBy);
        stringBuilder.append("   ws_" + windowSizeForFrameDataToFeatureConversion);
        stringBuilder.append("   wsp_" + windowSpacingForFrameDataToFeatureConversion);
        stringBuilder.append("   hmd_" + sensorUsageHMD.toString());
        stringBuilder.append("   hc_" + sensorUsageHandControllers.toString());
        stringBuilder.append("   shc_" + allowSingleHandController);
        stringBuilder.append("   t_" + minimumNumberOfTrackers + "-" + maximumNumberOfTrackers);
        stringBuilder.append("   s_" + minimumNumberOfSensors + "-" + maximumNumberOfSensors);

        return stringBuilder.toString();
    }


    public static String summaryBig() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Classifiers:   ");
        for (ClassifierFactory.ClassifierType classifier : classifiersToUse) {
            stringBuilder.append(classifier.toString());
            stringBuilder.append("   ");
        }
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("scale all features by:   " + scaleAllFeaturesBy);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("Window Size for frame data to feature conversion:   " + windowSizeForFrameDataToFeatureConversion);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("Window Spacing for frame data to feature conversion:   " + windowSpacingForFrameDataToFeatureConversion);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("sensor usage HMD:   " + sensorUsageHMD.toString());
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("sensor usage HandController:   " + sensorUsageHandControllers.toString());
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("disallow single HandController:   " + allowSingleHandController);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("maximum number Of Trackers:  " + maximumNumberOfTrackers);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("minimum number Of Trackers:  " + minimumNumberOfTrackers);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("maximum number of Sensors:   " + maximumNumberOfSensors);
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("minimum number of Sensors:   " + minimumNumberOfSensors);
        stringBuilder.append(System.lineSeparator());


        return stringBuilder.toString();
    }


    public static ArrayList<ClassifierFactory.ClassifierType> getClassifiersToUse() {
        return classifiersToUse;
    }

    public static String getInputBaseFolder() {
        return inputBaseFolder;
    }

    public static String outputBaseFolder() {
        return outputBaseFolder;
    }

    public static String getOutputFolderTag() {
        return outputFolderTag;
    }

    public static double getWindowSizeForFrameDataToFeatureConversion() {
        return windowSizeForFrameDataToFeatureConversion;
    }

    public static double getWindowSpacingForFrameDataToFeatureConversion() {
        return windowSpacingForFrameDataToFeatureConversion;
    }

    public static SensorUsage getSensorUsageHMD() {
        return sensorUsageHMD;
    }

    public static SensorUsage getSensorUsageHandControllers() {
        return sensorUsageHandControllers;
    }


    public static int getMaximumNumberOfTrackers() {
        return maximumNumberOfTrackers;
    }

    public static int getMaximumNumberOfSensors() {
        return maximumNumberOfSensors;
    }

    public static boolean allowSingleHandController() {
        return allowSingleHandController;
    }

    public static int getMinimumNumberOfTrackers() {
        return minimumNumberOfTrackers;
    }

    public static int getMinimumNumberOfSensors() {
        return minimumNumberOfSensors;
    }

    public static boolean writeAllModelsToFolder() {
        return writeAllModelsToFolder;
    }


    public static boolean featureTagsAllowed(FeatureTag... tags) {

        for (FeatureTag tag : tags) {
            if (forbiddenFeatureTags.contains(tag)) {
                return false;
            }
        }

        return true;
    }

    public static boolean useIndividualFeatureFilesForEachSubject() {
        return useIndividualFeatureFilesForEachSubject;
    }

    public static boolean isSensorCombinationBlocked(SensorPermutation sensorPermutation) {
        // do not block any combination if no specific combination was requested
        if (onlyAllowSensorPermutations.length == 0) {
            return false;
            // otherwise, search for a fitting combination
        } else {

            ArrayList<String> sensorsInPermutation = sensorPermutation.getIncludedSensors();

            for (int i = 0; i < onlyAllowSensorPermutations.length; i++) {

                String[] allowedSensorPermutation = onlyAllowSensorPermutations[i];

                if (allowedSensorPermutation.length == sensorsInPermutation.size()) {
                    HashSet<String> sensorsAllowed = new HashSet<>(Arrays.asList(allowedSensorPermutation));
                    HashSet<String> sensorsIncluded = new HashSet<>(sensorsInPermutation);
                    if (sensorsAllowed.equals(sensorsIncluded)) {
                        return false;
                    }
                }
            }
        }

        // if no fitting combination was found, this combination is blocked
        return true;

    }

    public static boolean isSensorBlocked(String sensor) {
        // do not block sensor if no specific combination was requested
        if (onlyAllowSensorPermutations.length == 0) {
            return false;
            // otherwise, search for a fitting combination
        } else {
            for (int i = 0; i < onlyAllowSensorPermutations.length; i++) {

                String[] allowedSensorPermutation = onlyAllowSensorPermutations[i];

                for (int k = 0; k < allowedSensorPermutation.length; k++) {
                    if (allowedSensorPermutation[k].equals(sensor)) {
                        return false;
                    }
                }
            }
        }

        // if no fitting combination was found, this combination is blocked
        return true;
    }

    public static boolean specificSensorCombinationRequested() {
        return onlyAllowSensorPermutations.length > 0;
    }


    public static double scaleAllFeaturesBy() {
        return scaleAllFeaturesBy;
    }


    public static boolean useExistingFeatureFile() {
        return useExistingFeatureFile;
    }

    public static String getExistingFeaturesInputFolder() {
        return existingFeaturesInputFolder;
    }

}
