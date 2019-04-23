package com.romanuhlig.weka.io;

import java.util.List;

/**
 * Helps converting between data types
 *
 * @author Roman Uhlig
 */
public class ConversionHelper {

    /**
     * Converts the given Integer List to an int array
     *
     * @param list
     * @return
     */
    public static int[] integerListToIntArray(List<Integer> list) {

        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).intValue();
        }
        return array;
    }
}
