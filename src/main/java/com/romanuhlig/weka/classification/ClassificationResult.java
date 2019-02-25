package com.romanuhlig.weka.classification;

import com.romanuhlig.weka.controller.GlobalData;
import com.romanuhlig.weka.io.SensorPermutation;
import com.romanuhlig.weka.math.MathHelper;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class ClassificationResult {

    private static ClassificationResultF1Comparator f1Comparator = new ClassificationResultF1Comparator();

    private final String classifier;
    private final String testDataSubject;
    private final int numberOfSensors;
    private final List<String> sensorList;
    private final double averageF1Score;
    private final double averageF1zeroNAN;
    private final double minAvgF1Person;
    private final double minimumF1PersonTask;
    private static String[] headerForCSV;
    private String[] dataForCSV;
    private String sensorSummary;
    private final long timeTaken;
    private final double accuracy;
    private final double[] averageF1PerTask;
    private static final DecimalFormat tableDoubleFormatter;
    private static final HashMap<String, String> sensorNamesForTable;

    static {
        // fort table output, use "." as decimal separator regardless of locale, and round to six digits
        tableDoubleFormatter = new DecimalFormat("0.000000");
        DecimalFormatSymbols dfs = tableDoubleFormatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        tableDoubleFormatter.setDecimalFormatSymbols(dfs);

        // use shorter identifiers for table output of sensors
        sensorNamesForTable = new HashMap<>();
        sensorNamesForTable.put("head", "He");
        sensorNamesForTable.put("hip", "Hi");
        sensorNamesForTable.put("lFoot", "lF");
        sensorNamesForTable.put("lForeArm", "lFA");
        sensorNamesForTable.put("lHand", "lH");
        sensorNamesForTable.put("lLeg", "lL");
        sensorNamesForTable.put("rArm", "rA");
        sensorNamesForTable.put("rFoot", "rF");
        sensorNamesForTable.put("rForeArm", "rFA");
        sensorNamesForTable.put("rHand", "rH");
        sensorNamesForTable.put("rLeg", "rL");
        sensorNamesForTable.put("spine", "Sp");

    }

    private ClassificationResult(String classifier, String testDataSubject,
                                 int numberOfSensors, List<String> sensorList, String sensorSummary,
                                 double averageF1Score, double averageF1zeroNAN,
                                 double minimumF1Person, double[] averageF1PerTask,
                                 double minimumAverageF1perTask, double minimumF1PersonTask,
                                 double accuracy, long timeTaken) {

        // create header for CSV writing later on, MIND THE ORDER, has to stay the same as below
        if (headerForCSV == null) {
            LinkedList<String> headerList = new LinkedList<>();
            headerList = new LinkedList<>();
            headerList.add("Classifier");
            headerList.add("F1-avg");
            headerList.add("F1-avg-nan-as-zero");
            headerList.add("F1-min-subject-avg");
            headerList.add("F1-min-task-avg");
            headerList.add("F1-min-single-task-subject");
            headerList.add("Number-of-sensors");
            //            headerList.add("sensorList");
            for (String sensor : GlobalData.getAllAvailableSensors()) {
                headerList.add(getSensorNameForTable(sensor));
            }
            headerList.add("Sensor-summary");
            headerList.add("Subject");
            headerList.add("Accuracy");
            headerList.add("Time-taken");
            headerList.add("color-F1-avg");
            headerList.add("color-F1-avg-nan-as-zero");
            headerList.add("color-F1-min-subject-avg");
            headerList.add("color-F1-min-task-avg");
            headerList.add("color-F1-min-single-task-subject");
            headerList.add("color-Accuracy");

            headerForCSV = headerList.toArray(new String[headerList.size()]);
        }


        // collect data for CSV writing later on, MIND THE ORDER
        LinkedList<String> dataForCSVList = new LinkedList<>();
        dataForCSVList.add(classifier);
        dataForCSVList.add(f1ResultForTable(averageF1Score));
        dataForCSVList.add(f1ResultForTable(averageF1zeroNAN));
        dataForCSVList.add(f1ResultForTable(minimumF1Person));
        dataForCSVList.add(f1ResultForTable(minimumAverageF1perTask));
        dataForCSVList.add(f1ResultForTable(minimumF1PersonTask));
        dataForCSVList.add(Integer.toString(numberOfSensors));
        //        dataForCSVList.add(sensorList);
        for (String sensor : GlobalData.getAllAvailableSensors()) {
            if (sensorList.contains(sensor)) {
                dataForCSVList.add(getSensorNameForTable(sensor));
            } else {
                dataForCSVList.add(" ");
            }
        }
        dataForCSVList.add(sensorSummary);
        dataForCSVList.add(testDataSubject);
        dataForCSVList.add(f1ResultForTable(accuracy));
        dataForCSVList.add(Long.toString(timeTaken));
        dataForCSVList.add(f1ColorForTable(averageF1Score));
        dataForCSVList.add(f1ColorForTable(averageF1zeroNAN));
        dataForCSVList.add(f1ColorForTable(minimumF1Person));
        dataForCSVList.add(f1ColorForTable(minimumAverageF1perTask));
        dataForCSVList.add(f1ColorForTable(minimumF1PersonTask));
        dataForCSVList.add(f1ColorForTable(accuracy));

        dataForCSV = dataForCSVList.toArray(new String[dataForCSVList.size()]);

        // collect original data for comparisons
        this.classifier = classifier;
        this.testDataSubject = testDataSubject;
        this.numberOfSensors = numberOfSensors;
        this.sensorList = sensorList;
        this.averageF1Score = averageF1Score;
        this.averageF1zeroNAN = averageF1zeroNAN;
        this.minAvgF1Person = minimumF1Person;
        this.minimumF1PersonTask = minimumF1PersonTask;
        this.sensorSummary = sensorSummary;
        this.timeTaken = timeTaken;
        this.accuracy = accuracy;
        this.averageF1PerTask = averageF1PerTask;
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
        double[] _averageF1perTask = new double[instances.numClasses()];
        for (int i = 0; i < instances.numClasses(); i++) {
            _averageF1 += evaluation.fMeasure(i);
            _averageF1zeroNAN += MathHelper.getZeroIfNAN(evaluation.fMeasure(i));
            _minimumF1 = MathHelper.getMinimumWithNAN(_minimumF1, evaluation.fMeasure(i));
            _averageF1perTask[i] = evaluation.fMeasure(i);
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
                        _averageF1, _averageF1zeroNAN, _averageF1, _averageF1perTask, _minimumF1, _minimumF1,
                        _accuracy, timeTaken);
        return classificationResult;

    }


    public static ClassificationResult summarizeClassifierResults(
            ArrayList<ClassificationResult> classifierResults) {

        // summarize the f1 scores
        double _averageClassifierF1 = 0;
        double _averageClassifierF1zeroNAN = 0;
        double _minAvgF1Person = Double.POSITIVE_INFINITY;
        double _minimumF1PersonTask = Double.POSITIVE_INFINITY;
        long _timeTaken = 0;
        double _averageAccuracy = 0;
        double[] _averageF1perTask = new double[classifierResults.get(0).averageF1PerTask.length];
        for (ClassificationResult result : classifierResults) {
            _averageClassifierF1 += result.averageF1Score;
            _averageClassifierF1zeroNAN += result.averageF1zeroNAN;
            _minAvgF1Person = MathHelper.getMinimumWithNAN(_minAvgF1Person, result.minAvgF1Person);
            _minimumF1PersonTask = MathHelper.getMinimumWithNAN(_minimumF1PersonTask, result.minimumF1PersonTask);
            _timeTaken += result.timeTaken;
            _averageAccuracy += result.accuracy;
            for (int i = 0; i < _averageF1perTask.length; i++) {
                _averageF1perTask[i] += result.averageF1PerTask[i];
            }
        }
        _averageClassifierF1 /= classifierResults.size();
        _averageClassifierF1zeroNAN /= classifierResults.size();
        _averageAccuracy /= classifierResults.size();
        for (int i = 0; i < _averageF1perTask.length; i++) {
            _averageF1perTask[i] /= classifierResults.size();
        }

        // determine minimum average F1 per task
        double _minimumAverageF1PerTask = Double.MAX_VALUE;
        for (int i = 0; i < _averageF1perTask.length; i++) {
            _minimumAverageF1PerTask = MathHelper.getMinimumWithNAN(_minimumAverageF1PerTask, _averageF1perTask[i]);
        }

        ClassificationResult old = classifierResults.get(0);
        String testDataSubject = "summary";

        ClassificationResult summaryResult = new ClassificationResult
                (old.classifier, testDataSubject, old.numberOfSensors, old.sensorList, old.sensorSummary,
                        _averageClassifierF1, _averageClassifierF1zeroNAN, _minAvgF1Person,
                        _averageF1perTask, _minimumAverageF1PerTask, _minimumF1PersonTask,
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

    public double getMinimumF1PersonTask() {
        return minimumF1PersonTask;
    }

    public static ClassificationResultF1Comparator getF1Comparator() {
        return f1Comparator;
    }


    public static class ClassificationResultF1Comparator implements Comparator<ClassificationResult> {
        public int compare(ClassificationResult c1, ClassificationResult c2) {

            if (Double.isNaN(c1.getMinimumF1PersonTask())) {
                if (Double.isNaN(c2.getMinimumF1PersonTask())) {

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
            } else if (Double.isNaN(c2.getMinimumF1PersonTask())) {
                return -1;
            } else {
                return Double.compare(c2.getMinimumF1PersonTask(), c1.getMinimumF1PersonTask());
            }

        }
    }


    public static String[] getHeaderForCSV() {
        return headerForCSV;
    }

    public String[] getDataForCSV() {
        return dataForCSV;
    }


    private String f1ResultForTable(double value) {
        if (Double.isNaN(value)) {
            return "invalid"; //"incalculable";
        } else if (value == 1) {
            return "1.0";
        } else {
            return ClassificationResult.tableDoubleFormatter.format(value);
        }
    }

    private String f1ColorForTable(double value) {
        if (Double.isNaN(value)) {
            return "\\cellcolor{tbTerrible}";
        } else if (value == 1) {
            return "\\cellcolor{tbPerfect}";
        } else {
            double breakPoint = 0.99;

            if (value <= breakPoint) {
                double colorProgression = 100 * (value / breakPoint);
                return "\\cellcolor{tbAverage!" + colorProgression + "!tbBad}";
            } else {
                double colorProgression = 100 * ((value - breakPoint) / (1 - breakPoint));
                return "\\cellcolor{tbGood!" + colorProgression + "!tbAverage}";
            }
        }
    }

    private String getSensorNameForTable(String sensor) {
        return sensorNamesForTable.get(sensor);
    }
}
