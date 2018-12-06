package com.romanuhlig.weka.classification;

import com.opencsv.bean.CsvBindByName;
import com.romanuhlig.weka.io.ConsoleHelper;
import com.romanuhlig.weka.io.SensorPermutation;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Comparator;

public class ClassificationResult {

    private static ClassificationResultF1Comperator f1Comperator = new ClassificationResultF1Comperator();

    @CsvBindByName
    private final String classifier;
    @CsvBindByName
    private final String testDataSubject;

    @CsvBindByName
    private final int numberOfSensors;

    @CsvBindByName
    private final String sensorList;

    @CsvBindByName
    private final double averageF1Score;


    private ClassificationResult(String classifier, String testDataSubject, int numberOfSensors, String sensorList, double averageF1Score) {
        this.classifier = classifier;
        this.testDataSubject = testDataSubject;
        this.numberOfSensors = numberOfSensors;
        this.sensorList = sensorList;
        this.averageF1Score = averageF1Score;
    }

    public static ClassificationResult constructClassificationResult(Evaluation evaluation, Classifier classifier, Instances instances, String subject,
                                                                     SensorPermutation sensorPermutation) {


        String _classifier = classifier.getClass().getSimpleName();
        String _testDataSubject = subject;
        int _numberOfSensors = sensorPermutation.getNumberOfSensors();
        String _sensorList = sensorPermutation.getSensorListRepresentation();

//        System.out.println("sensors: " + sensorPermutation.getNumberOfSensors());

        double _averageF1 = 0;
        for (int i = 0; i < instances.numClasses(); i++) {
            _averageF1 += evaluation.fMeasure(i);
//            System.out.println("recall " + i + " " + evaluation.recall(i));
//            System.out.println("precision " + i + " " + evaluation.precision(i));
//            System.out.println("fmeasure " + i + " " + evaluation.fMeasure(i));
//            System.out.println("true pos rate " + i + " " + evaluation.truePositiveRate(i));
//            System.out.println("false neg rate " + i + " " + evaluation.falseNegativeRate(i));
//            System.out.println("true pos num " + i + " " + evaluation.numTruePositives(i));
//            System.out.println("false neg num " + i + " " + evaluation.numFalseNegatives(i));
        }
        _averageF1 /= instances.numClasses();

//        System.out.println("fmeasure avg: " + averageF1Score);
//        System.out.println("fmeasure Wei: " + evaluation.weightedFMeasure());
//        System.out.println("fmeasure uMA: " + evaluation.unweightedMacroFmeasure());
//        System.out.println("fmeasure uMI: " + evaluation.unweightedMicroFmeasure());
//        ConsoleHelper.printConfusionMatrix(evaluation.confusionMatrix());

        ClassificationResult classificationResult = new ClassificationResult(_classifier, _testDataSubject, _numberOfSensors, _sensorList, _averageF1);
        return classificationResult;

    }


    public static ClassificationResult summarizeClassifierResults(ArrayList<ClassificationResult> classifierResults) {

        // average the f1 score
        double averageClassifierF1 = 0;
        for (ClassificationResult result : classifierResults) {
            averageClassifierF1 += result.averageF1Score;
        }
        averageClassifierF1 /= classifierResults.size();

        ClassificationResult old = classifierResults.get(0);
        String testDataSubject = "summary";

        ClassificationResult summaryResult = new ClassificationResult
                (old.classifier, testDataSubject, old.numberOfSensors, old.sensorList, averageClassifierF1);
        return summaryResult;

    }


    public String getClassifier() {
        return classifier;
    }

    public String getTestDataSubject() {
        return testDataSubject;
    }

    public int getNumberOfSensors() {
        return numberOfSensors;
    }

    public String getSensorList() {
        return sensorList;
    }

    public double getAverageF1Score() {
        return averageF1Score;
    }

    public static ClassificationResultF1Comperator getF1Comperator() {
        return f1Comperator;
    }


    public static class ClassificationResultF1Comperator implements Comparator<ClassificationResult> {
        public int compare(ClassificationResult c1, ClassificationResult c2) {

            if (Double.isNaN(c1.getAverageF1Score())) {
                if (Double.isNaN(c2.getAverageF1Score())) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (Double.isNaN(c2.getAverageF1Score())) {
                return -1;
            } else {
                return Double.compare(c2.averageF1Score, c1.averageF1Score);
            }

        }
    }


}
