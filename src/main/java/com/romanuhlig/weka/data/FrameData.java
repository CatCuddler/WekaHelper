package com.romanuhlig.weka.data;

import com.opencsv.bean.CsvBindByName;
import com.romanuhlig.weka.math.MathHelper;

/**
 * Represents the data collected during a single frame, for a single sensor
 */
public class FrameData {

    @CsvBindByName(column = "tag")
    private String sensorPosition;
    @CsvBindByName
    private String subject;
    @CsvBindByName
    private String activity;

    @CsvBindByName(column = "rawPosX")
    private double calPosX;
    @CsvBindByName(column = "rawPosY")
    private double calPosY;
    @CsvBindByName(column = "rawPosZ")
    private double calPosZ;
    @CsvBindByName(column = "rawRotX")
    private double calRotX;
    @CsvBindByName(column = "rawRotY")
    private double calRotY;
    @CsvBindByName(column = "rawRotZ")
    private double calRotZ;
    @CsvBindByName(column = "rawRotW")
    private double calRotW;
    @CsvBindByName(column = "rawAngVelX")
    private double angVelX;
    @CsvBindByName(column = "rawAngVelY")
    private double angVelY;
    @CsvBindByName(column = "rawAngVelZ")
    private double angVelZ;
    @CsvBindByName(column = "rawLinVelX")
    private double linVelX;
    @CsvBindByName(column = "rawLinVelY")
    private double linVelY;
    @CsvBindByName(column = "rawLinVelZ")
    private double linVelZ;

    // the acceleration can not be read directly from the sensors, and has to be derived using two frames
    private double linAccelerationX;
    private double linAccelerationY;
    private double linAccelerationZ;
    private double angAccelerationX;
    private double angAccelerationY;
    private double angAccelerationZ;

    @CsvBindByName
    private double scale;
    @CsvBindByName
    private double time;

    // acceleration has to be calculated from the previous frame,
    // which leaves data points where that data can not be accurate
    boolean derivedDataCalculated = false;
    private double frameDuration = 0;

    // whether invalid values were detected while reading the data
    private boolean invalid = false;


    /**
     * Empty constructor, DO NOT USE
     * Required by OpenCSV to fill class automatically when reading from csv file, but should not be used manually
     */
    public FrameData() {
    }


    public FrameData(String sensorPosition, String subject, String activity,
                     double calPosX, double calPosY, double calPosZ,
                     double calRotX, double calRotY, double calRotZ, double calRotW,
                     double angVelX, double angVelY, double angVelZ,
                     double linVelX, double linVelY, double linVelZ,
                     double scale, double time) {
        this.sensorPosition = sensorPosition;
        this.subject = subject;
        this.activity = activity;
        this.calPosX = calPosX;
        this.calPosY = calPosY;
        this.calPosZ = calPosZ;
        this.calRotX = calRotX;
        this.calRotY = calRotY;
        this.calRotZ = calRotZ;
        this.calRotW = calRotW;
        this.angVelX = angVelX;
        this.angVelY = angVelY;
        this.angVelZ = angVelZ;
        this.linVelX = linVelX;
        this.linVelY = linVelY;
        this.linVelZ = linVelZ;
        this.scale = scale;
        this.time = time;
    }

    public void computeAdditionalDataBasedOnPreviousFrame(FrameData previousFrame) {

        // linear acceleration
        this.linAccelerationX = MathHelper.calculateAccelerationFromVelocity(previousFrame.linVelX, this.linVelX, previousFrame.time, this.time);
        this.linAccelerationY = MathHelper.calculateAccelerationFromVelocity(previousFrame.linVelY, this.linVelY, previousFrame.time, this.time);
        this.linAccelerationZ = MathHelper.calculateAccelerationFromVelocity(previousFrame.linVelZ, this.linVelZ, previousFrame.time, this.time);

        // angular acceleration
        this.angAccelerationX = MathHelper.calculateAccelerationFromVelocity(previousFrame.angVelX, this.angVelX, previousFrame.time, this.time);
        this.angAccelerationY = MathHelper.calculateAccelerationFromVelocity(previousFrame.angVelY, this.angVelY, previousFrame.time, this.time);
        this.angAccelerationZ = MathHelper.calculateAccelerationFromVelocity(previousFrame.angVelZ, this.angVelZ, previousFrame.time, this.time);

        this.frameDuration = this.time - previousFrame.time;

        derivedDataCalculated = true;
    }

    public String getSensorPosition() {
        return sensorPosition;
    }

    public String getSubject() {
        return subject;
    }

    public String getActivity() {
        return activity;
    }

    public double getCalPosX() {
        return calPosX;
    }

    public double getCalPosY() {
        return calPosY;
    }

    public double getCalPosZ() {
        return calPosZ;
    }

    public double getCalRotX() {
        return calRotX;
    }

    public double getCalRotY() {
        return calRotY;
    }

    public double getCalRotZ() {
        return calRotZ;
    }

    public double getCalRotW() {
        return calRotW;
    }

    public double getAngVelX() {
        return angVelX;
    }

    public double getAngVelY() {
        return angVelY;
    }

    public double getAngVelZ() {
        return angVelZ;
    }

    public double getLinVelX() {
        return linVelX;
    }

    public double getLinVelY() {
        return linVelY;
    }

    public double getLinVelZ() {
        return linVelZ;
    }

    public double getLinAccelerationX() {
        return linAccelerationX;
    }

    public double getLinAccelerationY() {
        return linAccelerationY;
    }

    public double getLinAccelerationZ() {
        return linAccelerationZ;
    }

    public double getAngAccelerationX() {
        return angAccelerationX;
    }

    public double getAngAccelerationY() {
        return angAccelerationY;
    }

    public double getAngAccelerationZ() {
        return angAccelerationZ;
    }

    public double getScale() {
        return scale;
    }

    public double getTime() {
        return time;
    }


    public boolean derivedDataWasCalculated() {
        return derivedDataCalculated;
    }

    public double getFrameDuration() {
        return frameDuration;
    }

    public void setInvalid() {
        this.invalid = true;
    }

    public boolean includesInvalidData(){
        return invalid;
    }

}
