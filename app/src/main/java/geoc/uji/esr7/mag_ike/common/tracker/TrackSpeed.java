package geoc.uji.esr7.mag_ike.common.tracker;

import java.sql.Timestamp;

/**
 * Created by diego on 25/06/16.
 */
public class TrackSpeed {
    private Timestamp timestamp;
    private Float speed;
    private TrackGPS gpsPosition;

    public TrackSpeed(float s){
        setTimestamp();
        speed = s;
    }

    public TrackSpeed(float s, TrackGPS gpsPos){
        setTimestamp();
        speed = s;
        gpsPosition = gpsPos;
    }

    private void setTimestamp(){
        Long timeMS = System.currentTimeMillis();
        timestamp = new Timestamp(timeMS);
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public TrackGPS getGpsPosition() {
        return gpsPosition;
    }

    public void setGpsPosition(TrackGPS gpsPosition) {
        this.gpsPosition = gpsPosition;
    }
}
