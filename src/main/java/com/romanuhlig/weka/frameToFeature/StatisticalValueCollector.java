package com.romanuhlig.weka.frameToFeature;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Collects data series and computes statistics for the collected data
 *
 * @author Roman Uhlig
 */
public class StatisticalValueCollector {


    private final ArrayList<Double> values = new ArrayList<>();
    private ArrayList<Double> sortedValues = new ArrayList<>();
    private boolean sortedValuesWereCreated = false;

    private final double bodySize;
    private final boolean scaleValuesByBodySize;
    private final double totalTimeForAllFrames;

    // the sum of all collected values, and whether each individual added value should be scaled
    // by the body size when it is being added (only for the running sum)
    private double runningSum = 0;
    private final boolean scaleRunningSumByFrameDuration;

    // the mean of all collected values, only calculated when needed
    private double unscaledMean = 0;
    private boolean unscaledMeanComputed = false;

    // the variance of all collected values, only calculated when needed
    private double variance = 0;
    private boolean varianceComputed = false;

    /**
     * Simple Comparator for the ordering of collected values by size
     */
    private final static Comparator<Double> valueComparator = new Comparator<Double>() {
        @Override
        public int compare(Double o1, Double o2) {
            return o1.compareTo(o2);
        }
    };

    /**
     * Creates an empty Value Collector with the given settings
     *
     * @param scaleRunningSumByFrameDuration
     * @param scaleValuesByBodySize
     * @param totalTimeForAllFrames
     * @param bodySize
     */
    public StatisticalValueCollector(boolean scaleRunningSumByFrameDuration, boolean scaleValuesByBodySize,
                                     double totalTimeForAllFrames, double bodySize) {
        this.scaleRunningSumByFrameDuration = scaleRunningSumByFrameDuration;
        this.totalTimeForAllFrames = totalTimeForAllFrames;
        this.scaleValuesByBodySize = scaleValuesByBodySize;
        this.bodySize = bodySize;
    }

    /**
     * Add a single new value
     * <p>
     * Values need to be added in (time) order
     *
     * @param value
     * @param frameDuration
     */
    public void addValue(double value, double frameDuration) {

        values.add(value);

        // if requested, scale the value by the duration of the frame
        if (scaleRunningSumByFrameDuration) {
            runningSum += value * frameDuration;
        } else {
            runningSum += value;
        }

        // after a new value was added, statistics have to be recreated when needed
        sortedValuesWereCreated = false;
        unscaledMeanComputed = false;
        varianceComputed = false;
    }

    /**
     * Create a sorted list of all collected values
     */
    private void createSortedValues() {
        if (!sortedValuesWereCreated) {
            sortedValues = new ArrayList<>(values);
            sortedValues.sort(valueComparator);
            sortedValuesWereCreated = true;
        }
    }

    /**
     * Set the lowest collected value to zero, and modify all other values by the same amount
     * <p>
     * For example, a list of (4,3,8) becomes (1,0,5), and a list of (4,-3,8) becomes (7,0,11)
     */
    public void adjustToLowestValueAsZero() {

        // determine lowest value
        double lowestValue = Double.MAX_VALUE;
        for (Double value : values) {
            if (value < lowestValue) {
                lowestValue = value;
            }
        }

        // adjust all values by the lowest value
        for (int i = 0; i < values.size(); i++) {
            values.set(i, values.get(i) - lowestValue);
        }
    }

    /**
     * Determine the requested percentile, after creating the sorted value list
     *
     * @param percentile
     * @return
     */
    public double sort_getPercentile(double percentile) {
        createSortedValues();

        // determine percentile
        int index = (int) ((sortedValues.size() - 1) * percentile);
        double value = sortedValues.get(index);
        return potentiallyScaledValue(value);
    }

    /**
     * Scale the value by body size if requested, or return it unchanged if not
     *
     * @param value
     * @return
     */
    private double potentiallyScaledValue(double value) {
        if (scaleValuesByBodySize) {
            return value / bodySize;
        } else {
            return value;
        }
    }

    /**
     * The mean of all collected values, scaled by the total time
     *
     * @return
     */
    public double getMeanScaledByTime() {
        return potentiallyScaledValue(runningSum / totalTimeForAllFrames);
    }

    /**
     * Get the minimum collected value, after creating the sorted value list
     *
     * @return
     */
    public double sort_getMin() {
        if (sortedValues.size() == 0) return 0;
        createSortedValues();
        return potentiallyScaledValue(sortedValues.get(0));
    }

    /**
     * Get the maximum collected value, after creating the sorted value list
     *
     * @return
     */
    public double sort_getMax() {
        createSortedValues();
        return potentiallyScaledValue(sortedValues.get(sortedValues.size() - 1));
    }

    /**
     * Get the absolute difference between the minimum and maximum collected value, after creating the sorted value list
     *
     * @return
     */
    public double sort_getRange() {
        createSortedValues();
        double min = sortedValues.get(0);
        double max = sortedValues.get(sortedValues.size() - 1);
        return potentiallyScaledValue(Math.abs(max - min));
    }

    /**
     * The root mean square of all collected values
     *
     * @return
     */
    public double getRootMeanSquare() {
        // = sqrt of the mean of the sum of all squares:
        // sum all squares
        double rootMeanSquare = 0;
        for (Double value : values) {
            rootMeanSquare += Math.pow(value, 2);
        }
        // mean (of the sum of all squares)
        rootMeanSquare /= values.size();
        // sqrt (of the mean of the sum of all squares)
        rootMeanSquare = Math.sqrt(rootMeanSquare);
        return potentiallyScaledValue(rootMeanSquare);
    }

    /**
     * Determine and set the unscaled mean of all collected values
     */
    private void computeUnscaledMean() {
        if (!unscaledMeanComputed) {

            unscaledMean = 0;
            for (Double value : values) {
                unscaledMean += value;
            }
            unscaledMean /= values.size();

            unscaledMeanComputed = true;
        }
    }

    /**
     * The standard deviation of all collected values
     * <p>
     * Also triggers computation of variance
     *
     * @return
     */
    public double getStandardDeviation() {
        computeVariance();
        double standardDeviation = Math.sqrt(variance);
        return potentiallyScaledValue(standardDeviation);
    }

    /**
     * The variance of all collected values
     *
     * @return
     */
    public double getVariance() {
        computeVariance();
        return potentiallyScaledValue(variance);
    }

    /**
     * Determine and set the variance of all collected values
     */
    private void computeVariance() {

        if (!varianceComputed) {
            computeUnscaledMean();

            variance = 0;
            for (Double value : values) {
                variance += Math.pow(value - unscaledMean, 2);
            }
            variance = variance / (values.size() - 1);

            varianceComputed = true;
        }
    }

    /**
     * The mean absolute deviation of all collected values
     *
     * @return
     */
    public double getMeanAbsoluteDeviation() {

        computeUnscaledMean();

        double meanAbsoluteDeviation = 0;
        for (Double value : values) {
            meanAbsoluteDeviation += Math.abs(value - unscaledMean);
        }
        meanAbsoluteDeviation /= values.size();

        return potentiallyScaledValue(meanAbsoluteDeviation);
    }

    /**
     * Get the interquartile range of all collected values, after creating the sorted value list
     *
     * @return
     */
    public double sort_getInterquartileRange() {
        createSortedValues();

        // q75 - q25
        double quartile1 = sortedValues.get((int) ((sortedValues.size() - 1) * 0.25));
        double quartile3 = sortedValues.get((int) ((sortedValues.size() - 1) * 0.75));

        return potentiallyScaledValue(quartile3 - quartile1);

    }

    /**
     * Get the mean crossing rate of all collected values, after creating the sorted value list
     *
     * @return
     */
    public double sort_getMeanCrossingRate() {
        createSortedValues();
        //double median = sortedValues.get((int) ((sortedValues.size() - 1) * 0.5));
        computeUnscaledMean();
        double mean = unscaledMean;

        // count instances where current and previous value are on opposite sides of mean
        double crossingRate = 0;
        for (int i = 1; i < values.size(); i++) {
            double previousValue = values.get(i - 1);
            double currentValue = values.get(i);

            if (previousValue < mean && currentValue > mean
                    || previousValue > mean && currentValue < mean) {
                crossingRate++;
            }
        }

        crossingRate /= (values.size() - 1);
        return crossingRate;
    }


}
