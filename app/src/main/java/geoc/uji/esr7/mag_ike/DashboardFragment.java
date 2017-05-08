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
import geoc.uji.esr7.mag_ike.common.status.Profile;


public class DashboardFragment extends Fragment {


    private onDashboardUpdate mListener;
    private Activity activity;
    private View view;
    private SeekBar sb_distance;
    private ImageView iv_gauge;
    private Chronometer chronometer;

    // Container Activity must implement this interface
    // Check Parent Activity
    public interface onDashboardUpdate {
        long getChronometerBase();
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

        if (context instanceof OnStatusChangeListener) {
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

    // Container Activity must implement this interface
    // Check Parent Activity
    public interface OnStatusChangeListener {
        void updateDashboardFromStatus(GameStatus s);
    }

    public DashboardFragment() {
        // Required empty public constructor
    }

    public void updateDashboardFromStatus(final GameStatus s){

        activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv;

                /*

                try {
                    if (s.getLast_distance() != s.no_data) {
                        tv = (TextView) getView().findViewById(R.id.value_distance);
                        tv.setText(String.format("%.0f", s.getLast_distance()  ));
                        sb_distance.setProgress((int) s.getLast_distance());
                    }
                    if (s.getSpeed() != s.no_data) {
                        tv = (TextView) getView().findViewById(R.id.value_speed);
                        tv.setText(String.format("%.2f", s.getSpeed() * 3.6));
                        if(s.getSpeed() < 2.5){
                            iv_gauge.setImageResource(R.drawable.ic_gauge_walking);
                        } else if (s.getSpeed() < 6.9){
                            iv_gauge.setImageResource(R.drawable.ic_gauge_cycling);
                        } else {
                            iv_gauge.setImageResource(R.drawable.ic_gauge_car);
                        }
                    }
                    tv = (TextView) getView().findViewById(R.id.value_contribution_location);
                    tv.setText(String.valueOf(s.getLocationContribution()));
                    tv = (TextView) getView().findViewById(R.id.value_contribution_distance);
                    tv.setText(String.valueOf(s.getDistanceContribution()));
                    tv = (TextView) getView().findViewById(R.id.value_contribution_speed);
                    tv.setText(String.valueOf(s.getSpeedContribution()));
                    tv = (TextView) getView().findViewById(R.id.value_contribution);
                    tv.setText(String.valueOf(s.getTotalContribution()));
                    tv = (TextView) getView().findViewById(R.id.tv_total_day);
                    tv.setText(String.valueOf(s.getCampaignLength()));
                    tv = (TextView) getView().findViewById(R.id.tv_current_day);
                    tv.setText(String.valueOf(s.getCampaignDay()));
                } catch (Exception e){
                    Log.i("Update", "Error on setting value - " + e.getMessage());
                }

                */
            }
        });

    }

}
