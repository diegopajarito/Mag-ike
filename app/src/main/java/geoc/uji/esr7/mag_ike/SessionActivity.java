package geoc.uji.esr7.mag_ike;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.concurrent.TimeUnit;

import geoc.uji.esr7.mag_ike.common.logger.Log;
import geoc.uji.esr7.mag_ike.common.logger.LogView;
import geoc.uji.esr7.mag_ike.common.logger.LogWrapper;
import geoc.uji.esr7.mag_ike.common.logger.MessageOnlyLogFilter;
import geoc.uji.esr7.mag_ike.common.status.Profile;

import geoc.uji.esr7.mag_ike.common.status.GameStatus;
import geoc.uji.esr7.mag_ike.common.tracker.TrackingService;

public class SessionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        DashboardFragment.OnStatusChangeListener, ProfileFragment.OnProfileChangeListener, DashboardFragment.onDashboardUpdate {


    private static final String TAG = "Cycling";
    // [START auth_variable_references]
    private GoogleApiClient mClient = null;
    // [END auth_variable_references]

    private static final int REQUEST_PERMISSIONS_EMAIL_CODE = 1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    // Added multiple listeners, one for each variable to measure
    // There are four listeners to be used,
    private OnDataPointListener locationListener;
    private OnDataPointListener speedListener;
    private OnDataPointListener distanceListener;
    private OnDataPointListener cyclingListener;
    private OnDataPointListener stepCountListener;
    // [END mListener_variable_reference]

    // The activity Service Intent
    private Intent mTrackingIntent;
    // The tracking activity ID
    private final int trackingServiceID = 1;
    // The notification builder for tracking service
    private NotificationCompat.Builder mNotificationBuilder;

    // Floating button to control the location tracking service
    FloatingActionButton btn_pause;

    // Wraps Android's native log framework.
    LogWrapper logWrapper;

    // sets zero as initial value for distance
    float accumulated_distance = 0;
    // the chronometer used for the dashboard
    Chronometer chronometer;

    // A status object used for storing game status through data fragments
    public GameStatus gameStatus;

    // Fragments for being used on interface development
    private DataFragment dataFragment;
    private ProfileFragment profileFragment = new ProfileFragment();
    private DashboardFragment dashboardFragment = new DashboardFragment();
    private AboutFragment aboutFragment = new AboutFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** dealing with interface set
         * Set the layout
         * check the fragments that are active when menu is selected
         * setting the toolbar
         * setting the side drawer layout
         * setting the avatar and personal details on the side drawer
         * setting the floating button for control the location trackin service
         */
        setContentView(R.layout.activity_session);

        android.support.v4.app.FragmentManager mSupportFM = getSupportFragmentManager();

        // Get existing fragment for Dashboard
        if (mSupportFM.findFragmentByTag(getString(R.string.dashboardFragment_label)) == null){
            dashboardFragment.setArguments(getIntent().getExtras());
            mSupportFM.beginTransaction()
                    .add(R.id.fragment_container, dashboardFragment, getString(R.string.dashboardFragment_label) ).commit();
        } else {
            dashboardFragment = (DashboardFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.dashboardFragment_label));
        }

        // Handling previous Status when app resumes using data fragments
        FragmentManager fm = getFragmentManager();
        dataFragment = (DataFragment) fm.findFragmentByTag("temporalStatus");
        // create the fragment and data the first time
        if (dataFragment == null) {
            // Setting Game Status
            gameStatus = new GameStatus(getResources());
            gameStatus.setDevice(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            gameStatus.setLanguage(getResources().getConfiguration().locale.getDisplayLanguage());
            gameStatus.setCountry(getResources().getConfiguration().locale.getDisplayCountry());
            getUserData();
            //gameStatus.getProfile().setEmail(getUserEmail());
            // add the fragment
            dataFragment = new DataFragment();
            fm.beginTransaction().add(dataFragment,"temporalStatus").commit();
            // load the data from the web
            dataFragment.setTemporalStatus(gameStatus);
        } else {
            gameStatus = dataFragment.getTemporalStatus();
        }

        // Starting Global Chronometer
        chronometer = new Chronometer(getApplicationContext());
        chronometer.start();

        // Starting and Setting the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setting the drawer layout, a toggle for open/close and a listener for selected items
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();


        // Setting the navigation view with avatar and personal identification
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Adding a floating button and its listener

        btn_pause = (FloatingActionButton) findViewById(R.id.btn_pause);
        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call the Service toggle
                boolean serviceStatus = toggleTrackingService();
                if (serviceStatus == true) {
                    Toast.makeText(view.getContext(), "Service Started", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(view.getContext(), "Service Stopped", Toast.LENGTH_SHORT).show();
                }

            }
        });


        /**
         * Setting options and services needed for the app
         * on-screen Logger - to be deleted
         * permissions checked for accessing the account data and fitness api
         * Android service for starting the location track
         * Logging into the Parse server for managing data storage
         */

        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        initializeLogging(); // To be deleted

        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        if (!checkPermissions_account() || !checkPermissions_fitness()) {
            requestPermissions_account();
            requestPermissions_fitness();
        }

        // Launch the toggle for the location tracking service for starting it
        toggleTrackingService();

        // Setting Parse Server with username and password
        checkParseLogIn();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // store the data in the fragment
        dataFragment.setTemporalStatus(gameStatus);

    }

    @Override
    protected void onResume(){
        super.onResume();

        // This ensures that if the user deies the permissiones then uses Settings to re-enable
        // them, the app will start working.
        buildFitnessClient();
        //Set Screen based on Status
        updateDashboardFromStatus(gameStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            if (mClient.isConnected())
                Toast.makeText(getApplicationContext(), R.string.action_fitness_text, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), R.string.action_fitness_text_error, Toast.LENGTH_SHORT).show();
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
                transaction.addToBackStack(null);
            }
        } else if ( (item.getItemId() == R.id.nav_share) ) {
            Toast.makeText(getApplicationContext(), "Share", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.nav_profile) {
            if (getSupportFragmentManager().findFragmentByTag(getString(R.string.profileFragment_label)) == null){
                transaction.add(profileFragment, getString(R.string.profileFragment_label));
            }
            if (!profileFragment.isVisible()){
                transaction.replace(R.id.fragment_container, profileFragment);
                transaction.addToBackStack(null);
            }
        } else if (item.getItemId() == R.id.nav_about) {
            if (getSupportFragmentManager().findFragmentByTag(getString(R.string.aboutFragment_label)) == null){
                transaction.add(aboutFragment,getString(R.string.aboutFragment_label));
            }
            if (!aboutFragment.isVisible()){
                transaction.replace(R.id.fragment_container, aboutFragment);
                transaction.addToBackStack(null);
            }
        }
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
                    Log.d("Parse", "Connected to Parse - Mag-ike.");
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    Log.d("Parse", "Connection to Parse failed - Mag-ike - " + e.toString());
                }
            }
        });
    }


    /**
     * Toggle method for control the Location tracking service
     * - starts/finish of the service
     * - sets icon of the floating button
     * - sets the notification while the service is running
     */

    private boolean toggleTrackingService(){
        boolean serviceStatus;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mTrackingIntent == null){
            mTrackingIntent = new Intent(this, TrackingService.class);
            this.startService(mTrackingIntent);
            mNotificationBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_bike_ride)
                            .setContentTitle(getString(R.string.notification_title))
                            .setContentText(getString(R.string.notification_text));
            // The stack builder object will contain an artificial back stack for the started Activity.
            // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(SessionActivity.class);
            // Adds the Intent of the current activity that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(getIntent());
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT );
            mNotificationBuilder.setContentIntent(resultPendingIntent);
            // trackingServiceID allows you to update the notification later on.
            mNotificationManager.notify(trackingServiceID, mNotificationBuilder.build());
            btn_pause.setImageResource(R.drawable.ic_button_pause);
            serviceStatus = true;
        } else {
            this.stopService(mTrackingIntent);
            mTrackingIntent = null;
            mNotificationManager.cancel(trackingServiceID);
            btn_pause.setImageResource(R.drawable.ic_button_play);
            serviceStatus = false;
        }
        Log.d("Toggle", String.valueOf(serviceStatus));
        return serviceStatus;
    }



    // [START auth_build_googleapiclient_beginning]
    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or having
     *  multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {

        if (mClient == null && checkPermissions_fitness()) {
            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.SENSORS_API)
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected to Google Fit!!!");


                                    // Now you can make calls to the Fitness APIs.
                                    findFitnessDataSources();
                                    //((MenuItem)((ActionMenuView)toolbar.getChildAt(1)).getChildAt(0)).setIcon(R.drawable.ic_cycling_enabled);
                                    //MenuItem mi = (MenuItem) toolbar.getChildAt(1);
                                    //mi.setIcon(R.drawable.ic_cycling_enabled);
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(TAG, "Connection to Google Fit lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(TAG,
                                                "Connection to Google Fit lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.i(TAG, "Google Play services connection failed. Cause: " +
                                    result.toString());
                            Snackbar.make(
                                    SessionActivity.this.findViewById(R.id.start_activity_view),
                                    "Exception while connecting to Google Play services: " +
                                            result.getErrorMessage(),
                                    Snackbar.LENGTH_INDEFINITE).show();
                        }
                    })
                    .build();
        }
    }
    // [END auth_build_googleapiclient_beginning]


    /**
     * Find available data sources and attempt to register on a specific {@link DataType}.
     * If the application cares about a data type but doesn't care about the source of the data,
     * this can be skipped entirely, instead calling
     *     {@link com.google.android.gms.fitness.SensorsApi
     *     #register(GoogleApiClient, SensorRequest, DataSourceListener)},
     * where the {@link SensorRequest} contains the desired data type.
     */
    private void findFitnessDataSources() {
        // [START find_data_sources]
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                //.setDataTypes(DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_CYCLING_PEDALING_CADENCE,DataType.TYPE_DISTANCE_CUMULATIVE) DataType.TYPE_LOCATION_TRACK, DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_WORKOUT_EXERCISE, DataType.AGGREGATE_STEP_COUNT_DELTA,
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE,
                        //DataType.TYPE_STEP_COUNT_CUMULATIVE,
                        DataType.TYPE_CYCLING_PEDALING_CADENCE,
                        DataType.TYPE_DISTANCE_CUMULATIVE, DataType.AGGREGATE_DISTANCE_DELTA,
                        DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY )
                // Can specify whether data type is raw or derived.
                .setDataSourceTypes(DataSource.TYPE_RAW, DataSource.TYPE_DERIVED)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            //every type of data will register a listener
                                // Listener for Location Data
                            if (dataSource.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE)) {
                                registerFitnessDataListener(dataSource, DataType.TYPE_LOCATION_SAMPLE);
                                //  Listener for Cycling Data
                            } else if (dataSource.getDataType().equals(DataType.TYPE_CYCLING_PEDALING_CADENCE)) {
                                registerFitnessDataListener(dataSource, DataType.TYPE_CYCLING_PEDALING_CADENCE);
                                // Listener for Speed Data
                            } else if (dataSource.getDataType().equals(DataType.TYPE_SPEED)) {
                                registerFitnessDataListener(dataSource, DataType.TYPE_SPEED);
                            } else if (dataSource.getDataType().equals(DataType.AGGREGATE_SPEED_SUMMARY)) {
                                registerFitnessDataListener(dataSource, DataType.AGGREGATE_SPEED_SUMMARY);
                                // Listener for Step Count
                            /*} else if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)) {
                                registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);*/
                                // Listener for Distance Data
                            } else if (dataSource.getDataType().equals(DataType.AGGREGATE_DISTANCE_DELTA)) {
                                registerFitnessDataListener(dataSource, DataType.AGGREGATE_DISTANCE_DELTA);
                            } else if (dataSource.getDataType().equals(DataType.TYPE_DISTANCE_CUMULATIVE)) {
                                registerFitnessDataListener(dataSource, DataType.TYPE_DISTANCE_CUMULATIVE);
                            }
                        }
                    }
                });
    }

    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
    private void registerFitnessDataListener(DataSource dataSource, final DataType dataType) {
        // [START register_data_listener]
        if (dataType == DataType.TYPE_LOCATION_SAMPLE){
            locationListener = new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    // Location variables no-data vales
                    float lat, lon, pres, alt;
                    lat = dataPoint.getValue(Field.FIELD_LATITUDE).asFloat();
                    lon = dataPoint.getValue(Field.FIELD_LONGITUDE).asFloat();
                    pres = dataPoint.getValue(Field.FIELD_ACCURACY).asFloat();
                    alt = dataPoint.getValue(Field.FIELD_ALTITUDE).asFloat();
                    // Store Data into server and update interface with new values
                    gameStatus.saveStatus_Eventually(lat,lon,alt,pres);
                    updateDashboardFromStatus(gameStatus);
                }
            };
            // Register listener with the sensor API
            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(1, TimeUnit.SECONDS)
                            .setAccuracyMode(SensorRequest.ACCURACY_MODE_HIGH)
                            .build(),
                    locationListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Location Listener registered!");
                            } else {
                                Log.i(TAG, "Location Listener not registered.");
                            }
                        }
                    });
        } else if (dataType == DataType.TYPE_SPEED || dataType == DataType.AGGREGATE_SPEED_SUMMARY){
            speedListener = new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    // Speed variables no-data vales
                    float speed = dataPoint.getValue(Field.FIELD_SPEED).asFloat();
                    String name = Field.FIELD_SPEED.getName();
                    // Store Data into server and update interface with new values
                    gameStatus.saveStatus_Eventually(name,speed);
                    updateDashboardFromStatus(gameStatus);
                }
            };
            // Register listener with the sensor API
            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(1, TimeUnit.SECONDS)
                            .setAccuracyMode(SensorRequest.ACCURACY_MODE_HIGH)
                            .build(),
                    speedListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Speed Listener registered!");
                            } else {
                                Log.i(TAG, "Speed Listener not registered.");
                            }
                        }
                    });
        } //else if (dataType == DataType.AGGREGATE_DISTANCE_DELTA || dataType == DataType.TYPE_DISTANCE_CUMULATIVE){
        else if (dataType == DataType.AGGREGATE_DISTANCE_DELTA ){
            distanceListener= new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    // Distance variables no-data vales
                    float distance = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat();
                    if (distance >= 0) {
                        accumulated_distance += distance;
                        // Store Data into server and update interface with new values
                        gameStatus.saveStatus_Eventually(distance, accumulated_distance);
                        updateDashboardFromStatus(gameStatus);
                    }
                }
            };
            // Register listener with the sensor API
            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(1, TimeUnit.MINUTES)
                            .setAccuracyMode(SensorRequest.ACCURACY_MODE_HIGH)
                            .build(),
                    distanceListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Distance Listener registered!");
                            } else {
                                Log.i(TAG, "Distance Listener not registered.");
                            }
                        }
                    });
        } else if (dataType == DataType.TYPE_CYCLING_PEDALING_CADENCE ){
            cyclingListener= new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    // Cadence variables no-data vales
                    float cadence = dataPoint.getValue(Field.FIELD_RPM).asFloat();
                    String name = Field.FIELD_RPM.getName();
                    // Store Data into server and update interface with new values
                    gameStatus.saveStatus_Eventually(name,cadence);
                    updateDashboardFromStatus(gameStatus);
                }
            };
            // Register listener with the sensor API
            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(1, TimeUnit.SECONDS)
                            .build(),
                    cyclingListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Cycling Listener registered!");
                            } else {
                                Log.i(TAG, "Cycling Listener not registered.");
                            }
                        }
                    });
        } /*else if (dataType == DataType.TYPE_STEP_COUNT_CUMULATIVE ){
            stepCountListener= new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    // Cadence variables no-data vales
                    int steps = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                    String name =Field.FIELD_STEPS.getName();
                    // Store Data into server and update interface with new values
                    gameStatus.saveStatus_Eventually(name,(float) steps);
                    updateDashboardFromStatus(gameStatus);
                }
            };
            // Register listener with the sensor API
            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(1, TimeUnit.MINUTES)
                            .build(),
                    stepCountListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Step Count Listener registered!");
                            } else {
                                Log.i(TAG, "Step Count Listener not registered.");
                            }
                        }
                    });
        }*/

    }


    /**
     * Unregister the listener with the Sensors API.
     */
    private void unregisterFitnessDataListener(final DataType dataType) {

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        if (dataType == DataType.TYPE_LOCATION_SAMPLE && locationListener != null){
            Fitness.SensorsApi.remove(mClient, locationListener).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, "Location Listener was removed!");
                    } else {
                        Log.i(TAG, "Location Listener was not removed.");
                    }
                }
            });
        } else if ((dataType == DataType.TYPE_SPEED || dataType == DataType.AGGREGATE_SPEED_SUMMARY) && speedListener != null){
            Fitness.SensorsApi.remove(mClient, speedListener).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, "Speed Listener was removed!");
                    } else {
                        Log.i(TAG, "Speed Listener was not removed.");
                    }
                }
            });
        } else if (dataType == DataType.AGGREGATE_DISTANCE_DELTA && distanceListener != null){
            Fitness.SensorsApi.remove(mClient, distanceListener).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, "Distance Listener was removed!");
                    } else {
                        Log.i(TAG, "Distance Listener was not removed.");
                    }
                }
            });
        } else if (dataType == DataType.TYPE_CYCLING_PEDALING_CADENCE){
            Fitness.SensorsApi.remove(mClient, cyclingListener).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, "Cycling Listener was removed!");
                    } else {
                        Log.i(TAG, "Cycling Listener was not removed.");
                    }
                }
            });
        }

        // [END unregister_data_listener]
    }


    /**
     * Initialize a custom log class tha outputs both toREQUEST_PERMISSIONS_EMAIL_CODE in-app targets and logcat
     */
    private void initializeLogging(){
        if (logWrapper == null) {
            // Wraps Android's native log framework.
            logWrapper = new LogWrapper();
            //Using Log, front-end to the logging chain, emulates adroid.util.log method signatures
            Log.setLogNode(logWrapper);
            // Filter strips out everything except the message text.
            MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
            logWrapper.setNext(msgFilter);
            // On screen logging via customized TextView.
            LogView logView = (LogView) findViewById(R.id.sample_logview);

            // Fixing this lint errors adds logic without benefit.
            // noinspection AndroidLintDeprecation
            logView.setTextAppearance(this, R.style.Log);
            logView.setMovementMethod(new ScrollingMovementMethod());

            logView.setBackgroundColor(Color.TRANSPARENT);
            msgFilter.setNext(logView);
            Log.i(TAG, "Ready");
        } else {
            Log.i(TAG, "Already started");
        }
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


    private void requestPermissions_fitness() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
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
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting fitness permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(SessionActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
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
    private void getUserData(){
        try {
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false, null, null, null, null);
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
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNTS);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(getString(R.string.tag_log), "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE || requestCode == REQUEST_PERMISSIONS_EMAIL_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(getString(R.string.tag_log), "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                buildFitnessClient();
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
     * Implementing interfaces for Profile Fragment
     */

    public boolean onProfileUpdated(Profile p){
        TextView tv;

        if (p.getAvatarName() != p.nameDefault) {
            tv = (TextView) this.findViewById(R.id.avatar_name_header);
            tv.setText(p.getAvatarName());
        }
        if (p.getEmail() != "") {
            tv = (TextView) this.findViewById(R.id.avatar_email_header);
            tv.setText(p.getEmail());
        }
        if (p.getAvatarId() != p.id_not_set){

        }

        return gameStatus.updateProfile(p);
    }

    @Override
    public Profile getCurrentProfile(){
        return gameStatus.getProfile();
    }


    @Override
    public void updateDashboardFromStatus(GameStatus s) {
        dashboardFragment.updateDashboardFromStatus(s);
    }


    @Override
    public long getChronometerBase() {
        return this.chronometer.getBase();
    }


}
