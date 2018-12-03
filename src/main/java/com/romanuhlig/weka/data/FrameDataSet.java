package com.romanuhlig.weka.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents all Data collected for one user, and one task
 */
public class FrameDataSet {

    // an inner List represents all data collected for an individual sensor
    private final ArrayList<ArrayList<FrameData>> allFrameData;

    private final String subject;

    private final String activity;

    public FrameDataSet(List<FrameData> originalFrameData) {

        subject = originalFrameData.get(0).getSubject();
        activity = originalFrameData.get(0).getActivity();


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

            //TODO: - once the velocity is actually recorded instead of derived, it is no longer necessary to delete a second line
            // delete a second line, since it used un-updated velocities from the first frame to update its acceleration
            sensorList.remove(0);
        }

        // sort sensor lists by name, to keep them in the same order later on
        sortSensorListsByPosition();
    }

    public FrameDataSet(ArrayList<ArrayList<FrameData>> allFrameData, String subject, String activity) {
        this.allFrameData = allFrameData;
        this.subject = subject;
        this.activity = activity;
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

    public ArrayList<FrameData> composeFlatList() {
        ArrayList<FrameData> flatList = new ArrayList<FrameData>();
        for (ArrayList<FrameData> sensorList : allFrameData
        ) {
            flatList.addAll(sensorList);
        }

        return flatList;
    }

    public ArrayList<ArrayList<FrameData>> getAllFrameData() {
        return allFrameData;
    }


    public ArrayList<FrameDataSet> separateFrameDataIntoWindows(double windowSize, double timeBetweenWindows) {

        // get first point in time
        ArrayList<FrameData> firstSensorList = allFrameData.get(0);
        FrameData firstDataPoint = firstSensorList.get(0);
        double startTime = firstDataPoint.getTime();

        // initialize counters
        LinkedList<Integer> potentialSegmentStartIndexes = new LinkedList<>();
        potentialSegmentStartIndexes.add(0);
        double lastPotentialSegmentStartTime = startTime;
        double currentSegmentStartTime = startTime;

        ArrayList<FrameDataSet> frameDataSegments = new ArrayList<>();

        for (int i = 0; i < firstSensorList.size(); i++) {

            double frameTime = firstSensorList.get(i).getTime();

            // add another segment start index if enough time has passed
            if (frameTime >= lastPotentialSegmentStartTime + timeBetweenWindows) {

                potentialSegmentStartIndexes.add(i);
                lastPotentialSegmentStartTime = firstSensorList.get(i).getTime();
            }

            // extract another segment if enough time has passed
            if (frameTime >= currentSegmentStartTime + windowSize) {

                // choose indexes for this segment
                int startIndex = potentialSegmentStartIndexes.getFirst();
                int endIndex = i;

                // extract a new segment for all sensors
                ArrayList<ArrayList<FrameData>> newFrameDataSegment = new ArrayList<>();
                for (ArrayList<FrameData> sensorList : allFrameData) {
                    ArrayList<FrameData> sensorSegment = new ArrayList<>(sensorList.subList(startIndex, endIndex));
                    newFrameDataSegment.add(sensorSegment);
                }
                frameDataSegments.add(new FrameDataSet(newFrameDataSegment, subject, activity));

                // update segment indexes and time
                potentialSegmentStartIndexes.removeFirst();
                currentSegmentStartTime = firstSensorList.get(potentialSegmentStartIndexes.getFirst()).getTime();
            }

        }

        return frameDataSegments;

    }

    public String getSubject() {
        return subject;
    }

    public String getActivity() {
        return activity;
    }

    private void sortSensorListsByPosition() {
        allFrameData.sort(new Comparator<ArrayList<FrameData>>() {
            @Override
            public int compare(ArrayList<FrameData> o1, ArrayList<FrameData> o2) {
                return o1.get(0).getSensorPosition().compareTo(o2.get(0).getSensorPosition());
            }
        });
    }

    public List<String> getAllSensorPositions() {
        LinkedList<String> sensorPositions = new LinkedList<>();
        for (ArrayList<FrameData> sensorList : allFrameData) {
            sensorPositions.add(sensorList.get(0).getSensorPosition());
        }
        return sensorPositions;
    }

}
