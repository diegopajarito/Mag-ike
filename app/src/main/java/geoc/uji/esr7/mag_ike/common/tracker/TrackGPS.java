package geoc.uji.esr7.mag_ike.common.tracker;


import java.sql.Timestamp;

/**
 * Created by diego on 25/06/16.
 */
public class TrackGPS {
    private Timestamp timestamp;
    private float latitude;
    private float longitude;
    private float precision;

    public TrackGPS(float lat, float lon, float pres){
        Long timeMS = System.currentTimeMillis();
        timestamp = new Timestamp(timeMS);
        latitude = lat;
        longitude = lon;
        precision = pres;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getPrecision() {
        return precision;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

}
