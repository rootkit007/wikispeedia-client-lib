package org.wikispeedia.models;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.simpleframework.xml.Attribute;

public class Marker {
	
	public static final double KPH_TO_MS = 0.277778D;
	public static final double MPH_TO_MS = 0.44704D;

	@Attribute(name="label",required=false)
	public String label;
	
	@Attribute(name="lat",required=false)
	public Double lat;
	@Attribute(name="lng",required=false)
	public Double lng;
	
	@Attribute(name="mph",required=false)
	public Integer mph;
	@Attribute(name="kph",required=false)
	public Integer kph;
	
	@Attribute(name="cog",required=false)
	public Double cog;
	
	/**
	 * Altitude in meters
	 */
	@Attribute(name="alt_meters",required=false)
	public Double altitude;
	
	@Attribute(name="deletedOn",required=false)
	public String deletedOn;
	
	/**
	 * Return difference between this marker's cog and another
	 * Low value indicates movement in the same direction
	 * @param otherCog
	 * @return
	 */
	public double compareToCog(Double otherCog) {
		if ( cog == null || otherCog == null ) return 180; // unknown cog
		return Math.min((cog-otherCog)<0?cog-otherCog+360:cog-otherCog, (otherCog-cog)<0?otherCog-cog+360:otherCog-cog);
	}
	
	/**
	 * Returns speed limit in m/s
	 * @return
	 */
	public double getSpeedLimitMS() {
		if ( kph != null && kph > 0 ) {
			return kph * KPH_TO_MS;
		}
		if ( mph != null && mph > 0 ) {
			return mph * MPH_TO_MS;
		}
		return 0;
	}

	public String toString() {
		return label + " kph " + kph + " mph " + mph;
	}
	
	public Date getDeletedOnDate() {
		if ( deletedOn == null )
			return null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date convertedCurrentDate = sdf.parse(deletedOn);
			return convertedCurrentDate;
		} catch (Exception ex) {
		}
		return null;
	}

}
