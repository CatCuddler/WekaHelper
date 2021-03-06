package com.romanuhlig.weka.math;

import com.romanuhlig.weka.frameToFeature.FrameData;

/**
 * Provides math related helper functions
 *
 * @author Roman Uhlig
 */
public class MathHelper {

    /**
     * Calculate the acceleration based on a change in velocity
     *
     * @param positionBegin
     * @param positionFinal
     * @param timeBegin
     * @param timeFinal
     * @return
     */
    public static double calculateVelocityFromPosition(
            double positionBegin, double positionFinal, double timeBegin, double timeFinal) {
        double velocity = (positionFinal - positionBegin) / (timeFinal - timeBegin);
        if (Double.isNaN(velocity) || Double.isInfinite(velocity))
            return 0;
        else
            return velocity;
    }

    /**
     * Calculate the acceleration based on a change in velocity
     *
     * @param velocityBegin
     * @param velocityFinal
     * @param timeBegin
     * @param timeFinal
     * @return
     */
    public static double calculateAccelerationFromVelocity(
            double velocityBegin, double velocityFinal, double timeBegin, double timeFinal) {
        double acceleration = (velocityFinal - velocityBegin) / (timeFinal - timeBegin);
        if (Double.isNaN(acceleration) || Double.isInfinite(acceleration))
            return 0;
        else
            return acceleration;
    }

    /**
     * The euclidean norm for two values
     *
     * @param a
     * @param b
     * @return
     */
    public static double EuclideanNorm(double a, double b) {
        return Math.sqrt(a * a + b * b);
    }

    /**
     * The euclidean norm for three values
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double EuclideanNorm(double x, double y, double z, double w) {
        return Math.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * The euclidean norm for three values
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double EuclideanNorm(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Determine the minimum of two values, where NAN is considered to be lower than any other value
     *
     * @param double1
     * @param double2
     * @return
     */
    public static double getMinimumWithNAN(double double1, double double2) {

        if (Double.isNaN(double1)) {
            return double1;
        }

        if (Double.isNaN(double2)) {
            return double2;
        }

        return Double.min(double1, double2);
    }

    /**
     * Return the original value if it is not NAN, and zero if it is
     *
     * @param value
     * @return
     */
    public static double getZeroIfNAN(double value) {
        if (Double.isNaN(value)) {
            return 0;
        } else {
            return value;
        }
    }

    /**
     * The distanceRawPosition between two 3D-points
     *
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(
                Math.pow(x2 - x1, 2)
                        + Math.pow(y2 - y1, 2)
                        + Math.pow(z2 - z1, 2));
    }

    /**
     * The distanceRawPosition between two 2D-points
     *
     * @param x1
     * @param z1
     * @param x2
     * @param z2
     * @return
     */
    public static double distance(double x1, double z1, double x2, double z2) {
        return Math.sqrt(
                Math.pow(x2 - x1, 2)
                        + Math.pow(z2 - z1, 2));
    }

    /**
     * The distanceRawPosition between two 1D-points
     *
     * @param x1
     * @param x2
     * @return
     */
    public static double distance(double x1, double x2) {
        return Math.abs(x2 - x1);
    }

    /**
     * The distanceRawPosition between the two 3D-points given by the raw position within the frame data
     *
     * @param frameDataA
     * @param frameDataB
     * @return
     */
    public static double distancePosition(FrameData frameDataA, FrameData frameDataB) {
        return distance(
                frameDataA.getPosX(), frameDataA.getPosY(), frameDataA.getPosZ(),
                frameDataB.getPosX(), frameDataB.getPosY(), frameDataB.getPosZ());
    }

    /**
     * Get x-axis rotations
     * @param rotX
     * @param rotY
     * @param rotZ
     * @param rotW
     * @return
     */
    public static double getRoll(double rotX, double rotY, double rotZ, double rotW) {
        double t0 = 2.0 * (rotW * rotX + rotY * rotZ);
        double t1 = 1.0 - 2.0 * (rotX * rotX + rotY * rotY);
        return Math.atan2(t0, t1);
    }

    /**
     * Get y-axis rotations
     * @param rotX
     * @param rotY
     * @param rotZ
     * @param rotW
     * @return
     */
    public static double getPitch(double rotX, double rotY, double rotZ, double rotW) {
        double t2 = 2.0 * (rotW * rotY - rotZ * rotX);
	    t2 = t2 > 1.0 ? 1.0 : t2;
	    t2 = t2 < -1.0 ? -1.0 : t2;
	    return Math.asin(t2);
    }

    /**
     * Get z-axis rotations
     * @param rotX
     * @param rotY
     * @param rotZ
     * @param rotW
     * @return
     */
    public static double getYaw(double rotX, double rotY, double rotZ, double rotW) {
        double t3 = 2.0 * (rotW * rotZ + rotX * rotY);
        double t4 = 1.0 - 2.0 * (rotY * rotY + rotZ * rotZ);
	    return Math.atan2(t3, t4);
    }
}
