package llc.ufwa.geo;

import llc.ufwa.collections.geospatial.GeoItem;

public class DefaultGeoItem implements GeoItem {

    private final double latitude;
    private final double longitude;
    private final double altitude;

    /**
     * 
     * @param latitude
     * @param longitude
     * @param altitude
     */
    public DefaultGeoItem(
        final double latitude,
        final double longitude,
        final double altitude
    ) {
        
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
