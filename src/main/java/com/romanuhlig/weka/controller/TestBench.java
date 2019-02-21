package com.romanuhlig.weka.controller;

import com.romanuhlig.weka.classification.ClassificationResult;
import com.romanuhlig.weka.classification.ClassifierFactory;
import com.romanuhlig.weka.classification.AddingConfusionMatrix;
import com.romanuhlig.weka.data.FrameDataReader;
import com.romanuhlig.weka.io.*;
import com.romanuhlig.weka.time.TimeHelper;
import org.apache.commons.lang3.time.StopWatch;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TestBench {


    public void run() throws Exception {

        StopWatch stopwatchFullProcess = new StopWatch();
        stopwatchFullProcess.start();

        // FrameDataReader.readFrameDataSet("./inputFrameData/TestUser__Task1__SID1__1542122933.csv");

        String startTime = TimeHelper.getDateWithSeconds();
        String outputFolderPath = TestBenchSettings.outputBaseFolder() + TestBenchSettings.summarySingleLine() + "   " + TestBenchSettings.getOutputFolderTag() + startTime + "/";


        FeatureExtractionResults featureExtractionResults;
        if (TestBenchSettings.useExistingFeatureFile()) {
            featureExtractionResults = FileWriter.readExistingFeatureSet(TestBenchSettings.getExistingFeaturesInputFolder());
        } else {
            featureExtractionResults = FrameDataReader.createFeatureSets(TestBenchSettings.getInputBaseFolder(), outputFolderPath);
            FileWriter.writeNewFeatureExtractionResults(featureExtractionResults, outputFolderPath, TestBenchSettings.getExistingFeaturesInputFolder(), "featureExtractionResults_" + startTime);
        }


        ArrayList<SensorPermutation> sensorPermutations = SensorPermutation.generateAllPermutations(featureExtractionResults.getAllSensorPositions());
        GlobalData.setAllAvailableSensors(featureExtractionResults.getAllSensorPositions());


        // remove sensor permutations we do not need for this run
        for (int i = sensorPermutations.size() - 1; i >= 0; i--) {

            SensorPermutation sensorPermutation = sensorPermutations.get(i);

            // specific sensor requests override all other criteria
            if (TestBenchSettings.specificSensorCombinationRequested()) {

                // check for specific sensor inclusion
                if (TestBenchSettings.isSensorCombinationBlocked(sensorPermutation)) {
                    sensorPermutations.remove(i);
                    continue;
                }

            } else {
                // otherwise, check all individual criteria

                // check for hand controller inclusion
                switch (TestBenchSettings.getSensorUsageHandControllers()) {
                    case CannotInclude:
                        if (sensorPermutation.includesAtLeastOneHandController()) {
                            sensorPermutations.remove(i);
                            continue;
                        }
                        break;
                    case MustInclude:
                        if (!sensorPermutation.includesBothHandControllers()) {
                            sensorPermutations.remove(i);
                            continue;
                        }
                        break;
                }
                if (!TestBenchSettings.allowSingleHandController()
                        && sensorPermutation.includesAtLeastOneHandController()
                        && !sensorPermutation.includesBothHandControllers()
                ) {
                    continue;
                }

                // check for HMD inclusion
                switch (TestBenchSettings.getSensorUsageHMD()) {
                    case CannotInclude:
                        if (sensorPermutation.includesHMD()) {
                            sensorPermutations.remove(i);
                            continue;
                        }
                        break;
                    case MustInclude:
                        if (!sensorPermutation.includesHMD()) {
                            sensorPermutations.remove(i);
                            continue;
                        }
                        break;
                }

                // check for tracker inclusion
                if (TestBenchSettings.getMaximumNumberOfTrackers() >= 0 &&
                        sensorPermutation.getNumberOfTrackers() > TestBenchSettings.getMaximumNumberOfTrackers()) {
                    sensorPermutations.remove(i);
                    continue;
                }
                if (TestBenchSettings.getMinimumNumberOfTrackers() >= 0 &&
                        sensorPermutation.getNumberOfTrackers() < TestBenchSettings.getMinimumNumberOfTrackers()) {
                    sensorPermutations.remove(i);
                    continue;
                }

                // check for overall sensor inclusion
                if (TestBenchSettings.getMaximumNumberOfSensors() >= 0 &&
                        sensorPermutation.getNumberOfSensors() > TestBenchSettings.getMaximumNumberOfSensors()) {
                    sensorPermutations.remove(i);
                    continue;
                }
                if (TestBenchSettings.getMinimumNumberOfSensors() >= 0 &&
                        sensorPermutation.getNumberOfSensors() < TestBenchSettings.getMinimumNumberOfSensors()) {
                    sensorPermutations.remove(i);
                    continue;
                }
            }


        }

        // System.out.println("permutations:     " + sensorPermutations.size());
//        for (SensorPermutation permutation : sensorPermutations) {
        //  System.out.println();

        //  for (String sensor : permutation.getIncludedSensors()) {
        //     System.out.print(sensor + "   ");
        //  }
//        }


        ClassifierFactory classifierFactory = new ClassifierFactory();


        StopWatch stopWatchEvaluation = new StopWatch();
        stopWatchEvaluation.start();
        StopWatch singleTestStopWatch = new StopWatch();
        HashMap<Classifier, Long> classifierTimeUsage = new HashMap<>();

        int numberOfEvaluationsCompleted = 0;

        String resultsBaseFolder = outputFolderPath + "results/";

        ArrayList<ClassificationResult> allResults = new ArrayList<>();

        HashMap<Integer, ArrayList<ClassificationResult>> sensorNumberResults = new HashMap<>();

        ArrayList<Classifier> classifiers = classifierFactory.getClassifiers(TestBenchSettings.getClassifiersToUse());

        int numberOfEvaluationsInTotal =
                sensorPermutations.size() * classifiers.size()
                        * featureExtractionResults.getIndividualTrainingAndTestFilePackages().size();


        // output information about test
        FileWriter.writeTextFile(TestBenchSettings.summaryBig(), outputFolderPath, "settings.txt");


        // Weka evaluation
        // ... sensor permutations
        for (SensorPermutation sensorPermutation : sensorPermutations) {


            ArrayList<ClassificationResult> sensorPermutationResults = new ArrayList<>();

            String outputFolderSensorPermutation = resultsBaseFolder + sensorPermutation.getNumberOfSensors() + " sensors/" + sensorPermutation.getFolderStringRepresentation() + "/";

            // ... classifiers
            for (Classifier classifier : classifiers) {

                ArrayList<ClassificationResult> classifierResults = new ArrayList<>();


                String outputFolderClassifier = outputFolderSensorPermutation + classifier.getClass().getSimpleName() + "/";

                AddingConfusionMatrix classifierAddingConfusionMatrix = new AddingConfusionMatrix();

                // ... test subjects
                for (int fp = 0; fp < featureExtractionResults.getIndividualTrainingAndTestFilePackages().size(); fp++) {


                    TrainingAndTestFilePackage filePackage =
                            featureExtractionResults.getIndividualTrainingAndTestFilePackages().get(fp);

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
                        Instances allFeaturesUnfiltered = featureExtractionResults.getCompleteFeatureSet().getTrainingDataUnfiltered();
                        RemoveWithValues removeSubject = new RemoveWithValues();

                        // determine nominal index of current subject within list of available subjects,
                        // from the perspective of the loaded weka data set
                        int subjectAttributeIndex = allFeaturesUnfiltered.numAttributes() - 2;
                        Attribute subjectAttribute = allFeaturesUnfiltered.attribute(subjectAttributeIndex);
                        int nominalIndexSubject = subjectAttribute.indexOfValue(filePackage.getSubject());

                        removeSubject.setAttributeIndex("" + (allFeaturesUnfiltered.numAttributes() - 1));
                        removeSubject.setNominalIndicesArr(new int[]{nominalIndexSubject});
                        removeSubject.setInputFormat(allFeaturesUnfiltered);
                        removeSubject.setModifyHeader(false);

                        if (TestBenchSettings.includeTestDataInTrainingData()) {
                            trainingDataAllSensors = allFeaturesUnfiltered;
                        } else {
                            trainingDataAllSensors = Filter.useFilter(allFeaturesUnfiltered, removeSubject);
                        }
                        removeSubject.setInvertSelection(true);
                        testDataAllSensors = Filter.useFilter(allFeaturesUnfiltered, removeSubject);
                    }


                    // remove attributes from sensors that are not included in this sensor permutation
                    // collect the wrong column indices
                    Enumeration<Attribute> allAttributes = trainingDataAllSensors.enumerateAttributes();
                    ArrayList<Attribute> allAttributesList = Collections.list(allAttributes);
                    ArrayList<Integer> attributesToRemove = new ArrayList<>();


                    for (int i = 0; i < allAttributesList.size(); i++) {
                        if (sensorPermutation.attributeForbidden(allAttributesList.get(i))) {
                            attributesToRemove.add(i);
                        }

                        // we also need to remove the subject name column, if it is still present due to using
                        // a single data file for all features
                        if (allAttributesList.get(i).name().contains("subject")) {
                            attributesToRemove.add(i);
                        }

                    }


                    int[] attributeIndicesToRemove = ConversionHelper.integerListToIntArray(attributesToRemove);


                    Instances trainingDataFinal = trainingDataAllSensors;
                    Instances testDataFinal = testDataAllSensors;

                    if (attributeIndicesToRemove.length > 0) {
                        Remove remove = new Remove();
                        remove.setAttributeIndicesArray(attributeIndicesToRemove);
                        remove.setInputFormat(trainingDataFinal);
                        trainingDataFinal = Filter.useFilter(trainingDataAllSensors, remove);
                        testDataFinal = Filter.useFilter(testDataAllSensors, remove);
                    }


                    // measure time for single evaluation
                    singleTestStopWatch.reset();
                    singleTestStopWatch.start();
                    // actual evaluation
                    classifier.buildClassifier(trainingDataFinal);
                    Evaluation eval = new Evaluation(trainingDataFinal);
                    eval.evaluateModel(classifier, testDataFinal);
                    // measure time for single evaluation
                    singleTestStopWatch.stop();
                    if (classifierTimeUsage.containsKey(classifier)) {
                        long overallTimeForClassifier = classifierTimeUsage.get(classifier);
                        classifierTimeUsage.put(
                                classifier,
                                overallTimeForClassifier + singleTestStopWatch.getTime(TimeUnit.MILLISECONDS));
                    } else {
                        classifierTimeUsage.put(classifier, singleTestStopWatch.getTime(TimeUnit.MILLISECONDS));
                    }


                    // file output
                    // current result
                    ClassificationResult classificationResult = ClassificationResult.constructClassificationResultForSinglePerson(eval, classifier, trainingDataFinal, filePackage.getSubject(), sensorPermutation);
                    FileWriter.writeClassificationResult(classificationResult, outputFolderSubject, "classificationResult");
                    // model
                    if (TestBenchSettings.writeAllModelsToFolder()) {
                        SerializationHelper.write(outputFolderSubject + "currentModel.model", classifier);
                    }

                    // features used
                    FileWriter.writeFeaturesUsed(trainingDataFinal, testDataFinal, outputFolderSubject, "features used.txt");


                    // confusion matrix
                    // output normal confusion matrix
                    FileWriter.writeTextFile(eval.toMatrixString(),
                            outputFolderSubject, "confusion matrix.txt");
                    // output confusion matrix for latex
                    AddingConfusionMatrix tasksAddingConfusionMatrix = new AddingConfusionMatrix();
                    tasksAddingConfusionMatrix.addResults(eval.confusionMatrix(), trainingDataFinal);
                    FileWriter.writeTextFile(tasksAddingConfusionMatrix.toOutputStringLatex(),
                            outputFolderSubject, "confusion matrix latex.txt");
                    // add confusion matrix to classifier summary
                    classifierAddingConfusionMatrix.addResults(eval.confusionMatrix(), trainingDataFinal);

                    // collect result for summaries
                    classifierResults.add(classificationResult);


                    // console output
                    // evaluation counter
                    numberOfEvaluationsCompleted++;
                    double timePerTask = stopWatchEvaluation.getTime(TimeUnit.MILLISECONDS) / numberOfEvaluationsCompleted;
                    float numberOfEvaluationsLeft = numberOfEvaluationsInTotal - numberOfEvaluationsCompleted;
                    int approximateTimeLeft = (int) ((timePerTask * numberOfEvaluationsLeft) / 1000);


                    System.out.println(
                            "evaluations done:  " + numberOfEvaluationsCompleted + " | " + numberOfEvaluationsInTotal
                                    + "     time left:  " + TimeHelper.secondsToTimeOutput(approximateTimeLeft));


//                    // attributes in current training instances
//                    System.out.println("attributes in current training instance:");
//                    for (int i = 0; i < trainingData.numAttributes(); i++) {
//                        System.out.println(trainingData.attribute(i).name());
//                    }

                    /*
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    System.out.println();

                    System.out.println(filePackage.getSubject());

                    System.out.println(sensorPermutation.getFolderStringRepresentation());

                    ArrayList<Attribute> attributesTraining = Collections.list(trainingData.enumerateAttributes());
                    ArrayList<Attribute> attributesTest = Collections.list(testData.enumerateAttributes());
                    System.out.println("number of training attributes from weka   " + attributesTraining.size());
                    System.out.println("number of test attributes from weka   " + attributesTest.size());


                    System.out.println(eval.toSummaryString("\nResults\n======\n", false));

                    // confusion matrix
                    double[][] matrix = eval.confusionMatrix();
                    for (int line = 0; line < matrix.length; line++) {
                        for (int column = 0; column < matrix[line].length; column++) {
                            System.out.print(Double.toString(matrix[line][column]));
                            System.out.print("     ");
                        }
                        System.out.println();
                    }
                    */

                }

                FileWriter.writeTextFile(classifierAddingConfusionMatrix.toOutputString(), outputFolderClassifier, "confusion matrix.txt");
                FileWriter.writeTextFile(classifierAddingConfusionMatrix.toOutputStringLatex(), outputFolderClassifier, "confusion matrix latex.txt");

                ClassificationResult classifierResultSummary = ClassificationResult.summarizeClassifierResults(classifierResults);
                classifierResults.add(classifierResultSummary);
                FileWriter.writeClassificationResults(classifierResults, outputFolderClassifier, "classificationResult");

                // collect for overall summary
                allResults.add(classifierResultSummary);

                // collect for sensor permutation summary
                sensorPermutationResults.add(classifierResultSummary);
                // collect for sensor number summary
                if (sensorNumberResults.containsKey(sensorPermutation.getNumberOfSensors())) {
                    sensorNumberResults.get(sensorPermutation.getNumberOfSensors()).add(classifierResultSummary);
                } else {
                    ArrayList<ClassificationResult> sensorNumberResultList = new ArrayList<>();
                    sensorNumberResultList.add(classifierResultSummary);
                    sensorNumberResults.put(sensorPermutation.getNumberOfSensors(), sensorNumberResultList);
                }


            }

            FileWriter.writeClassificationResults(sensorPermutationResults, outputFolderSensorPermutation, "classificationResult");


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

        // output runtime
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
