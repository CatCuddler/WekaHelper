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

    @CsvBindByName(column = "rawPosX")
    private double rawPosX;
    @CsvBindByName(column = "rawPosY")
    private double rawPosY;
    @CsvBindByName(column = "rawPosZ")
    private double rawPosZ;
    @CsvBindByName(column = "rawRotX")
    private double rawRotX;
    @CsvBindByName(column = "rawRotY")
    private double rawRotY;
    @CsvBindByName(column = "rawRotZ")
    private double rawRotZ;
    @CsvBindByName(column = "rawRotW")
    private double rawRotW;
    @CsvBindByName(column = "rawAngVelX")
    private double rawAngVelX;
    @CsvBindByName(column = "rawAngVelY")
    private double rawAngVelY;
    @CsvBindByName(column = "rawAngVelZ")
    private double rawAngVelZ;
    @CsvBindByName(column = "rawLinVelX")
    private double rawLinVelX;
    @CsvBindByName(column = "rawLinVelY")
    private double rawLinVelY;
    @CsvBindByName(column = "rawLinVelZ")
    private double rawLinVelZ;

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
     * @param rawPosX
     * @param rawPosY
     * @param rawPosZ
     * @param rawRotX
     * @param rawRotY
     * @param rawRotZ
     * @param rawRotW
     * @param rawAngVelX
     * @param rawAngVelY
     * @param rawAngVelZ
     * @param rawLinVelX
     * @param rawLinVelY
     * @param rawLinVelZ
     * @param scale
     * @param time
     */
    public FrameData(String sensorPosition, String subject, String activity,
                     double rawPosX, double rawPosY, double rawPosZ,
                     double rawRotX, double rawRotY, double rawRotZ, double rawRotW,
                     double rawAngVelX, double rawAngVelY, double rawAngVelZ,
                     double rawLinVelX, double rawLinVelY, double rawLinVelZ,
                     double scale, double time) {
        this.sensorPosition = sensorPosition;
        this.subject = subject;
        this.activity = activity;
        this.rawPosX = rawPosX;
        this.rawPosY = rawPosY;
        this.rawPosZ = rawPosZ;
        this.rawRotX = rawRotX;
        this.rawRotY = rawRotY;
        this.rawRotZ = rawRotZ;
        this.rawRotW = rawRotW;
        this.rawAngVelX = rawAngVelX;
        this.rawAngVelY = rawAngVelY;
        this.rawAngVelZ = rawAngVelZ;
        this.rawLinVelX = rawLinVelX;
        this.rawLinVelY = rawLinVelY;
        this.rawLinVelZ = rawLinVelZ;
        this.scale = scale;
        this.time = time;
    }

    /**
     * Fill out the data for this frame that requires knowledge of the previous frame
     *
     * @param previousFrame
     */
    public void computeAdditionalDataBasedOnPreviousFrame(FrameData previousFrame) {

        // linear acceleration
        this.linAccelerationX = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.rawLinVelX, this.rawLinVelX, previousFrame.time, this.time);
        this.linAccelerationY = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.rawLinVelY, this.rawLinVelY, previousFrame.time, this.time);
        this.linAccelerationZ = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.rawLinVelZ, this.rawLinVelZ, previousFrame.time, this.time);

        // angular acceleration
        this.angAccelerationX = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.rawAngVelX, this.rawAngVelX, previousFrame.time, this.time);
        this.angAccelerationY = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.rawAngVelY, this.rawAngVelY, previousFrame.time, this.time);
        this.angAccelerationZ = MathHelper.calculateAccelerationFromVelocity(
                previousFrame.rawAngVelZ, this.rawAngVelZ, previousFrame.time, this.time);

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
     * The unmodified x position, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawPosX() {
        return rawPosX;
    }

    /**
     * Final position
     *
     * @return
     */
    public double getFinalPosX() {
        return rawPosX;
    }

    /**
     * The unmodified y position, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawPosY() {
        return rawPosY;
    }

    /**
     * Final position
     *
     * @return
     */
    public double getFinalPosY() {
        return rawPosY;
    }

    /**
     * The unmodified z position, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawPosZ() {
        return rawPosZ;
    }

    /**
     * Final position
     *
     * @return
     */
    public double getFinalPosZ() {
        return rawPosZ;
    }

    /**
     * The unmodified x rotation, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawRotX() {
        return rawRotX;
    }

    /**
     * The unmodified y rotation, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawRotY() {
        return rawRotY;
    }

    /**
     * The unmodified z rotation, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawRotZ() {
        return rawRotZ;
    }

    /**
     * The unmodified w rotation, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawRotW() {
        return rawRotW;
    }

    /**
     * The unmodified x angular velocity, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawAngVelX() {
        return rawAngVelX;
    }

    /**
     * The unmodified y angular velocity, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawAngVelY() {
        return rawAngVelY;
    }

    /**
     * The unmodified z angular velocity, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawAngVelZ() {
        return rawAngVelZ;
    }

    /**
     * The unmodified x linear velocity, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawLinVelX() {
        return rawLinVelX;
    }

    /**
     * The unmodified y linear velocity, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawLinVelY() {
        return rawLinVelY;
    }

    /**
     * The unmodified z linear velocity, as reported by the sensor for this frame
     *
     * @return
     */
    public double getRawLinVelZ() {
        return rawLinVelZ;
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
