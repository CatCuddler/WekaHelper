package com.romanuhlig.weka.classification;

import com.romanuhlig.weka.controller.GlobalData;
import com.romanuhlig.weka.io.SensorPermutation;
import com.romanuhlig.weka.math.MathHelper;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ClassificationResult {

    private static ClassificationResultF1Comparator f1Comparator = new ClassificationResultF1Comparator();

    private final String classifier;
    private final String testDataSubject;
    private final int numberOfSensors;
    private final List<String> sensorList;
    private final double averageF1Score;
    private final double averageF1zeroNAN;
    private final double minAvgF1Person;
    private final double minimumF1Task;
    private static String[] headerForCSV;
    private String[] dataForCSV;
    private String sensorSummary;
    private final long timeTaken;
    private final double accuracy;


    private ClassificationResult(String classifier, String testDataSubject,
                                 int numberOfSensors, List<String> sensorList, String sensorSummary,
                                 double averageF1Score, double averageF1zeroNAN,
                                 double minimumF1Person, double minimumF1Task,
                                 double accuracy, long timeTaken) {

        // create header for CSV writing later on, MIND THE ORDER, has to stay the same as below
        if (headerForCSV == null) {
            LinkedList<String> headerList = new LinkedList<>();
            headerList = new LinkedList<>();
            headerList.add("Classifier");
            headerList.add("F1 average");
            headerList.add("F1 average (NAN as Zero)");
            headerList.add("F1 min Person");
            headerList.add("F1 min Task single Person");
            headerList.add("Number of Sensors");
            //            headerList.add("sensorList");
            for (String sensor : GlobalData.getAllAvailableSensors()) {
                headerList.add(sensor);
            }
            headerList.add("Sensor Summary");
            headerList.add("Subject");
            headerList.add("Accuracy");
            headerList.add("Time Taken");
            headerForCSV = headerList.toArray(new String[headerList.size()]);
        }

        // collect data for CSV writing later on, MIND THE ORDER
        LinkedList<String> dataForCSVList = new LinkedList<>();
        dataForCSVList.add(classifier);
        dataForCSVList.add(Double.toString(averageF1Score));
        dataForCSVList.add(Double.toString(averageF1zeroNAN));
        dataForCSVList.add(Double.toString(minimumF1Person));
        dataForCSVList.add(Double.toString(minimumF1Task));
        dataForCSVList.add(Integer.toString(numberOfSensors));
        //        dataForCSVList.add(sensorList);
        for (String sensor : GlobalData.getAllAvailableSensors()) {
            if (sensorList.contains(sensor)) {
                dataForCSVList.add(sensor);
            } else {
                dataForCSVList.add(" ");
            }
        }
        dataForCSVList.add(sensorSummary);
        dataForCSVList.add(testDataSubject);
        dataForCSVList.add(Double.toString(accuracy));
        dataForCSVList.add(Long.toString(timeTaken));
        dataForCSV = dataForCSVList.toArray(new String[dataForCSVList.size()]);

        // collect original data for comparisons
        this.classifier = classifier;
        this.testDataSubject = testDataSubject;
        this.numberOfSensors = numberOfSensors;
        this.sensorList = sensorList;
        this.averageF1Score = averageF1Score;
        this.averageF1zeroNAN = averageF1zeroNAN;
        this.minAvgF1Person = minimumF1Person;
        this.minimumF1Task = minimumF1Task;
        this.sensorSummary = sensorSummary;
        this.timeTaken = timeTaken;
        this.accuracy = accuracy;
    }

    public static ClassificationResult constructClassificationResultForSinglePerson(
            Evaluation evaluation, Classifier classifier, Instances instances,
            String subject, SensorPermutation sensorPermutation, long timeTaken) {


        String _classifier = classifier.getClass().getSimpleName();
        String _testDataSubject = subject;
        int _numberOfSensors = sensorPermutation.getNumberOfSensors();
        List<String> _sensorList = sensorPermutation.getIncludedSensors();
        String _sensorSummary = sensorPermutation.getSensorListRepresentation();

//        System.out.println("sensors: " + sensorPermutation.getNumberOfSensors());

        double _averageF1 = 0;
        double _averageF1zeroNAN = 0;
        double _minimumF1 = Double.POSITIVE_INFINITY;
        for (int i = 0; i < instances.numClasses(); i++) {
            _averageF1 += evaluation.fMeasure(i);
            _averageF1zeroNAN += MathHelper.getZeroIfNAN(evaluation.fMeasure(i));
            _minimumF1 = MathHelper.getMinimumWithNAN(_minimumF1, evaluation.fMeasure(i));

//            System.out.println("recall " + i + " " + evaluation.recall(i));
//            System.out.println("precision " + i + " " + evaluation.precision(i));
//            System.out.println("fmeasure " + i + " " + evaluation.fMeasure(i));
//            System.out.println("true pos rate " + i + " " + evaluation.truePositiveRate(i));
//            System.out.println("false neg rate " + i + " " + evaluation.falseNegativeRate(i));
//            System.out.println("true pos num " + i + " " + evaluation.numTruePositives(i));
//            System.out.println("false neg num " + i + " " + evaluation.numFalseNegatives(i));
        }
        _averageF1 /= instances.numClasses();
        _averageF1zeroNAN /= instances.numClasses();

        double _accuracy = evaluation.correct() / (evaluation.correct() + evaluation.incorrect());

        //        System.out.println("fmeasure avg: " + averageF1Score);
//        System.out.println("fmeasure Wei: " + evaluation.weightedFMeasure());
//        System.out.println("fmeasure uMA: " + evaluation.unweightedMacroFmeasure());
//        System.out.println("fmeasure uMI: " + evaluation.unweightedMicroFmeasure());
//        ConsoleHelper.printConfusionMatrix(evaluation.confusionMatrix());

        ClassificationResult classificationResult = new ClassificationResult
                (_classifier, _testDataSubject, _numberOfSensors, _sensorList, _sensorSummary,
                        _averageF1, _averageF1zeroNAN, _averageF1, _minimumF1, _accuracy, timeTaken);
        return classificationResult;

    }


    public static ClassificationResult summarizeClassifierResults(
            ArrayList<ClassificationResult> classifierResults) {

        // summarize the f1 scores
        double _averageClassifierF1 = 0;
        double _averageClassifierF1zeroNAN = 0;
        double _minAvgF1Person = Double.POSITIVE_INFINITY;
        double _minimumF1Task = Double.POSITIVE_INFINITY;
        long _timeTaken = 0;
        double _averageAccuracy = 0;
        for (ClassificationResult result : classifierResults) {
            _averageClassifierF1 += result.averageF1Score;
            _averageClassifierF1zeroNAN += result.averageF1zeroNAN;
            _minAvgF1Person = MathHelper.getMinimumWithNAN(_minAvgF1Person, result.minAvgF1Person);
            _minimumF1Task = MathHelper.getMinimumWithNAN(_minimumF1Task, result.minimumF1Task);
            _timeTaken += result.timeTaken;
            _averageAccuracy += result.accuracy;
        }
        _averageClassifierF1 /= classifierResults.size();
        _averageClassifierF1zeroNAN /= classifierResults.size();
        _averageAccuracy /= classifierResults.size();

        ClassificationResult old = classifierResults.get(0);
        String testDataSubject = "summary";

        ClassificationResult summaryResult = new ClassificationResult
                (old.classifier, testDataSubject, old.numberOfSensors, old.sensorList, old.sensorSummary,
                        _averageClassifierF1, _averageClassifierF1zeroNAN, _minAvgF1Person, _minimumF1Task,
                        _averageAccuracy, _timeTaken);
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

    public List<String> getSensorList() {
        return sensorList;
    }

    public double getAverageF1Score() {
        return averageF1Score;
    }

    public double getAverageF1zeroNAN() {
        return averageF1zeroNAN;
    }

    public double getMinAvgF1Person() {
        return minAvgF1Person;
    }

    public double getMinimumF1Task() {
        return minimumF1Task;
    }

    public static ClassificationResultF1Comparator getF1Comparator() {
        return f1Comparator;
    }


    public static class ClassificationResultF1Comparator implements Comparator<ClassificationResult> {
        public int compare(ClassificationResult c1, ClassificationResult c2) {

            if (Double.isNaN(c1.getMinimumF1Task())) {
                if (Double.isNaN(c2.getMinimumF1Task())) {

                    // if min f1 is NAN for both, do the same test for non-NAN-f1 instead
                    if (Double.isNaN(c1.getAverageF1zeroNAN())) {
                        if (Double.isNaN(c2.getAverageF1zeroNAN())) {
                            return 0;
                        } else {
                            return 1;
                        }
                    } else if (Double.isNaN(c2.getAverageF1zeroNAN())) {
                        return -1;
                    } else {
                        return Double.compare(c2.averageF1zeroNAN, c1.averageF1zeroNAN);
                    }

                } else {
                    return 1;
                }
            } else if (Double.isNaN(c2.getMinimumF1Task())) {
                return -1;
            } else {
                return Double.compare(c2.getMinimumF1Task(), c1.getMinimumF1Task());
            }

        }
    }


    public static String[] getHeaderForCSV() {
        return headerForCSV;
    }

    public String[] getDataForCSV() {
        return dataForCSV;
    }
}
