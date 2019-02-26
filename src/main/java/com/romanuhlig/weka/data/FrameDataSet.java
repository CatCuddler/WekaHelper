package com.romanuhlig.weka.data;

import com.romanuhlig.weka.math.MathHelper;
import com.sun.tools.corba.se.idl.constExpr.ShiftLeft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents all Data collected for one user, and one task
 */
public class FrameDataSet {

    // an inner List represents all data collected for an individual sensor
    private final ArrayList<List<FrameData>> allSensorLists;

    private final String subject;

    private final String activity;

    private double timeOfLastAddedEntry;

    public FrameDataSet(String subject, String activity) {
        this.subject = subject;
        this.activity = activity;
        allSensorLists = new ArrayList<>();
    }

    public FrameDataSet(List<FrameData> originalFrameData) {

        subject = originalFrameData.get(0).getSubject();
        activity = originalFrameData.get(0).getActivity();
        allSensorLists = new ArrayList<>();

        // collect all frame data in its corresponding sensor list
        for (FrameData frameData : originalFrameData) {
            addFrameData(frameData);
        }
        // check data consistency for last frame
        checkDataConsistencyForPreviousFrame();
    }

    public FrameDataSet(ArrayList<List<FrameData>> allSensorLists, String subject, String activity) {
        this.allSensorLists = allSensorLists;
        this.subject = subject;
        this.activity = activity;
    }


    public void addFrameData(FrameData newFrameData) {

        if (!allSensorLists.isEmpty() && newFrameData.getTime() != timeOfLastAddedEntry) {
            checkDataConsistencyForPreviousFrame();
        }

        timeOfLastAddedEntry = newFrameData.getTime();

        boolean sensorPositionAlreadyKnown = false;

        // find the corresponding sensor list, and add the new data
        for (List<FrameData> sensorList : allSensorLists) {
            if (!sensorList.isEmpty()
                    && sensorList.get(0).getSensorPosition().equals(newFrameData.getSensorPosition())) {
                sensorList.add(newFrameData);
                sensorPositionAlreadyKnown = true;
                // compute acceleration
                if (sensorList.size() > 1) {
                    FrameData previousFrameData = sensorList.get(sensorList.size() - 2);
                    newFrameData.computeAdditionalDataBasedOnPreviousFrame(previousFrameData);

                    // remove the first pieces of data, where acceleration could not yet have been set correctly
                    if (!previousFrameData.derivedDataWasCalculated()) {
                        // can only happen when the first data pieces are added,
                        // meaning the previous piece of data has to be the first in the list
                        sensorList.remove(0);
                    }
                }
                break;
            }
        }


        // if no sensor list exist for this sensor yet, add a new one
        if (!sensorPositionAlreadyKnown) {
            ArrayList<FrameData> newSensorList = new ArrayList<FrameData>();
            newSensorList.add(newFrameData);
            addNewSensorListAndSortByPosition(newSensorList);
        }

    }

    private void checkDataConsistencyForPreviousFrame() {

//        System.out.println("checking");

        int latestIndex = allSensorLists.get(0).size() - 1;
//        System.out.println("1");

//        System.out.println("all sensors:");
//        for (int i = 0; i < allSensorLists.size(); i++) {
//            System.out.println(allSensorLists.get(i).get(latestIndex).getSensorPosition());
//        }

        for (int a = 0; a < allSensorLists.size(); a++) {

            FrameData frameDataA = allSensorLists.get(a).get(latestIndex);

            // single sensor checks
            // height can always be checked for sanity
            if (frameDataA.getSensorPosition().equals("lForeArm")
                    || frameDataA.getSensorPosition().equals("rForeArm")
                    || frameDataA.getSensorPosition().equals("lHand")
                    || frameDataA.getSensorPosition().equals("rHand")) {
                if (frameDataA.getCalPosY() > 3.0) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("head")) {
                if (frameDataA.getCalPosY() > 2.75) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("rArm")) {
                if (frameDataA.getCalPosY() > 2.5) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("spine")) {
                if (frameDataA.getCalPosY() > 2.25) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("hip")) {
                if (frameDataA.getCalPosY() > 1.5) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("lLeg")
                    || frameDataA.getSensorPosition().equals("rLeg")
                    || frameDataA.getSensorPosition().equals("lFoot")
                    || frameDataA.getSensorPosition().equals("rFoot")) {
                if (frameDataA.getCalPosY() > 1.0) {
                    frameDataA.setInvalid();
                }
            }

            for (int b = a + 1; b < allSensorLists.size(); b++) {
//                System.out.println("3");

                FrameData frameDataB = allSensorLists.get(b).get(latestIndex);

                double distance = MathHelper.distance(frameDataA, frameDataB);

                // no distance should be higher than this, no matter which sensor combination
                if (distance > 3.5) {
                    frameDataA.setInvalid();
                    frameDataB.setInvalid();
                }

                // dual sensor checks
                // sensor lists are sorted alphabetically, dual sensor checks have to list the "lower" one first
                if (frameDataA.getSensorPosition().equals("head")) {
                    if (frameDataB.getSensorPosition().equals("spine")) {
                        if (distance > 0.95) {
                            frameDataA.setInvalid();
                            frameDataB.setInvalid();
                        }
                    }
                } else if (frameDataA.getSensorPosition().equals("hip")) {
                    if (frameDataB.getSensorPosition().equals("spine")) {
                        if (distance > 0.9) {
                            frameDataA.setInvalid();
                            frameDataB.setInvalid();
                        }
                    }
                } else if (frameDataA.getSensorPosition().equals("lFoot")) {
                    if (frameDataB.getSensorPosition().equals("lLeg")) {
                        if (distance > 0.5) {
                            frameDataA.setInvalid();
                            frameDataB.setInvalid();
                        }
                    }
                } else if (frameDataA.getSensorPosition().equals("lForeArm")) {
                    if (frameDataB.getSensorPosition().equals("lHand")) {
                        if (distance > 0.5) {
                            frameDataA.setInvalid();
                            frameDataB.setInvalid();
                        }
                    }
                } else if (frameDataA.getSensorPosition().equals("rArm")) {
                    if (frameDataB.getSensorPosition().equals("rForeArm")) {
                        if (distance > 0.6) {
                            frameDataA.setInvalid();
                            frameDataB.setInvalid();
                        }
                    }
                } else if (frameDataA.getSensorPosition().equals("rFoot")) {
                    if (frameDataB.getSensorPosition().equals("rLeg")) {
                        if (distance > 0.5) {
                            frameDataA.setInvalid();
                            frameDataB.setInvalid();
                        }
                    }
                } else if (frameDataA.getSensorPosition().equals("rForeArm")) {
                    if (frameDataB.getSensorPosition().equals("rHand")) {
                        if (distance > 0.5) {
                            frameDataA.setInvalid();
                            frameDataB.setInvalid();
                        }
                    }
                }
            }

        }
    }


    void addNewSensorListAndSortByPosition(ArrayList<FrameData> newSensorList) {

        allSensorLists.add(newSensorList);
        sortSensorListsByPosition();

    }


    public ArrayList<FrameData> composeFlatList() {
        ArrayList<FrameData> flatList = new ArrayList<FrameData>();
        for (List<FrameData> sensorList : allSensorLists
        ) {
            flatList.addAll(sensorList);
        }

        return flatList;
    }

    public ArrayList<List<FrameData>> getAllSensorLists() {
        return allSensorLists;
    }

    static int segmentsLeftOut = 0;

    public ArrayList<FrameDataSet> separateFrameDataIntoValidWindows(double windowSize, double timeBetweenWindows) {

        // get first point in time
        List<FrameData> firstSensorList = allSensorLists.get(0);
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
                ArrayList<List<FrameData>> newFrameDataSegment = new ArrayList<>();
                for (List<FrameData> sensorList : allSensorLists) {
                    // if for any reason it becomes necessary to change the window lists afterwards,
                    // this needs to be a normal ArrayList instead of a sublist
                    // a sublist will change the original list when it changes
                    List<FrameData> sensorSegment = sensorList.subList(startIndex, endIndex);
                    newFrameDataSegment.add(sensorSegment);
                }

                FrameDataSet newFrameDataSet = new FrameDataSet(newFrameDataSegment, subject, activity);

                if (newFrameDataSet.includesOnlyValidData()) {
                    frameDataSegments.add(newFrameDataSet);
                } else {
                    System.out.println("segments left out:  " + segmentsLeftOut++);
                }


                // update segment indexes and time
                potentialSegmentStartIndexes.removeFirst();
                currentSegmentStartTime = firstSensorList.get(potentialSegmentStartIndexes.getFirst()).getTime();
            }

        }

        return frameDataSegments;

    }

    private boolean includesOnlyValidData() {
        for (List<FrameData> sensor : allSensorLists) {
            for (FrameData frameData : sensor) {
                if (frameData.includesInvalidData()) {
                    return false;
                }
            }
        }
        return true;
    }

    public FrameDataSet getLatestDataForWindowSizeAndRemoveEarlierData(double windowSize) {


        List<FrameData> firstSensorList = allSensorLists.get(0);

        // determine index of first data point in time frame
        double latestTime = firstSensorList.get(firstSensorList.size() - 1).getTime();
        int indexOfFirstSensorData = 0;
        for (int i = firstSensorList.size() - 1; i >= 0; i--) {
            if (latestTime - firstSensorList.get(i).getTime() >= windowSize) {
                indexOfFirstSensorData = i;
                break;
            }
        }

        // collect sublists within the required time frame
        ArrayList<List<FrameData>> newSensorLists = new ArrayList<>();
        for (int i = 0; i < allSensorLists.size(); i++) {
            List<FrameData> oldSensorList = allSensorLists.get(i);
            ArrayList<FrameData> newSensorList =
                    new ArrayList<>(oldSensorList.subList(indexOfFirstSensorData, oldSensorList.size() - 1));
            newSensorLists.add(newSensorList);
            // we will not need the older data either, so we can forget it by also using the reduced list
            allSensorLists.set(i, newSensorList);
        }

        return new FrameDataSet(newSensorLists, subject, activity);


    }

    public String getSubject() {
        return subject;
    }

    public String getActivity() {
        return activity;
    }

    private void sortSensorListsByPosition() {
        allSensorLists.sort(new Comparator<List<FrameData>>() {
            @Override
            public int compare(List<FrameData> o1, List<FrameData> o2) {
                return o1.get(0).getSensorPosition().compareTo(o2.get(0).getSensorPosition());
            }
        });
    }

    public ArrayList<String> getAllSensorPositions() {
        ArrayList<String> sensorPositions = new ArrayList<>();
        for (List<FrameData> sensorList : allSensorLists) {
            sensorPositions.add(sensorList.get(0).getSensorPosition());
        }
        return sensorPositions;
    }


    public boolean enoughDataForWindowSize(double windowSize) {

        if (allSensorLists.size() > 0 && allSensorLists.get(0).size() > 0) {
            List<FrameData> firstSensorList = allSensorLists.get(0);
            double startTime = firstSensorList.get(0).getTime();
            double endTime = firstSensorList.get(firstSensorList.size() - 1).getTime();
            return (endTime - startTime) > windowSize;
        } else {
            return false;
        }

    }


}
