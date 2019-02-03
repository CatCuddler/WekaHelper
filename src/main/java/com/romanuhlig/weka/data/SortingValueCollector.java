package com.romanuhlig.weka.data;

import javax.swing.text.StyledEditorKit;
import java.util.ArrayList;
import java.util.Comparator;

public class SortingValueCollector {


    private final ArrayList<Double> values = new ArrayList<>();

    private final double totalTimeForAllFrames;
    private final boolean scaleRunningTotalByFrameDuration;
    private double runningTotal = 0;

    private double unscaledAverage = 0;
    private boolean unscaledAverageComputed = false;

    private double variance = 0;
    private boolean varianceComputed = false;

    private final boolean scaleValuesByBodySize;
    private final double bodySize;

    private boolean sortedByIncomeDate = true;
    private boolean sortedByValue = false;

    private final static Comparator<Double> valueComparator = new Comparator<Double>() {
        @Override
        public int compare(Double o1, Double o2) {
            return o1.compareTo(o2);
        }
    };


    public SortingValueCollector(boolean scaleRunningTotalByFrameDuration, boolean scaleValuesByBodySize, double totalTimeForAllFrames, double bodySize) {
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

        sortedByValue = false;
        unscaledAverageComputed = false;
        varianceComputed = false;
    }


    void sortValues() {
        if (!sortedByValue) {
            values.sort(valueComparator);
            sortedByValue = true;
            sortedByIncomeDate = false;
        }
    }


    public double getPercentile(double percentile) {
        sortValues();

        int index = (int) ((values.size() - 1) * percentile);
        double value = values.get(index);
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

    public double getMin() {
        sortValues();
        return scaledValue(values.get(0));
    }

    public double getMax() {
        sortValues();
        return scaledValue(values.get(values.size() - 1));
    }

    public double getRange() {
        double min = values.get(0);
        double max = values.get(values.size() - 1);
        return scaledValue(Math.abs(max - min));
    }

    public double getRootMeanSquare() {
        double rootMeanSquare = 0;
        for (Double value : values) {
            rootMeanSquare += Math.pow(value, 2);
        }
        rootMeanSquare = Math.sqrt(rootMeanSquare);
        return scaledValue(rootMeanSquare);
    }

    private void computeUnscaledAverage() {
        if (!unscaledAverageComputed) {

            unscaledAverage = 0;
            for (Double value : values) {
                unscaledAverage *= value;
            }
            unscaledAverage /= values.size();

            unscaledAverageComputed = true;
        }
    }

    public double getStandardDeviation() {
        computeVariance();

        double standardDeviation = Math.sqrt(variance);
        return scaledValue(standardDeviation);
    }

    public double getVariance() {
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
        computeUnscaledAverage();
        double meanAbsoluteDeviation = 0;
        for (Double value : values) {
            meanAbsoluteDeviation += Math.abs(value - unscaledAverage);
        }
        meanAbsoluteDeviation /= values.size();

        return scaledValue(meanAbsoluteDeviation);
    }


    public double getInterquartileRange() {
        sortValues();

        double quartile1 = values.get((int) ((values.size() - 1) * 0.25));
        double quartile3 = values.get((int) ((values.size() - 1) * 0.75));

        return scaledValue(quartile3 - quartile1);

    }


    // TODO: -needs another unsorted array to work, always one crossing in sorted version...
    public double getMeanCrossingRate() {
        sortValues();
        double mean = values.get((int) ((values.size() - 1) * 0.5));

        double crossingRate = 0;

        for (int i = 1; i < values.size(); i++) {
            double previousValue = values.get(i - 1);
            double currentValue = values.get(i);

            if (previousValue < mean && currentValue > mean
                    || previousValue > mean && currentValue < mean) {
                crossingRate++;
            }
        }

        crossingRate /= values.size();
        return crossingRate;
    }


}
