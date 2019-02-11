package com.romanuhlig.weka.ConvexHull;

import java.util.Objects;

public final class ConvexHullPoint implements Comparable<ConvexHullPoint> {

    public final double x;
    public final double y;


    public ConvexHullPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }


    public String toString() {
        return String.format("ConvexHullPoint(%g, %g)", x, y);
    }


    public boolean equals(Object obj) {
        if (!(obj instanceof ConvexHullPoint))
            return false;
        else {
            ConvexHullPoint other = (ConvexHullPoint)obj;
            return x == other.x && y == other.y;
        }
    }


    public int hashCode() {
        return Objects.hash(x, y);
    }


    public int compareTo(ConvexHullPoint other) {
        if (x != other.x)
            return Double.compare(x, other.x);
        else
            return Double.compare(y, other.y);
    }

}