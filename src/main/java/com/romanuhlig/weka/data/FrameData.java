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
    private double calPosX;
    @CsvBindByName
    private double calPosY;
    @CsvBindByName
    private double calPosZ;
    @CsvBindByName
    private double calRotX;
    @CsvBindByName
    private double calRotY;
    @CsvBindByName
    private double calRotZ;
    @CsvBindByName
    private double calRotW;
    @CsvBindByName
    private double angVelX;
    @CsvBindByName
    private double angVelY;
    @CsvBindByName
    private double angVelZ;
    @CsvBindByName
    private double linVelX;
    @CsvBindByName
    private double linVelY;
    @CsvBindByName
    private double linVelZ;

    //TODO: - deal with angular acceleration as well
    //TODO: - either read real velocity values, or compute them as well
    // the acceleration can not be read directly from the sensors, and has to be derived
    private double accelerationX;
    private double accelerationY;
    private double accelerationZ;

    @CsvBindByName
    private double scale;
    @CsvBindByName
    private double time;


    public void setAccelerationBasedOnPreviousFrame(FrameData previousFrame) {
        this.accelerationX = MathHelper.calculateAccelerationFromVelocity(previousFrame.linVelX, this.linVelX, previousFrame.time, this.time);
        this.accelerationY = MathHelper.calculateAccelerationFromVelocity(previousFrame.linVelY, this.linVelY, previousFrame.time, this.time);
        this.accelerationZ = MathHelper.calculateAccelerationFromVelocity(previousFrame.linVelZ, this.linVelZ, previousFrame.time, this.time);
    }



    public String getSensorPosition() {
        return sensorPosition;
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

    public double getAccelerationX() {
        return accelerationX;
    }

    public double getAccelerationY() {
        return accelerationY;
    }

    public double getAccelerationZ() {
        return accelerationZ;
    }

    public double getScale() {
        return scale;
    }

    public double getTime() {
        return time;
    }
}
