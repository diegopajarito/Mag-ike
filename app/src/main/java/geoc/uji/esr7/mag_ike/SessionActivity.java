package geoc.uji.esr7.mag_ike;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import geoc.uji.esr7.mag_ike.common.logger.Log;
import geoc.uji.esr7.mag_ike.common.logger.LogRecord;
import geoc.uji.esr7.mag_ike.common.status.GameStatus;
import geoc.uji.esr7.mag_ike.common.status.Profile;
import geoc.uji.esr7.mag_ike.common.tracker.TrackingService;

public class SessionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        DashboardFragment.OnLocationChangeListener, ProfileFragment.OnProfileChangeListener, DashboardFragment.onDashboardUpdate,
        LeaderBoardFragment.onLeaderBoardUpdate, ScoreFragment.onLeaderBoardUpdate {

    private static final int REQUEST_PERMISSIONS_EMAIL_CODE = 1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_MULTIPLE_PERMISSIONS_EMAILCONTACTS_CODE = 123;
    private boolean LOCATION_PERMISSION_GRANTED = false;
    private boolean CONTACTS_PERMISSION_GRANTED = false;
    private static final int REQUEST_OAUTH = 1431;


    LogRecord logRecord;

    private static final String TAG = "Cycling";
    // [START auth_variable_references]
    private GoogleApiClient mClient = null;
    // [END auth_variable_references]

    // Connection result activity for dealing with google fit
    private ConnectionResult mFitResultResolution;

    // The activity Service Intent
    private Intent mTrackingIntent;

    // The Shared preferences objects
    private SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSPEditor;

    // Floating button to control the location tracking service
    FloatingActionButton btn_pause;


    // Navigation View to Control interface for Experiment
    private NavigationView navigationView;

    // A status object used for storing game status through data fragments
    public GameStatus gameStatus;

    // Fragments for being used on interface development
    private ProfileFragment profileFragment = new ProfileFragment();
    private DashboardFragment dashboardFragment = new DashboardFragment();
    private SurveyFragment dashboardSurvey = new SurveyFragment();
    private LeaderBoardFragment leaderboardFragment = new LeaderBoardFragment();
    private ScoreFragment scoreFragment = new ScoreFragment();
    private AboutFragment aboutFragment = new AboutFragment();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Setting options and services needed for the app
         * on-screen Logger - to be deleted
         * permissions checked for accessing the account data and fitness api
         * Android service for starting the location track
         * Logging into the Parse server for managing data storage
         */

        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        LOCATION_PERMISSION_GRANTED = checkPermissions_fitness();
        CONTACTS_PERMISSION_GRANTED = checkPermissions_account();


        /** dealing with interface set
         * Set the layout
         * check the fragments that are active when menu is selected
         * setting the toolbar
         * setting the side drawer layout
         * setting the avatar and personal details on the side drawer
         * setting the floating button for control the location trackin service
         */
        setContentView(R.layout.activity_session);

        // Starting and Setting the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setting the drawer layout, a toggle for open/close and a listener for selected items
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();


        // Setting the navigation view with avatar and personal identification
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Adding a floating button and its listener

        btn_pause = (FloatingActionButton) findViewById(R.id.btn_pause);
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (gameStatus.isTrackingServiceStatus()) {
                    gameStatus.setTrackingServiceStatus(stopTrackingService());
                } else {
                    gameStatus.setTrackingServiceStatus(startTrackingService());
                }

            }
        });


        LocalBroadcastManager.getInstance(this).registerReceiver(mFitStatusReceiver, new IntentFilter(TrackingService.FIT_NOTIFY_INTENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationReceiver, new IntentFilter(TrackingService.LOCATION_UPDATE_INTENT));

        // Setting Parse Server with username and password
        checkParseLogIn();


    }

    @Override
    protected void onStop() {
        saveStatusOnSharedPreferences(gameStatus);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFitStatusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mFitStatusReceiver);
        super.onStop();

    }

    @Override
    protected void onResume(){
        super.onResume();

        if (LOCATION_PERMISSION_GRANTED && CONTACTS_PERMISSION_GRANTED) {
            gameStatus = new GameStatus(getResources());
            Date now = Calendar.getInstance().getTime();

            // Game setup
            if (isSharedPreferencesEmpty()) {
                gameStatus.setDevice(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
                gameStatus.setLanguage(getResources().getConfiguration().locale.getDisplayLanguage());
                gameStatus.setCountry(getResources().getConfiguration().locale.getDisplayCountry());
                gameStatus.setCampaignStartDate(now);
                checkUserData();
                //saveStatusOnSharedPreferences(gameStatus);
            } else {
                updateStatusFromSharedPreferences();

                logRecord = LogRecord.getInstance();
                logRecord.setUpLogRecord(getResources(),gameStatus.getDevice());

                if (gameStatus.getExperimentProfile() == null)
                    gameStatus.getExperimentProfileFromServer(this);

                //
                long start = getTripStartDateOnSharedPreferences();
                if (start > 0) {
                    gameStatus.getTrip().setStartTime(new Date(start));
                } else {
                    gameStatus.getTrip().setStartTime(now);
                }

                // Update
                gameStatus.getLeaderboard().updateTrips(this);

                // Set Fragment
                android.support.v4.app.FragmentManager mSupportFM = getSupportFragmentManager();

                // Get existing fragment for Dashboard
                if (mSupportFM.findFragmentByTag(getString(R.string.dashboardFragment_label)) == null) {
                    dashboardFragment.setArguments(getIntent().getExtras());
                    mSupportFM.beginTransaction()
                            .add(R.id.fragment_container, dashboardFragment, getString(R.string.dashboardFragment_label)).commit();
                } else {
                    dashboardFragment = (DashboardFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.dashboardFragment_label));
                }
                // If tracking service is not started, start it
                if (!gameStatus.isTrackingServiceStatus() && checkPermissions_fitness()) {
                    gameStatus.setTrackingServiceStatus(startTrackingService());
                }

                //Set Dashboard fragment and sidebar
                updateSidebarFromProfile();

            }
        } else {
            // Show snack bar
            // Disable all menu but about
            // Set Info Fragment

            requestPermissions(LOCATION_PERMISSION_GRANTED, CONTACTS_PERMISSION_GRANTED);
        }


    }


    public void setUpExperimentInterface(String status) {

        if(gameStatus.getCampaignDay()>gameStatus.getCampaignLength()){
            gameStatus.setExperimentStatus(false);
        } else {
            gameStatus.setExperimentStatus(true);
            if (status.equals(getResources().getString(R.string.experiment_profile_collaboration)))
                navigationView.getMenu().findItem(R.id.nav_leader_board).setEnabled(false);
        }



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Long start_time =  getTripStartDateOnSharedPreferences();
        if (start_time > 0 ) {
            Date stop_time = new Date(Calendar.getInstance().getTimeInMillis());
            gameStatus.getTrip().setStopTime(stop_time);
            gameStatus.getTrip().addTripToCounter();
            gameStatus.saveTrip_Eventually();
        }

        saveStatusOnSharedPreferences(gameStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == R.id.action_location) {
            Toast.makeText(getApplicationContext(), R.string.action_location_text, Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.action_cycling) {
            Toast.makeText(getApplicationContext(), R.string.action_fitness_text, Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Handle navigation view item clicks here.
        if (item.getItemId() == R.id.nav_play) {
            if(!dashboardFragment.isVisible()){
                transaction.replace(R.id.fragment_container, dashboardFragment);

                getFragmentManager().popBackStack(null, getFragmentManager().POP_BACK_STACK_INCLUSIVE);
            }
        } else if (item.getItemId() == R.id.nav_leader_board) {
            if (!leaderboardFragment.isVisible()){
                transaction.replace(R.id.fragment_container, leaderboardFragment);

            } else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.leaderboardFragment_label)) == null){
                transaction.add(profileFragment, getString(R.string.leaderboardFragment_label));

            }
        } else if (item.getItemId() == R.id.nav_score) {
            if (!scoreFragment.isVisible()){
                transaction.replace(R.id.fragment_container, scoreFragment);

            } else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.dashboardScoreFragment_label)) == null){
                transaction.add(scoreFragment, getString(R.string.dashboardScoreFragment_label));

            }
        }
        /*else if (item.getItemId() == R.id.nav_survey) {
            if (!dashboardTagsFragment.isVisible()){
                transaction.replace(R.id.fragment_container, dashboardTagsFragment);
                //transaction.addToBackStack();
            } else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.dashboardTagsFragment_label)) == null){
                transaction.add(dashboardTagsFragment, getString(R.string.dashboardTagsFragment_label));
                //transaction.addToBackStack(null);
            }
        }*/ else if (item.getItemId() == R.id.nav_profile) {
            if (!profileFragment.isVisible()){
                transaction.replace(R.id.fragment_container, profileFragment);
                transaction.addToBackStack(null);
            } else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.profileFragment_label)) == null){
                transaction.add(profileFragment, getString(R.string.profileFragment_label));

            }
        } else if (item.getItemId() == R.id.nav_about) {
            if (!aboutFragment.isVisible()){
                transaction.replace(R.id.fragment_container, aboutFragment);

            } else if (getSupportFragmentManager().findFragmentByTag(getString(R.string.aboutFragment_label)) == null){
                transaction.add(aboutFragment,getString(R.string.aboutFragment_label));
            }
        }
        transaction.addToBackStack(null);
        transaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void checkParseLogIn(){
        ParseUser.logInInBackground(getString(R.string.username_parse), getString(R.string.password_parse),
                new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    Log.i(getString(R.string.tag_log), "Connected to Parse - Mag-ike.");
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    Log.i(getString(R.string.tag_log), "Connection to Parse failed - Mag-ike - " + e.toString());
                }
            }
        });
    }


    /**
     * Two methods for control the Location tracking service
     * They are controlled based on the gameStatus Service tag
     * First: starts the service and timer, sets icon to pause and notification
     * Second: stops the service, timer and notification, sets icon to play
     */

    private boolean startTrackingService(){


        mTrackingIntent = new Intent(this, TrackingService.class);
        this.startService(mTrackingIntent);

        onTrackingServiceStart(gameStatus.getTrip().getStartTime().getTime());
        saveTripStartDateOnSharedPreferences();

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SessionActivity.class);
        // Adds the Intent of the current activity that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(getIntent());

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT );

        btn_pause.setImageResource(R.drawable.ic_button_pause);
        Log.i(getString(R.string.tag_log), "Tracking service Started");
        return true;
    }

    private boolean stopTrackingService(){
        this.stopService(mTrackingIntent);
        btn_pause.setImageResource(R.drawable.ic_button_play);
        onTrackingServiceStop();
        mTrackingIntent = null;
        Log.i(getString(R.string.tag_log), "Tracking service Stopped");
        return false;
    }



    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions_fitness() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPermissions_account() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissions(boolean permission_location, boolean permission_contacts) {

        final String[] permissions_string;
        if (!permission_location && !permission_contacts)
            permissions_string = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.GET_ACCOUNTS};
        else if (!permission_location && permission_contacts)
            permissions_string = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        else if (permission_location && !permission_contacts)
            permissions_string = new String[]{Manifest.permission.GET_ACCOUNTS};
        else {
            return;}



        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.

        Boolean shouldShowRequestPermissionRationale_ACCESS_FINE_LOCATION = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        Boolean shouldShowRequestPermissionRationale_GET_ACCOUNTS_ = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS);
        if ( shouldShowRequestPermissionRationale_ACCESS_FINE_LOCATION || shouldShowRequestPermissionRationale_GET_ACCOUNTS_) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission

                            ActivityCompat.requestPermissions(SessionActivity.this, permissions_string,
                                    REQUEST_MULTIPLE_PERMISSIONS_EMAILCONTACTS_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting fitness permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(SessionActivity.this, permissions_string,
                    REQUEST_MULTIPLE_PERMISSIONS_EMAILCONTACTS_CODE);
        }

    }

    private void requestPermissions_account() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.GET_ACCOUNTS);
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(android.R.id.content),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(SessionActivity.this,
                                    new String[]{Manifest.permission.GET_ACCOUNTS},
                                    REQUEST_PERMISSIONS_EMAIL_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting account permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(SessionActivity.this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    // Functions gets an intent to start an activity
    // then onActivityResult will set variables up
    private void checkUserData(){
        try {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
            startActivityForResult(intent, REQUEST_PERMISSIONS_EMAIL_CODE);
        } catch (ActivityNotFoundException e) {
            Log.i(TAG, "Error requesting user data");
        }
    }

    /**
     * Callback received when default user data is requested
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSIONS_EMAIL_CODE && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            gameStatus.getProfile().setEmail(accountName);
            saveStatusOnSharedPreferences(gameStatus);
            updateSidebarFromProfile();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(getString(R.string.tag_log), "onRequestPermissionResult");
        if (requestCode == REQUEST_MULTIPLE_PERMISSIONS_EMAILCONTACTS_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(getString(R.string.tag_log), "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                LOCATION_PERMISSION_GRANTED = checkPermissions_fitness();
                CONTACTS_PERMISSION_GRANTED = checkPermissions_account();

                Log.i(getString(R.string.tag_log), "User granted the permission");
            } else {
                // Permission denied.

                // In this Activity we've chosen to notify the user that they
                // have rejected a core permission for the app since it makes the Activity useless.
                // We're communicating this message in a Snackbar since this is a sample app, but
                // core permissions would typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        SessionActivity.this.findViewById(android.R.id.content),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }


    /**
     * Implementing interface update for side bar
     */

    public void updateSidebarFromProfile(){
        TextView tv = (TextView) findViewById(R.id.avatar_name_header);
        if (tv != null){
            tv.setText(gameStatus.getProfile().getAvatarName());
        }
        tv = (TextView) findViewById(R.id.avatar_email_header);
        if (tv != null){
            tv.setText(gameStatus.getProfile().getEmail());
        }

    }



    /**
     * Implementing interfaces for Profile Fragment
     */

    public boolean onProfileUpdated(Profile p){
        boolean updated = false;
        if (gameStatus.getProfile() != p){
            updated = gameStatus.updateProfile(p);
            saveStatusOnSharedPreferences(gameStatus);
            updateSidebarFromProfile();
        }
        return updated;
    }

    @Override
    public Profile getCurrentProfile(){
        return gameStatus.getProfile();
    }


    @Override
    public GameStatus getGameStatus() {
        return gameStatus;
    }

    @Override
    public long getChronometerBase() {
        long value;
        if (gameStatus != null) {
            value = gameStatus.getTrip().getStartTime().getTime();
        } else {
            value = Calendar.getInstance().getTime().getTime();
        }
        return value;
    }

    @Override
    public void onTrackingServiceStart(long base){
        dashboardFragment.onTrackingServiceStart(base);
    }

    @Override
    public void onTrackingServiceStop(){
        dashboardFragment.onTrackingServiceStop();
    }

    @Override
    public int getDayOfCampaign() {
        return gameStatus.getCampaignDay();
    }

    @Override
    public int getTripCounter() {
        return gameStatus.getTrip().getTrip_counter();
    }

    @Override
    public void onTagsUpdated(List<String> tags) {
        gameStatus.getTrip().setTags(tags);
    }

    /**
     * Using a Broadcast Receiver to communicate from Service to Activity
     * Action, check permissions
     */
    private BroadcastReceiver mFitStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent.hasExtra(TrackingService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE) &&
                    intent.hasExtra(TrackingService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE)) {
                //Recreate the connection result
                int statusCode = intent.getIntExtra(TrackingService.FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE, 0);
                PendingIntent pendingIntent = intent.getParcelableExtra(TrackingService.FIT_EXTRA_NOTIFY_FAILED_INTENT);
                ConnectionResult result = new ConnectionResult(statusCode, pendingIntent);
                Log.d(getString(R.string.tag_log), "Fit connection failed - opening connect screen.");
                fitHandleFailedConnection(result);
            }
            if (intent.hasExtra(TrackingService.FIT_EXTRA_CONNECTION_MESSAGE)) {
                Log.d(getString(R.string.tag_log), "Fit connection successful - closing connect screen if it's open.");

                fitHandleConnection();
            }
        }
    };

    /**
     * Using a Broadcast Receiver to communicate from Service to Activity
     * Action, update interface
     */

    private BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float speed = intent.getFloatExtra(getString(R.string.speed_tag),0);
            float distance = intent.getFloatExtra(getString(R.string.last_distance_tag),0);
            updateDashboard(speed, distance);
        }
    };

    private void fitHandleConnection() {
        Toast.makeText(this, "Fit connected", Toast.LENGTH_SHORT).show();
    }

    private void fitHandleFailedConnection(ConnectionResult result) {
        Log.i(TAG, "Activity Thread Google Fit Connection failed. Cause: " + result.toString());
        logRecord.writeLog_Eventually("Activity Thread Google Fit Connection failed. Cause: " + result.toString());
        if (!result.hasResolution()) {
            // Show the localized error dialog
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), SessionActivity.this, 0).show();
            return;
        } else if (result.getErrorCode() == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
            try {
                Log.d(TAG, "Google Fit connection failed with OAuth failure.  Trying to ask for consent (again)");
                logRecord.writeLog_Eventually("Google Fit connection failed with OAuth failure.  Trying to ask for consent (again)");
                result.startResolutionForResult(SessionActivity.this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Activity Thread Google Fit Exception while starting resolution activity", e);
                logRecord.writeLog_Eventually("Activity Thread Google Fit Exception while starting resolution activity - " + e.getMessage());
            }
        } else if (result.getErrorCode() == FitnessStatusCodes.SIGN_IN_REQUIRED) {
        try {
            Log.d(TAG, "Google Fit connection required to sign in failure.  Trying to ask for consent (again)");
            logRecord.writeLog_Eventually("Google Fit connection failed with OAuth failure.  Trying to ask for consent (again)");
            result.startResolutionForResult(SessionActivity.this, ConnectionResult.SIGN_IN_REQUIRED);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Activity Thread Google Fit Exception while starting resolution activity", e);
            logRecord.writeLog_Eventually("Activity Thread Google Fit Exception while starting resolution activity - " + e.getMessage());
        }
        }else {
            Log.i(TAG, "Activity Thread Google Fit Attempting to resolve failed connection");
            logRecord.writeLog_Eventually("Activity Thread Google Fit Attempting to resolve failed connection");
            mFitResultResolution = result;
        }

/*
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an authorization dialog is displayed to the user.
        if (!authInProgress) {
            //if (result.getErrorCode() == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
                try {
                    Log.d(TAG, "Google Fit connection failed with OAuth failure.  Trying to ask for consent (again)");
                    result.startResolutionForResult(SessionActivity.this, REQUEST_OAUTH);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Activity Thread Google Fit Exception while starting resolution activity", e);
            //    }
            //} else {

                Log.i(TAG, "Activity Thread Google Fit Attempting to resolve failed connection");
                    logRecord.writeLog_Eventually("Activity Thread Google Fit Attempting to resolve failed connection");
                mFitResultResolution = result;

            }

        }*/
    }






    /**
     * methods to deal with temporal data on shared preferences
     */

    public boolean isSharedPreferencesEmpty(){
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getAll().isEmpty();
    }

    // Checks if any value from game status or profile have changed and stores the value as key/value in shared preferences
    public void saveStatusOnSharedPreferences(GameStatus temporalStatus){
        mSharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        mSPEditor = mSharedPreferences.edit();
        if(!gameStatus.getDevice().equals(null)) {
            mSPEditor.putString(this.gameStatus.device_tag, this.gameStatus.getDevice());
        }
        if(!gameStatus.getCountry().equals(null)) {
            mSPEditor.putString(this.gameStatus.country_tag, this.gameStatus.getCountry());
        }
        if(!gameStatus.getLanguage().equals(null)) {
            mSPEditor.putString(this.gameStatus.language_tag, this.gameStatus.getLanguage());
        }
        if(gameStatus.getCampaignStartDate().getTime() > 0){
            mSPEditor.putLong(this.gameStatus.campaign_start_date_tag, this.gameStatus.getCampaignStartDate().getTime());
        }
        if(gameStatus.getTrip().getTrip_counter() > 0){
            mSPEditor.putInt(this.gameStatus.trip_counter_tag, this.gameStatus.getTrip().getTrip_counter());
        }
        if(gameStatus.getTag_count() > 1){
            mSPEditor.putInt(this.gameStatus.tags_count_tag, this.gameStatus.getTag_count());
        }
        if(gameStatus.isExperiment() != null){
            mSPEditor.putBoolean(this.gameStatus.experiment_status_tag, this.gameStatus.isExperiment());
        }
        if(gameStatus.getExperimentProfile() !=null ){
            mSPEditor.putString(this.gameStatus.experiment_profile_tag, this.gameStatus.getExperimentProfile());
        }
        if(!gameStatus.getProfile().getAvatarName().equals(getText(R.string.avatar_label))) {
            mSPEditor.putString(this.gameStatus.avatar_tag, this.gameStatus.getProfile().getAvatarName());
        }
        if(gameStatus.getProfile().getAvatarId() != this.gameStatus.getProfile().id_not_set) {
            mSPEditor.putInt(this.gameStatus.avatar_id_tag, this.gameStatus.getProfile().getAvatarId());
        }
        if(!gameStatus.getProfile().getGender().equals(gameStatus.getProfile().text_not_set)) {
            mSPEditor.putString(this.gameStatus.gender_tag, this.gameStatus.getProfile().getGender());
        }
        if(!gameStatus.getProfile().getAgeRange().equals(gameStatus.getProfile().text_not_set)) {
            mSPEditor.putString(this.gameStatus.age_range_tag, this.gameStatus.getProfile().getAgeRange());
        }
        if(gameStatus.getProfile().isBikeRented() != false){
            mSPEditor.putBoolean(this.gameStatus.bike_rented_tag, this.gameStatus.getProfile().isBikeRented());
        }
        if(gameStatus.getProfile().getBikeType() != this.gameStatus.getProfile().id_not_set){
            mSPEditor.putInt(this.gameStatus.bike_type_tag, this.gameStatus.getProfile().getBikeType());
        }
        if(!gameStatus.getProfile().getEmail().equals(gameStatus.getProfile().text_not_set)){
            mSPEditor.putString(this.gameStatus.email_tag, this.gameStatus.getProfile().getEmail());
        }
        mSPEditor.commit();
    }

    // Checks key/values in shared preferences different than default values and stored into game status and profile
    public boolean updateStatusFromSharedPreferences(){
        boolean changed=false;
        String notSet = getString(R.string.text_no_set);
        String value;
        boolean value_bool;
        int value_int;
        long value_long;
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        value = sharedPreferences.getString(getString(R.string.device_tag), notSet);
        if (!value.equals(notSet)){
            gameStatus.setDevice(value);
            changed = true;
        }
        value = sharedPreferences.getString(gameStatus.country_tag, notSet);
        if (!value.equals(notSet)){
            gameStatus.setCountry(value);
            changed = true;
        }
        value = sharedPreferences.getString(gameStatus.language_tag, notSet);
        if (!value.equals(notSet)){
            gameStatus.setLanguage(value);
            changed = true;
        }
        value_long = sharedPreferences.getLong(gameStatus.campaign_start_date_tag, gameStatus.getProfile().id_not_set);
        if (value_long != gameStatus.getProfile().id_not_set){
            gameStatus.setCampaignStartDate(new Date(value_long));
            changed = true;
        }
        value_bool = sharedPreferences.getBoolean(gameStatus.experiment_status_tag, true);
        gameStatus.setExperimentStatus(value_bool);
        value = sharedPreferences.getString(gameStatus.experiment_profile_tag, notSet);
        if (!value.equals(notSet)){
            if (value.equals(getResources().getString(R.string.experiment_profile_collaboration)))
                gameStatus.setExperimentProfile(getResources().getString(R.string.experiment_profile_collaboration));
            else
                gameStatus.setExperimentProfile(getResources().getString(R.string.experiment_profile_competition));
        }
        value_int = sharedPreferences.getInt(gameStatus.trip_counter_tag, 1);
        if (value_int > 1){
            gameStatus.getTrip().setTrip_counter(value_int);
            changed = true;
        }
        value_int = sharedPreferences.getInt(gameStatus.tags_count_tag, 0);
        if (value_int > 0){
            gameStatus.setTag_count(value_int);
            changed = true;
        }
        value = sharedPreferences.getString(gameStatus.avatar_tag, notSet);
        if (!value.equals(notSet)){
            gameStatus.getProfile().setAvatarName(value);
            changed = true;
        }
        value_int = sharedPreferences.getInt(gameStatus.avatar_id_tag, gameStatus.getProfile().id_not_set);
        if (value_int != gameStatus.getProfile().id_not_set){
            gameStatus.getProfile().setAvatarId(value_int);
            changed = true;
        }
        value = sharedPreferences.getString(gameStatus.gender_tag, notSet);
        if (!value.equals(notSet)){
            gameStatus.getProfile().setGender(value);
            changed = true;
        }
        value = sharedPreferences.getString(gameStatus.age_range_tag, notSet);
        if (!value.equals(notSet)){
            gameStatus.getProfile().setAgeRange(value);
            changed = true;
        }
        value_bool = sharedPreferences.getBoolean(gameStatus.bike_rented_tag, false);
        if (value_bool != false){
            gameStatus.getProfile().setBikeRented(value_bool);
            changed = true;
        }
        value_int = sharedPreferences.getInt(gameStatus.bike_type_tag, gameStatus.getProfile().id_not_set);
        if (value_int != gameStatus.getProfile().id_not_set){
            gameStatus.getProfile().setBikeType(value_int);
            changed = true;
        }
        value = sharedPreferences.getString(gameStatus.email_tag, notSet);
        if (!value.equals(notSet)){
            gameStatus.getProfile().setEmail(value);
            changed = true;
        }
        return changed;
    }


    public void saveTripStartDateOnSharedPreferences() {
        // may be here the trip details should be fixed
        mSharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        long start_time = mSharedPreferences.getLong(getString(R.string.trip_start_date_tag), 0);
        if (start_time == 0){
            mSPEditor = mSharedPreferences.edit();
            Date now = new Date(Calendar.getInstance().getTimeInMillis());
            gameStatus.getTrip().setStartTime(now);
            mSPEditor.putLong(this.gameStatus.trip_start_date_tag, gameStatus.getTrip().getStartTime().getTime());
            mSPEditor.commit();
        } else{
            gameStatus.getTrip().setStartTime(new Date(start_time));
        }
    }

    public long getTripStartDateOnSharedPreferences() {

        mSharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        long value_long = mSharedPreferences.getLong(getString(R.string.trip_start_date_tag), 0);
        if (value_long > 0){
            mSPEditor = mSharedPreferences.edit();
            mSPEditor.remove(this.gameStatus.trip_start_date_tag);
            mSPEditor.commit();
        }
        return value_long;
    }

    @Override
    public void updateDashboard(float speed, float distance) {
        if (dashboardFragment.isVisible()) {
            dashboardFragment.updateDashboard(speed, distance);
        }
    }

    @Override
    public void onTripsUpdated(int counter){
        gameStatus.getTrip().setTrip_counter(counter);
        if (dashboardFragment.isVisible()) {
            dashboardFragment.updateTripCounter();
        }
    }

    @Override
    public void updateScore() {

        if (scoreFragment.isVisible() &&
                gameStatus.getLeaderboard().getOwn_trips()>0 &&
                gameStatus.getLeaderboard().getTotal_trips()>0 &&
                gameStatus.getLeaderboard().getPosition_trips()>0 )
            scoreFragment.updateLeaderBoardTrips(gameStatus.getLeaderboard());

        if (scoreFragment.isVisible() &&
                gameStatus.getLeaderboard().getOwn_tags()>0 &&
                gameStatus.getLeaderboard().getTotal_tags()>0)
            scoreFragment.updateLeaderBoardTags(gameStatus.getLeaderboard());
    }

    @Override
    public void updateLeaderBoard(){
        if (leaderboardFragment.isVisible()) {
            leaderboardFragment.updateTop(gameStatus.getLeaderboard());
        }
    }

    @Override
    public void loadLeaderBoard() {
        if (leaderboardFragment.isVisible()) {
            gameStatus.getLeaderboard().updateLeaderBoard(this);
        }
    }

    @Override
    public void loadScore() {
            gameStatus.getLeaderboard().updateScore(this);

    }
}
