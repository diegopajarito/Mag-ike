package geoc.uji.esr7.mag_ike;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.server.converter.StringToIntConverter;
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
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import geoc.uji.esr7.mag_ike.common.logger.Log;
import geoc.uji.esr7.mag_ike.common.logger.LogView;
import geoc.uji.esr7.mag_ike.common.logger.LogWrapper;
import geoc.uji.esr7.mag_ike.common.logger.MessageOnlyLogFilter;
import geoc.uji.esr7.mag_ike.common.tracker.ActivityTracker;
import geoc.uji.esr7.mag_ike.common.tracker.TrackSpeed;

public class StartActivity extends AppCompatActivity {

    public static final String TAG = "BasicSensorsApi";
    // [START auth_variable_references]
    private GoogleApiClient mClient = null;
    // [END auth_variable_references]

    private TextView tv;


    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    // Added multiple listeners, one for each variable to measure
    private OnDataPointListener locationListener;
    private OnDataPointListener speedListener;
    private OnDataPointListener distanceListener;
    // [END mListener_variable_reference]

    // The activity Tracker
    private ActivityTracker actTracker = new ActivityTracker();

    // A counter for collected points
    int counter_points = 0;
    float accumulated_distance = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Put application specific code here.


        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });


        tv = (TextView) findViewById(R.id.txv_ShowSpeed);

        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        initializeLogging();

        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        if (!checkPermissions()) {
            requestPermissions();
        }



        // Parse setup

        ParseUser.logInInBackground("test@test.com", "test", new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // Hooray! The user is logged in.
                    Log.d("Hooray", "Hooray! The user is logged - Mag-ike.");
                } else {
                    // Signup failed. Look at the ParseException to see what happened.
                    Log.d("Signup failed", "Signup failed - Mag-ike - " + e.toString());
                }
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();

        // This ensures that if the user deies the permissiones then uses Settings to re-enable
        // them, the app will start working.
        buildFitnessClient();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (mClient == null && checkPermissions()) {
            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.SENSORS_API)
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    findFitnessDataSources();
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(TAG,
                                                "Connection lost.  Reason: Service Disconnected");
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
                                    StartActivity.this.findViewById(R.id.start_activity_view),
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
                //.setDataTypes(DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_CYCLING_PEDALING_CADENCE,DataType.TYPE_DISTANCE_CUMULATIVE)
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE, DataType.AGGREGATE_STEP_COUNT_DELTA,
                        DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_CYCLING_PEDALING_CADENCE,
                        DataType.TYPE_DISTANCE_CUMULATIVE, DataType.AGGREGATE_SPEED_SUMMARY,
                        DataType.AGGREGATE_DISTANCE_DELTA, DataType.TYPE_LOCATION_TRACK,
                        DataType.TYPE_SPEED, DataType.TYPE_WORKOUT_EXERCISE)
                // Can specify whether data type is raw or derived.
                .setDataSourceTypes(DataSource.TYPE_RAW, DataSource.TYPE_DERIVED)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        Log.i(TAG, "Result: " + dataSourcesResult.getStatus().toString());
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            //Log.i(TAG, "Data source found: " + dataSource.toString());
                            Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());


                            //every type of data will register a listener
                                // Listener for Location Data
                            if (dataSource.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE)) {
                                    //&& mListener == null) {
                                Log.i(TAG, "Data source for LOCATION_SAMPLE found!  Registering.");
                                registerFitnessDataListener(dataSource,
                                        DataType.TYPE_LOCATION_SAMPLE);
                            /*} else if (dataSource.getDataType().equals(DataType.TYPE_CYCLING_PEDALING_CADENCE)) {
                                Log.i(TAG, "Data source for CYCLING_PEDALING found!  Registering.");
                                registerFitnessDataListener(dataSource,
                                        DataType.TYPE_CYCLING_PEDALING_CADENCE);*/
                                // Listener for Speed Data
                            } else if (dataSource.getDataType().equals(DataType.TYPE_SPEED)) {
                                Log.i(TAG, "Data source for TYPE_SPEED found!  Registering.");
                                registerFitnessDataListener(dataSource,
                                        DataType.TYPE_SPEED);
                            } else if (dataSource.getDataType().equals(DataType.AGGREGATE_SPEED_SUMMARY)) {
                                Log.i(TAG, "Data source for AGGREGATE_SPEED_SUMMARY found!  Registering.");
                                registerFitnessDataListener(dataSource,
                                        DataType.AGGREGATE_SPEED_SUMMARY);
                            /*} else if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                                Log.i(TAG, "Data source for STEP_COUNT found!  Registering.");
                                registerFitnessDataListener(dataSource,
                                        DataType.TYPE_STEP_COUNT_DELTA);*/
                                // Listener for Distance Data
                            } else if (dataSource.getDataType().equals(DataType.AGGREGATE_DISTANCE_DELTA)) {
                                Log.i(TAG, "Data source for AGGREGATE_DISTANCE_DELTA found!  Registering.");
                                registerFitnessDataListener(dataSource,
                                        DataType.AGGREGATE_DISTANCE_DELTA);
                            }
                        }
                    }
                });
        // [END find_data_sources]
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
                    float lat=-999;
                    float lon=-999;
                    float pres=-999;
                    float alt=-999;
                    // Get Location Variables and change no-data values
                    for (Field field : dataPoint.getDataType().getFields()){
                        Value val = dataPoint.getValue(field);
                        String name = field.getName();
                        if (name.equals("latitude") && val.isSet()){
                            lat = Float.parseFloat(val.toString());
                        } else if (name.equals("longitude") && val.isSet()){
                            lon = Float.parseFloat(val.toString());
                        } else if (name.equals("accuracy") && val.isSet()){
                            pres = Float.parseFloat(val.toString());
                        } else if (name.equals("altitude") && val.isSet()){
                            alt = Float.parseFloat(val.toString());
                        }
                    }
                    // Store Data into server and update interface with new values
                    parseStoreGPSPoint(lat, lon, pres, alt);
                    updateCoordinatesOnScreen(lat, lon, alt);
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
                    float speed = -999;
                    for (Field field : dataPoint.getDataType().getFields()) {
                        Value val = dataPoint.getValue(field);
                        String name = field.getName();
                        if (name.equals("speed") && val.isSet()) {
                            speed = Float.parseFloat(val.toString());
                        }
                    }
                    // Store Data into server and update interface with new values
                    //parseStoreSpeedPoint(lat, lon, pres, alt);
                    updateSpeedOnScreen(speed);
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
        } else if (dataType == DataType.AGGREGATE_DISTANCE_DELTA ){
            distanceListener= new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    // Distance variables no-data vales
                    float distance = -999;
                    for (Field field : dataPoint.getDataType().getFields()) {
                        Value val = dataPoint.getValue(field);
                        String name = field.getName();
                        if (name.equals("distance") && val.isSet()) {
                            distance = Float.parseFloat(val.toString());
                        }
                    }
                    // Store Data into server and update interface with new values
                    parseStoreSpeedPoint(distance);
                    updateDistanceOnScreen(distance);
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
        }

    }


    /**
     *
     * To consider as an intial version of listener
     */
    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
    /*private void registerSpeedDataListener(DataSource dataSource, final DataType dataType) {
        // [START register_data_listener]
        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                float lat=-999;
                float lon=-999;
                float pres=-999;
                float alt=-999;
                float speed = -1;

                if (dataType == DataType.TYPE_LOCATION_SAMPLE){
                    Log.i(TAG, "Location data detected");
                    for (Field field : dataPoint.getDataType().getFields()){
                        Value val = dataPoint.getValue(field);
                        String name = field.getName();
                        if (name.equals("latitude") && val.isSet()){
                            lat = Float.parseFloat(val.toString());
                        } else if (name.equals("longitude") && val.isSet()){
                            lon = Float.parseFloat(val.toString());
                        } else if (name.equals("accuracy") && val.isSet()){
                            pres = Float.parseFloat(val.toString());
                        } else if (name.equals("altitude") && val.isSet()){
                            alt = Float.parseFloat(val.toString());
                        }
                    }
                    parseStoreGPSPoint(lat, lon, pres, alt);
                    updateCoordinatesOnScreen(lat, lon, alt);
                } else if (dataType == DataType.TYPE_SPEED || dataType == DataType.AGGREGATE_SPEED_SUMMARY){
                    Log.i(TAG, "Speed data detected");
                    for (Field field: dataPoint.getDataType().getFields()) {
                        Value val = dataPoint.getValue(field);
                        String name = field.getName();
                        if (name.equals("speed") && val.isSet()){
                            speed = Float.parseFloat(val.toString());
                        }
                    }
                    updateSpeedOnScreen(speed);
                }


            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener registered!");
                        } else {
                            Log.i(TAG, "Listener not registered.");
                        }
                    }
                });
        // [END register_data_listener]
    } */








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
        }

        // [END unregister_data_listener]
    }


    /**
     * Initialize a custom log class tha outputs both to in-app targets and logcat
     */
    private void initializeLogging(){
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        //Using Log, front-end to the logging chain, emulates adroid.util.log method signatures
        Log.setLogNode(logWrapper);
        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter= new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
        // On screen logging via customized TextView.
        LogView logView = (LogView) findViewById(R.id.sample_logview);

        // Fixing this lint errors adds logic without benefit.
        // noinspection AndroidLintDeprecation
        logView.setTextAppearance(this, R.style.Log);
        logView.setMovementMethod(new ScrollingMovementMethod());

        logView.setBackgroundColor(Color.WHITE);
        msgFilter.setNext(logView);
        Log.i(TAG, "Ready");

    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.start_activity_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(StartActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(StartActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
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
                        findViewById(R.id.start_activity_view),
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
    // Methods related to Parse Options
    **/

    // Store GPS point
    public void parseStoreGPSPoint(float lat ,float lon, float alt, float pres){
        //log GPS track to Parse
        ParseObject o = new ParseObject("Magike_GPS");
        o.put("lat", lat);
        o.put("lon", lon);
        o.put("altitude", alt);
        o.put("precision", pres);
        o.put("time", new Date());
        o.put("device", Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
        o.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {

                    Log.d("PARSE - SAVE OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE FAILED", String.valueOf(e));
                }
            }
        });
    }

    // Store speed
    public void parseStoreSpeedPoint(float speed){
        ParseObject o = new ParseObject("Magike_Speed");
        o.put("speed", speed);
        o.put("time", new Date());
        o.put("device", Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
        o.saveEventually(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("PARSE - SAVE OK", String.valueOf(e));
                } else {
                    Log.d("PARSE - SAVE FAILED", String.valueOf(e));
                }
            }
        });
    }


    /**
     * Methods related to Interface Update
     */

    // Update coordinates on Screen
    public void updateCoordinatesOnScreen(float lat, float lon, float alt){

        View content_view = findViewById(R.id.start_activity_view);
        TextView tv_value_latitude = (TextView) content_view.findViewById(R.id.value_latitude);
        tv_value_latitude.setText(String.valueOf(lat));
        TextView tv_value_longitude = (TextView) content_view.findViewById(R.id.value_longitude);
        tv_value_longitude.setText(String.valueOf(lon));
        TextView tv_value_altitude = (TextView) content_view.findViewById(R.id.value_altitude);
        tv_value_altitude.setText(String.valueOf(alt));

        updateCollectedPoints();
    }

    // Update speed on Screen
    public void updateSpeedOnScreen(float speed){
        TextView tv;
        tv = (TextView) findViewById(R.id.value_speed);
        tv.setText(String.valueOf(speed));
        updateCollectedPoints();
    }

    // Update distance on Screen
    public void updateDistanceOnScreen(float distance){
        accumulated_distance += distance;
        TextView tv;
        tv = (TextView) findViewById(R.id.value_distance);
        tv.setText(String.valueOf(accumulated_distance));
        updateCollectedPoints();
    }

    public void updateCollectedPoints(){
        TextView tv_contribution = (TextView) findViewById(R.id.value_contribution);
        tv_contribution.setText(String.valueOf(counter_points++));
    }
}
