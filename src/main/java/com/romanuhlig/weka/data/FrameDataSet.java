package com.romanuhlig.weka.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents all Data collected for one user, and one task
 */
public class FrameDataSet {

    // an inner List represents all data collected for an individual sensor
    ArrayList<ArrayList<FrameData>> allFrameData;

    public FrameDataSet(List<FrameData> originalFrameData) {
        allFrameData = new ArrayList<ArrayList<FrameData>>();

        // collect all frame data in its corresponding sensor list
        for (FrameData frameData : originalFrameData) {
            addFrameDataChronologically(frameData);
        }

        // compute acceleration
        int frameDataPerSensor = allFrameData.get(0).size();
        for (int i = 1; i < frameDataPerSensor; i++) {
            for (ArrayList<FrameData> sensorList : allFrameData) {
                sensorList.get(i).setAccelerationBasedOnPreviousFrame(sensorList.get(i - 1));

            }
        }

        // remove the first frame for each sensor, because no acceleration can be computed
        for (ArrayList<FrameData> sensorList : allFrameData) {
            sensorList.remove(0);
        }

    }


    private void addFrameDataChronologically(FrameData newFrameData) {

        boolean sensorPositionAlreadyKnown = false;

        // find the corresponding sensor list, and add the new data
        for (ArrayList<FrameData> sensorList : allFrameData) {
            if (!sensorList.isEmpty()
                    && sensorList.get(0).getSensorPosition().equals(newFrameData.getSensorPosition())) {
                sensorList.add(newFrameData);
                sensorPositionAlreadyKnown = true;
                break;
            }
        }

        // if no sensor list exist for this sensor yet, add a new one
        if (!sensorPositionAlreadyKnown) {
            ArrayList<FrameData> newSensorList = new ArrayList<FrameData>();
            newSensorList.add(newFrameData);
            allFrameData.add(newSensorList);
        }
    }
}
