package com.romanuhlig.weka.frameToFeature;

import com.opencsv.bean.CsvBindByName;
import com.romanuhlig.weka.math.MathHelper;

/**
 * Represents the data collected during a single frame, for a single sensor
 * <p>
 * Primarily used for reading recorded data through OpenCSV
 *
 * @author Roman Uhlig
 */
public class FrameData {

    // these fields are automatically filled by OpenCSV through their column name
    @CsvBindByName(column = "tag")
    private String sensorPosition;
    // if the column and field name are identical, no additional declaration is required
    @CsvBindByName
    private String subject;
    @CsvBindByName
    private String activity;

    @CsvBindByName(column = "finalPosX")
    private double posX;
    @CsvBindByName(column = "finalPosY")
    private double posY;
    @CsvBindByName(column = "finalPosZ")
    private double posZ;
    @CsvBindByName(column = "finalRotX")
    private double rotX;
    @CsvBindByName(column = "finalRotY")
    private double rotY;
    @CsvBindByName(column = "finalRotZ")
    private double rotZ;
    @CsvBindByName(column = "finalRotW")
    private double rotW;
    /*@CsvBindByName(column = "angVelX")
    private double angVelX;
    @CsvBindByName(column = "angVelY")
    private double angVelY;
    @CsvBindByName(column = "angVelZ")
    private double angVelZ;
    @CsvBindByName(column = "angVelW") // Missing in .csv
    private double angVelW;
    @CsvBindByName(column = "linVelX")
    private double linVelX;
    @CsvBindByName(column = "linVelY")
    private double linVelY;
    @CsvBindByName(column = "linVelZ")
    private double linVelZ;*/
    private double linVelX;
    private double linVelY;
    private double linVelZ;
    private double angVelX;
    private double angVelY;
    private double angVelZ;
    private double angVelW;

    // the acceleration can not be read directly from the sensors, and has to be derived using two frames
    private double linAccelerationX;
    private double linAccelerationY;
    private double linAccelerationZ;
    private double angAccelerationX;
    private double angAccelerationY;
    private double angAccelerationZ;
    private double angAccelerationW;

    @CsvBindByName
    private double scale;
    @CsvBindByName
    private double time;

    // acceleration and frame duration have to be calculated from the previous frame,
    // which leaves data points where that data can not be determined
    private boolean derivedDataCalculated = false;
    private double frameDuration = 0;

    // whether invalid values were detected while reading the data
    private boolean invalid = false;


    /**
     * DO NOT USE, empty constructor:
     * Required by OpenCSV to fill fields automatically when reading from csv file, but should not be used manually
     */
    public FrameData() {
    }


    /**
     * Create new Frame Data with the given attributes
     *
     * @param sensorPosition
     * @param subject
     * @param activity
     * @param posX
     * @param posY
     * @param posZ
     * @param rotX
     * @param rotY
     * @param rotZ
     * @param rotW
     * @param angVelX
     * @param angVelY
     * @param angVelZ
     * @param linVelX
     * @param linVelY
     * @param linVelZ
     * @param scale
     * @param time
     */
    public FrameData(String sensorPosition, String subject, String activity,
                     double posX, double posY, double posZ,
                     double rotX, double rotY, double rotZ, double rotW,
                     //double angVelX, double angVelY, double angVelZ,
                     //double linVelX, double linVelY, double linVelZ,
                     double scale, double time) {
        this.sensorPosition = sensorPosition;
        this.subject = subject;
        this.activity = activity;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.rotW = rotW;
        /*this.angVelX = angVelX;
        this.angVelY = angVelY;
        this.angVelZ = angVelZ;
        this.linVelX = linVelX;
        this.linVelY = linVelY;
        this.linVelZ = linVelZ;*/
        this.scale = scale;
        this.time = time;
    }

    /**
     * Fill out the data for this frame that requires knowledge of the previous frame
     *
     * @param previousFrame
     */
    public void computeAdditionalDataBasedOnPreviousFrame(FrameData previousFrame) {
        
        // linear velocity
        this.linVelX = MathHelper.calculateVelocityFromPosition(
                previousFrame.posX, this.posX, previousFrame.time, this.time);
        this.linVelY = MathHelper.calculateVelocityFromPosition(
                previousFrame.posY, this.posY, previousFrame.time, this.time);
        this.linVelZ = MathHelper.calculateVelocityFromPosition(
                previousFrame.posZ, this.posZ, previousFrame.time, this.time);
        
        // angular velocity 
        this.angVelX = MathHelper.calculateVelocityFromPosition(
                previousFrame.rotX, this.rotX, previousFrame.time, this.time);
        this.angVelY = MathHelper.calculateVelocityFromPosition(
                previousFrame.rotY, this.rotY, previousFrame.time, this.time);
        this.angVelZ = MathHelper.calculateVelocityFromPosition(
                previousFrame.rotZ, this.rotZ, previousFrame.time, this.time);
        this.angVelW = MathHelper.calculateVelocityFromPosition(
                previousFrame.rotW, this.rotW, previousFrame.time, this.time);

        // linear acceleration
        this.linAccelerationX = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.linVelX, this.linVelX, previousFrame.time, this.time);
        this.linAccelerationY = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.linVelY, this.linVelY, previousFrame.time, this.time);
        this.linAccelerationZ = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.linVelZ, this.linVelZ, previousFrame.time, this.time);

        // angular acceleration
        this.angAccelerationX = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.angVelX, this.angVelX, previousFrame.time, this.time);
        this.angAccelerationY = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.angVelY, this.angVelY, previousFrame.time, this.time);
        this.angAccelerationZ = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.angVelZ, this.angVelZ, previousFrame.time, this.time);
        this.angAccelerationW = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.angVelW, this.angVelW, previousFrame.time, this.time);

        // frame duration
        this.frameDuration = this.time - previousFrame.time;

        derivedDataCalculated = true;
    }

    /**
     * The position the sensor is worn, also known as the "tag"
     *
     * @return
     */
    public String getSensorPosition() {
        return sensorPosition;
    }

    /**
     * The subject that recorded the data
     *
     * @return
     */
    public String getSubject() {
        return subject;
    }

    /**
     * The recorded activity
     *
     * @return
     */
    public String getActivity() {
        return activity;
    }

    /**
     * The x position
     *
     * @return
     */
    public double getPosX() {
        return posX;
    }

    /**
     * The y position
     *
     * @return
     */
    public double getPosY() {
        return posY;
    }

    /**
     * The z position
     *
     * @return
     */
    public double getPosZ() {
        return posZ;
    }

    /**
     * The x rotation
     *
     * @return
     */
    public double getRotX() {
        return rotX;
    }

    /**
     * The y rotation
     *
     * @return
     */
    public double getRotY() {
        return rotY;
    }

    /**
     * The z rotation
     *
     * @return
     */
    public double getRotZ() {
        return rotZ;
    }

    /**
     * The w rotation
     *
     * @return
     */
    public double getRotW() {
        return rotW;
    }

    /**
     * The x angular velocity
     *
     * @return
     */
    public double getAngVelX() {
        return angVelX;
    }

    /**
     * The y angular velocity
     *
     * @return
     */
    public double getAngVelY() {
        return angVelY;
    }

    /**
     * The z angular velocity
     *
     * @return
     */
    public double getAngVelZ() {
        return angVelZ;
    }

    /**
     * The w angular velocity
     *
     * @return
     */
    public double getAngVelW() {
        return angVelW;
    }

    /**
     * The x linear velocity
     *
     * @return
     */
    public double getLinVelX() {
        return linVelX;
    }

    /**
     * The y linear velocity
     *
     * @return
     */
    public double getLinVelY() {
        return linVelY;
    }

    /**
     * The z linear velocity
     *
     * @return
     */
    public double getLinVelZ() {
        return linVelZ;
    }

    /**
     * The x linear acceleration, derived from the velocity of this frame and the previous frame
     *
     * @return
     */
    public double getLinAccelerationX() {
        return linAccelerationX;
    }

    /**
     * The y linear acceleration, derived from the velocity of this frame and the previous frame
     *
     * @return
     */
    public double getLinAccelerationY() {
        return linAccelerationY;
    }

    /**
     * The z linear acceleration, derived from the velocity of this frame and the previous frame
     *
     * @return
     */
    public double getLinAccelerationZ() {
        return linAccelerationZ;
    }

    /**
     * The x angular acceleration, derived from the velocity of this and the previous frame
     *
     * @return
     */
    public double getAngAccelerationX() {
        return angAccelerationX;
    }

    /**
     * The y angular acceleration, derived from the velocity of this and the previous frame
     *
     * @return
     */
    public double getAngAccelerationY() {
        return angAccelerationY;
    }

    /**
     * The z angular acceleration, derived from the velocity of this and the previous frame
     *
     * @return
     */
    public double getAngAccelerationZ() {
        return angAccelerationZ;
    }

    /**
     * The w angular acceleration, derived from the velocity of this and the previous frame
     *
     * @return
     */
    public double getAngAccelerationW() {
        return angAccelerationW;
    }

    /**
     * The scale, as determined by the height of the HMD during the subject calibration
     *
     * @return
     */
    public double getScale() {
        return scale;
    }

    /**
     * The timestamp when this frame was recorded
     *
     * @return
     */
    public double getTime() {
        return time;
    }

    /**
     * Whether the previous frame was already used to derive additional features for this frame
     *
     * @return
     */
    public boolean derivedDataWasCalculated() {
        return derivedDataCalculated;
    }

    /**
     * The difference in time between this frame and the previous frame
     *
     * @return
     */
    public double getFrameDuration() {
        return frameDuration;
    }

    /**
     * Mark this frame as containing data which cannot have been recorded correctly
     * <p>
     * E.g., if the sensor reported to be 100 meters above the ground
     */
    public void setInvalid() {
        this.invalid = true;
    }

    public boolean includesInvalidData() {
        return invalid;
    }

}
