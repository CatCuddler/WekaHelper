package com.romanuhlig.weka.classification;

import weka.core.Instances;
import java.util.HashMap;

/**
 * Adds up confusion matrices, and outputs them to latex code or plaintext
 *
 * @author Roman Uhlig
 */
public class AddingConfusionMatrix {

    // confusion matrix data
    private double[][] values;
    private String[] classNames;

    // alternative class names to be used for text output
    private static HashMap<String, String> classNameForLines = new HashMap<>();
    private static HashMap<String, String> classNameForColumns = new HashMap<>();

    // lookup array required to assign letters to columns in array output
    private static final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();


    static {
        initializeAlternativeClassNames();
    }

    private static void initializeAlternativeClassNames() {
        classNameForLines.put("jogging", "Jogging");
        classNameForLines.put("kick", "Kick");
        classNameForLines.put("kickPunch", "Kick-Punch");
        classNameForLines.put("lateralBounding", "Lateral Bounding");
        classNameForLines.put("lunges", "Lunges");
        classNameForLines.put("punch", "Punch");
        classNameForLines.put("sitting", "Sitting");
        classNameForLines.put("squats", "Squats");
        classNameForLines.put("standing", "Standing");
        classNameForLines.put("walking", "Walking");
        classNameForLines.put("punch", "Punch");

        classNameForColumns.put("jogging", "Jogging");
        classNameForColumns.put("kick", "Kick");
        classNameForColumns.put("kickPunch", "Kick-Punch");
        classNameForColumns.put("lateralBounding", "Lat. Bound.");
        classNameForColumns.put("lunges", "Lunges");
        classNameForColumns.put("punch", "Punch");
        classNameForColumns.put("sitting", "Sitting");
        classNameForColumns.put("squats", "Squats");
        classNameForColumns.put("standing", "Standing");
        classNameForColumns.put("walking", "Walking");
        classNameForColumns.put("punch", "Punch");
    }

    /**
     * Add values of new confusion matrix to this one
     *
     * @param newValues    values of the new confusion matrix
     * @param trainingData the instances used to train the model which produced the new confusion matrix
     */
    public void addResults(double[][] newValues, Instances trainingData) {

        // initialize class names if this is the first confusion matrix to be added
        if (values == null) {

            // values can just be assigned instead of added for the first matrix
            values = newValues;

            classNames = new String[trainingData.classAttribute().numValues()];
            for (int i = 0; i < trainingData.classAttribute().numValues(); i++) {
                classNames[i] = trainingData.classAttribute().value(i);
            }

        } else {
            // otherwise, add new to existing values
            for (int i = 0; i < values.length; i++) {
                for (int k = 0; k < values.length; k++) {
                    values[i][k] += newValues[i][k];
                }
            }
        }
    }

    /**
     * Alternative name for a given class, to be used in its stead when outputting to a row label
     *
     * @param standardName
     * @return
     */
    private String lineTaskName(String standardName) {
        return classNameForLines.get(standardName);
    }

    /**
     * Alternative name for a given class, to be used in its stead when outputting to a column label
     *
     * @param standardName
     * @return
     */
    private String columnTaskName(String standardName) {
        return classNameForColumns.get(standardName);
    }

    /**
     * Plaintext representation of this confusion matrix
     *
     * @return
     */
    public String toOutputString() {
        StringBuilder stringBuilder = new StringBuilder();

        int columnWidth = getDigitsInHighestNumber() + 1;

        // heading above table
        stringBuilder.append("=== Confusion Matrix summary (custom output) === ");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());

        // first line (header)
        for (int i = 0; i < classNames.length; i++) {
            stringBuilder.append(prependToLength(alphabet[i], columnWidth));
        }
        stringBuilder.append("     <-- classified as");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());

        // values and row labels
        for (int line = 0; line < values.length; line++) {
            for (int column = 0; column < values.length; column++) {
                stringBuilder.append(prependToLength(values[line][column], columnWidth));
            }

            stringBuilder.append("     ");
            stringBuilder.append(alphabet[line]);
            stringBuilder.append(" = ");
            stringBuilder.append(classNames[line]);
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }

    /**
     * Latex code representation of this confusion matrix
     *
     * @return
     */
    public String toOutputStringLatex() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\\begin{table}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("\\begin{adjustbox}{max width=\\confusionMatrixWidth,center}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("\\begin{tabularx}{\\textwidth}{c|l|");
        for (int i = 0; i < classNames.length; i++) {
            stringBuilder.append("C{\\confusionMatrixColumnWidth}|");
        }
        stringBuilder.append("}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append(
                "\\multicolumn{1}{l}{} & \\multicolumn{1}{l}{} & \\multicolumn{"
                        + classNames.length
                        + "}{c}{\\textbf{Predicted Class}}");
        stringBuilder.append("\\\\");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("\\hhline{*{2}{~}|*{" + classNames.length + "}{-|}}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("\\multicolumn{1}{l}{} & ");
        for (int i = 0; i < classNames.length; i++) {
            stringBuilder.append("& \\rotatebox{90}{\\hspace{1pt}" + columnTaskName(classNames[i]) + "\\hspace{6pt}}");
        }
        stringBuilder.append("\\\\");
        stringBuilder.append(System.lineSeparator());


        stringBuilder.append("\\hhline{*{1}{~}|*{" + (classNames.length + 1) + "}{-|}}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("\\multirow{" + classNames.length + "}{*}{\\rotatebox{90}{\\textbf{Actual Class}\\hspace{8pt}}} ");
        stringBuilder.append(System.lineSeparator());


        for (int i = 0; i < classNames.length; i++) {
            stringBuilder.append(" & " + lineTaskName(classNames[i]));

            // determine overall number of data points for this task
            double taskSum = 0;
            for (int v = 0; v < values[i].length; v++) {
                taskSum += values[i][v];
            }


            for (int v = 0; v < values[i].length; v++) {

                stringBuilder.append(" & ");

                double value = values[i][v];

                if (value != 0) {

                    // change color based on the fraction of the data for this task
                    double colorProgression = 100 * (value / taskSum);

                    if (i == v) {
                        stringBuilder.append("\\cellcolor{cmGoodHigh!" + colorProgression + "!cmGoodLow}");
                    } else {
                        stringBuilder.append("\\cellcolor{cmBadHigh!" + colorProgression + "!cmBadLow}");
                    }

                    stringBuilder.append((int) value);

                }

            }
            stringBuilder.append("\\\\");
            stringBuilder.append("\\hhline{*{1}{~}|*{" + (classNames.length + 1) + "}{-|}}");
            stringBuilder.append(System.lineSeparator());
        }


        stringBuilder.append("\\end{tabularx}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append(" \\end{adjustbox}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("\\caption{Confusion matrix example}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("\\label{confusionMatrixExample}");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("\\end{table}");

        return stringBuilder.toString();
    }

    /**
     * Determine the highest number of digits used within this confusion matrix
     *
     * @return
     */
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

    /**
     * Add spaces at beginning of original string, until it has the desired length
     *
     * @param original
     * @param goalLength
     * @return
     */
    private String prependToLength(String original, int goalLength) {

        int originalLength = original.length();
        String prependedString = "";
        for (int i = 0; i < goalLength - originalLength; i++) {
            prependedString += " ";
        }

        prependedString += original;

        return prependedString;

    }

    /**
     * Add spaces before the original character, to produce a string of the desired length
     *
     * @param original
     * @param goalLength
     * @return
     */
    private String prependToLength(char original, int goalLength) {
        return prependToLength("" + original, goalLength);
    }

    /**
     * Add spaces before the original integer, to produce a string of the desired length
     * Replaces zeros with a placeholder value for better readability of plaintext output
     *
     * @param original
     * @param goalLength
     * @return
     */
    private String prependToLength(int original, int goalLength) {
        if (original != 0) {
            return prependToLength("" + original, goalLength);
        } else {
            return prependToLength(".", goalLength);
        }
    }

    /**
     * Add spaces before the original double value (cast to integer), to produce a string of the desired length
     * Replaces zeros with a placeholder value for better readability of plaintext output
     *
     * @param original will be cast to integer
     * @param goalLength
     * @return
     */
    private String prependToLength(double original, int goalLength) {
        if (original != 0) {
            return prependToLength((int) original, goalLength);
        } else {
            return prependToLength(".", goalLength);
        }
    }


}
