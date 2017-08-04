package geoc.uji.esr7.mag_ike;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;


import geoc.uji.esr7.mag_ike.common.logger.Log;



public class DashboardFragment extends Fragment {


    private onDashboardUpdate mListener;
    private Activity activity;
    private View view;
    private ImageView iv_gauge;
    private Chronometer chronometer;


    public DashboardFragment() {
        // Required empty public constructor
    }

    // Container Activity must implement this interface
    // Check Parent Activity
    public interface onDashboardUpdate {
        long getChronometerBase();
        void onTrackingServiceStart(long base);
        void onTrackingServiceStop();
        int getDayOfCampaign();
        int getTripCounter();
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
                    if (distance>0){
                        tv.setText(String.format("%.0f", distance  ));
                    } else{
                        tv.setText(getText(R.string.dashboard_default_cero));
                    }

                    tv = (TextView) getView().findViewById(R.id.value_speed);
                    if (speed < 0){
                        tv.setText(getText(R.string.dashboard_default_nodata));
                        iv_gauge.setImageResource(R.drawable.ic_speed_bywalking);
                    } else if(speed < 2.5){
                        iv_gauge.setImageResource(R.drawable.ic_speed_bywalking);
                    } else if (speed < 6.9){
                        iv_gauge.setImageResource(R.drawable.ic_speed_bycycling);
                    } else {
                        iv_gauge.setImageResource(R.drawable.ic_speed_bycar);
                    }

                } catch (Exception e){
                    Log.i("Update", "Error on setting value - " + e.getMessage());
                }


            }
        });

    }

    public void onTrackingServiceStop(){
        stopChronometer();
    }

    public void onTrackingServiceStart(long base){
        startChronometer(base);
    }

    private void setTripStatus(final int day, final int trip){
        activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) getView().findViewById(R.id.tv_day);
                tv.setText(String.valueOf(day));
                tv = (TextView) getView().findViewById(R.id.tv_trips);
                tv.setText(String.valueOf(trip));
                }
            });
    }

    private void startChronometer(long base){
        chronometer = (Chronometer) view.findViewById(R.id.chronometer_session);
        chronometer.setBase(base);
        chronometer.start();
    }

    private void stopChronometer(){
        chronometer = (Chronometer) view.findViewById(R.id.chronometer_session);
        chronometer.stop();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // After Inflating this view this one should be returned, take care of a new inflate it will erase any change
        view  = inflater.inflate(R.layout.fragment_dashboard, container, false);

        startChronometer(mListener.getChronometerBase());
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

    @Override
    public void onResume() {
        super.onResume();
        setTripStatus(mListener.getDayOfCampaign(), mListener.getTripCounter());
    }
}
