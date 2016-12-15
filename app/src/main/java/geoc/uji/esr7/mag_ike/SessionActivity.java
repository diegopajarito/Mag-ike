package geoc.uji.esr7.mag_ike;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import geoc.uji.esr7.mag_ike.common.logger.Log;
import geoc.uji.esr7.mag_ike.common.logger.LogView;
import geoc.uji.esr7.mag_ike.common.logger.LogWrapper;
import geoc.uji.esr7.mag_ike.common.logger.MessageOnlyLogFilter;
import geoc.uji.esr7.mag_ike.common.status.Profile;
import geoc.uji.esr7.mag_ike.common.tracker.ActivityTracker;

import geoc.uji.esr7.mag_ike.common.status.GameStatus;


public class SessionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        DashboardFragment.OnStatusChangeListener, ProfileFragment.OnProfileChangeListener, DashboardFragment.onDashboardUpdate {

    public static final String GAME_STATUS = "GameStatusFile";
    public static final String TAG = "Cyclist - BasicSensorsApi";
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


    // Wraps Android's native log framework.
    LogWrapper logWrapper;

    // Global variables to be used during app execution
    private SharedPreferences settings;
    public GameStatus gameStatus;
    private float accumulated_distance = 0;
    public Chronometer chronometer;
    private ImageView iv_avatar;


    // Fragments for being used on interface development
    private DataFragment dataFragment;
    private ProfileFragment profileFragment = new ProfileFragment();
    private DashboardFragment dashboardFragment = new DashboardFragment();
    private AboutFragment aboutFragment = new AboutFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        if (!checkPermissions_account() || !checkPermissions_fitness()) {
            requestPermissions_account();
            requestPermissions_fitness();
        }

        // Recover Stored values for campaign setup
        settings = getSharedPreferences(GAME_STATUS, 0);
        Long storedStart = settings.getLong(getString(R.string.campaign_start_date_tag), 0);
        if (storedStart == 0) {
            // Saving the first day of execution of the app in the file
            SharedPreferences.Editor editor = settings.edit();
            storedStart = new Date().getTime();
            editor.putLong(getString(R.string.campaign_start_date_tag), storedStart);
            editor.commit();
        }
        String storedEmail =  settings.getString(getString(R.string.email_tag),"");

        // Start Service for tracking Location, distance, speed, cadence

        // Set the content View
        setContentView(R.layout.activity_session);


        // Starting Global Chronometer
        chronometer = new Chronometer(getApplicationContext());
        chronometer.start();
        if (savedInstanceState != null) {
            chronometer.setBase(savedInstanceState.getLong(getString(R.string.chronometer_base_tag)));
        }

        // Create a Fragment Manager and setting it to start with the one called dashboard
        android.support.v4.app.FragmentManager supportFm;
        supportFm = getSupportFragmentManager();
        if (supportFm.findFragmentByTag(getString(R.string.dashboardFragment_label)) == null){
            dashboardFragment.setArguments(getIntent().getExtras());
            supportFm.beginTransaction()
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

            // A method for defining all this initial setup for gameStatus
            gameStatus = new GameStatus(getResources());
            gameStatus.setDevice(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            gameStatus.setLanguage(getResources().getConfiguration().locale.getDisplayLanguage());
            gameStatus.setCountry(getResources().getConfiguration().locale.getDisplayCountry());
            gameStatus.getProfile().setEmail(storedEmail);
            gameStatus.setCampaignLength(getResources().getInteger(R.integer.dashboard_campaign_length));
            gameStatus.setCampaignStartDate(new Date(storedStart));

            // add the data fragment
            dataFragment = new DataFragment();
            fm.beginTransaction().add(dataFragment,"temporalStatus").commit();
            // load the data from the web
            dataFragment.setTemporalStatus(gameStatus);
        } else {
            gameStatus = dataFragment.getTemporalStatus();
        }


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


        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        initializeLogging();


        // Setting Parse Server with username and password
        checkParseLogIn();


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Refresh Screen to prevent deal with Refresh and Screen Rotation
        updateDashboardFromStatus(gameStatus);
        //updateSideBarFromProfile();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(getString(R.string.chronometer_base_tag), chronometer.getBase());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume(){
        super.onResume();

        // This ensures that if the user deies the permissiones then uses Settings to re-enable
        // them, the app will start working.
        buildFitnessClient();

    }

    @Override
    protected void onStop() {
        super.onStop();
        // store the data in the fragment
        dataFragment.setTemporalStatus(gameStatus);



        // Set Service Stop Timer
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
            GridLayout gl = (GridLayout) findViewById(R.id.status_contribution);
            if (gl.getVisibility() == View.INVISIBLE)
                gl.setVisibility(View.VISIBLE);
            else
                gl.setVisibility(View.INVISIBLE);
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

    private void updateSideBarFromProfile(){
        TextView tv;
        RadioGroup rg;
        RadioButton rb;
        // Update Avatar name and email at Side bar
        tv = (TextView) this.findViewById(R.id.avatar_email_header);
        tv.setText(gameStatus.getProfile().getEmail());
        if (!gameStatus.getProfile().getAvatarName().equals("")) {
            tv = (TextView) this.findViewById(R.id.avatar_name_header);
            tv.setText(gameStatus.getProfile().getAvatarName());
        }
        // Update Avatar ImageView at Side bar
        if (gameStatus.getProfile().getAvatarId() != gameStatus.getProfile().id_not_set){
            iv_avatar = (ImageView) this.findViewById(R.id.avatar_icon_header);
            rg = (RadioGroup) this.findViewById(R.id.rg_avatar);
            rb = (RadioButton) rg.findViewById(rg.getCheckedRadioButtonId());
            iv_avatar.setImageDrawable(rb.getBackground());
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
            TextView tv;
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            settings = getSharedPreferences(GAME_STATUS, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(getString(R.string.email_tag), accountName);
            editor.commit();
            //Check additional parameters
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNTS);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE || requestCode == REQUEST_PERMISSIONS_EMAIL_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
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
     * It stores last values
     */

    public boolean onProfileUpdated(Profile p){

        SharedPreferences settings = getSharedPreferences(GAME_STATUS, 0);
        SharedPreferences.Editor editor = settings.edit();
        updateSideBarFromProfile();
        // Store profile in sharedPreferences
        editor.putString(getString(R.string.avatar_tag), p.getAvatarName());
        editor.putInt(getString(R.string.avatar_id_tag), p.getAvatarId());
        editor.putString(getString(R.string.email_tag), p.getEmail());
        editor.putString(getString(R.string.age_range_tag), p.getAgeRange());
        editor.putBoolean(getString(R.string.bike_rented_tag), p.isBikeRented());
        editor.putInt(getString(R.string.bike_type_tag), p.getBikeType());
        editor.commit();
        return gameStatus.updateProfile(p);
    }

    /**
     * Checks if there are some additional data saved and update the profile
     *
     */
    @Override
    public Profile getCurrentProfile(){
        SharedPreferences settings = getSharedPreferences(GAME_STATUS, 0);
        if ( !settings.getString(getString(R.string.avatar_tag), "").equals("") )
            gameStatus.getProfile().setAvatarName(settings.getString(getString(R.string.avatar_tag), ""));
        if ( settings.getInt(getString(R.string.avatar_id_tag), -1) != -1 )
            gameStatus.getProfile().setAvatarId(settings.getInt(getString(R.string.avatar_id_tag), -1));
        if ( !settings.getString(getString(R.string.email_tag), "").equals("") )
            gameStatus.getProfile().setEmail(settings.getString(getString(R.string.email_tag), ""));
        if ( !settings.getString(getString(R.string.age_range_tag), "").equals("") )
            gameStatus.getProfile().setAgeRange(settings.getString(getString(R.string.age_range_tag), ""));
        gameStatus.getProfile().setBikeRented(settings.getBoolean(getString(R.string.bike_rented_tag), false));
        if ( settings.getInt(getString(R.string.bike_type_tag), -1) != -1 )
            gameStatus.getProfile().setBikeType(settings.getInt(getString(R.string.bike_type_tag), -1));
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
