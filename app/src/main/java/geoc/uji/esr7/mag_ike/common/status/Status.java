package geoc.uji.esr7.mag_ike.common.status;


import android.content.res.Resources;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.Date;

import geoc.uji.esr7.mag_ike.R;
import geoc.uji.esr7.mag_ike.common.logger.Log;


/**
 * Created by diego on 3/09/16.
 */
public class Status {
    private ParseObject parseObject;
    private String status_class;
    private String device_tag;
    private String device;
    private String latitude_tag;
    private float latitude;
    private String longitude_tag;
    private float longitude;
    private String altitude_tag;
    private int altitude;
    private String precision_tag;
    private int precision;
    private String time_gps_tag;
    private Date time_gps;
    private String distance_tag;
    private float distance;
    private String time_distance_tag;
    private Date time_distance;
    private String speed_tag;
    private float speed;
    private String time_speed_tag;
    private Date time_speed;
    private final float no_data = Float.valueOf(R.string.value_nodata);

    public Status(Resources res) {


        //Setting the parse class name from resources
        this.status_class = res.getString(R.string.status_class_parse);

        //Setting all properties to no_data Value
        latitude = longitude = speed = no_data;
        altitude = precision = (int) no_data;
        time_gps = time_speed = time_distance = new Date();

        //Setting all properties tags from resources
        device_tag = res.getString(R.string.device_tag);
        latitude_tag = res.getString(R.string.latitude_tag);
        longitude_tag = res.getString(R.string.longitude_tag);
        altitude_tag = res.getString(R.string.altitude_tag);
        precision_tag = res.getString(R.string.precision_tag);
        time_gps_tag = res.getString(R.string.time_gps_tag);
        distance_tag = res.getString(R.string.distance_tag);
        time_distance_tag = res.getString(R.string.time_distance_tag);
        speed_tag = res.getString(R.string.speed_tag);
        time_speed_tag = res.getString(R.string.time_speed_tag);


    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        if (latitude != no_data)
            this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        if (longitude != no_data)
            this.longitude = longitude;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        if (altitude != no_data)
            this.altitude = altitude;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        if (precision != no_data)
            this.precision = precision;
    }

    public Date getTime_gps() {
        return time_gps;
    }

    public void setTime_gps(Date time_gps) {
        this.time_gps = time_gps;
    }

    public Date getTime_distance() {
        return time_distance;
    }

    public void setTime_distance(Date time_distance) {
        this.time_distance = time_distance;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        if (distance != no_data)
            this.distance = distance;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if(speed != no_data)
            this.speed = speed;
    }

    public Date getTime_speed() {
        return time_speed;
    }

    public void setTime_speed(Date time_speed) {
        this.time_speed = time_speed;
    }

    public void saveStatus_Eventually(String dev, float lat, float lon, float alt, float pre){

        parseObject = new ParseObject(this.status_class);
        this.setDevice(dev);
        parseObject.add(device_tag,this.device);
        this.setLatitude(lat);
        parseObject.add(latitude_tag,this.latitude);
        this.setLongitude(lon);
        parseObject.add(longitude_tag,this.longitude);
        this.setAltitude((int) alt);
        parseObject.add(altitude_tag,this.altitude);
        this.setPrecision((int) pre);
        parseObject.add(precision_tag,this.precision);
        this.setTime_gps(new Date());
        parseObject.add(time_gps_tag,this.time_gps);
        parseObject.add(distance_tag,this.distance);
        parseObject.add(time_distance_tag,this.time_distance);
        parseObject.add(speed_tag,this.speed);
        parseObject.add(time_speed_tag,this.time_speed);

        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - SAVE OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE FAILED", String.valueOf(e));
                }
            }
        });

        //return answer;
    }

    public void saveStatus_Eventually(String dev, String label, float value){
        parseObject = new ParseObject(this.status_class);
        this.setDevice(dev);
        parseObject.add(device_tag,this.device);
        parseObject.add(latitude_tag,this.latitude);
        parseObject.add(longitude_tag,this.longitude);
        parseObject.add(altitude_tag,this.altitude);
        parseObject.add(precision_tag,this.precision);
        parseObject.add(time_gps_tag,this.time_gps);
        if (label.equals("speed")) {
            parseObject.add(distance_tag,this.distance);
            parseObject.add(time_distance_tag, this.time_distance);
            this.setSpeed(value);
            parseObject.add(speed_tag, this.speed);
            this.setTime_speed(new Date());
            parseObject.add(time_speed_tag, this.time_speed);
        } else if (label.equals("distance")){
            this.setDistance(value);
            parseObject.add(distance_tag,this.distance);
            this.setTime_distance(new Date());
            parseObject.add(time_distance_tag, this.time_distance);
            parseObject.add(speed_tag, this.speed);
            parseObject.add(time_speed_tag, this.time_speed);
        } else {
            return;
        }
        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    //Log.d("PARSE - SAVE OK", String.valueOf(e));
                } else {
                    //Log.d("PARSE - SAVE FAILED", String.valueOf(e));

                }
            }
        });

        //return answer;
    }
}
