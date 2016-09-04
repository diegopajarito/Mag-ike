package geoc.uji.esr7.mag_ike.common.parse;

import android.app.Application;
import android.content.res.Resources;

import com.parse.Parse;

import geoc.uji.esr7.mag_ike.R;

/**
 * Created by pajarito on 27/06/16.
 */
public class MagikeApplication extends Application{

    private Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        // Getting settings from settings XML File in values
        String server_url = getResources().getString(R.string.server_parse); // '/' important after 'parse'
        String applicationId = getResources().getString(R.string.application_id_parse);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId(applicationId)
                .server(server_url)//)
                .build());


    }
}
