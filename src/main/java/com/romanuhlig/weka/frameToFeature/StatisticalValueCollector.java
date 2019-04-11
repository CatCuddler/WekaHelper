package com.romanuhlig.weka.frameToFeature;

import java.util.ArrayList;
import java.util.Comparator;

public class StatisticalValueCollector {


    private final ArrayList<Double> values = new ArrayList<>();
    private ArrayList<Double> sortedValues = new ArrayList<>();

    private final double totalTimeForAllFrames;
    private final boolean scaleRunningTotalByFrameDuration;
    private double runningTotal = 0;

    private double unscaledAverage = 0;
    private boolean unscaledAverageComputed = false;

    private double variance = 0;
    private boolean varianceComputed = false;

    private final boolean scaleValuesByBodySize;
    private final double bodySize;

    private boolean createdSortedValues = false;

    private final static Comparator<Double> valueComparator = new Comparator<Double>() {
        @Override
        public int compare(Double o1, Double o2) {
            return o1.compareTo(o2);
        }
    };


    public StatisticalValueCollector(boolean scaleRunningTotalByFrameDuration, boolean scaleValuesByBodySize, double totalTimeForAllFrames, double bodySize) {
        this.scaleRunningTotalByFrameDuration = scaleRunningTotalByFrameDuration;
        this.totalTimeForAllFrames = totalTimeForAllFrames;
        this.scaleValuesByBodySize = scaleValuesByBodySize;
        this.bodySize = bodySize;
    }

    public void addValue(double value, double frameDuration) {

        values.add(value);

        if (scaleRunningTotalByFrameDuration) {
            runningTotal += value * frameDuration;
        } else {
            runningTotal += value;
        }

        createdSortedValues = false;
        unscaledAverageComputed = false;
        varianceComputed = false;
    }


    private void createSortedValues() {
        if (!createdSortedValues) {
            sortedValues = new ArrayList<>(values);
            sortedValues.sort(valueComparator);
            createdSortedValues = true;
        }
    }


    public void adjustToLowestValueAsZero() {

        double lowestValue = Double.MAX_VALUE;
        for (Double value : values) {
            if (value < lowestValue) {
                lowestValue = value;
            }
        }
        for (int i = 0; i < values.size(); i++) {
            values.set(i, values.get(i) - lowestValue);
        }


    }


    public double sort_getPercentile(double percentile) {
        createSortedValues();

        int index = (int) ((sortedValues.size() - 1) * percentile);
        double value = sortedValues.get(index);
        return scaledValue(value);
    }

    private double scaledValue(double value) {
        if (scaleValuesByBodySize) {
            return value / bodySize;
        } else {
            return value;
        }
    }

    public double getAverage() {
        return scaledValue(runningTotal / totalTimeForAllFrames);
    }

    public double sort_getMin() {
        createSortedValues();
        return scaledValue(sortedValues.get(0));
    }

    public double sort_getMax() {
        createSortedValues();
        return scaledValue(sortedValues.get(sortedValues.size() - 1));
    }

    public double sort_getMaxAbsolute() {
        createSortedValues();
        return Math.max(Math.abs(sort_getMin()), Math.abs(sort_getMax()));
    }


    public double sort_getRange() {
        createSortedValues();
        double min = sortedValues.get(0);
        double max = sortedValues.get(sortedValues.size() - 1);
        return scaledValue(Math.abs(max - min));
    }

    public double getRootMeanSquare() {
        // sqrt of the average of all squared values
        double rootMeanSquare = 0;
        // squares
        for (Double value : values) {
            rootMeanSquare += Math.pow(value, 2);
        }
        // average
        rootMeanSquare /= values.size();
        // sqrt
        rootMeanSquare = Math.sqrt(rootMeanSquare);
        return scaledValue(rootMeanSquare);
    }

    private void computeUnscaledAverage() {
        if (!unscaledAverageComputed) {

            unscaledAverage = 0;
            for (Double value : values) {
                unscaledAverage += value;
            }
            unscaledAverage /= values.size();

            unscaledAverageComputed = true;
        }
    }

    public double getStandardDeviation() {
        // sqrt of average squared distance from average
        computeVariance();

        double standardDeviation = Math.sqrt(variance);
        return scaledValue(standardDeviation);
    }

    public double getVariance() {
        // average squared distance from average
        computeVariance();

        return scaledValue(variance);
    }

    private void computeVariance() {

        if (!varianceComputed) {
            computeUnscaledAverage();

            variance = 0;
            for (Double value : values) {
                variance += Math.pow(value - unscaledAverage, 2);
            }
            variance = variance / (values.size() - 1);

            varianceComputed = true;
        }

    }

    public double getMeanAbsoluteDeviation() {
        // average distance from average
        computeUnscaledAverage();
        double meanAbsoluteDeviation = 0;
        for (Double value : values) {
            meanAbsoluteDeviation += Math.abs(value - unscaledAverage);
        }
        meanAbsoluteDeviation /= values.size();

        return scaledValue(meanAbsoluteDeviation);
    }


    public double sort_getInterquartileRange() {
        // q75 - q25
        createSortedValues();

        double quartile1 = sortedValues.get((int) ((sortedValues.size() - 1) * 0.25));
        double quartile3 = sortedValues.get((int) ((sortedValues.size() - 1) * 0.75));

        return scaledValue(quartile3 - quartile1);

    }


    public double getMedianCrossingRate() {
        createSortedValues();

        double median = sortedValues.get((int) ((sortedValues.size() - 1) * 0.5));

        double crossingRate = 0;

        for (int i = 1; i < values.size(); i++) {
            double previousValue = values.get(i - 1);
            double currentValue = values.get(i);

            if (previousValue < median && currentValue > median
                    || previousValue > median && currentValue < median) {
                crossingRate++;
            }
        }

        crossingRate /= (values.size() - 1);
        return crossingRate;
    }


}
