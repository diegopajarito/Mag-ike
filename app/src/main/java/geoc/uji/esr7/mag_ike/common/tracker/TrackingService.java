package geoc.uji.esr7.mag_ike.common.tracker;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import geoc.uji.esr7.mag_ike.common.logger.Log;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Location Track class - this class will start the location tracking action.
 */
public class TrackingService extends IntentService {

    public TrackingService() {
        super("TrackingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {



        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


        final String action = "Tracking Service Starting";
        Log.d("Service",action);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final String action = "Tracking Service Starting";
        Log.d("Service",action);
    }
}
