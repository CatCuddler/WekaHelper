package com.romanuhlig.weka.classification;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

import java.util.ArrayList;

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

    public String toOutputStringLatex() {
        StringBuilder stringBuilder = new StringBuilder();

//    \begin{table}
        stringBuilder.append("\\begin{table}");
        stringBuilder.append(System.lineSeparator());

//    \centering
        stringBuilder.append("\\centering");
        stringBuilder.append(System.lineSeparator());

//    \begin{tabular}{c|l|C{1cm}|C{1cm}|C{1cm}|C{1cm}|C{1cm}|C{1cm}|}
        stringBuilder.append("\\begin{tabular}{c|l|");
        for (int i = 0; i < tasks.length; i++) {
            stringBuilder.append("C{0.8cm}|");
        }
        stringBuilder.append("}");
        stringBuilder.append(System.lineSeparator());

//    \multicolumn{1}{l}{}             & \multicolumn{1}{l}{} & \multicolumn{6}{c}{predicted class}                       \\
        stringBuilder.append(
                "\\multicolumn{1}{l}{} & \\multicolumn{1}{l}{} & \\multicolumn{"
                        + tasks.length
                        + "}{c}{\\textbf{predicted class}}");
        stringBuilder.append("\\\\");
        stringBuilder.append(System.lineSeparator());

//    \cline{3-8}
//        \hhline{*{2}{~}|*{10}{-|}}

        stringBuilder.append("\\hhline{*{2}{~}|*{" + tasks.length + "}{-|}}");
        stringBuilder.append(System.lineSeparator());

//    \multicolumn{1}{l}{}             &                      & \rotatebox{90}{Lying\hspace{8pt}} & \rotatebox{90}{Standing\hspace{8pt}} & \rotatebox{90}{Sitting\hspace{8pt}} & \rotatebox{90}{Walking\hspace{8pt}} & \rotatebox{90}{Running\hspace{8pt}} & \rotatebox{90}{Cycling\hspace{8pt}}  \\
        stringBuilder.append("\\multicolumn{1}{l}{} & ");
        for (int i = 0; i < tasks.length; i++) {
            stringBuilder.append("& \\rotatebox{90}{" + tasks[i] + "\\hspace{8pt}}");
        }
        stringBuilder.append("\\\\");
        stringBuilder.append(System.lineSeparator());

//    \cline{2-8}
//        \hhline{*{1}{~|}*{11}{-|}}

        stringBuilder.append("\\hhline{*{1}{~}|*{" + (tasks.length + 1) + "}{-|}}");
        stringBuilder.append(System.lineSeparator());

//    \multirow{6}{*}{\rotatebox{90}{actual class\hspace{8pt}}} & Lying                & 95   & 5       &         &         &         &          \\
        stringBuilder.append("\\multirow{" + tasks.length + "}{*}{\\rotatebox{90}{\\textbf{actual class}\\hspace{8pt}}} ");
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
            stringBuilder.append(" & " + tasks[i]);

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

                    String.fo
                    stringBuilder.append((int) value);

                }

            }
            stringBuilder.append("\\\\");
            stringBuilder.append("\\hhline{*{1}{~}|*{" + (tasks.length + 1) + "}{-|}}");
            stringBuilder.append(System.lineSeparator());
        }


//    \end{tabular}
        stringBuilder.append("\\end{tabular}");
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
