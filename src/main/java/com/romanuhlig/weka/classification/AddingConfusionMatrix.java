package com.romanuhlig.weka.classification;

import weka.core.Instances;

import java.util.HashMap;

public class AddingConfusionMatrix {

    private double[][] values;
    private String[] tasks;
    private Instances instances;

    private static HashMap<String, String> taskNameForLines = new HashMap<>();
    private static HashMap<String, String> taskNameForColumns = new HashMap<>();

    private static final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();


    static {
        taskNameForLines.put("jogging", "Jogging");
        taskNameForLines.put("kick", "Kick");
        taskNameForLines.put("kickPunch", "Kick-Punch");
        taskNameForLines.put("lateralBounding", "Lateral Bounding");
        taskNameForLines.put("lunges", "Lunges");
        taskNameForLines.put("punch", "Punch");
        taskNameForLines.put("sitting", "Sitting");
        taskNameForLines.put("squats", "Squats");
        taskNameForLines.put("standing", "Standing");
        taskNameForLines.put("walking", "Walking");
        taskNameForLines.put("punch", "Punch");

        taskNameForColumns.put("jogging", "Jogging");
        taskNameForColumns.put("kick", "Kick");
        taskNameForColumns.put("kickPunch", "Kick-Punch");
        taskNameForColumns.put("lateralBounding", "Lat. Bound.");
        taskNameForColumns.put("lunges", "Lunges");
        taskNameForColumns.put("punch", "Punch");
        taskNameForColumns.put("sitting", "Sitting");
        taskNameForColumns.put("squats", "Squats");
        taskNameForColumns.put("standing", "Standing");
        taskNameForColumns.put("walking", "Walking");
        taskNameForColumns.put("punch", "Punch");
    }


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

    private String lineTaskName(String standardName) {
        return taskNameForLines.get(standardName);
    }

    private String columnTaskName(String standardName) {
        return taskNameForColumns.get(standardName);
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

    public String toOutputStringLatex() {
        StringBuilder stringBuilder = new StringBuilder();

//    \begin{table}
        stringBuilder.append("\\begin{table}");
        stringBuilder.append(System.lineSeparator());

//    \centering
        stringBuilder.append("\\centering");
        stringBuilder.append(System.lineSeparator());

//    \begin{tabular}{c|l|C{1cm}|C{1cm}|C{1cm}|C{1cm}|C{1cm}|C{1cm}|}
        stringBuilder.append("\\begin{tabularx}{\\textwidth}{c|l|");
        for (int i = 0; i < tasks.length; i++) {
            stringBuilder.append("C{0.8cm}|");
        }
        stringBuilder.append("}");
        stringBuilder.append(System.lineSeparator());

//    \multicolumn{1}{l}{}             & \multicolumn{1}{l}{} & \multicolumn{6}{c}{Predicted Class}                       \\
        stringBuilder.append(
                "\\multicolumn{1}{l}{} & \\multicolumn{1}{l}{} & \\multicolumn{"
                        + tasks.length
                        + "}{c}{\\textbf{Predicted Class}}");
        stringBuilder.append("\\\\");
        stringBuilder.append(System.lineSeparator());

//    \cline{3-8}
//        \hhline{*{2}{~}|*{10}{-|}}

        stringBuilder.append("\\hhline{*{2}{~}|*{" + tasks.length + "}{-|}}");
        stringBuilder.append(System.lineSeparator());

//    \multicolumn{1}{l}{}             &                      & \rotatebox{90}{Lying\hspace{8pt}} & \rotatebox{90}{Standing\hspace{8pt}} & \rotatebox{90}{Sitting\hspace{8pt}} & \rotatebox{90}{Walking\hspace{8pt}} & \rotatebox{90}{Running\hspace{8pt}} & \rotatebox{90}{Cycling\hspace{8pt}}  \\
        stringBuilder.append("\\multicolumn{1}{l}{} & ");
        for (int i = 0; i < tasks.length; i++) {
            stringBuilder.append("& \\rotatebox{90}{" + columnTaskName(tasks[i]) + "\\hspace{8pt}}");
        }
        stringBuilder.append("\\\\");
        stringBuilder.append(System.lineSeparator());

//    \cline{2-8}
//        \hhline{*{1}{~|}*{11}{-|}}

        stringBuilder.append("\\hhline{*{1}{~}|*{" + (tasks.length + 1) + "}{-|}}");
        stringBuilder.append(System.lineSeparator());

//    \multirow{6}{*}{\rotatebox{90}{actual class\hspace{8pt}}} & Lying                & 95   & 5       &         &         &         &          \\
        stringBuilder.append("\\multirow{" + tasks.length + "}{*}{\\rotatebox{90}{\\textbf{Actual Class}\\hspace{8pt}}} ");
        stringBuilder.append(System.lineSeparator());


//    \cline{2-8}
//                                     & Standing             & 10   & 75      & 10     & 5      &         &          \\
//    \cline{2-8}
//                                     & Sitting              & 500    &          & 8500     &         &         & 1000      \\
//    \cline{2-8}
//                                     & Walking              &       & 25       &         & 75      &         &          \\
//    \cline{2-8}
//                                     & Running              &       & 10       &         & 40      & 50      &          \\
//    \cline{2-8}
//                                     & Cycling              &       &          & 20      &         &         & 80       \\
//    \cline{2-8}
        for (int i = 0; i < tasks.length; i++) {
            stringBuilder.append(" & " + lineTaskName(tasks[i]));

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

//                    \color{blue!20!black}


                    if (i == v) {
                        stringBuilder.append("\\cellcolor{cmGoodHigh!" + colorProgression + "!cmGoodLow}");
                    } else {
                        stringBuilder.append("\\cellcolor{cmBadHigh!" + colorProgression + "!cmBadLow}");
                    }

                    stringBuilder.append((int) value);

                }

            }
            stringBuilder.append("\\\\");
            stringBuilder.append("\\hhline{*{1}{~}|*{" + (tasks.length + 1) + "}{-|}}");
            stringBuilder.append(System.lineSeparator());
        }


//    \end{tabular}
        stringBuilder.append("\\end{tabularx}");
        stringBuilder.append(System.lineSeparator());

//    \caption{Confusion matrix example}
        stringBuilder.append("\\caption{Confusion matrix example}");
        stringBuilder.append(System.lineSeparator());

//    \label{confusionMatrixExample}
        stringBuilder.append("\\label{confusionMatrixExample}");
        stringBuilder.append(System.lineSeparator());

//    \end{table}
        stringBuilder.append("\\end{table}");


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
