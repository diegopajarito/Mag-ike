package geoc.uji.esr7.mag_ike;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;

import geoc.uji.esr7.mag_ike.common.status.LeaderBoardStatus;


public class LeaderBoardFragment extends Fragment {
    private onLeaderBoardUpdate mListener;
    private Activity activity;
    private View view;



    public LeaderBoardFragment() {
        // Required empty public constructor

    }

    public interface onLeaderBoardUpdate{
        void updateLeaderBoard();
        void loadLeaderBoard();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof onLeaderBoardUpdate) {
            mListener = (onLeaderBoardUpdate) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onLeaderBoardUpdate");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener.loadLeaderBoard();

    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.updateLeaderBoard();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public void updateLeaderBoard(final LeaderBoardStatus lb){

        activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv;
                try {
                    tv = (TextView) getView().findViewById(R.id.tv_trips_total);
                    tv.setText(String.valueOf(lb.getTotal_trips()));
                    tv = (TextView) getView().findViewById(R.id.tv_trips_own);
                    tv.setText(String.valueOf(lb.getOwn_trips()));
                    tv = (TextView) getView().findViewById(R.id.tv_trips_contribution);
                    String participation = String.format("%.1f", (double) lb.getOwn_trips() / (double) lb.getTotal_trips() * 100.0);
                    tv.setText(String.valueOf(participation));
                    tv = (TextView) getView().findViewById(R.id.tv_trips_position);
                    tv.setText(String.valueOf(lb.getPosition_trips()));
                    ExpandableListView top3_trips = (ExpandableListView) getView().findViewById(R.id.ls_trips_a_top3);
                    ArrayList top = lb.getTop3TripsList();
                    tv = (TextView) getView().findViewById(R.id.tv_tags_count_label2);
                    tv.setText(top.toString());

                } catch (Exception e){
                    Log.i("Update", "Error on setting value - " + e.getMessage());
                }


            }
        });

    }
}
