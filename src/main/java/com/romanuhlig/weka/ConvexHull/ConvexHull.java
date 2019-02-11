package com.romanuhlig.weka.ConvexHull;/*
 * Convex hull algorithm - Library (Java)
 * 
 * Copyright (c) 2017 Project Nayuki
 * https://www.nayuki.io/page/convex-hull-algorithm
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (see COPYING.txt and COPYING.LESSER.txt).
 * If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class ConvexHull {
	
	// Returns a new list of convexHullPoints representing the convex hull of
	// the given set of convexHullPoints. The convex hull excludes collinear convexHullPoints.
	// This algorithm runs in O(n log n) time.
	public static List<ConvexHullPoint> makeHull(List<ConvexHullPoint> convexHullPoints) {
		List<ConvexHullPoint> newConvexHullPoints = new ArrayList<>(convexHullPoints);
		Collections.sort(newConvexHullPoints);
		return makeHullPresorted(newConvexHullPoints);
	}
	
	
	// Returns the convex hull, assuming that each convexHullPoints[i] <= convexHullPoints[i + 1]. Runs in O(n) time.
	public static List<ConvexHullPoint> makeHullPresorted(List<ConvexHullPoint> convexHullPoints) {
		if (convexHullPoints.size() <= 1)
			return new ArrayList<>(convexHullPoints);
		
		// Andrew's monotone chain algorithm. Positive y coordinates correspond to "up"
		// as per the mathematical convention, instead of "down" as per the computer
		// graphics convention. This doesn't affect the correctness of the result.
		
		List<ConvexHullPoint> upperHull = new ArrayList<>();
		for (ConvexHullPoint p : convexHullPoints) {
			while (upperHull.size() >= 2) {
				ConvexHullPoint q = upperHull.get(upperHull.size() - 1);
				ConvexHullPoint r = upperHull.get(upperHull.size() - 2);
				if ((q.x - r.x) * (p.y - r.y) >= (q.y - r.y) * (p.x - r.x))
					upperHull.remove(upperHull.size() - 1);
				else
					break;
			}
			upperHull.add(p);
		}
		upperHull.remove(upperHull.size() - 1);
		
		List<ConvexHullPoint> lowerHull = new ArrayList<>();
		for (int i = convexHullPoints.size() - 1; i >= 0; i--) {
			ConvexHullPoint p = convexHullPoints.get(i);
			while (lowerHull.size() >= 2) {
				ConvexHullPoint q = lowerHull.get(lowerHull.size() - 1);
				ConvexHullPoint r = lowerHull.get(lowerHull.size() - 2);
				if ((q.x - r.x) * (p.y - r.y) >= (q.y - r.y) * (p.x - r.x))
					lowerHull.remove(lowerHull.size() - 1);
				else
					break;
			}
			lowerHull.add(p);
		}
		lowerHull.remove(lowerHull.size() - 1);
		
		if (!(upperHull.size() == 1 && upperHull.equals(lowerHull)))
			upperHull.addAll(lowerHull);
		return upperHull;
	}
	
}




