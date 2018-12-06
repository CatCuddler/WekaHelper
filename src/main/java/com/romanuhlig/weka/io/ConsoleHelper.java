package com.romanuhlig.weka.io;

public class ConsoleHelper {


    public static void printConfusionMatrix(double[][] matrix) {

        for (int line = 0; line < matrix.length; line++) {
            for (int column = 0; column < matrix[line].length; column++) {
                System.out.print(Double.toString(matrix[line][column]));
                System.out.print("     ");
            }
            System.out.println();
        }

    }

}
