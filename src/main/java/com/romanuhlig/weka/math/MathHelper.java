package com.romanuhlig.weka.math;

public class MathHelper {


    public static double calculateVelocityFromPosition(double positionBegin, double positionFinal, double timeBegin, double timeFinal){
        return (positionFinal - positionBegin) / (timeFinal - timeBegin);
    }

    public static double calculateAccelerationFromVelocity(double velocityBegin, double velocityFinal, double timeBegin, double timeFinal) {
        return (velocityFinal - velocityBegin) / (timeFinal - timeBegin);
    }

}
