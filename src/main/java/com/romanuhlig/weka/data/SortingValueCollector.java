package com.romanuhlig.weka.data;

import java.util.ArrayList;
import java.util.Comparator;

public class SortingValueCollector {


    private final ArrayList<Double> values = new ArrayList<>();

    private final double totalTimeForAllFrames;
    private final boolean scaleRunningTotalByFrameDuration;
    private double runningTotal = 0;

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
    }


    void sortValues() {
        if (!sortedByValue) {
            values.sort(valueComparator);
            sortedByValue = true;
            sortedByIncomeDate = false;
        }
    }

    public double getPercentile50Median() {
        return getPercentile(0.5);
    }

    public double getPercentile25() {
        return getPercentile(0.25);
    }

    public double getPercentile75() {
        return getPercentile(0.75);
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

}
