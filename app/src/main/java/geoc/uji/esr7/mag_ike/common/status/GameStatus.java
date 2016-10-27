package geoc.uji.esr7.mag_ike.common.status;


import android.content.res.Resources;
import android.widget.Toast;

import com.google.android.gms.fitness.data.Field;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.Date;

import geoc.uji.esr7.mag_ike.R;
import geoc.uji.esr7.mag_ike.common.logger.Log;


/**
 * Created by diego on 3/09/16.
 */
public class GameStatus {
    private ParseObject parseObject;
    private Profile profile;
    private String device;
    private String language;
    private String country;
    private float latitude;
    private float longitude;
    private float altitude;
    private float precision;
    private Date time_gps;
    private float distance;
    private float last_distance;
    private Date time_distance;
    private float speed;
    private Date time_speed;
    private float cycling;
    private Date time_cycling;
    private int locationContribution =0;
    private int distanceContribution =0;
    private int speedContribution =0;
    private int cyclingContribution=0;


    //Setting Tags and default values
    public final float no_data = Float.valueOf(R.integer.value_nodata);
    private String status_class;
    private String profile_class;
    private String device_tag;
    private String language_tag;
    private String country_tag;
    private String avatar_tag;
    private String avatar_id_tag;
    private String gender_tag;
    private String age_range_tag;
    private String bike_type_tag;
    private String bike_type_id_tag;
    private String email_tag;
    private String latitude_tag;
    private String longitude_tag;
    private String altitude_tag;
    private String precision_tag;
    private String time_gps_tag;
    private String distance_tag;
    private String last_distance_tag;
    private String time_distance_tag;
    private String speed_tag;
    private String time_speed_tag;
    private String cycling_tag;
    private String time_cycling_tag;


    public GameStatus(Resources res) {


        //Setting the parse class name from resources
        this.status_class = res.getString(R.string.status_class_parse);
        this.profile_class = res.getString(R.string.profile_class_parse);

        //Setting all properties to no_data Value
        latitude = longitude = altitude = precision = speed = cycling = no_data;
        time_gps = time_speed = time_distance = time_cycling = new Date();

        //Setting default profile
        this.profile = new Profile();

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
        cycling_tag = res.getString(R.string.cycling_tag);
        time_cycling_tag = res.getString(R.string.time_cycling_tag);
        language_tag = res.getString(R.string.language_tag);
        country_tag = res.getString(R.string.country_tag);
        avatar_tag = res.getString(R.string.avatar_tag);
        avatar_id_tag = res.getString(R.string.avatar_id_tag);
        gender_tag = res.getString(R.string.gender_tag);
        age_range_tag = res.getString(R.string.age_range_tag);
        bike_type_tag = res.getString(R.string.bike_type_tag);
        bike_type_id_tag = res.getString(R.string.bike_type_id_tag);
        email_tag = res.getString(R.string.email_tag);

    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getLanguage() { return language; }

    public String getCountry() { return country; }

    public void setCountry(String country) { this.country = country; }

    public void setLanguage(String language) { this.language = language; }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        if (latitude != no_data)
            this.latitude = latitude;
    }

    public float getLongitude() { return longitude; }

    public void setLongitude(float longitude) {
        if (longitude != no_data)
            this.longitude = longitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        if (altitude != no_data)
            this.altitude = altitude;
    }

    public float getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        if (precision != no_data)
            this.precision = precision;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        if (distance != no_data)
            this.distance = distance;
    }

    public float getLast_distance() {
        return last_distance;
    }

    public void setLast_distance(float last_distance) {
        this.last_distance = last_distance;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if(speed != no_data)
            this.speed = speed;
    }

    public float getCycling() { return cycling; }

    public void setCycling(float cycling) {
        if (cycling != no_data)
            this.cycling = cycling;
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

    public Date getTime_speed() {
        return time_speed;
    }

    public void setTime_speed(Date time_speed) {
        this.time_speed = time_speed;
    }

    public Date getTime_cycling() { return time_cycling; }

    public void setTime_cycling(Date time_cycling) { this.setTime_cycling(time_cycling);}

    public Profile getProfile() {
        return profile;
    }

    public int getLocationContribution() { return locationContribution; }

    public void addLocationContribution() { this.locationContribution += 1; }

    public int getDistanceContribution() { return distanceContribution; }

    public void addDistanceContribution() { this.distanceContribution += 1; }

    public int getSpeedContribution() { return speedContribution; }

    public void addSpeedContribution() { this.speedContribution += 1; }

    public int getCyclingContribution() { return cyclingContribution; }

    public void addCyclingContribution() { this.cyclingContribution =+ 1; }

    public int getTotalContribution() {return getLocationContribution() + getDistanceContribution() + getSpeedContribution() + getCyclingContribution(); }

    public boolean updateProfile(Profile p){
        Boolean updated = this.profile.updateProfile(p);
        if(updated)
            saveProfile_Eventually();
        return updated;
    }

    public void saveStatus_Eventually(float lat, float lon, float alt, float pre){

        parseObject = new ParseObject(this.status_class);
        parseObject.put(device_tag,this.getDevice());
        this.setLatitude(lat);
        parseObject.put(latitude_tag,this.getLatitude());
        this.setLongitude(lon);
        parseObject.put(longitude_tag,this.getLongitude());
        this.setAltitude(alt);
        parseObject.put(altitude_tag,this.getAltitude());
        this.setPrecision((int) pre);
        parseObject.put(precision_tag,this.getPrecision());
        this.setTime_gps(new Date());
        parseObject.put(time_gps_tag,this.getTime_gps());
        parseObject.put(distance_tag,this.getDistance());
        parseObject.put(time_distance_tag,this.getTime_distance());
        parseObject.put(speed_tag,this.getSpeed());
        parseObject.put(time_speed_tag,this.getTime_speed());
        parseObject.put(cycling_tag,this.getCycling());
        parseObject.put(time_cycling_tag,this.getTime_cycling());
        addLocationContribution();

        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - LOCATION SAVED OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE LOCATION FAILED", String.valueOf(e));
                }
            }
        });

        //return answer;
    }

    public void saveStatus_Eventually(final String label, float value){
        parseObject = new ParseObject(this.status_class);
        parseObject.put(device_tag,this.getDevice());
        parseObject.put(latitude_tag,this.getLatitude());
        parseObject.put(longitude_tag,this.getLongitude());
        parseObject.put(altitude_tag,this.getAltitude());
        parseObject.put(precision_tag,this.getPrecision());
        parseObject.put(time_gps_tag,this.getTime_gps());
        if (label.equals(Field.FIELD_SPEED.getName())) {
            parseObject.put(distance_tag,this.getDistance());
            parseObject.put(time_distance_tag, this.getTime_distance());
            this.setSpeed(value);
            parseObject.put(speed_tag, this.getSpeed());
            this.setTime_speed(new Date());
            parseObject.put(time_speed_tag, this.getTime_speed());
            parseObject.put(cycling_tag,this.cycling);
            parseObject.put(time_cycling_tag,this.getCycling());
            addSpeedContribution();
        } else if (label.equals(Field.FIELD_RPM.getName())){
            parseObject.put(distance_tag,this.getDistance());
            parseObject.put(time_distance_tag, this.getTime_distance());
            parseObject.put(speed_tag, this.getSpeed());
            parseObject.put(time_speed_tag, this.getTime_speed());
            this.setCycling(value);
            parseObject.put(cycling_tag,this.getCycling());
            this.setTime_cycling(new Date());
            parseObject.put(time_cycling_tag,this.getTime_cycling());
            addCyclingContribution();
        } else {
            return;
        }
        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - " + label + " SAVED OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE " + label + " FAILED", String.valueOf(e));

                }
            }
        });
    }

    public void saveStatus_Eventually(float distance, float last_distance){
        parseObject = new ParseObject(this.status_class);
        parseObject.put(device_tag,this.getDevice());
        parseObject.put(latitude_tag,this.getLatitude());
        parseObject.put(longitude_tag,this.getLongitude());
        parseObject.put(altitude_tag,this.getAltitude());
        parseObject.put(precision_tag,this.getPrecision());
        parseObject.put(time_gps_tag,this.getTime_gps());
        this.setDistance(distance);
        parseObject.put(distance_tag,this.getDistance());
        this.setLast_distance(last_distance);
        parseObject.put(last_distance_tag,this.getLast_distance());
        this.setTime_distance(new Date());
        parseObject.put(time_distance_tag, this.getTime_distance());
        parseObject.put(speed_tag, this.getSpeed());
        parseObject.put(time_speed_tag, this.getTime_speed());
        parseObject.put(cycling_tag,this.getCycling());
        parseObject.put(time_cycling_tag,this.getTime_cycling());
        addDistanceContribution();
        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - DISTANCE SAVED OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE DISTANCE FAILED", String.valueOf(e));

                }
            }
        });
    }

    private void saveProfile_Eventually(){
        parseObject = new ParseObject(this.profile_class);
        parseObject.put(device_tag, this.getDevice());
        parseObject.put(country_tag, this.getCountry());
        parseObject.put(language_tag, this.getLanguage());
        parseObject.put(avatar_tag, this.profile.getAvatarName());
        parseObject.put(avatar_id_tag, this.profile.getAvatarId());
        parseObject.put(gender_tag, this.profile.getGender());
        parseObject.put(age_range_tag, this.profile.getAgeRange());
        parseObject.put(bike_type_tag, this.profile.getBikeType());
        parseObject.put(email_tag, this.profile.getEmail());
        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - profile SAVED OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE profile FAILED", String.valueOf(e));
                }
            }
        });
    }

}
