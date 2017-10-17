package geoc.uji.esr7.mag_ike.common.tracker;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

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

import java.util.concurrent.TimeUnit;

import geoc.uji.esr7.mag_ike.R;
import geoc.uji.esr7.mag_ike.SessionActivity;
import geoc.uji.esr7.mag_ike.common.logger.Log;
import geoc.uji.esr7.mag_ike.common.logger.LogRecord;
import geoc.uji.esr7.mag_ike.common.status.LocationRecord;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Location Track class - this class will start the location tracking action.
 */
public class TrackingService extends IntentService {

    //Log on server
    LogRecord logRecord;


    // Google Fit client
    private GoogleApiClient mClient;
    private boolean mTryingToConnect = false;
    private static final int TRACKING_NOTIFICATION_ID = 23;
    private LocalBroadcastManager broadcaster;


    public static final String LOCATION_UPDATE_INTENT = "locationUpdateIntent";
    public static final String FIT_NOTIFY_INTENT = "fitStatusUpdateIntent";
    public static final String FIT_EXTRA_CONNECTION_MESSAGE = "fitFirstConnection";
    public static final String FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE = "fitExtraFailedStatusCode";
    public static final String FIT_EXTRA_NOTIFY_FAILED_INTENT = "fitExtraFailedIntent";
    public static final String CYCLIST_TRIP_LOCATION_INTENT = "cyclistTripLocationIntent";
    public static String START_POINT_TAG = "start";
    public static String END_POINT_TAG = "end";


    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    // Added multiple listeners, one for each variable to measure
    // There are four listeners to be used,
    private OnDataPointListener locationListener;
    private OnDataPointListener speedListener;
    private OnDataPointListener distanceListener;
    // [END mListener_variable_reference]


    // Game status used for storing data into the database
    // Should be redefined after
    LocationRecord locationRecord;

    // sets zero as initial value for distance
    float accumulated_distance = 0;

    // sets true for identifying the starting point of the trip
    boolean startPoint = true;





    public TrackingService() {
        super("TrackingService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        logRecord = LogRecord.getInstance();
        String device = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        logRecord.setUpLogRecord(getResources(), device);
        locationRecord = new LocationRecord(getResources());
        locationRecord.setDevice(device);
        //Setting the broadcaster for updating user interface
        broadcaster = LocalBroadcastManager.getInstance(this);

        buildFitnessClient();

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

        if (!mClient.isConnected() && !mTryingToConnect) {
            connectGoogleClient();
        }

        // Setup for foreground Service
        Intent notificationIntent = new Intent(this, SessionActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_bike_ride)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(TRACKING_NOTIFICATION_ID, notification);

        return START_STICKY;

    }

    @Override
    protected void onHandleIntent(Intent intent) {


        //block until google fit connects.  Give up after 10 seconds.
        if (!mClient.isConnected()) {
            connectGoogleClient();
        }
        if (mClient.isConnected()) {
            logRecord.writeLog_Eventually("Connected to Fit Client");
        } else {
            //Not connected
            stopSelf();
            logRecord.writeLog_Eventually("Fit wasn't able to connect, so the request failed.");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        final String action = "Tracking Service Stopping";
        Log.i(getString(R.string.tag_log), action);
        findFitnessDataSourcesUnregister();
        notifyTripLocation(END_POINT_TAG);
        stopForeground(true);

    }


    private void connectGoogleClient() {
        mTryingToConnect = true;
        mClient.connect();
        int n = 1;
        //Wait until the service either connects or fails to connect
        while (mTryingToConnect && n < 5) {
            //while (mClient.isConnected()) {
            try {
                mClient.connect();
                Log.i(getString(R.string.tag_log), "Trying to connect the Fit Client .... " + n++);
                Thread.sleep(500, 0);
            } catch (InterruptedException e) {
                logRecord.writeLog_Eventually("Error while trying to connect to Fic Client  after " + String.valueOf(n) + " attempts - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }



    // [START auth_build_googleapiclient_beginning]

    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {

        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(getString(R.string.tag_log), "Connected to Google Fit!!!");
                                logRecord.writeLog_Eventually("Fitness Client Connected");
                                mTryingToConnect = false;
                                // Now you can make calls to the Fitness APIs.
                                findFitnessDataSources();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(getString(R.string.tag_log), "Connection to Google Fit lost.  Cause: Network Lost.");
                                    logRecord.writeLog_Eventually("Connection to Google Fit lost.  Cause: Network Lost.");
                                } else if (i
                                        == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(getString(R.string.tag_log), "Connection to Google Fit lost.  Reason: Service Disconnected");
                                    logRecord.writeLog_Eventually("Connection to Google Fit lost.  Reason: Service Disconnected");
                                }
                            }

                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {

                                Log.d(getString(R.string.tag_log), "onConnectionFailedListener error :" + result.getErrorMessage());
                                logRecord.writeLog_Eventually("onConnectionFailedListener error :" + result.getErrorMessage());
                                mTryingToConnect = false;
                                notifyUiFailedConnection(result);

                            }
                        })
                .build();
        Log.i(getString(R.string.tag_log), "Fitness Client built");

    }
    // [END auth_build_google api client_beginning]


    /**
     * Find available data sources and attempt to register on a specific {@link DataType}.
     * If the application cares about a data type but doesn't care about the source of the data,
     * this can be skipped entirely, instead calling
     * {@link com.google.android.gms.fitness.SensorsApi
     * #register(GoogleApiClient, SensorRequest, DataSourceListener)},
     * where the {@link SensorRequest} contains the desired data type.
     */
    private void findFitnessDataSources() {
        // [START find_data_sources]
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                //.setDataTypes(DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_CYCLING_PEDALING_CADENCE,DataType.TYPE_DISTANCE_CUMULATIVE) DataType.TYPE_LOCATION_TRACK, DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_WORKOUT_EXERCISE, DataType.AGGREGATE_STEP_COUNT_DELTA,
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE, DataType.TYPE_LOCATION_TRACK,
                        //DataType.TYPE_STEP_COUNT_CUMULATIVE,
                        DataType.TYPE_CYCLING_PEDALING_CADENCE, DataType.TYPE_DISTANCE_DELTA,
                        DataType.TYPE_DISTANCE_CUMULATIVE, DataType.AGGREGATE_DISTANCE_DELTA,
                        DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
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
                            } else if (dataSource.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
                                registerFitnessDataListener(dataSource, DataType.TYPE_DISTANCE_DELTA);
                            } else if (dataSource.getDataType().equals(DataType.TYPE_DISTANCE_CUMULATIVE)) {
                                registerFitnessDataListener(dataSource, DataType.TYPE_DISTANCE_CUMULATIVE);
                            }
                        }
                    }
                });
    }

    /**
     * Find available data sources and attempt to unregister all of them for each {@link DataType}.
     * {@link com.google.android.gms.fitness.SensorsApi
     * #unregister(GoogleApiClient, SensorRequest, DataSourceListener)},
     * where the {@link SensorRequest} contains the desired data type.
     */
    private void findFitnessDataSourcesUnregister() {
        // [START find_data_sources]
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                //.setDataTypes(DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_CYCLING_PEDALING_CADENCE,DataType.TYPE_DISTANCE_CUMULATIVE) DataType.TYPE_LOCATION_TRACK, DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_WORKOUT_EXERCISE, DataType.AGGREGATE_STEP_COUNT_DELTA,
                .setDataTypes(DataType.TYPE_LOCATION_SAMPLE,
                        //DataType.TYPE_STEP_COUNT_CUMULATIVE,
                        DataType.TYPE_CYCLING_PEDALING_CADENCE, DataType.TYPE_DISTANCE_DELTA,
                        DataType.TYPE_DISTANCE_CUMULATIVE, DataType.AGGREGATE_DISTANCE_DELTA,
                        DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
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
                                Fitness.SensorsApi.remove(mClient, locationListener).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (status.isSuccess()) {
                                            Log.i(getString(R.string.tag_log), "Location Listener was removed!");
                                        } else {
                                            Log.i(getString(R.string.tag_log), "Location Listener was not removed.");
                                        }
                                    }
                                });
                            } else if (dataSource.getDataType().equals(DataType.TYPE_SPEED)) {
                                Fitness.SensorsApi.remove(mClient, speedListener).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (status.isSuccess()) {
                                            Log.i(getString(R.string.tag_log), "Speed Listener was removed!");
                                        } else {
                                            Log.i(getString(R.string.tag_log), "Speed Listener was not removed.");
                                        }
                                    }
                                });
                            } else if (dataSource.getDataType().equals(DataType.AGGREGATE_SPEED_SUMMARY)) {
                                Fitness.SensorsApi.remove(mClient, speedListener).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (status.isSuccess()) {
                                            Log.i(getString(R.string.tag_log), "Speed Listener was removed!");
                                        } else {
                                            Log.i(getString(R.string.tag_log), "Speed Listener was not removed.");
                                        }
                                    }
                                });
                            } else if (dataSource.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
                                Fitness.SensorsApi.remove(mClient, distanceListener).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (status.isSuccess()) {
                                            Log.i(getString(R.string.tag_log), "Distance Listener was removed!");
                                        } else {
                                            Log.i(getString(R.string.tag_log), "Distance Listener was not removed.");
                                        }
                                    }
                                });
                            } else if (dataSource.getDataType().equals(DataType.TYPE_DISTANCE_CUMULATIVE)) {
                                Fitness.SensorsApi.remove(mClient, distanceListener).setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (status.isSuccess()) {
                                            Log.i(getString(R.string.tag_log), "Distance Listener was removed!");
                                        } else {
                                            Log.i(getString(R.string.tag_log), "Distance Listener was not removed.");
                                        }
                                    }
                                });
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
        //logRecord.writeLog_Eventually("DataSource received: " + dataType.getName());
        // [START register_data_listener]
        if (dataType == DataType.TYPE_LOCATION_SAMPLE) {
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
                    locationRecord.saveLocation_Eventually(lat, lon, alt, pres);
                    if(startPoint){
                        notifyTripLocation(START_POINT_TAG);
                        startPoint = false;
                    } else {
                        notifyTripLocation("");
                    }
                }
            };
            // Register listener with the sensor API
            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(3, TimeUnit.SECONDS)
                            .setMaxDeliveryLatency(30, TimeUnit.SECONDS)
                            .setAccuracyMode(SensorRequest.ACCURACY_MODE_HIGH)
                            .build(),
                    locationListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(getString(R.string.tag_log), "Location Listener registered!");
                                logRecord.writeLog_Eventually("Location Listener registered!");
                            } else {
                                Log.i(getString(R.string.tag_log), "Location Listener not registered.");
                                logRecord.writeLog_Eventually("Location Listener not registered!");
                            }
                        }
                    });
        } else if (dataType == DataType.TYPE_SPEED || dataType == DataType.AGGREGATE_SPEED_SUMMARY) {
            speedListener = new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    // Speed variables no-data vales
                    float speed = dataPoint.getValue(Field.FIELD_SPEED).asFloat();
                    String name = Field.FIELD_SPEED.getName();
                    // Store Data into server and update interface with new values
                    locationRecord.saveMeasurement_Eventually(getString(R.string.speed_tag), speed);
                    notifyUiChange();
                }
            };
            // Register listener with the sensor API
            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(30, TimeUnit.SECONDS)
                            .setMaxDeliveryLatency(1, TimeUnit.MINUTES)
                            .setAccuracyMode(SensorRequest.ACCURACY_MODE_HIGH)
                            .build(),
                    speedListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(getString(R.string.tag_log), "Speed Listener registered!");
                            } else {
                                Log.i(getString(R.string.tag_log), "Speed Listener not registered.");
                            }
                        }
                    });
        } //else if (dataType == DataType.AGGREGATE_DISTANCE_DELTA || dataType == DataType.TYPE_DISTANCE_CUMULATIVE){
        else if (dataType == DataType.AGGREGATE_DISTANCE_DELTA || dataType == DataType.TYPE_DISTANCE_DELTA) {
            distanceListener = new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    // Distance variables no-data vales
                    float distance = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat();
                    if (distance >= 0) {
                        accumulated_distance += distance;
                        String name = Field.FIELD_SPEED.getName();
                        // Store Data into server and update interface with new values
                        locationRecord.saveMeasurement_Eventually(getString(R.string.distance_tag), distance);
                        locationRecord.saveMeasurement_Eventually(getString(R.string.last_distance_tag), accumulated_distance);
                        notifyUiChange();
                    }
                }
            };
            // Register listener with the sensor API
            Fitness.SensorsApi.add(
                    mClient,
                    new SensorRequest.Builder()
                            .setDataSource(dataSource) // Optional but recommended for custom data sets.
                            .setDataType(dataType) // Can't be omitted.
                            .setSamplingRate(30, TimeUnit.SECONDS)
                            .setMaxDeliveryLatency(1, TimeUnit.MINUTES)
                            .setAccuracyMode(SensorRequest.ACCURACY_MODE_HIGH)
                            .build(),
                    distanceListener)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(getString(R.string.tag_log), "Distance Listener registered!");
                            } else {
                                Log.i(getString(R.string.tag_log), "Distance Listener not registered.");
                            }
                        }
                    });
        }


    }


    private void notifyUiFailedConnection(ConnectionResult result) {
        Intent intent = new Intent(FIT_NOTIFY_INTENT);
        intent.putExtra(FIT_EXTRA_NOTIFY_FAILED_STATUS_CODE, result.getErrorCode());
        intent.putExtra(FIT_EXTRA_NOTIFY_FAILED_INTENT, result.getResolution());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    public void notifyUiChange() {
        Intent intent = new Intent(LOCATION_UPDATE_INTENT);
        intent.putExtra(getString(R.string.speed_tag), locationRecord.getSpeed());
        intent.putExtra(getString(R.string.distance_tag), locationRecord.getDistance());
        intent.putExtra(getString(R.string.last_distance_tag), locationRecord.getLast_distance());
        broadcaster.sendBroadcast(intent);
    }

    public void notifyTripLocation(String typePoint) {
        Intent intent = new Intent(CYCLIST_TRIP_LOCATION_INTENT);
        intent.putExtra(getString(R.string.trip_point_type_tag), typePoint);
        intent.putExtra(getString(R.string.longitude_tag), locationRecord.getLongitude());
        intent.putExtra(getString(R.string.latitude_tag), locationRecord.getLatitude());
        broadcaster.sendBroadcast(intent);
    }
}
