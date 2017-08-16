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
;

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
}
