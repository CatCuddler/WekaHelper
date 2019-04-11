package com.romanuhlig.weka.frameToFeature;

import com.romanuhlig.weka.math.MathHelper;

import java.util.*;

/**
 * Represents all data for a single recording produced by one subject for one task
 *
 * @author Roman Uhlig
 */
public class FrameDataSet {

    // any inner List represents all data collected for a single individual sensor
    private final ArrayList<List<FrameData>> allSensorLists;

    private final String subject;
    private final String activity;

    private double timestampOfLatestEntry;

    // for debugging purposes, count the number of frame data sets which have been removed
    // because they contained invalid data
    private static int segmentsLeftOut = 0;


    /**
     * Construct an empty FrameDataSet for the given subject and activity
     *
     * @param subject
     * @param activity
     */
    public FrameDataSet(String subject, String activity) {
        this.subject = subject;
        this.activity = activity;
        allSensorLists = new ArrayList<>();
    }

    /**
     * Construct a new FrameDataSet from the given raw data
     *
     * @param originalFrameData
     */
    public FrameDataSet(List<FrameData> originalFrameData) {

        subject = originalFrameData.get(0).getSubject();
        activity = originalFrameData.get(0).getActivity();
        allSensorLists = new ArrayList<>();

        // collect all frame data in its corresponding sensor list
        for (FrameData frameData : originalFrameData) {
            addFrameData(frameData);
        }
        // check data consistency for last added frame
        checkDataConsistencyForNewestData();
    }

    /**
     * Construct a FrameDataSet from previously prepared data
     * <p>
     * No further checks will be made, the data needs to be ordered and checked for consistencies already
     *
     * @param allSensorLists
     * @param subject
     * @param activity
     */
    public FrameDataSet(ArrayList<List<FrameData>> allSensorLists, String subject, String activity) {
        this.allSensorLists = allSensorLists;
        this.subject = subject;
        this.activity = activity;
    }

    /**
     * Add a single sensor reading to this set
     * <p>
     * FrameData needs to be added in order, with the highest timestamp added last
     *
     * @param newFrameData
     */
    public void addFrameData(FrameData newFrameData) {

        // if the data that was last added has another timestamp, the previous frame has been completed
        // in that case, check the previous frame for data inconsistencies
        if (!allSensorLists.isEmpty() && newFrameData.getTime() != timestampOfLatestEntry) {
            checkDataConsistencyForNewestData();
        }
        timestampOfLatestEntry = newFrameData.getTime();


        // find the corresponding sensor list for the new data, and add it there
        boolean sensorPositionAlreadyKnown = false;
        for (List<FrameData> sensorList : allSensorLists) {
            if (!sensorList.isEmpty() && sensorList.get(0).getSensorPosition().equals(newFrameData.getSensorPosition())) {
                sensorList.add(newFrameData);
                sensorPositionAlreadyKnown = true;

                // if this is not the first frame to be added, compute additional data
                // which depends on the previous frame to be known
                if (sensorList.size() > 1) {
                    FrameData previousFrameData = sensorList.get(sensorList.size() - 2);
                    newFrameData.computeAdditionalDataBasedOnPreviousFrame(previousFrameData);

                    // remove the first frame, where that data could not yet have been set correctly
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

    /**
     * Within the newest entries for each sensor, mark data which contains impossible values
     * <p>
     * E.g. a sensor that is 10 meters above the ground
     */
    private void checkDataConsistencyForNewestData() {

        int indexForNewestData = allSensorLists.get(0).size() - 1;

        for (int a = 0; a < allSensorLists.size(); a++) {

            FrameData frameDataA = allSensorLists.get(a).get(indexForNewestData);

            // single sensor checks
            // height can always be checked for sanity
            if (frameDataA.getSensorPosition().equals("lForeArm")
                    || frameDataA.getSensorPosition().equals("rForeArm")
                    || frameDataA.getSensorPosition().equals("lHand")
                    || frameDataA.getSensorPosition().equals("rHand")) {
                if (frameDataA.getRawPosY() > 3.0) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("head")) {
                if (frameDataA.getRawPosY() > 2.75) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("rArm")) {
                if (frameDataA.getRawPosY() > 2.5) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("spine")) {
                if (frameDataA.getRawPosY() > 2.25) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("hip")) {
                if (frameDataA.getRawPosY() > 1.5) {
                    frameDataA.setInvalid();
                }
            } else if (frameDataA.getSensorPosition().equals("lLeg")
                    || frameDataA.getSensorPosition().equals("rLeg")
                    || frameDataA.getSensorPosition().equals("lFoot")
                    || frameDataA.getSensorPosition().equals("rFoot")) {
                if (frameDataA.getRawPosY() > 1.0) {
                    frameDataA.setInvalid();
                }
            }

            // dual sensor checks
            // based on the maximum achievable distance between neighbouring sensors
            for (int b = a + 1; b < allSensorLists.size(); b++) {

                FrameData frameDataB = allSensorLists.get(b).get(indexForNewestData);
                double distance = MathHelper.distance(frameDataA, frameDataB);

                // no distance should be higher than this, no matter which sensor combination
                if (distance > 3.5) {
                    frameDataA.setInvalid();
                    frameDataB.setInvalid();
                }

                // specific pairs
                // sensor lists are sorted alphabetically, dual sensor checks have to list the "lower" one first
                switch (frameDataA.getSensorPosition()) {
                    case "head":
                        if (frameDataB.getSensorPosition().equals("spine")) {
                            if (distance > 0.95) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        } else if (frameDataB.getSensorPosition().equals("lForeArm")
                                || frameDataB.getSensorPosition().equals("rForeArm")) {
                            if (distance > 1.5) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        }
                        break;
                    case "hip":
                        if (frameDataB.getSensorPosition().equals("spine")) {
                            if (distance > 0.9) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        } else if (frameDataB.getSensorPosition().equals("lLeg")
                                || frameDataB.getSensorPosition().equals("rLeg")) {
                            if (distance > 1.4) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        }
                        break;
                    case "lFoot":
                        if (frameDataB.getSensorPosition().equals("lLeg")) {
                            if (distance > 0.5) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        }
                        break;
                    case "lForeArm":
                        if (frameDataB.getSensorPosition().equals("lHand")) {
                            if (distance > 0.5) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        } else if (frameDataB.getSensorPosition().equals("spine")) {
                            if (distance > 1.1) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        }
                        break;
                    case "rArm":
                        if (frameDataB.getSensorPosition().equals("rForeArm")) {
                            if (distance > 0.6) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        } else if (frameDataB.getSensorPosition().equals("spine")) {
                            if (distance > 0.8) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        }
                        break;
                    case "rFoot":
                        if (frameDataB.getSensorPosition().equals("rLeg")) {
                            if (distance > 0.5) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        }
                        break;
                    case "rForeArm":
                        if (frameDataB.getSensorPosition().equals("rHand")) {
                            if (distance > 0.5) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        } else if (frameDataB.getSensorPosition().equals("spine")) {
                            if (distance > 1.1) {
                                frameDataA.setInvalid();
                                frameDataB.setInvalid();
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Add the new sensor list to this data set and sort all sensor lists by their sensor names
     *
     * @param newSensorList
     */
    void addNewSensorListAndSortByPosition(ArrayList<FrameData> newSensorList) {

        allSensorLists.add(newSensorList);
        sortSensorListsByPosition();

    }

    /**
     * Get all sensor lists contained in this set
     *
     * @return
     */
    public ArrayList<List<FrameData>> getAllSensorLists() {
        return allSensorLists;
    }

    /**
     * Separate this FrameDataSet into pieces of the given length, with the given distance between them
     *
     * @param windowSize
     * @param timeBetweenWindows
     * @return
     */
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

        // prepare to collect created sets
        ArrayList<FrameDataSet> frameDataSegments = new ArrayList<>();

        // advance through the timestamps of any sensor list to determine the points of separation
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

                // add new segment, or throw it out if it contains wrong sensor readings
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

    /**
     * Whether any of the included data has been determined to result from an erroneous sensor reading
     *
     * @return
     */
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

    /**
     * Get a new FrameDataSet which represents the latest data of the given length, and remove all earlier data
     * <p>
     * Useful for live classification, allowing to continuously update a FrameDataSet
     * and get the newest data when needed, without letting it grow forever
     *
     * @param windowSize
     * @return
     */
    public FrameDataSet getLatestDataForWindowSizeAndRemoveEarlierData(double windowSize) {


        // determine index of first data point within requested time frame
        List<FrameData> firstSensorList = allSensorLists.get(0);
        double latestTime = firstSensorList.get(firstSensorList.size() - 1).getTime();
        int indexOfFirstSensorData = 0;
        for (int i = firstSensorList.size() - 1; i >= 0; i--) {
            if (latestTime - firstSensorList.get(i).getTime() >= windowSize) {
                indexOfFirstSensorData = i;
                break;
            }
        }

        // collect sublists within the requested time frame
        ArrayList<List<FrameData>> newSensorLists = new ArrayList<>();
        for (int i = 0; i < allSensorLists.size(); i++) {
            List<FrameData> oldSensorList = allSensorLists.get(i);
            ArrayList<FrameData> newSensorList =
                    new ArrayList<>(oldSensorList.subList(indexOfFirstSensorData, oldSensorList.size() - 1));
            newSensorLists.add(newSensorList);
            // forget the old data by using the reduced list from now on
            allSensorLists.set(i, newSensorList);
        }

        // using the newSensorList ensures a true copy instead of a mirror
        return new FrameDataSet(newSensorLists, subject, activity);
    }

    /**
     * The subject which recorded all data within this set
     *
     * @return
     */
    public String getSubject() {
        return subject;
    }

    /**
     * The activity performed for all data within this set
     *
     * @return
     */
    public String getActivity() {
        return activity;
    }

    /**
     * Sort all sensor lists by their sensor name
     */
    private void sortSensorListsByPosition() {
        allSensorLists.sort(new Comparator<List<FrameData>>() {
            @Override
            public int compare(List<FrameData> o1, List<FrameData> o2) {
                return o1.get(0).getSensorPosition().compareTo(o2.get(0).getSensorPosition());
            }
        });
    }

    /**
     * Get the names of all sensors positions which recorded data for this set
     *
     * @return
     */
    public ArrayList<String> getAllSensorPositions() {
        ArrayList<String> sensorPositions = new ArrayList<>();
        for (List<FrameData> sensorList : allSensorLists) {
            sensorPositions.add(sensorList.get(0).getSensorPosition());
        }
        return sensorPositions;
    }

    /**
     * Determine if this set contains enough data to cover the given window size
     *
     * @param windowSize
     * @return
     */
    public boolean enoughDataForWindowSize(double windowSize) {
        // if there is data at all, simply determine earliest and latest data
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
