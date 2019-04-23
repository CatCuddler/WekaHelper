package com.romanuhlig.weka.classification;

import com.romanuhlig.weka.controller.GlobalData;
import com.romanuhlig.weka.io.SensorSubset;
import com.romanuhlig.weka.math.MathHelper;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
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
    // for this summary statistic, invalid F1 scores are treated as zero
    private final double averageF1zeroNAN;
    private final double minAvgF1Person;
    private final double minimumF1PersonTask;
    private final long timeTaken;
    private final double accuracy;
    private final double[] averageF1PerTask;
    private String sensorSummary;

    // bonus information for csv output
    private static String[] headerForCSV;
    private String[] dataForCSV;

    // formatting / organisation of data
    private static ClassificationResultF1Comparator f1Comparator = new ClassificationResultF1Comparator();
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
     * @param averageF1zeroNAN
     * @param minimumF1Person
     * @param averageF1PerTask
     * @param minimumAverageF1perTask
     * @param minimumF1PersonTask
     * @param accuracy
     * @param timeTaken
     */
    private ClassificationResult(String classifier, String testDataSubject,
                                 int numberOfSensors, List<String> sensorList, String sensorSummary,
                                 double averageF1Score, double averageF1zeroNAN,
                                 double minimumF1Person, double[] averageF1PerTask,
                                 double minimumAverageF1perTask, double minimumF1PersonTask,
                                 double accuracy, long timeTaken) {

        // create header for CSV writing later on, MIND THE ORDER, has to stay the same as values below
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
        String _testDataSubject = subject;
        int _numberOfSensors = sensorSubset.getNumberOfSensors();
        List<String> _sensorList = sensorSubset.getIncludedSensors();
        String _sensorSummary = sensorSubset.getSensorListRepresentation();

        // calculate specialized F1 score variants
        double _averageF1 = 0;
        double _averageF1zeroNAN = 0;
        double _minimumF1 = Double.POSITIVE_INFINITY;
        double[] _averageF1perTask = new double[instances.numClasses()];
        for (int i = 0; i < instances.numClasses(); i++) {
            _averageF1 += evaluation.fMeasure(i);
            _averageF1zeroNAN += MathHelper.getZeroIfNAN(evaluation.fMeasure(i));
            _minimumF1 = MathHelper.getMinimumWithNAN(_minimumF1, evaluation.fMeasure(i));
            _averageF1perTask[i] = evaluation.fMeasure(i);
        }
        _averageF1 /= instances.numClasses();
        _averageF1zeroNAN /= instances.numClasses();

        // calculate accuracy
        double _accuracy = evaluation.correct() / (evaluation.correct() + evaluation.incorrect());

        // return a newly constructed classification result, using the calculated values
        ClassificationResult classificationResult = new ClassificationResult
                (_classifier, _testDataSubject, _numberOfSensors, _sensorList, _sensorSummary,
                        _averageF1, _averageF1zeroNAN, _averageF1, _averageF1perTask, _minimumF1, _minimumF1,
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
        double _averageClassifierF1zeroNAN = 0;
        double _minAvgF1Person = Double.POSITIVE_INFINITY;
        double _minimumF1PersonTask = Double.POSITIVE_INFINITY;
        long _timeTaken = 0;
        double _averageAccuracy = 0;
        double[] _averageF1perTask = new double[classifierResults.get(0).averageF1PerTask.length];
        // compute various averages
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

        // take metadata from the previous results
        ClassificationResult old = classifierResults.get(0);

        // there is no single subject in any summary
        String testDataSubject = "summary";

        ClassificationResult summaryResult = new ClassificationResult
                (old.classifier, testDataSubject, old.numberOfSensors, old.sensorList, old.sensorSummary,
                        _averageClassifierF1, _averageClassifierF1zeroNAN, _minAvgF1Person,
                        _averageF1perTask, _minimumAverageF1PerTask, _minimumF1PersonTask,
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
     * The average overall F1 score, if non-computable F1 scores are treated as zero
     *
     * @return
     */
    public double getAverageF1zeroNAN() {
        return averageF1zeroNAN;
    }

    /**
     * The minimum average F1 score for any subject
     *
     * @return
     */
    public double getMinAvgF1Person() {
        return minAvgF1Person;
    }

    /**
     * The minimum F1 score across all subjects and exercises
     *
     * @return
     */
    public double getMinimumF1PersonTask() {
        return minimumF1PersonTask;
    }

    /**
     * Orders ClassificationResults based primarily on their minimum F1 score
     *
     * @return
     */
    public static ClassificationResultF1Comparator getF1Comparator() {
        return f1Comparator;
    }

    /**
     * Orders ClassificationResults based primarily on their minimum F1 score
     */
    public static class ClassificationResultF1Comparator implements Comparator<ClassificationResult> {
        public int compare(ClassificationResult c1, ClassificationResult c2) {

            if (Double.isNaN(c1.getMinimumF1PersonTask())) {
                if (Double.isNaN(c2.getMinimumF1PersonTask())) {

                    // if minimum f1 is NAN for both, base comparison on the average F1 score with NAN treated as zero
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
                    // only first value is NAN
                    return 1;
                }
            } else if (Double.isNaN(c2.getMinimumF1PersonTask())) {
                // only second value is NAN
                return -1;
            } else {
                // use the minimum F1 scores for comparison if both are valid
                return Double.compare(c2.getMinimumF1PersonTask(), c1.getMinimumF1PersonTask());
            }

        }
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
     * Reformats and renames certain values for the csv output
     * @param value
     * @return
     */
    private String f1ResultForTable(double value) {
        if (Double.isNaN(value)) {
            return "\\tableIncalculable"; //"incalculable";
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
