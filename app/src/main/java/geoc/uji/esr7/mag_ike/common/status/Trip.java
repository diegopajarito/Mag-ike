package geoc.uji.esr7.mag_ike.common.status;

import java.util.Date;

/**
 * Created by pajarito on 19/07/2017.
 */

public class Trip {
    private int tripCounter;
    private Date startTime;
    private Date stopTime;

    public Trip (){
        tripCounter = 1;
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
}
