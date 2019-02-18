package com.romanuhlig.weka.classification;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class AddingConfusionMatrix {

    double[][] values;
    String[] tasks;
    Instances instances;

    private static final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();


    public void addResults(double[][] newValues, Instances trainingData) {

        if (values == null) {
            values = newValues;

            tasks = new String[trainingData.classAttribute().numValues()];
            for (int i = 0; i < trainingData.classAttribute().numValues(); i++) {
                tasks[i] = trainingData.classAttribute().value(i);
            }

        } else {
            for (int i = 0; i < values.length; i++) {
                for (int k = 0; k < values.length; k++) {
                    values[i][k] += newValues[i][k];
                }
            }
        }


    }

    public String toOutputString() {
        StringBuilder stringBuilder = new StringBuilder();

        int columnWidth = getDigitsInHighestNumber() + 1;

        // heading
        stringBuilder.append("=== Confusion Matrix summary (custom output) === ");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());

        // first line (header)
        for (int i = 0; i < tasks.length; i++) {
            stringBuilder.append(prependToLength(alphabet[i], columnWidth));
        }
        stringBuilder.append("     <-- classified as");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());


        for (int line = 0; line < values.length; line++) {
            for (int column = 0; column < values.length; column++) {
                stringBuilder.append(prependToLength(values[line][column], columnWidth));
            }

            stringBuilder.append("     ");
            stringBuilder.append(alphabet[line]);
            stringBuilder.append(" = ");
            stringBuilder.append(tasks[line]);
            stringBuilder.append(System.lineSeparator());
        }


        return stringBuilder.toString();

    }


    private int getDigitsInHighestNumber() {
        double highestNumber = 0;
        for (int i = 0; i < values.length; i++) {
            for (int k = 0; k < values.length; k++) {
                if (values[i][k] > highestNumber) {
                    highestNumber = values[i][k];
                }
            }
        }

        return Integer.toString((int) highestNumber).length();
    }

    private String prependToLength(String original, int goalLength) {

        int originalLength = original.length();
        String prependedString = "";
        for (int i = 0; i < goalLength - originalLength; i++) {
            prependedString += " ";
        }

        prependedString += original;

        return prependedString;

    }

    private String prependToLength(char original, int goalLength) {
        return prependToLength("" + original, goalLength);
    }

    private String prependToLength(int original, int goalLength) {
        if (original != 0) {
            return prependToLength("" + original, goalLength);
        } else {
            return prependToLength(".", goalLength);
        }

    }

    private String prependToLength(double original, int goalLength) {
        if (original != 0) {
            return prependToLength((int) original, goalLength);
        } else {
            return prependToLength(".", goalLength);
        }
    }


}
