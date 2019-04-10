package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassifierFactory;
import com.romanuhlig.weka.classification.ClassifierFactory.ClassifierType;
import com.romanuhlig.weka.io.SensorSubset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Determines the settings used throughout the feature extraction, training and testing process
 *
 * @author Roman Uhlig
 */
public class TestBenchSettings {


    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    //////////////////                                //////////////////
    //////////////////            Settings            //////////////////
    //////////////////                                //////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////


    // Do not generate new features, read old file instead?
    // To read an old file, place the existing featureExtractionResults file (not the actual feature file)
    // in the existingFeaturesInputFolder specified further below
    private static boolean useExistingFeatureFile = false;

    // leave empty if you want to use the standard folder name, generated from the chosen settings
    private static String forceFolderName = "test";

    // window size and spacing for feature generation
    private static double windowSizeForFrameDataToFeatureConversion = 1;
    private static double windowSpacingForFrameDataToFeatureConversion = 1;

    // whether to include data of tested subject in training data (subject independent: None)
    private static SubjectDataInclusion subjectDataInclusion = SubjectDataInclusion.None;

    // types of features to exclude (does not apply to existing feature file, if reused)
    private static ArrayList<FeatureType> forbiddenFeatureTypes = new ArrayList<>(Arrays.asList(
//            FeatureType.Angular
//            FeatureType.SubjectOrientationRelevant
//            FeatureType.DualSensorCombination
//            FeatureType.Position
//            FeatureType.Velocity
//            FeatureType.Acceleration
    ));

    // scale all features by fixed amount (required to adapt data to some algorithms, should be left at 1 if no errors)
    private static double scaleAllFeaturesBy = 1;

    // force usage of exactly these sensor combinations, not more or less
    // if left empty, more generalized options below will be used
    private static String[][] onlyAllowSensorSubset = new String[][]{
//            /////////////////////////////   standard test set   /////////////////////////////
//            {},                                                                              // trackers only
//            {"head"},                                                                        // HMD
//            {"lHand", "rHand"},                                                              // Hands
//            {"head", "lHand", "rHand"},                                                      // HMD + Hands
//            {"head", "lHand", "rHand", "hip", "lLeg", "rLeg"},                               // HMD + base_IK + Hands
//            {"head", "lForeArm", "rForeArm", "hip", "lLeg", "rLeg"},                         // HMD + IK_2
//            {"head", "lHand", "rHand", "lForeArm", "rForeArm", "hip", "lLeg", "rLeg"},       // HMD + IK_2 + Hands


//            /////////////////////////////   arm and leg 2 tracker combinations:   /////////////////////////////
            {"rForeArm", "lLeg"}
            ,
            {"rForeArm", "rLeg"}
            ,
            {"rArm", "lLeg"}
            ,
            {"rArm", "rLeg"}
            ,
            {"lForeArm", "lLeg"}
            ,
            {"lForeArm", "rLeg"}
    };

    // Force usage of at least these sensor combinations,
    // with number of trackers (all possible combinations) added according to the rule-based settings below.
    // Only applied if the tracker-less list above is empty.
    // If left empty, more generalized options below will be used.
    private static String[][] minimumSensorSubset = new String[][]{
//            /////////////////////////////   standard test set   /////////////////////////////
//            {},                                                                              // trackers only
//            {"head"},                                                                        // HMD
//            {"lHand", "rHand"},                                                              // Hands
//            {"head", "lHand", "rHand"},                                                      // HMD + Hands
//            {"head", "lHand", "rHand", "hip", "lLeg", "rLeg"},                               // HMD + base_IK + Hands
//            {"head", "lForeArm", "rForeArm", "hip", "lLeg", "rLeg"},                         // HMD + IK_2
//            {"head", "lHand", "rHand", "lForeArm", "rForeArm", "hip", "lLeg", "rLeg"},       // HMD + IK_2 + Hands
    };


    // rule-based sensor subset selection = all subsets that follow the specified rules will be tested
    // allow / force usage of HMD and handcontrollers
    private static SensorUsage sensorUsageHMD = SensorUsage.MustInclude;
    private static SensorUsage sensorUsageHandControllers = SensorUsage.MustInclude;
    // allow usage of single handcontroller (instead of only both or none)
    private static boolean allowSingleHandController = false;
    // number of trackers
    // choose below 0 to express any number is allowed
    private static int minimumNumberOfTrackers = 2;
    private static int maximumNumberOfTrackers = 2;
    // sensor number refers to HMD, handcontrollers and all trackers added up
    // choose below 0 to express any number is allowed
    private static int minimumNumberOfSensors = -111;
    private static int maximumNumberOfSensors = -111;


    // classification algorithms to compare
    private static ArrayList<ClassifierFactory.ClassifierType> classifiersToUse = new ArrayList<>(Arrays.asList(
//            ClassifierType.SMOfeatureSelected
//            ,
//            ClassifierType.J48
//            ,
//            ClassifierType.RandomForest
//            ,
//            ClassifierType.NaiveBayes
//            ,
            ClassifierType.SMO
//            ,
//            ClassifierType.LibSVM
//            ,
//            ClassifierType.LibLinear
//            ,
//            ClassifierType.LMT
//            ,
//            ClassifierType.JRip
//            ,
//            ClassifierType.SimpleLogistic
//            ,
//            ClassifierType.VotedPerceptron
//            ,
//            ClassifierType.SGD
//            ,
//            ClassifierType.Logistic
//            ,
//            ClassifierType.REPTree
//            ,
//            ClassifierType.RandomTree
//            ,
//            ClassifierType.OneR                   // only useful for sanity checks
//            ,
//            ClassifierType.ZeroR                  // only useful for sanity checks
//            ,
//            ClassifierType.IBk                    // lazy -> slow + needs to keep data
//            ,
//            ClassifierType.KStar                  // lazy -> slow + needs to keep data
//            ,
//            ClassifierType.MultilayerPerceptron   // very, very slow in training
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


    // save all created models to their respective subject folder
    private static boolean writeAllModelsToFolder = false;

    // Write one feature file per subject, instead of writing all into one file (and separating later).
    // Much slower due to frequent need to reload data, and needs much more memory due to size of training files.
    // Good for debugging at the start, but no longer tested as well as the standard setting (false),
    // checks recommended before using it again.
    private static boolean useIndividualFeatureFilesForEachSubject = false;


    // Input and output folders
    // collected exercise data
    private static String inputBaseFolder = "./inputFrameData/currentInput";
    // place the existing featureExtractionResults file (not the actual feature file) here for feature reuse
    private static String existingFeaturesInputFolder = "./inputFrameData/existingFeatures";
    // base folder for results
    private static String outputBaseFolder = "./outputResults/";
    // optional tag to add to automatically generated output folder name
    private static String outputFolderTag = "";


    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    /////////////////                                 //////////////////
    /////////////////         End of Settings         //////////////////
    /////////////////                                 //////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////


    // whether the sensor can, must, or cannot be included in a subset
    public enum SensorUsage {
        MayInclude, MustInclude, CannotInclude
    }

    // Used to specify personal models
    // None = subject independent
    // Half = half of the data is included in training data, other half in test data (middle removed to avoid overlap)
    // HalfAndNoOtherData = like Half, but no other subject's data is included in training data
    // All = all of the subject data is included in both training and test data
    // AllAndNoOtherData = only the subject data is included in both training and test data
    // (All and AllAndNoOtherData are useful for sanity checks, or to produce models with data from all participants)
    public enum SubjectDataInclusion {
        None, Half, HalfAndNoOtherData, All, AllAndNoOtherData
    }

    // types of features that can be excluded from feature extraction process
    public enum FeatureType {
        Angular,
        SubjectOrientationRelevant,
        DualSensorCombination,
        Position,
        Velocity,
        Acceleration
    }

    /**
     * Summarizes the most important TestBench Settings within a single line
     *
     * @return
     */
    public static String getSettingsAsFolderName() {

        // can be overwritten with a manually chosen folder name
        if (forceFolderName != null && forceFolderName != "") {
            return forceFolderName;
        }

        StringBuilder stringBuilder = new StringBuilder();

        // start with all used classifiers
        stringBuilder.append("C");
        for (ClassifierFactory.ClassifierType classifier : classifiersToUse) {
            stringBuilder.append("_");
            stringBuilder.append(classifier.toString());
        }

        // add some major (abbreviated) settings
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


    /**
     * Summarizes the most important settings as a multiline text
     *
     * @return
     */
    public static String getSettingsSummary() {
        StringBuilder stringBuilder = new StringBuilder();

        // start with all used classifiers
        stringBuilder.append("Classifiers:   ");
        for (ClassifierFactory.ClassifierType classifier : classifiersToUse) {
            stringBuilder.append(classifier.toString());
            stringBuilder.append("   ");
        }
        stringBuilder.append(System.lineSeparator());

        // add some major settings
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

        stringBuilder.append("include subject data   " + subjectDataInclusion.toString());
        stringBuilder.append(System.lineSeparator());

        return stringBuilder.toString();
    }

    /**
     * The classifiers that have been chosen to be used
     *
     * @return
     */
    public static ArrayList<ClassifierFactory.ClassifierType> getClassifiersToUse() {
        return classifiersToUse;
    }

    /**
     * The base folder for the training data input
     *
     * @return
     */
    public static String getInputBaseFolder() {
        return inputBaseFolder;
    }

    /**
     * The base folder for the result data output
     *
     * @return
     */
    public static String outputBaseFolder() {
        return outputBaseFolder;
    }

    /**
     * The optional tag which should be added to the output folder name
     *
     * @return
     */
    public static String getOutputFolderTag() {
        return outputFolderTag;
    }

    /**
     * The window size for the feature extraction process
     *
     * @return
     */
    public static double getWindowSizeForFrameDataToFeatureConversion() {
        return windowSizeForFrameDataToFeatureConversion;
    }

    /**
     * The spacing between two windows for the feature extraction process
     *
     * @return
     */
    public static double getWindowSpacingForFrameDataToFeatureConversion() {
        return windowSpacingForFrameDataToFeatureConversion;
    }

    /**
     * Whether the HMD should be included in sensor subsets
     *
     * @return
     */
    public static SensorUsage getSensorUsageHMD() {
        return sensorUsageHMD;
    }

    /**
     * Whether the handcontrollers should be included in sensor subsets
     *
     * @return
     */
    public static SensorUsage getSensorUsageHandControllers() {
        return sensorUsageHandControllers;
    }

    /**
     * The maximum number of tracers to be included in sensor subsets
     *
     * @return
     */
    public static int getMaximumNumberOfTrackers() {
        return maximumNumberOfTrackers;
    }

    /**
     * The maximum number of sensors to be included in sensor subsets
     *
     * @return
     */
    public static int getMaximumNumberOfSensors() {
        return maximumNumberOfSensors;
    }

    /**
     * Whether to allow the inclusion of single handcontrollers (rather than just none, or both) in sensor subsets
     *
     * @return
     */
    public static boolean allowSingleHandController() {
        return allowSingleHandController;
    }

    /**
     * The minimum number of trackers to be included in sensor subsets
     *
     * @return
     */
    public static int getMinimumNumberOfTrackers() {
        return minimumNumberOfTrackers;
    }

    /**
     * The minimum number of sensors to be included in sensor subsets
     *
     * @return
     */
    public static int getMinimumNumberOfSensors() {
        return minimumNumberOfSensors;
    }

    /**
     * Whether to save all created models to their subject folder
     *
     * @return
     */
    public static boolean writeAllModelsToFolder() {
        return writeAllModelsToFolder;
    }

    /**
     * Determine whether a feature that belongs to all the stated types is allowed to be used
     *
     * @param types
     * @return
     */
    public static boolean featureTagsAllowed(FeatureType... types) {

        for (FeatureType type : types) {
            if (forbiddenFeatureTypes.contains(type)) {
                return false;
            }
        }

        return true;
    }


    /**
     * Whether one feature file should be used per subject, instead of writing all into one file (and separating later)
     * <p>
     * Much slower due to frequent need to reload data, and needs much more memory due to size of training files.
     * Good for debugging at the start, but no longer tested as well as the standard setting (false),
     * checks recommended before using it again.
     *
     * @return
     */
    public static boolean useIndividualFeatureFilesForEachSubject() {
        return useIndividualFeatureFilesForEachSubject;
    }

    /**
     * Test whether the given sensor subset is blocked from use
     * due to choice of specific combinations only
     *
     * @param sensorSubset
     * @return
     */
    public static boolean isBlockedBySpecificRequests(SensorSubset sensorSubset) {
        // do not block any combination if no specific combination was requested
        if (onlyAllowSensorSubset.length == 0) {
            return false;
            // otherwise, search for a fitting combination
        } else {

            ArrayList<String> sensorsInSubset = sensorSubset.getIncludedSensors();

            for (int i = 0; i < onlyAllowSensorSubset.length; i++) {

                String[] allowedSensorSubsets = onlyAllowSensorSubset[i];

                if (allowedSensorSubsets.length == sensorsInSubset.size()) {
                    HashSet<String> sensorsAllowed = new HashSet<>(Arrays.asList(allowedSensorSubsets));
                    HashSet<String> sensorsIncluded = new HashSet<>(sensorsInSubset);
                    if (sensorsAllowed.equals(sensorsIncluded)) {
                        return false;
                    }
                }
            }
        }

        // if no fitting combination was found, this combination is blocked
        return true;

    }

    /**
     * Test whether the given sensor subset is blocked from use
     * due to choice of specific combinations with added trackers
     *
     * @param sensorSubset
     * @return
     */
    public static boolean isBlockedByMinimumRequests(SensorSubset sensorSubset) {

        // only possible if minimum requests were made
        if (minimumSensorSubset.length == 0) {
            return false;
        } else {
            // search for a fitting combination

            ArrayList<String> sensorsInSubset = sensorSubset.getIncludedSensors();

            for (int i = 0; i < minimumSensorSubset.length; i++) {

                String[] allowedSensorSubset = minimumSensorSubset[i];

                if (allowedSensorSubset.length + maximumNumberOfTrackers >= sensorsInSubset.size()) {

                    if (minimumNumberOfTrackers >= 0) {
                        if (allowedSensorSubset.length + minimumNumberOfTrackers > sensorsInSubset.size()) {
                            continue;
                        }
                    }

                    // create HashSets for two combinations to be tested against each other
                    HashSet<String> allowedCombination = new HashSet<>(Arrays.asList(allowedSensorSubset));
                    HashSet<String> sensorsIncluded = new HashSet<>(sensorsInSubset);

                    // check if the sets contain each other
                    if (sensorsIncluded.containsAll(allowedCombination)) {
                        if (sensorsIncluded.size() == allowedCombination.size()) {
                            return false;
                        } else {
                            // if they are not the same size, the new set may only contain additional trackers,
                            // not handcontrollers or the HMD (as these will not be requested to be added later)
                            sensorsIncluded.removeAll(allowedCombination);
                            if (!sensorsIncluded.contains("lHand") && !sensorsIncluded.contains("rHand")
                                    && !sensorsIncluded.contains("head")) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        // if no fitting combination was found, this combination is blocked
        return true;
    }

    /**
     * Test whether the given sensor is blocked from use
     * due to choice of specific combinations only
     *
     * @param sensor
     * @return
     */
    public static boolean isSensorBlocked(String sensor) {
        // do not block sensor if no specific combination was requested
        if (onlyAllowSensorSubset.length == 0) {
            return false;
            // otherwise, search for a fitting combination
        } else {
            for (int i = 0; i < onlyAllowSensorSubset.length; i++) {

                String[] allowedSensorSubset = onlyAllowSensorSubset[i];

                for (int k = 0; k < allowedSensorSubset.length; k++) {
                    if (allowedSensorSubset[k].equals(sensor)) {
                        return false;
                    }
                }
            }
        }

        // if no fitting combination was found, this combination is blocked
        return true;
    }

    /**
     * Whether one or more specific sensor combinations were requested, rather than a rule based choice
     *
     * @return
     */
    public static boolean specificSensorCombinationRequested() {
        return onlyAllowSensorSubset.length > 0;
    }

    /**
     * Whether one or more sensor combinations to be combined with additional trackers was requested
     *
     * @return
     */
    public static boolean minimumSensorCombinationRequested() {
        return minimumSensorSubset.length > 0;
    }

    /**
     * A scalar to be applied to all features
     * <p>
     * Required to adapt data to some algorithms, should be left at 1 if there are no errors
     *
     * @return
     */
    public static double scaleAllFeaturesBy() {
        return scaleAllFeaturesBy;
    }

    /**
     * Whether to use an existing feature file instead of generating a new one
     *
     * @return
     */
    public static boolean useExistingFeatureFile() {
        return useExistingFeatureFile;
    }

    /**
     * The folder in which existing feature reference file has to be placed
     * <p>
     * The feature reference file is not the feature file itself,
     * but the file containing the location of all feature files
     *
     * @return
     */
    public static String getExistingFeaturesInputFolder() {
        return existingFeaturesInputFolder;
    }

    /**
     * Whether to include data from the current subject in the training data
     * <p>
     * Specifies whether personal models should be created
     *
     * @return
     */
    public static SubjectDataInclusion getSubjectTrainingDataInclusion() {
        return subjectDataInclusion;
    }
}
