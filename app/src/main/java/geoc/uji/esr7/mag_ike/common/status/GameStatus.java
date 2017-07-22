package geoc.uji.esr7.mag_ike.common.status;


import android.content.res.Resources;
import android.widget.Toast;

import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.fitness.data.Field;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;

import geoc.uji.esr7.mag_ike.R;
import geoc.uji.esr7.mag_ike.common.logger.Log;


/**
 * Created by diego on 3/09/16.
 */
public class GameStatus {
    private ParseObject parseObject;
    private Profile profile;
    private Trip trip;
    private String device;
    private String language;
    private String country;
    private Date campaign_start_date;
    private int campaign_length;
    private boolean trackingServiceStatus;



    //Setting Tags and default values
    public float no_data;
    private String status_class;
    private String profile_class;
    private String trip_class;
    public final String device_tag;
    public final String language_tag;
    public final String country_tag;
    public final String campaign_start_date_tag;
    public final String trip_start_date_tag;
    public final String trip_stop_date_tag;
    public final String trip_counter_tag;
    public final String avatar_tag;
    public final String avatar_id_tag;
    public final String gender_tag;
    public final String age_range_tag;
    public final String bike_type_tag;
    public final String bike_rented_tag;
    public final String email_tag;



    public GameStatus(Resources res) {


        //Setting the parse class name from resources
        this.status_class = res.getString(R.string.status_class_parse);
        this.profile_class = res.getString(R.string.profile_class_parse);
        this.trip_class = res.getString(R.string.trip_class_parse);

        //Setting default profile
        this.profile = new Profile();
        this.trip = new Trip();

        //Setting no data for starting date
        this.campaign_start_date = new Date(getProfile().id_not_set);

        //Setting all property tags from resources
        // those are used for saving with parse
        campaign_length = res.getInteger(R.integer.dashboard_campaign_length);
        campaign_start_date_tag = res.getString(R.string.campaign_start_tag);
        device_tag = res.getString(R.string.device_tag);
        language_tag = res.getString(R.string.language_tag);
        country_tag = res.getString(R.string.country_tag);
        trip_counter_tag = res.getString(R.string.trip_counter_tag);
        trip_start_date_tag = res.getString(R.string.trip_start_date_tag);
        trip_stop_date_tag = res.getString(R.string.trip_stop_date_tag);
        avatar_tag = res.getString(R.string.avatar_tag);
        avatar_id_tag = res.getString(R.string.avatar_id_tag);
        gender_tag = res.getString(R.string.gender_tag);
        age_range_tag = res.getString(R.string.age_range_tag);
        bike_type_tag = res.getString(R.string.bike_type_tag);
        bike_rented_tag = res.getString(R.string.bike_rented_tag);
        email_tag = res.getString(R.string.email_tag);

    }

    public void setCampaignStartDate(Date start) { this.campaign_start_date = start; }

    public Date getCampaignStartDate() { return campaign_start_date; }

    public int getCampaignDay(){
        Date today = new Date();
        int days = (int) Math.ceil( ( today.getTime() - getCampaignStartDate().getTime() ) / 1000.0 / 60.0 / 60.0 / 24.0 );
        if (days > this.getCampaignLength()) {
            setCampaignStartDate(today);
            days = (int) Math.ceil((today.getTime() - getCampaignStartDate().getTime()) / 1000.0 / 60.0 / 60.0 / 24.0);
        }
        return days;
    }

    public int getCampaignLength() {
        return campaign_length;
    }

    public void setCampaignLength(int campaign_length) {
        this.campaign_length = campaign_length;
    }

    public boolean isTrackingServiceStatus() { return trackingServiceStatus; }

    public void setTrackingServiceStatus(boolean trackingServiceStatus) { this.trackingServiceStatus = trackingServiceStatus; }

    public String getDevice() { return device; }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getLanguage() { return language; }

    public String getCountry() { return country; }

    public void setCountry(String country) { this.country = country; }

    public void setLanguage(String language) { this.language = language; }

    public Profile getProfile() {
        return profile;
    }

    public Trip getTrip() { return trip; }

    public boolean updateProfile(Profile p){
        Boolean updated = this.profile.updateProfile(p);
        if(updated)
            saveProfile_Eventually();
        return updated;
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
        parseObject.put(bike_rented_tag, this.profile.isBikeRented());
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

    public void saveTrip_Eventually(){
        parseObject = new ParseObject(this.trip_class);
        parseObject.put(device_tag, this.getDevice());
        parseObject.put(trip_counter_tag, this.trip.getTrip_counter());
        parseObject.put(trip_start_date_tag, this.trip.getStartTime());
        parseObject.put(trip_stop_date_tag, this.trip.getStopTime());
        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - trip SAVED OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE trip FAILED", String.valueOf(e));
                }
            }
        });
    }

}
