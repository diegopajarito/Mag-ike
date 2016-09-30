package geoc.uji.esr7.mag_ike;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import geoc.uji.esr7.mag_ike.common.status.GameStatus;
import geoc.uji.esr7.mag_ike.common.status.Profile;


public class DashboardFragment extends Fragment {


    private OnStatusChangeListener mListener;

    // Container Activity must implement this interface
    // Check Parent Activity
    public interface OnStatusChangeListener {
        void updateDashboardFromStatus(GameStatus s);
    }

    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStatusChangeListener) {
            mListener = (OnStatusChangeListener) context;
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


    public void updateDashboardFromStatus(final GameStatus s){

        Activity act = getActivity();
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) getView().findViewById(R.id.value_distance);
                tv.setText(String.valueOf(s.getDistance()));
                tv = (TextView) getView().findViewById(R.id.value_speed);
                tv.setText(String.valueOf(s.getSpeed()*3.6));
                tv = (TextView) getView().findViewById(R.id.value_contribution_location);
                tv.setText(String.valueOf(s.getLocationContribution()));
                tv = (TextView) getView().findViewById(R.id.value_contribution_distance);
                tv.setText(String.valueOf(s.getDistanceContribution()));
                tv = (TextView) getView().findViewById(R.id.value_contribution_speed);
                tv.setText(String.valueOf(s.getSpeedContribution()));
                tv = (TextView) getView().findViewById(R.id.value_contribution);
                tv.setText(String.valueOf(s.getTotalContribution()));
            }
        });

    }
}
