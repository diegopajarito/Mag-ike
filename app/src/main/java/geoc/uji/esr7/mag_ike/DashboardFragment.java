package geoc.uji.esr7.mag_ike;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import geoc.uji.esr7.mag_ike.common.logger.Log;
import geoc.uji.esr7.mag_ike.common.status.GameStatus;
import geoc.uji.esr7.mag_ike.common.status.LocationRecord;
import geoc.uji.esr7.mag_ike.common.status.Profile;


public class DashboardFragment extends Fragment {


    private onDashboardUpdate mListener;
    private Activity activity;
    private View view;
    private SeekBar sb_distance;
    private ImageView iv_gauge;
    private Chronometer chronometer;


    public DashboardFragment() {
        // Required empty public constructor
    }

    // Container Activity must implement this interface
    // Check Parent Activity
    public interface onDashboardUpdate {
        long getChronometerBase();
    }

    // Container Activity must implement this interface
    // Check Parent Activity
    public interface OnLocationChangeListener {
        void updateDashboard(float speed, float distance);
    }

    public void updateDashboard(final float speed, final float distance){

        activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv;
                try {

                        tv = (TextView) getView().findViewById(R.id.value_distance);
                        tv.setText(String.format("%.0f", distance  ));

                    //if (lr.getSpeed() != lr.no_data) {
                        tv = (TextView) getView().findViewById(R.id.value_speed);
                        tv.setText(String.format("%.2f", speed * 3.6));
                        if(speed < 2.5){
                            iv_gauge.setImageResource(R.drawable.ic_gauge_walking);
                        } else if (speed < 6.9){
                            iv_gauge.setImageResource(R.drawable.ic_gauge_cycling);
                        } else {
                            iv_gauge.setImageResource(R.drawable.ic_gauge_car);
                        }
                    //)}

                } catch (Exception e){
                    Log.i("Update", "Error on setting value - " + e.getMessage());
                }


            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // After Inflating this view this one should be returned, take care of a new inflate it will erase any change
        view  = inflater.inflate(R.layout.fragment_dashboard, container, false);
        

        sb_distance = (SeekBar) view.findViewById(R.id.sb_distance);
        iv_gauge = (ImageView) view.findViewById(R.id.gauge);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer_session);
        chronometer.setBase(mListener.getChronometerBase());
        chronometer.start();

        return view;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof onDashboardUpdate) {
            mListener = (onDashboardUpdate) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



}
