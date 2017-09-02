package geoc.uji.esr7.mag_ike;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import geoc.uji.esr7.mag_ike.common.adapter.TopAdapter;
import geoc.uji.esr7.mag_ike.common.status.GameStatus;
import geoc.uji.esr7.mag_ike.common.status.LeaderBoardStatus;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScoreFragment extends Fragment {
    private onLeaderBoardUpdate mListener;
    private Activity activity;
    private View view;

    public ScoreFragment() {
        // Required empty public constructor
    }


    public interface onLeaderBoardUpdate{
        GameStatus getGameStatus();
        void updateScore();
        void loadScore();
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
        mListener.loadScore();
        setRetainInstance(true);
    }
    @Override
    public void onResume() {
        super.onResume();
        mListener.updateScore();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mListener.updateScore();
        view = inflater.inflate(R.layout.fragment_score, container, false);
       TextView tv;
        ImageView iv;
        if (mListener.getGameStatus().isExperiment()) {
            if (mListener.getGameStatus().getExperimentProfile().equals(getResources().getString(R.string.experiment_profile_competition))) {
                tv = (TextView) view.findViewById(R.id.tv_trips_contribution);
                tv.setVisibility(View.INVISIBLE);
                iv = (ImageView) view.findViewById(R.id.iv_percent_trips);
                iv.setVisibility(View.INVISIBLE);
                tv = (TextView) view.findViewById(R.id.tv_tags_contribution);
                tv.setVisibility(View.INVISIBLE);
                iv = (ImageView) view.findViewById(R.id.iv_percent_tags);
                iv.setVisibility(View.INVISIBLE);
            } else {
                tv = (TextView) view.findViewById(R.id.tv_trips_position);
                tv.setVisibility(View.INVISIBLE);
                iv = (ImageView) view.findViewById(R.id.iv_trips);
                iv.setVisibility(View.INVISIBLE);
                tv = (TextView) view.findViewById(R.id.tv_tags_position);
                tv.setVisibility(View.INVISIBLE);
                iv = (ImageView) view.findViewById(R.id.iv_tags);
                iv.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void updateLeaderBoardTrips(final LeaderBoardStatus lb){

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
                    tv.setText(participation);
                    tv = (TextView) getView().findViewById(R.id.tv_trips_position);
                    tv.setText(String.valueOf(lb.getPosition_trips()));
                } catch (Exception e){
                    Log.i("Update", "Error on setting value - " + e.getMessage());
                }


            }
        });

    }

    public void updateLeaderBoardTags(final LeaderBoardStatus lb){

        activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv;
                try {
                    tv = (TextView) getView().findViewById(R.id.tv_tags_total);
                    tv.setText((String.valueOf(lb.getTotal_tags())));
                    tv = (TextView) getView().findViewById(R.id.tv_tags_own);
                    tv.setText((String.valueOf(lb.getOwn_tags())));
                    tv = (TextView) getView().findViewById(R.id.tv_tags_position);
                    tv.setText(String.valueOf(lb.getPosition_tags()));
                    tv = (TextView) getView().findViewById(R.id.tv_tags_contribution);
                    String participation = String.format("%.1f", (double) lb.getOwn_tags() / (double) lb.getTotal_tags() * 100.0);
                    tv.setText(participation);

                } catch (Exception e){
                    Log.i("Update", "Error on setting value - " + e.getMessage());
                }


            }
        });

    }

}
