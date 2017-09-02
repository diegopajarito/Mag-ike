package geoc.uji.esr7.mag_ike;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import geoc.uji.esr7.mag_ike.common.logger.Log;
import geoc.uji.esr7.mag_ike.common.status.GameStatus;


public class DashboardFragment extends Fragment implements AdapterView.OnItemSelectedListener {


    private onDashboardUpdate mListener;
    private Activity activity;
    private View view;
    private ImageView iv_gauge;
    private Chronometer chronometer;
    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;
    private boolean[] firstTime;
    private ArrayList<String> elements;


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
        void onTripsUpdated(int counter);
        void onTagsUpdated(List<String> tags);
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
                        tv.setText(String.format("%,.0f", distance  ));
                    } else{
                        tv.setText(getText(R.string.dashboard_default_cero));
                    }

                    tv = (TextView) getView().findViewById(R.id.value_speed);
                    tv.setText(String.format("%.0f", speed));
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

    public void updateTripCounter(){
        activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) getView().findViewById(R.id.tv_trips);
                tv.setText(String.valueOf(mListener.getTripCounter()));
            }
        });
    }

    private void startChronometer(long base){
        if(view !=null) {
            chronometer = (Chronometer) view.findViewById(R.id.chronometer_session);
            long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();

            chronometer.setBase(base - elapsedRealtimeOffset);
            chronometer.start();
        }
    }

    private void stopChronometer(){
        chronometer = (Chronometer) view.findViewById(R.id.chronometer_session);
        chronometer.stop();
    }

    public void refreshAdapterWithNewTag(Spinner spinner, String newTag){
        elements = new ArrayList<>();
        elements.add(0, newTag);
        String[] arrayString;
        ArrayAdapter<CharSequence> adapter;
        if (spinner.equals(spinner1)) {
            elements.addAll(Arrays.asList(getResources().getStringArray(R.array.tag_array_1)));
            arrayString = elements.toArray(new String[0]);
            spinner1 = (Spinner) view.findViewById(R.id.sp_tag_1);
            adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, arrayString);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner1.setAdapter(adapter);
            firstTime[0] = true;
            spinner1.setOnItemSelectedListener(this);
        } else if (spinner.equals(spinner2)) {
            elements.addAll(Arrays.asList(getResources().getStringArray(R.array.tag_array_2)));
            arrayString = elements.toArray(new String[0]);
            spinner2 = (Spinner) view.findViewById(R.id.sp_tag_2);
            adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, arrayString);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(adapter);
            firstTime[1] = true;
            spinner2.setOnItemSelectedListener(this);
        }else {
            elements.addAll(Arrays.asList(getResources().getStringArray(R.array.tag_array_3)));
            arrayString = elements.toArray(new String[0]);
            spinner3 = (Spinner) view.findViewById(R.id.sp_tag_3);
            adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, arrayString);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner3.setAdapter(adapter);
            firstTime[2] = true;
            spinner3.setOnItemSelectedListener(this);
        }



    }

    private void setTagsSpinners(){
        String[] arrayString;
        ArrayAdapter<CharSequence> adapter;
        firstTime = new boolean[3];
        Arrays.fill(firstTime, Boolean.TRUE);

        //* first Spinner
        elements = new ArrayList<>();
        elements.add(0, getString(R.string.tag_choose));
        elements.add(1, getString(R.string.tag_add));
        elements.addAll( Arrays.asList(getResources().getStringArray(R.array.tag_array_1)) );
        arrayString = elements.toArray(new String[0]);
        // Populate all ArrayAdapters using string arrays and a default spinner layout
        spinner1 = (Spinner) view.findViewById(R.id.sp_tag_1);
        // ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.tag_array_1, android.R.layout.simple_spinner_item);
        adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, arrayString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(this);

        //* second Spinner
        elements = new ArrayList<>();
        elements.add(0, getString(R.string.tag_choose));
        elements.add(1, getString(R.string.tag_add));
        elements.addAll( Arrays.asList(getResources().getStringArray(R.array.tag_array_2)) );
        arrayString = elements.toArray(new String[0]);
        // Populate all ArrayAdapters using string arrays and a default spinner layout
        spinner2 = (Spinner) view.findViewById(R.id.sp_tag_2);
        // ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.tag_array_1, android.R.layout.simple_spinner_item);
        adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, arrayString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter);
        spinner2.setOnItemSelectedListener(this);

        //* third Spinner
        elements = new ArrayList<>();
        elements.add(0, getString(R.string.tag_choose));
        elements.add(1, getString(R.string.tag_add));
        elements.addAll( Arrays.asList(getResources().getStringArray(R.array.tag_array_3)) );
        arrayString = elements.toArray(new String[0]);
        // Populate all ArrayAdapters using string arrays and a default spinner layout
        spinner3 = (Spinner) view.findViewById(R.id.sp_tag_3);
        // ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.tag_array_1, android.R.layout.simple_spinner_item);
        adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, arrayString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter);
        spinner3.setOnItemSelectedListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // After Inflating this view this one should be returned, take care of a new inflate it will erase any change
        view  = inflater.inflate(R.layout.fragment_dashboard, container, false);

        setTagsSpinners();

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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

    @Override
    public void onPause() {
        super.onPause();
        List<String> tags = new ArrayList<>();
        if (!spinner1.getSelectedItem().equals(getString(R.string.tag_choose)) && !spinner1.getSelectedItem().equals(R.string.tag_add))
            tags.add(0, spinner1.getSelectedItem().toString());
        if (!spinner2.getSelectedItem().equals(getString(R.string.tag_choose)) && !spinner2.getSelectedItem().equals(R.string.tag_add))
            tags.add(0, spinner2.getSelectedItem().toString());
        if (!spinner3.getSelectedItem().equals(getString(R.string.tag_choose)) && !spinner3.getSelectedItem().equals(R.string.tag_add))
            tags.add(0, spinner3.getSelectedItem().toString());
        mListener.onTagsUpdated(tags);
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, View view, int pos, long id) {
        // If the selected item is the one for a new tag, create an alert dialog for typing
        if (parent.equals(spinner1) && firstTime[0] == false){
            if (parent.getItemAtPosition(pos).toString().equals(getString(R.string.tag_add))){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getText(R.string.app_name));
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton(getString(R.string.tag_add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshAdapterWithNewTag((Spinner) parent, input.getText().toString());
                    }
                });
                builder.setNegativeButton(getString(R.string.tag_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        parent.setSelection(2);
                    }
                });

                //parent.setSelected();

                builder.show();
            }
            spinner2.setVisibility(View.VISIBLE);
        } else if (parent.equals(spinner1) && firstTime[0] == true){
            firstTime[0] = false;
        } else if (parent.equals(spinner2) && firstTime[1] == false){
            if (parent.getItemAtPosition(pos).toString().equals(getString(R.string.tag_add))){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getText(R.string.app_name));
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton(getString(R.string.tag_add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshAdapterWithNewTag((Spinner) parent, input.getText().toString());
                    }
                });
                builder.setNegativeButton(getString(R.string.tag_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        parent.setSelection(2);
                    }
                });

                //parent.setSelected();

                builder.show();
            }
            spinner3.setVisibility(View.VISIBLE);
        } else if (parent.equals(spinner2) && firstTime[1] == true){
            firstTime[1] = false;
        } else if (parent.equals(spinner3) && firstTime[2] == false){
            if (parent.getItemAtPosition(pos).toString().equals(getString(R.string.tag_add))){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getText(R.string.app_name));
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton(getString(R.string.tag_add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshAdapterWithNewTag((Spinner) parent, input.getText().toString());
                    }
                });
                builder.setNegativeButton(getString(R.string.tag_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        parent.setSelection(2);
                    }
                });

                //parent.setSelected();

                builder.show();
            }
        } else if (parent.equals(spinner3) && firstTime[2] == true){
            firstTime[2] = false;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
