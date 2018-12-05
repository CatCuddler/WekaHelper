package com.romanuhlig.weka;

import com.romanuhlig.weka.classification.ClassificationResult;
import com.romanuhlig.weka.classification.ClassifierFactory;
import com.romanuhlig.weka.data.FrameDataReader;
import com.romanuhlig.weka.io.*;
import com.romanuhlig.weka.time.TimeHelper;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class Main {


    // settings
    static ArrayList<ClassifierFactory.ClassifierType> classifiersToUse = new ArrayList<>(Arrays.asList(
            ClassifierFactory.ClassifierType.J48,
            ClassifierFactory.ClassifierType.RandomForest,
            ClassifierFactory.ClassifierType.NaiveBayes,
            ClassifierFactory.ClassifierType.OneR,
            ClassifierFactory.ClassifierType.ZeroR));


    // folders
    static String inputFilePath = "./inputFrameData";
    static String outputFilePathBase = "./outputResults/";
    static String outputFolderTag = "";


    public static void main(String[] args) throws Exception {

        StopWatch stopwatchFullProcess = new StopWatch();
        stopwatchFullProcess.start();

        // FrameDataReader.readFrameDataSet("./inputFrameData/TestUser__Task1__SID1__1542122933.csv");

        String startTime = TimeHelper.getDateWithSeconds();

        String outputFilePath = outputFilePathBase + startTime + outputFolderTag;

        FeatureExtractionResults featureExtractionResults = FrameDataReader.createFeatureSets(inputFilePath, outputFilePath);

        ArrayList<SensorPermutation> sensorPermutations = SensorPermutation.generateAllPermutations(featureExtractionResults.getSensorPositions());

        System.out.println("permutations:     " + sensorPermutations.size());
        for (SensorPermutation permutation : sensorPermutations) {

            System.out.println();

            for (String sensor : permutation.getIncludedSensors()) {
                System.out.print(sensor + "   ");
            }

        }


        ClassifierFactory classifierFactory = new ClassifierFactory();


        StopWatch stopWatchEvaluation = new StopWatch();
        stopWatchEvaluation.start();

        int numberOfEvaluationsCompleted = 0;

        String outputFolderMain = outputFilePath + "/results/";

        // Weka evaluation
        for (SensorPermutation sensorPermutation : sensorPermutations) {

            String outputFolderSensorPermutation = outputFolderMain + sensorPermutation.getNumberOfSensors() + " sensors/" + sensorPermutation.getFolderStringRepresentation() + "/";

            ArrayList<Classifier> classifiers = classifierFactory.getClassifiers(classifiersToUse);

            for (Classifier classifier : classifiers) {

                String outputFolderClassifier = outputFolderSensorPermutation + classifier.getClass().getSimpleName() + "/";

                for (TrainingAndTestFilePackage filePackage : featureExtractionResults.getTrainingAndTestFilePackages()) {

                    String outputFolderSubject = outputFolderClassifier + filePackage.getSubject() + "/";

                    // setup data sources
                    // training data
                    Instances trainingDataUnfiltered = filePackage.getTrainingDataUnfiltered();
                    // test data
                    Instances testDataUnfiltered = filePackage.getTestDataUnfiltered();


                    // remove attributes from sensors that should not be included in this sensor permutation
                    Enumeration<Attribute> allAttributes = trainingDataUnfiltered.enumerateAttributes();
                    ArrayList<Attribute> allAttributesList = Collections.list(allAttributes);
                    ArrayList<Integer> attributesToRemove = new ArrayList<>();
                    for (int i = 0; i < allAttributesList.size(); i++) {
                        if (sensorPermutation.attributeForbidden(allAttributesList.get(i))) {
                            attributesToRemove.add(i);
                        }
                    }

                    int[] attributeIndicesToRemove = ConversionHelper.integerListToIntArray(attributesToRemove);


                    Instances trainingData = trainingDataUnfiltered;
                    Instances testData = testDataUnfiltered;

                    if (attributeIndicesToRemove.length > 0) {
                        Remove remove = new Remove();
                        remove.setAttributeIndicesArray(attributeIndicesToRemove);
                        remove.setInputFormat(trainingData);
                        trainingData = Filter.useFilter(trainingDataUnfiltered, remove);
                        testData = Filter.useFilter(testDataUnfiltered, remove);
                    }


                    // actual evaluation
                    classifier.buildClassifier(trainingData);
                    Evaluation eval = new Evaluation(trainingData);
                    eval.evaluateModel(classifier, testData);


                    // file output
                    ClassificationResult classificationResult = new ClassificationResult(eval, classifier, trainingData, filePackage.getSubject(), sensorPermutation);
                    FileWriter.writeClassificationResult(classificationResult, outputFolderSubject, "classificationResult");


                    // console output

                    System.out.println(numberOfEvaluationsCompleted++);

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


            }


        }

        stopWatchEvaluation.stop();
        stopwatchFullProcess.stop();

        System.out.println("Evaluation took: " + stopWatchEvaluation.getTime(TimeUnit.SECONDS));
        System.out.println("everything took: " + stopwatchFullProcess.getTime(TimeUnit.SECONDS));

    }

}
