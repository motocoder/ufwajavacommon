package llc.ufwa.geo;

import java.io.Serializable;

import llc.ufwa.collections.geospatial.GeoItem;

/**
 * 
 * @author swagner
 *
 */
public class RawPoint implements Serializable, GeoItem {
	
	private static final long serialVersionUID = -5873461152430476211L;
	
	private final double latitude;
	private final double longitude;
	private final double altitude;
	
	private final long acquiredTime;
	private final float speed;
	private final float bearing;
	
	private final float accuracy;
	
	/**
	 * 
	 * @param latitude
	 * @param longitude
	 * @param acquiredTime
	 * @param speed
	 * @param bearing
	 * @param altitude
	 * @param accuracy
	 */
	public RawPoint(
		final double latitude,
		final double longitude,
	    final long acquiredTime,
	    final float speed,
	    final float bearing,
	    final double altitude,
	    final float accuracy
	) {
		this.accuracy = accuracy;
		this.latitude = latitude;
		this.longitude = longitude;		
		this.acquiredTime = acquiredTime;
		this.speed = speed;
		this.bearing = bearing;
		this.altitude = altitude;
	}

	public long getAcquiredTime() {
		return acquiredTime;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public float getBearing() {
		return bearing;
	}
		
	public double getAltitude() {
		return altitude;
	}
	
	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public float getAccuracy() {
		return accuracy;
	}
}
