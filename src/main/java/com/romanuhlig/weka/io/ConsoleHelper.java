package com.romanuhlig.weka.io;

/**
 * Provides formatting for the console output
 *
 * @author Roman Uhlig
 */
public class ConsoleHelper {

    /**
     * Output the given matrix to the console
     *
     * @param matrix
     */
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
