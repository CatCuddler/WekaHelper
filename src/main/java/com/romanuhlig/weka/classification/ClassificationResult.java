package com.romanuhlig.weka.classification;

import com.romanuhlig.weka.controller.GlobalData;
import com.romanuhlig.weka.io.SensorSubset;
import com.romanuhlig.weka.math.MathHelper;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.RandomCommittee;
import weka.core.Instances;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Summarizes results of training and testing a model
 *
 * @author Roman Uhlig
 */
public class ClassificationResult {


    // information about the evaluation results
    private final String classifier;
    private final String testDataSubject;
    private final int numberOfSensors;
    private final List<String> sensorList;
    private final double averageF1Score;
    private final double averagePrecision;
    private final double averageRecall;
    private final double averageTPRate;
    private final double averageFPRate;
    private final long timeTaken;
    private final double accuracy;
    private final double[] averageF1PerTask;
    private String sensorSummary;

    // bonus information for csv output
    private static String[] headerForCSV;
    private String[] dataForCSV;

    // formatting / organisation of data
    private static DecimalFormat tableDoubleFormatter;
    private static HashMap<String, String> sensorNamesForTable;

    // initialization before first use
    static {
        initializeTableDecimalFormatting();
        initializeTableSensorIdentifiers();
    }

    /**
     * Setup number formatting for tables to use "." as decimal separator
     */
    private static void initializeTableDecimalFormatting() {
        // fort table output, use "." as decimal separator regardless of locale, and round to six digits
        tableDoubleFormatter = new DecimalFormat("0.000000");
        DecimalFormatSymbols dfs = tableDoubleFormatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        tableDoubleFormatter.setDecimalFormatSymbols(dfs);
    }

    /**
     * Setup shortened names for sensors within tables
     */
    private static void initializeTableSensorIdentifiers() {
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

    /**
     * Construct classification result from given data
     *
     * @param classifier
     * @param testDataSubject
     * @param numberOfSensors
     * @param sensorList
     * @param sensorSummary
     * @param averageF1Score
     * @param averagePrecision
     * @param averageRecall
     * @param averageTPRate
     * @param averageFPRate
     * @param averageF1PerTask
     * @param accuracy
     * @param timeTaken
     */
    private ClassificationResult(String classifier, String testDataSubject,
                                 int numberOfSensors, List<String> sensorList, String sensorSummary,
                                 double averageF1Score,
                                 double averagePrecision, double averageRecall,
                                 double averageTPRate, double averageFPRate,
                                 double[] averageF1PerTask,
                                 double accuracy, long timeTaken) {

        // create header for CSV writing later on, MIND THE ORDER, has to stay the same as values below
        if (headerForCSV == null) {
            LinkedList<String> headerList = new LinkedList<>();
            headerList = new LinkedList<>();
            headerList.add("Classifier");
            headerList.add("F1");
            headerList.add("Precision");
            headerList.add("Recall");
            headerList.add("TP Rate");
            headerList.add("FP Rate");
            headerList.add("Number-of-sensors");
            for (String sensor : GlobalData.getAllAvailableSensors()) {
                headerList.add(getSensorNameForTable(sensor));
            }
            headerList.add("Sensor-summary");
            headerList.add("Subject");
            headerList.add("Accuracy");
            headerList.add("Time-taken");

            for (String activity : GlobalData.getAllActivities()) {
                headerList.add(activity);
            }

            headerForCSV = headerList.toArray(new String[headerList.size()]);
        }


        // collect data for CSV writing later on, MIND THE ORDER
        LinkedList<String> dataForCSVList = new LinkedList<>();
        dataForCSVList.add(classifier);
        dataForCSVList.add(f1ResultForTable(averageF1Score));
        dataForCSVList.add(f1ResultForTable(averagePrecision));
        dataForCSVList.add(f1ResultForTable(averageRecall));
        dataForCSVList.add(f1ResultForTable(averageTPRate));
        dataForCSVList.add(f1ResultForTable(averageFPRate));
        dataForCSVList.add(Integer.toString(numberOfSensors));
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

        for (int i = 0; i < averageF1PerTask.length; i++) {
            dataForCSVList.add(Double.toString(averageF1PerTask[i]));
        }

        dataForCSV = dataForCSVList.toArray(new String[dataForCSVList.size()]);

        // collect original data for comparisons
        this.classifier = classifier;
        this.testDataSubject = testDataSubject;
        this.numberOfSensors = numberOfSensors;
        this.sensorList = sensorList;
        this.averageF1Score = averageF1Score;
        this.averagePrecision = averagePrecision;
        this.averageRecall = averageRecall;
        this.averageTPRate = averageTPRate;
        this.averageFPRate = averageFPRate;
        this.sensorSummary = sensorSummary;
        this.timeTaken = timeTaken;
        this.accuracy = accuracy;
        this.averageF1PerTask = averageF1PerTask;
    }

    /**
     * Construct classification result for a single model produced for one subject
     *
     * @param evaluation
     * @param classifier
     * @param instances
     * @param subject
     * @param sensorSubset
     * @param timeTaken
     * @return
     */
    public static ClassificationResult constructClassificationResultForSinglePerson(
            Evaluation evaluation, Classifier classifier, Instances instances,
            String subject, SensorSubset sensorSubset, long timeTaken) {

        // retrieve basic information
        String _classifier = classifier.getClass().getSimpleName();
        if (classifier.getClass().equals(AdaBoostM1.class)) {
            _classifier = _classifier + '_' +
                    ((AdaBoostM1) classifier).getClassifier().getClass().getSimpleName();
        } else if (classifier.getClass().equals(Bagging.class)) {
            _classifier = _classifier + '_' +
                    ((Bagging) classifier).getClassifier().getClass().getSimpleName();
        } else if (classifier.getClass().equals(RandomCommittee.class)) {
            _classifier = _classifier + '_' +
                    ((RandomCommittee) classifier).getClassifier().getClass().getSimpleName();
        }

        String _testDataSubject = subject;
        int _numberOfSensors = sensorSubset.getNumberOfSensors();
        List<String> _sensorList = sensorSubset.getIncludedSensors();
        String _sensorSummary = sensorSubset.getSensorListRepresentation();

        // calculate specialized F1 score variants
        double _averageF1 = 0;
        double _averagePrecision = 0;
        double _averageRecall = 0;
        double _averageTPRate = 0;
        double _averageFPRate = 0;
        double[] _averageF1perTask = new double[instances.numClasses()];
        for (int i = 0; i < instances.numClasses(); i++) {
            _averageF1 += replaceNaNByZero(evaluation.fMeasure(i));
            _averagePrecision += replaceNaNByZero(evaluation.precision(i));
            _averageRecall += replaceNaNByZero(evaluation.recall(i));
            _averageTPRate += replaceNaNByZero(evaluation.truePositiveRate(i));
            _averageFPRate += replaceNaNByZero(evaluation.falsePositiveRate(i));
            _averageF1perTask[i] = replaceNaNByZero(evaluation.fMeasure(i));
        }
        _averageF1 /= instances.numClasses();
        _averagePrecision /= instances.numClasses();
        _averageRecall /= instances.numClasses();
        _averageTPRate /= instances.numClasses();
        _averageFPRate /= instances.numClasses();

        // calculate accuracy
        double _accuracy = evaluation.correct() / (evaluation.correct() + evaluation.incorrect());

        // return a newly constructed classification result, using the calculated values
        ClassificationResult classificationResult = new ClassificationResult
                (_classifier, _testDataSubject, _numberOfSensors, _sensorList, _sensorSummary,
                        _averageF1,
                        _averagePrecision, _averageRecall,
                        _averageTPRate, _averageFPRate,
                        _averageF1perTask,
                        _accuracy, timeTaken);
        return classificationResult;

    }

    /**
     * Combine multiple classification results into one
     * All of the original results must share the same metadata (e.g. Classifier, sensors used)
     *
     * @param classifierResults
     * @return
     */
    public static ClassificationResult summarizeClassifierResults(
            ArrayList<ClassificationResult> classifierResults) {

        // summarize the f1 scores
        double _averageClassifierF1 = 0;
        double _averageClassifierPrecision = 0;
        double _averageClassifierRecall = 0;
        double _averageClassifierTPRate = 0;
        double _averageClassifierFPRate = 0;
        long _timeTaken = 0;
        double _averageAccuracy = 0;
        double[] _averageF1perTask = new double[classifierResults.get(0).averageF1PerTask.length];
        // compute various averages
        for (ClassificationResult result : classifierResults) {
            _averageClassifierF1 += result.averageF1Score;
            _averageClassifierPrecision += result.averagePrecision;
            _averageClassifierRecall += result.averageRecall;
            _averageClassifierTPRate += result.averageTPRate;
            _averageClassifierFPRate += result.averageFPRate;
            _timeTaken += result.timeTaken;
            _averageAccuracy += result.accuracy;
            for (int i = 0; i < _averageF1perTask.length; i++) {
                _averageF1perTask[i] += result.averageF1PerTask[i];
            }
        }
        _averageClassifierF1 /= classifierResults.size();
        _averageClassifierPrecision /= classifierResults.size();
        _averageClassifierRecall /= classifierResults.size();
        _averageClassifierTPRate /= classifierResults.size();
        _averageClassifierFPRate /= classifierResults.size();
        _averageAccuracy /= classifierResults.size();
        for (int i = 0; i < _averageF1perTask.length; i++) {
            _averageF1perTask[i] /= classifierResults.size();
        }

        // take metadata from the previous results
        ClassificationResult old = classifierResults.get(0);

        // there is no single subject in any summary
        String testDataSubject = "summary";

        ClassificationResult summaryResult = new ClassificationResult
                (old.classifier, testDataSubject, old.numberOfSensors, old.sensorList, old.sensorSummary,
                        _averageClassifierF1,
                        _averageClassifierPrecision, _averageClassifierRecall,
                        _averageClassifierTPRate, _averageClassifierFPRate,
                        _averageF1perTask,
                        _averageAccuracy, _timeTaken);
        return summaryResult;

    }

    /**
     * Name of the classifier that was used
     *
     * @return
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Name of the test subject
     *
     * @return
     */
    public String getTestDataSubject() {
        return testDataSubject;
    }

    /**
     * The number of sensors used for the classification
     *
     * @return
     */
    public int getNumberOfSensors() {
        return numberOfSensors;
    }

    /**
     * Names of all sensors used for the classification
     *
     * @return
     */
    public List<String> getSensorList() {
        return sensorList;
    }

    /**
     * The average overall F1 score
     *
     * @return
     */
    public double getAverageF1Score() {
        return averageF1Score;
    }

    /**
     * The header for all values that get written to csv files from this class
     * Compatible with getDataForCSV
     * @return
     */
    public static String[] getHeaderForCSV() {
        return headerForCSV;
    }

    /**
     * Data that represents a line in a csv file
     * Compatible with getHeaderForCSV
     * @return
     */
    public String[] getDataForCSV() {
        return dataForCSV;
    }

    /**
     * Replace NaN values by Zero
     * @param value
     * @return
     */
    private static double replaceNaNByZero(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        } else {
            return value;
        }
    }

    /**
     * Reformats and renames certain values for the csv output
     * @param value
     * @return
     */
    private String f1ResultForTable(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        } else if (value == 1) {
            return "1.0";
        } else {
            return ClassificationResult.tableDoubleFormatter.format(value);
        }
    }

    /**
     * A helper string for the color formatting of results within latex code
     * @param value
     * @return
     */
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

    /**
     * Shortened sensor names for output tables
     * @param sensor
     * @return
     */
    private String getSensorNameForTable(String sensor) {
        return sensorNamesForTable.get(sensor);
    }
}