package geoc.uji.esr7.mag_ike.common.tracker;

/**
 * Created by diego on 25/06/16.
 */
public interface TrackNode {

    public void addGPSTrack(float lat, float lon, float pres);

    public void addSpeedTrack(float speed);
}
