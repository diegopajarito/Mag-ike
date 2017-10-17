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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();
        mListener.loadLeaderBoard();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        // Inflate the layout for this fragment
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

    private HashMap<String, List<String>> getTop3HashMap(List<String> headers_list, ArrayList<String> items_trips, ArrayList<String> items_tags){
        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
        hashMap.put(headers_list.get(0), items_trips);
        hashMap.put(headers_list.get(1), items_tags);
        return hashMap;
    }

    private HashMap<String, List<Integer>> getTop3HashValues(List<String> headers_list, ArrayList<Integer> values_trips, ArrayList<Integer> values_tags ){
        HashMap<String, List<Integer>> hashMap = new HashMap<String, List<Integer>>();
        hashMap.put(headers_list.get(0), values_trips);
        hashMap.put(headers_list.get(1), values_tags);
        return hashMap;
    }

    public void updateTop(final LeaderBoardStatus lb){
        activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Top 3
                    List<String> topHeaders = getTopHeaders();
                    HashMap<String, List<String>> hashMapAvatars = getTop3HashMap(topHeaders, lb.getTop3TripsList(), lb.getTop3TagsList());
                    HashMap<String, List<Integer>> hashMapValues = getTop3HashValues(topHeaders, lb.getTop3TripsValuesList(), lb.getTop3TagsValuesList());
                    ExpandableListView top3_trips = (ExpandableListView) getView().findViewById(R.id.ls_trips_top3);
                    TopAdapter adapter = new TopAdapter(topHeaders,hashMapAvatars, hashMapValues, getContext() );
                    top3_trips.setAdapter(adapter);
                    top3_trips.expandGroup(0);
                    top3_trips.expandGroup(1);

                } catch (Exception e){
                    Log.i(getString(R.string.tag_log), "Error on setting top - " + e.getMessage());
                }
            }
        });
    }

}
