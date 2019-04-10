package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassificationResult;
import com.romanuhlig.weka.classification.ClassifierFactory;
import com.romanuhlig.weka.classification.ConfusionMatrixSummary;
import com.romanuhlig.weka.data.FrameDataReader;
import com.romanuhlig.weka.io.*;
import com.romanuhlig.weka.time.TimeHelper;

import org.apache.commons.lang3.time.StopWatch;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Manages the feature extraction, training and evaluation process from start to end
 *
 * @author Roman Uhlig
 */
public class TestBench {


    /**
     * Create features, train and evaluate models according to the current settings
     *
     * @throws Exception
     */
    public void run() {

        // this stopwatch measures the whole process, from start to end
        StopWatch stopwatchFullProcess = new StopWatch();
        stopwatchFullProcess.start();

        // determine base path for the feature and evaluation data
        String startTime = TimeHelper.getDateWithSeconds();
        String outputFolderPath = TestBenchSettings.outputBaseFolder() + TestBenchSettings.getSettingsAsFolderName()
                + "   " + TestBenchSettings.getOutputFolderTag() + startTime + "/";

        // load / create features
        FeatureExtractionResults featureExtractionResults;
        if (TestBenchSettings.useExistingFeatureFile()) {
            // load existing feature file reference, if requested
            featureExtractionResults = FileWriter.readExistingFeatureSet(
                    TestBenchSettings.getExistingFeaturesInputFolder());
        } else {
            // create new feature file
            featureExtractionResults = FrameDataReader.createFeatureSets(
                    TestBenchSettings.getInputBaseFolder(), outputFolderPath);
            FileWriter.writeNewFeatureExtractionResults(featureExtractionResults, outputFolderPath,
                    TestBenchSettings.getExistingFeaturesInputFolder(), "featureExtractionResults_" + startTime);
        }

        // determine all possible sensor subsets for the given dataset
        ArrayList<SensorSubset> sensorSubsets = SensorSubset.generateAllSubsets(
                featureExtractionResults.getAllSensorPositions());
        GlobalData.setAllAvailableSensors(featureExtractionResults.getAllSensorPositions());

        // remove the sensor subsets we do not need for this run
        for (int i = sensorSubsets.size() - 1; i >= 0; i--) {

            SensorSubset sensorSubset = sensorSubsets.get(i);

            // specific sensor requests override all other criteria
            if (TestBenchSettings.specificSensorCombinationRequested()) {
                // check for specific sensor inclusion
                if (TestBenchSettings.isBlockedBySpecificRequests(sensorSubset)) {
                    sensorSubsets.remove(i);
                    continue;
                }
            }
            // if a set of sensors must be included, with others still allowed, that also takes precedence
            else if (TestBenchSettings.minimumSensorCombinationRequested()) {
                if (TestBenchSettings.isBlockedByMinimumRequests(sensorSubset)) {
                    sensorSubsets.remove(i);
                    continue;
                }
            }
            // otherwise, check all individual criteria
            else {
                // check for hand controller inclusion
                switch (TestBenchSettings.getSensorUsageHandControllers()) {
                    case CannotInclude:
                        if (sensorSubset.includesAtLeastOneHandController()) {
                            sensorSubsets.remove(i);
                            continue;
                        }
                        break;
                    case MustInclude:
                        if (!sensorSubset.includesBothHandControllers()) {
                            sensorSubsets.remove(i);
                            continue;
                        }
                        break;
                }
                // check whether it is allowed to have only one instead of two handcontrollers
                if (!TestBenchSettings.allowSingleHandController()
                        && sensorSubset.includesAtLeastOneHandController()
                        && !sensorSubset.includesBothHandControllers()) {
                    continue;
                }

                // check for HMD inclusion
                switch (TestBenchSettings.getSensorUsageHMD()) {
                    case CannotInclude:
                        if (sensorSubset.includesHMD()) {
                            sensorSubsets.remove(i);
                            continue;
                        }
                        break;
                    case MustInclude:
                        if (!sensorSubset.includesHMD()) {
                            sensorSubsets.remove(i);
                            continue;
                        }
                        break;
                }

                // check for right number of trackers
                if (TestBenchSettings.getMaximumNumberOfTrackers() >= 0 &&
                        sensorSubset.getNumberOfTrackers() > TestBenchSettings.getMaximumNumberOfTrackers()) {
                    sensorSubsets.remove(i);
                    continue;
                }
                if (TestBenchSettings.getMinimumNumberOfTrackers() >= 0 &&
                        sensorSubset.getNumberOfTrackers() < TestBenchSettings.getMinimumNumberOfTrackers()) {
                    sensorSubsets.remove(i);
                    continue;
                }

                // check for right number of sensors (including trackers) overall
                if (TestBenchSettings.getMaximumNumberOfSensors() >= 0 &&
                        sensorSubset.getNumberOfSensors() > TestBenchSettings.getMaximumNumberOfSensors()) {
                    sensorSubsets.remove(i);
                    continue;
                }
                if (TestBenchSettings.getMinimumNumberOfSensors() >= 0 &&
                        sensorSubset.getNumberOfSensors() < TestBenchSettings.getMinimumNumberOfSensors()) {
                    sensorSubsets.remove(i);
                    continue;
                }
            }
        }


        // initialize additional data for test run:
        // create the chosen classifiers
        ClassifierFactory classifierFactory = new ClassifierFactory();
        ArrayList<Classifier> classifiers = classifierFactory.getClassifiers(TestBenchSettings.getClassifiersToUse());
        // stop watch for a ongoing time tracking during the evaluation process
        StopWatch stopWatchEvaluation = new StopWatch();
        stopWatchEvaluation.start();
        // stop watch for individual evaluations
        StopWatch singleTestStopWatch = new StopWatch();
        // collection for total time used by individual classifiers
        HashMap<Classifier, Long> classifierTimeUsage = new HashMap<>();
        // base folder for just the evaluation results
        String resultsBaseFolder = outputFolderPath + "results/";
        // collect all evaluation results
        ArrayList<ClassificationResult> allResults = new ArrayList<>();
        // collect the evaluation results for each number of sensors
        HashMap<Integer, ArrayList<ClassificationResult>> sensorNumberResults = new HashMap<>();
        // count the number of evaluations for estimation of remaining time
        int numberOfEvaluationsCompleted = 0;
        int numberOfEvaluationsInTotal = sensorSubsets.size() * classifiers.size()
                * featureExtractionResults.getSubjectTrainingAndTestFilePackages().size();

        // output the settings for this run
        FileWriter.writeTextFile(TestBenchSettings.getSettingsSummary(), outputFolderPath, "settings.txt");


        // Training and evaluation for all sensor subsets, classifiers and test subjects
        // ... all sensor subsets
        for (SensorSubset sensorSubset : sensorSubsets) {

            // prepare to collect all results for this sensor subset
            ArrayList<ClassificationResult> sensorSubsetResults = new ArrayList<>();
            String outputFolderSensorSubset = resultsBaseFolder + sensorSubset.getNumberOfSensors() + " sensors/"
                    + sensorSubset.getFolderStringRepresentation() + "/";

            // ... all classifiers
            for (Classifier classifier : classifiers) {

                // prepare to collect all results for this classifier
                ArrayList<ClassificationResult> classifierResults = new ArrayList<>();
                String outputFolderClassifier = outputFolderSensorSubset + classifier.getClass().getSimpleName() + "/";
                ConfusionMatrixSummary classifierConfusionMatrixSummary = new ConfusionMatrixSummary();

                // ... all test subjects
                for (int fp = 0; fp < featureExtractionResults.getSubjectTrainingAndTestFilePackages().size(); fp++) {

                    // retrieve features for this subject
                    TrainingAndTestFilePackage filePackage =
                            featureExtractionResults.getSubjectTrainingAndTestFilePackages().get(fp);

                    // prepare to collect all results for this subject
                    String outputFolderSubject = outputFolderClassifier + filePackage.getSubject() + "/";

                    // setup data sources
                    Instances trainingDataAllSensors;
                    Instances testDataAllSensors;

                    // when using individual files for test and training for each subject, just load them
                    if (TestBenchSettings.useIndividualFeatureFilesForEachSubject()) {
                        trainingDataAllSensors = filePackage.getTrainingDataUnfiltered();
                        testDataAllSensors = filePackage.getTestDataUnfiltered();
                    } else {
                        // otherwise, load the complete data file and separate both parts
                        Instances allDataUnfiltered = featureExtractionResults.getCompleteFeatureSet().getTrainingDataUnfiltered();

                        // determine nominal index of current subject within list of available subjects,
                        // from the perspective of the loaded weka data set
                        int subjectAttributeIndex = allDataUnfiltered.numAttributes() - 2;
                        int classAttributeIndex = allDataUnfiltered.numAttributes() - 1;

                        // Determine how much data to remove from the subject, for each task,
                        // in case not all should be removed from training set (personal model).
                        // The tasks are saved in blocks within the feature set for each subject
                        ArrayList<Integer> instancesPerTask = new ArrayList<>();
                        ArrayList<Integer> instancesToKeepInTrainingDataPerTask = new ArrayList<>();
                        ArrayList<Integer> instancesToRemoveFromTestDataPerTask = new ArrayList<>();
                        if (TestBenchSettings.getSubjectTrainingDataInclusion() == TestBenchSettings.SubjectDataInclusion.Half
                                || TestBenchSettings.getSubjectTrainingDataInclusion() == TestBenchSettings.SubjectDataInclusion.HalfAndNoOtherData) {
                            String previousTask = "";
                            // go through the list backwards, as we will have to do the same when deleting instances later
                            for (int i = allDataUnfiltered.size() - 1; i >= 0; i--) {
                                Instance instance = allDataUnfiltered.get(i);
                                String instanceSubject = instance.stringValue(subjectAttributeIndex);
                                String instanceClass = instance.stringValue(classAttributeIndex);
                                // count the task, or add a new one, if this is the current subject
                                if (instanceSubject.equals(filePackage.getSubject())) {
                                    if (!instanceClass.equals(previousTask)) {
                                        instancesPerTask.add(0);
                                        previousTask = instanceClass;
                                    }
                                    instancesPerTask.set(instancesPerTask.size() - 1, instancesPerTask.get(instancesPerTask.size() - 1) + 1);
                                }
                            }
                            // after determining how much data there is per task, determine how much to remove and keep
                            for (int i = 0; i < instancesPerTask.size(); i++) {
                                Integer instancesForTask = instancesPerTask.get(i);
                                // leave out data in the middle to account for window size, to prevent data that
                                // could end up in both training and test data
                                int instancesToLeaveOutForWindow =
                                        (int) Math.ceil(TestBenchSettings.getWindowSizeForFrameDataToFeatureConversion()
                                                / TestBenchSettings.getWindowSpacingForFrameDataToFeatureConversion())
                                                - 1;
                                int instancesToKeepForTrainingData = (int) Math.ceil((instancesForTask - instancesToLeaveOutForWindow) / 2f);
                                instancesToKeepInTrainingDataPerTask.add((instancesToKeepForTrainingData));
                                instancesToRemoveFromTestDataPerTask.add(instancesToKeepForTrainingData + instancesToLeaveOutForWindow);
                            }
                        }

                        // if requested to include all subject data (for sanity checks), just keep it as it is
                        if (TestBenchSettings.getSubjectTrainingDataInclusion() == TestBenchSettings.SubjectDataInclusion.All) {
                            trainingDataAllSensors = new Instances(allDataUnfiltered);
                        } else if (TestBenchSettings.getSubjectTrainingDataInclusion() == TestBenchSettings.SubjectDataInclusion.Half
                                || TestBenchSettings.getSubjectTrainingDataInclusion() == TestBenchSettings.SubjectDataInclusion.HalfAndNoOtherData) {
                            // if some subject data is to be kept, copy the data and filter out what is required
                            trainingDataAllSensors = new Instances(allDataUnfiltered);
                            // remove the required amount of subject data for each class in the training data
                            int countForCurrentClass = 0;
                            int classIndex = -1;
                            String previousTask = "";
                            for (int i = trainingDataAllSensors.size() - 1; i >= 0; i--) {
                                Instance instance = trainingDataAllSensors.get(i);
                                String instanceSubject = instance.stringValue(subjectAttributeIndex);
                                String instanceClass = instance.stringValue(classAttributeIndex);
                                // delete second half of each class
                                if (instanceSubject.equals(filePackage.getSubject())) {
                                    if (!instanceClass.equals(previousTask)) {
                                        countForCurrentClass = 0;
                                        previousTask = instanceClass;
                                        classIndex++;
                                    }
                                    countForCurrentClass++;
                                    if (countForCurrentClass > instancesToKeepInTrainingDataPerTask.get(classIndex))
                                        trainingDataAllSensors.remove(i);
                                }
                            }

                            // also delete all data from other subjects, if only subject data should be included
                            if (TestBenchSettings.getSubjectTrainingDataInclusion() == TestBenchSettings.SubjectDataInclusion.HalfAndNoOtherData) {
                                for (int i = trainingDataAllSensors.size() - 1; i >= 0; i--) {
                                    Instance instance = trainingDataAllSensors.get(i);
                                    String instanceSubject = instance.stringValue(subjectAttributeIndex);
                                    if (!instanceSubject.equals(filePackage.getSubject())) {
                                        trainingDataAllSensors.remove(i);
                                    }
                                }
                            }

                        } else {
                            // normal case, the current subject needs to be removed from training data
                            // copy the training data and remove all subject data
                            trainingDataAllSensors = new Instances(allDataUnfiltered);
                            for (int i = trainingDataAllSensors.size() - 1; i >= 0; i--) {
                                Instance instance = trainingDataAllSensors.get(i);
                                String instanceSubject = instance.stringValue(subjectAttributeIndex);
                                if (instanceSubject.equals(filePackage.getSubject())) {
                                    trainingDataAllSensors.remove(i);
                                }
                            }
                        }

                        // create test data, and remove all but the current subject
                        testDataAllSensors = new Instances(allDataUnfiltered);
                        for (int i = testDataAllSensors.size() - 1; i >= 0; i--) {
                            Instance instance = testDataAllSensors.get(i);
                            String instanceSubject = instance.stringValue(subjectAttributeIndex);
                            if (!instanceSubject.equals(filePackage.getSubject())) {
                                testDataAllSensors.remove(i);
                            }
                        }

                        // if some of the subject data was supposed to stay in the training data,
                        // it should not also be in the test data and needs to be removed
                        if (TestBenchSettings.getSubjectTrainingDataInclusion() == TestBenchSettings.SubjectDataInclusion.Half
                                || TestBenchSettings.getSubjectTrainingDataInclusion() == TestBenchSettings.SubjectDataInclusion.HalfAndNoOtherData) {

                            int countForCurrentClass = 0;
                            int classIndex = -1;
                            String previousClass = "";
                            for (int i = testDataAllSensors.size() - 1; i >= 0; i--) {
                                Instance instance = testDataAllSensors.get(i);
                                String instanceClass = instance.stringValue(classAttributeIndex);

                                // delete first half of each class
                                if (!instanceClass.equals(previousClass)) {
                                    countForCurrentClass = 0;
                                    previousClass = instanceClass;
                                    classIndex++;
                                }
                                countForCurrentClass++;
                                if (countForCurrentClass <= instancesToRemoveFromTestDataPerTask.get(classIndex)) {
                                    testDataAllSensors.remove(i);
                                }
                            }
                        }

                        // only for debugging purposes, one can use the test data as the training data
                        if (TestBenchSettings.getSubjectTrainingDataInclusion()
                                == TestBenchSettings.SubjectDataInclusion.AllAndNoOtherData) {
                            trainingDataAllSensors = new Instances(testDataAllSensors);
                        }
                    }

                    // remove attributes from sensors that are not included in this sensor subset
                    // collect the column indices of offending attributes
                    Enumeration<Attribute> allAttributes = trainingDataAllSensors.enumerateAttributes();
                    ArrayList<Attribute> allAttributesList = Collections.list(allAttributes);
                    ArrayList<Integer> attributesToRemove = new ArrayList<>();
                    for (int i = 0; i < allAttributesList.size(); i++) {
                        if (sensorSubset.attributeForbidden(allAttributesList.get(i))) {
                            attributesToRemove.add(i);
                        }
                        // we also need to remove the subject name column,
                        // if it is still present due to using a single data file for all features
                        if (allAttributesList.get(i).name().contains("subject")) {
                            attributesToRemove.add(i);
                        }
                    }

                    // create a filter that can remove all identified unwanted attributes
                    int[] attributeIndicesToRemove = ConversionHelper.integerListToIntArray(attributesToRemove);
                    Instances trainingDataFinal = trainingDataAllSensors;
                    Instances testDataFinal = testDataAllSensors;

                    // remove the attributes from training and test data
                    if (attributeIndicesToRemove.length > 0) {
                        Remove remove = new Remove();
                        remove.setAttributeIndicesArray(attributeIndicesToRemove);
                        try {
                            remove.setInputFormat(trainingDataFinal);
                            trainingDataFinal = Filter.useFilter(trainingDataAllSensors, remove);
                            testDataFinal = Filter.useFilter(testDataAllSensors, remove);
                        } catch (Exception e) {
                            System.out.println("Unable to apply filter:");
                            e.printStackTrace();
                            System.exit(-1);
                        }
                    }


                    // measure time for single evaluation
                    singleTestStopWatch.reset();
                    singleTestStopWatch.start();

                    // build and evaluate model for current sensor subset, classifier and subject
                    Evaluation eval = null;
                    try {
                        classifier.buildClassifier(trainingDataFinal);
                        eval = new Evaluation(trainingDataFinal);
                        eval.evaluateModel(classifier, testDataFinal);
                    } catch (Exception e) {
                        System.out.println("Unable to train and evaluate model:");
                        e.printStackTrace();
                        System.exit(-1);
                    }


                    // measure time for a single evaluation, and add up time used by current classifier
                    singleTestStopWatch.stop();
                    if (classifierTimeUsage.containsKey(classifier)) {
                        long overallTimeForClassifier = classifierTimeUsage.get(classifier);
                        classifierTimeUsage.put(
                                classifier,
                                overallTimeForClassifier + singleTestStopWatch.getTime(TimeUnit.MILLISECONDS));
                    } else {
                        classifierTimeUsage.put(classifier, singleTestStopWatch.getTime(TimeUnit.MILLISECONDS));
                    }


                    // collect and store evaluation results:
                    // current result
                    ClassificationResult classificationResult = ClassificationResult.constructClassificationResultForSinglePerson
                            (eval, classifier, trainingDataFinal, filePackage.getSubject(), sensorSubset, singleTestStopWatch.getTime(TimeUnit.MILLISECONDS));
                    FileWriter.writeClassificationResult(classificationResult, outputFolderSubject, "classificationResult");
                    // current model
                    if (TestBenchSettings.writeAllModelsToFolder()) {
                        try {
                            SerializationHelper.write(outputFolderSubject + "currentModel.model", classifier);
                        } catch (Exception e) {
                            System.out.println("Unable to save model:");
                            e.printStackTrace();
                            System.exit(-1);
                        }
                    }
                    // features used
                    FileWriter.writeFeaturesUsed(trainingDataFinal, testDataFinal, outputFolderSubject, "features used.txt");

                    // confusion matrix:
                    // output normal confusion matrix
                    try {
                        FileWriter.writeTextFile(eval.toMatrixString(),
                                outputFolderSubject, "confusion matrix.txt");
                    } catch (Exception e) {
                        System.out.println("Unable to save confusion matrix:");
                        e.printStackTrace();
                        System.exit(-1);
                    }
                    // output confusion matrix for latex
                    ConfusionMatrixSummary tasksConfusionMatrixSummary = new ConfusionMatrixSummary();
                    tasksConfusionMatrixSummary.addResults(eval.confusionMatrix(), trainingDataFinal);
                    FileWriter.writeTextFile(tasksConfusionMatrixSummary.toOutputStringLatex(),
                            outputFolderSubject, "confusion matrix latex.txt");
                    // add confusion matrix to classifier summary
                    classifierConfusionMatrixSummary.addResults(eval.confusionMatrix(), trainingDataFinal);

                    // collect result for summaries
                    classifierResults.add(classificationResult);

                    // console output: evaluation counter and estimation of remaining time
                    numberOfEvaluationsCompleted++;
                    double timePerTask = stopWatchEvaluation.getTime(TimeUnit.MILLISECONDS) / numberOfEvaluationsCompleted;
                    float numberOfEvaluationsLeft = numberOfEvaluationsInTotal - numberOfEvaluationsCompleted;
                    int approximateTimeLeft = (int) ((timePerTask * numberOfEvaluationsLeft) / 1000);
                    System.out.println(
                            "evaluations done:  " + numberOfEvaluationsCompleted + " | " + numberOfEvaluationsInTotal
                                    + "     time left:  " + TimeHelper.secondsToTimeOutput(approximateTimeLeft));
                }

                // collect and store results for current classifier:
                // summarized confusion matrix
                FileWriter.writeTextFile(classifierConfusionMatrixSummary.toOutputString(), outputFolderClassifier, "confusion matrix.txt");
                FileWriter.writeTextFile(classifierConfusionMatrixSummary.toOutputStringLatex(), outputFolderClassifier, "confusion matrix latex.txt");
                // summarized results
                ClassificationResult classifierResultSummary = ClassificationResult.summarizeClassifierResults(classifierResults);
                classifierResults.add(classifierResultSummary);
                FileWriter.writeClassificationResults(classifierResults, outputFolderClassifier, "classificationResult");

                // collect for overall summary
                allResults.add(classifierResultSummary);
                // collect for sensor subset summary
                sensorSubsetResults.add(classifierResultSummary);
                // collect for sensor number summary
                if (sensorNumberResults.containsKey(sensorSubset.getNumberOfSensors())) {
                    sensorNumberResults.get(sensorSubset.getNumberOfSensors()).add(classifierResultSummary);
                } else {
                    ArrayList<ClassificationResult> sensorNumberResultList = new ArrayList<>();
                    sensorNumberResultList.add(classifierResultSummary);
                    sensorNumberResults.put(sensorSubset.getNumberOfSensors(), sensorNumberResultList);
                }
            }

            // store results for current sensor subset
            FileWriter.writeClassificationResults(sensorSubsetResults, outputFolderSensorSubset, "classificationResult");
        }

        // write all results
        allResults.sort(ClassificationResult.getF1Comparator());
        FileWriter.writeClassificationResults(allResults, resultsBaseFolder, "classificationResult");
        // write the overall results in base folder as well, for easier access
        FileWriter.writeClassificationResults(allResults, outputFolderPath, "classificationResult");

        // write sensor number results
        Iterator<Integer> sensorNumberIterator = sensorNumberResults.keySet().iterator();
        while (sensorNumberIterator.hasNext()) {
            Integer sensorNumber = sensorNumberIterator.next();
            String outputFolderSensorNumber = resultsBaseFolder + sensorNumber + " sensors/";
            ArrayList<ClassificationResult> sensorNumberResultsSorted = sensorNumberResults.get(sensorNumber);
            sensorNumberResultsSorted.sort(ClassificationResult.getF1Comparator());
            FileWriter.writeClassificationResults(sensorNumberResultsSorted, outputFolderSensorNumber, "classificationResult");
        }

        // output runtime for overall evaluation and individual classifiers
        stopWatchEvaluation.stop();
        stopwatchFullProcess.stop();
        System.out.println();
        System.out.println("duration of evaluation:   " + TimeHelper.secondsToTimeOutput(stopWatchEvaluation.getTime(TimeUnit.SECONDS)));
        System.out.println("duration overall:         " + TimeHelper.secondsToTimeOutput(stopwatchFullProcess.getTime(TimeUnit.SECONDS)));
        System.out.println("Classifier time usage:");
        for (Classifier classifier : classifierTimeUsage.keySet()) {
            System.out.println(classifierTimeUsage.get(classifier) + "     " + classifier.getClass().getSimpleName());
        }
    }
}
