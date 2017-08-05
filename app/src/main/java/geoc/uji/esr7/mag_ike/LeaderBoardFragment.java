package geoc.uji.esr7.mag_ike;


import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import geoc.uji.esr7.mag_ike.common.adapter.TopAdapter;
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

    private List<String> getTopHeaders(){
        List<String> headers_list = new ArrayList<String>();
        headers_list.add(getString(R.string.leaderboard_trips_top_label));
        headers_list.add(getString(R.string.leaderboard_tags_top_label));
        return headers_list;
    }

    private HashMap<String, List<String>> getTop3HashMap(ArrayList<String> items_trips, ArrayList<String> items_tags){
        List<String> headers_list = getTopHeaders();

        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
        hashMap.put(headers_list.get(0), items_trips);
        hashMap.put(headers_list.get(1), items_tags);

        return hashMap;

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
                    ExpandableListView top3_trips = (ExpandableListView) getView().findViewById(R.id.ls_trips_top3);
                    top3_trips.setAdapter(new TopAdapter(getTopHeaders(),getTop3HashMap(lb.getTop3TripsList(), lb.getTop3TripsList()), getContext() ));


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
