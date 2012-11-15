package llc.ufwa.collections.geospatial;

public abstract class AbstractGeoItem implements GeoItem {
	
	private final double latitude;
	private final double longitude;
	private final double altitude;
	
	/**
	 * Forcing x y and z to be immutable otherwise spatial collection cannot remain sorted at insertion.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public AbstractGeoItem(final double latitude, final double longitude, final double altitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	@Override
	public double getLatitude() {
		return latitude;
	}

	@Override
	public double getLongitude() {
		return longitude;
	}

	@Override
	public double getAltitude() {
		return altitude;
	}
	
	
	

}
