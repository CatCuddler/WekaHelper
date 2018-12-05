package com.romanuhlig.weka.io;

import java.util.List;

public class ConversionHelper {

    public static int[] integerListToIntArray(List<Integer> list) {

        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).intValue();
        }
        return array;
    }

}
