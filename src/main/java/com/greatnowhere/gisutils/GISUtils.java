package com.greatnowhere.gisutils;

import android.location.Location;

public class GISUtils {
	
	@SuppressWarnings("unused")
	private static final String TAG = GISUtils.class.getCanonicalName();
	
	public static final double EARTH_RADIUS = 6371000D;
	
	/**
	 * Returns bounding box centered on location
	 * 
	 * @param loc
	 * @param radius, in meters
	 * @return long lat long lat
	 */
	public static double[] getBoundingBoxCoords(Location loc, Double radius) {
		double dY = 360 * radius / EARTH_RADIUS;
		double dX = dY * Math.cos(Math.toRadians(loc.getLatitude()));
		return new double[] { loc.getLongitude() - dX, loc.getLatitude() - dY, loc.getLongitude() + dX, loc.getLatitude() + dY};
	}

}
