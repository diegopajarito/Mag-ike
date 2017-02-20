package geoc.uji.esr7.mag_ike.common.tracker;

import android.content.Context;
import android.widget.TextView;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;

import java.util.ArrayList;
import java.util.List;

import geoc.uji.esr7.mag_ike.R;

/**
 * Created by diego on 25/06/16.
 */
public class ActivityTracker  {
    private ArrayList<TrackGPS> arrayTrackGPS = new ArrayList<>();
    private ArrayList<TrackSpeed> arrayTrackSpeed = new ArrayList<>();


    public void addTrackGPS(TrackGPS t){
        arrayTrackGPS.add(t);
    }

    public void addTrackSpeed(TrackSpeed t){
        arrayTrackSpeed.add(t);
    }

    public TrackSpeed getLastSpeed(){
        int last = arrayTrackSpeed.size();
        return arrayTrackSpeed.get(last-1);
    }



}
