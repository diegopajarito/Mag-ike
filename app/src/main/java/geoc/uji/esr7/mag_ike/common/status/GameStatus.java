package geoc.uji.esr7.mag_ike.common.status;


import android.app.Activity;
import android.content.res.Resources;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import geoc.uji.esr7.mag_ike.R;
import geoc.uji.esr7.mag_ike.SessionActivity;
import geoc.uji.esr7.mag_ike.common.logger.Log;
import geoc.uji.esr7.mag_ike.common.logger.LogRecord;


/**
 * Created by diego on 3/09/16.
 */
public class GameStatus {
    private Resources resources;
    private ParseObject parseObject;
    private Profile profile;
    private LeaderBoardStatus leaderboard;
    private Trip trip;
    private String device;
    private String language;
    private String country;
    private Date campaign_start_date;
    private int campaign_length;
    private int tag_count;
    private boolean trackingServiceStatus;



    //Setting Tags and default values
    public float no_data;
    private String status_class;
    private String profile_class;
    private String trip_class;
    private String tag_class;
    private String experiment_class;
    public final String device_tag;
    public final String language_tag;
    public final String country_tag;
    public final String campaign_start_date_tag;
    public final String trip_tag;
    public final String trip_start_date_tag;
    public final String trip_stop_date_tag;
    public final String trip_counter_tag;
    public final String trip_end_point_tag;
    public final String text_tag;
    public final String createdAt_tag;
    public final String profile_tag;
    public final String tags_count_tag;
    public final String avatar_tag;
    public final String avatar_id_tag;
    public final String gender_tag;
    public final String age_range_tag;
    public final String bike_type_tag;
    public final String bike_rented_tag;
    public final String email_tag;
    public final String experiment_status_tag;
    public final String experiment_profile_tag;


    private Boolean experimentStatus;
    private String experimentProfile;



    public GameStatus(Resources res) {
        this.resources = res;
        //Setting the parse class name from resources
        this.status_class = res.getString(R.string.status_class_parse);
        this.profile_class = res.getString(R.string.profile_class_parse);
        this.trip_class = res.getString(R.string.trip_class_parse);
        this.tag_class = res.getString(R.string.tags_class_parse);
        this.experiment_class = res.getString(R.string.experiment_class_parse);

        //Setting default profile
        this.profile = new Profile();
        this.trip = new Trip();
        this.leaderboard = new LeaderBoardStatus(resources);

        //Setting no data for starting date
        this.campaign_start_date = new Date(getProfile().id_not_set);

        //Setting all property tags from resources
        // those are used for saving with parse
        campaign_length = res.getInteger(R.integer.dashboard_campaign_length);
        campaign_start_date_tag = res.getString(R.string.campaign_start_tag);
        device_tag = res.getString(R.string.device_tag);
        language_tag = res.getString(R.string.language_tag);
        country_tag = res.getString(R.string.country_tag);
        trip_tag = res.getString(R.string.trip_counter_tag);
        trip_counter_tag = res.getString(R.string.trip_counter_tag);
        trip_start_date_tag = res.getString(R.string.trip_start_date_tag);
        trip_stop_date_tag = res.getString(R.string.trip_stop_date_tag);
        trip_end_point_tag = res.getString(R.string.trip_end_point_tag);
        text_tag = res.getString(R.string.text_tag);
        createdAt_tag = res.getString(R.string.createdAt_tag);
        profile_tag = res.getString(R.string.profile_tag);
        tags_count_tag = res.getString(R.string.tags_counter_tag);
        avatar_tag = res.getString(R.string.avatar_tag);
        avatar_id_tag = res.getString(R.string.avatar_id_tag);
        gender_tag = res.getString(R.string.gender_tag);
        age_range_tag = res.getString(R.string.age_range_tag);
        bike_type_tag = res.getString(R.string.bike_type_tag);
        bike_rented_tag = res.getString(R.string.bike_rented_tag);
        email_tag = res.getString(R.string.email_tag);
        experiment_status_tag = res.getString(R.string.experiment_status_tag);
        experiment_profile_tag = res.getString(R.string.experiment_profile_tag);

    }

    public void setCampaignStartDate(Date start) { this.campaign_start_date = start; }

    public Date getCampaignStartDate() { return campaign_start_date; }

    public int getCampaignDay(){
        Date today = new Date();
        int days = (int) Math.ceil( ( today.getTime() - getCampaignStartDate().getTime() ) / 1000.0 / 60.0 / 60.0 / 24.0 );
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

    public LeaderBoardStatus getLeaderboard() { return leaderboard; }

    public Trip getTrip() { return trip; }

    public int getTag_count() {
        return tag_count;
    }

    public void setTag_count(int tag_count) {
        this.tag_count = tag_count;
    }

    public void addTagCount(){
        this.tag_count++;
    }

    public boolean updateProfile(Profile p){
        Boolean updated = this.profile.updateProfile(p);
        if(updated)
            saveProfile_Eventually();
        return updated;
    }

    public Boolean isExperiment() {
        return experimentStatus;
    }

    public String getExperimentProfile() {
        return experimentProfile;
    }

    public void setExperimentStatus(Boolean status){
        this.experimentStatus = status;
    }

    public void setExperimentProfile(String profile){
        this.experimentProfile = profile;
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
                    String text;
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
        parseObject.put(trip_end_point_tag, new ParseGeoPoint(this.getTrip().getLatitudeEndPoint(), this.getTrip().getLongitudeEndPoint()));
        parseObject.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - trip SAVED OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE trip FAILED", String.valueOf(e));
                }
            }
        });
        saveTags_Eventually();
    }


    public void saveTags_Eventually(){
        for (int i = 0; i < getTrip().getTags().size(); i++) {
            this.addTagCount();
            parseObject = new ParseObject(this.tag_class);
            parseObject.put(device_tag, this.getDevice());
            parseObject.put(trip_tag, this.trip.getTrip_counter());
            parseObject.put(tags_count_tag, this.getTag_count());
            parseObject.put(text_tag,this.getTrip().getTags().get(i));
            parseObject.saveEventually(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("PARSE - tag SAVED OK", String.valueOf(e));
                    } else {
                        Log.d("PARSE - SAVE tag FAILED", String.valueOf(e));
                    }
                }
            });
        }
    }

    public void saveExperimentProfile_Eventually(){

        parseObject = new ParseObject(this.experiment_class);
        parseObject.put(device_tag, this.getDevice());
        parseObject.put(profile_tag, this.getExperimentProfile());
        parseObject.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - profile SAVED OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE profile FAILED", String.valueOf(e));
                }
            }
        });
    }

    public void updateExperimentStatus(String status){
        //Looks like the object id is needed
    }


    public void getExperimentProfileFromServer(final SessionActivity activity){
        String[] deviceArray = {device};
        final ParseQuery<ParseObject> query = ParseQuery.getQuery(experiment_class);
        query.whereContainedIn(device_tag, Arrays.asList(deviceArray));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> results, ParseException e) {
                if (e == null ){ //define when there is an existing record here
                    if (results.size()>0){
                        String results_profile = results.get(0).getString(profile_tag);
                        experimentProfile = results_profile;
                        activity.setUpExperimentInterface(experimentProfile);
                    } else {
                        ParseQuery<ParseObject> query_last_experiment = ParseQuery.getQuery(experiment_class);
                        query_last_experiment.orderByDescending(createdAt_tag);
                        query_last_experiment.setLimit(1);
                        query_last_experiment.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> results, ParseException e) {
                                if (e == null) { //define when there is an existing record here
                                    if (results.size()>0) {
                                        String results_profile = results.get(0).getString(profile_tag);
                                        String results_device = results.get(0).getString((device_tag));
                                        if (device.equals(results_device)) {
                                            experimentProfile = results_profile;
                                        }else {
                                            if (results_profile.equals(resources.getString(R.string.experiment_profile_collaboration))) {
                                                experimentProfile = resources.getString(R.string.experiment_profile_competition);
                                            } else if (results_profile.equals(resources.getString(R.string.experiment_profile_competition))) {
                                                experimentProfile = resources.getString(R.string.experiment_profile_collaboration);
                                            }
                                        }
                                    }
                                    saveExperimentProfile_Eventually();
                                    activity.setUpExperimentInterface(experimentProfile);
                                } else {
                                    android.util.Log.d("Cyclist", "Error: " + e.getMessage());
                                }
                            }
                        });
                    }
                } else {
                    android.util.Log.d("Cyclist", "Error: " + e.getMessage());
                }
            }
        });
    }

}
