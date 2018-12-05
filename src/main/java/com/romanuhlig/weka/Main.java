package com.romanuhlig.weka;

import com.romanuhlig.weka.data.FrameDataReader;
import com.romanuhlig.weka.io.FeatureExtractionResults;
import com.romanuhlig.weka.io.SensorPermutation;
import com.romanuhlig.weka.io.TrainingAndTestFilePackage;
import com.romanuhlig.weka.time.TimeHelper;

import java.util.ArrayList;
import java.util.Collections;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

public class Main {


    static String inputFilePath = "./inputFrameData";
    static String outputFilePathBase = "./outputResults/";
    static String outputFolderTag = "";


    public static void main(String[] args) throws Exception {

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

        // test weka
        for (TrainingAndTestFilePackage filePackage : featureExtractionResults.getTrainingAndTestFilePackages()) {

            // setup data sources
            // training data
            DataSource trainingSource = new DataSource(filePackage.getTrainingFilePath());
            Instances trainingData = trainingSource.getDataSet();
            trainingData.setClassIndex(trainingData.numAttributes() - 1);
            // test data
            DataSource testSource = new DataSource(filePackage.getTestFilePath());
            Instances testData = testSource.getDataSet();
            testData.setClassIndex(testData.numAttributes() - 1);

            System.out.println(filePackage.getSubject());

            Classifier classifier = new J48();
            classifier.buildClassifier(trainingData);
            Evaluation eval = new Evaluation(trainingData);
            eval.evaluateModel(classifier, testData);
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

        }

    }

}
