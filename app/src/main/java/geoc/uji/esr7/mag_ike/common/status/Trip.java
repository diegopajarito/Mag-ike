package geoc.uji.esr7.mag_ike.common.status;

import java.util.Date;
import java.util.List;

/**
 * Created by pajarito on 19/07/2017.
 */

public class Trip {
    private int tripCounter;
    private Date startTime;
    private Date stopTime;
    private List<String> tags;
    private float latitudeStartPoint;
    private float longitudeStartPoint;
    private float latitudeCurrentLocation;
    private float longitudeCurrentLocation;
    private float latitudeEndPoint;
    private float longitudeEndPoint;


    public Trip (){
        tripCounter = 0;
    }

    public void addTripToCounter() {
        this.tripCounter ++;
    }

    public int getTrip_counter() {
        return tripCounter;
    }

    public void setTrip_counter(int counter){
        this.tripCounter = counter;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setStartPoint(float lat, float lon){
        latitudeStartPoint= lat;
        longitudeStartPoint = lon;
        updateCurrentLocation(latitudeStartPoint,longitudeStartPoint);
    }

    public void updateCurrentLocation(float lat, float lon){
        latitudeCurrentLocation= lat;
        longitudeCurrentLocation = lon;
    }

    public void setEndPoint(float lat, float lon){
        latitudeEndPoint= lat;
        longitudeEndPoint = lon;
    }

    public float getLatitudeStartPoint() {
        return latitudeStartPoint;
    }

    public float getLongitudeStartPoint() {
        return longitudeStartPoint;
    }

    public float getLatitudeCurrentLocation() {
        return latitudeCurrentLocation;
    }

    public float getLongitudeCurrentLocation() {
        return longitudeCurrentLocation;
    }

    public float getLatitudeEndPoint(){
        return latitudeEndPoint;
    }

    public float getLongitudeEndPoint(){
        return longitudeEndPoint;
    }


}
