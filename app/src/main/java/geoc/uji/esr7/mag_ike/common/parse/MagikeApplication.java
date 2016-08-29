package geoc.uji.esr7.mag_ike.common.parse;

import android.app.Application;

import com.parse.Parse;

import geoc.uji.esr7.mag_ike.R;

/**
 * Created by pajarito on 27/06/16.
 */
public class MagikeApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("enableDashboard")
                .server("http://150.128.97.150:1337/parse")   // '/' important after 'parse' //Should be changed for a String from XML file
                .build());


    }
}
