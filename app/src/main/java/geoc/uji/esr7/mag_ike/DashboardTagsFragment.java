package geoc.uji.esr7.mag_ike;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardTagsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private onDashboardUpdate mListener;
    private Activity activity;
    private View view;
    private ImageView iv_gauge;
    private Chronometer chronometer;
    private boolean[] firstTime;
    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;
    private ArrayList<String> elements;

    public DashboardTagsFragment() {
        // Required empty public constructor
    }

    public interface onDashboardUpdate {
        long getChronometerBase();
        int getDayOfCampaign();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] arrayString;
        ArrayAdapter<CharSequence> adapter;
        firstTime = new boolean[3];
        Arrays.fill(firstTime, Boolean.TRUE);

        view = inflater.inflate(R.layout.fragment_dashboard_tags, container, false);
        iv_gauge = (ImageView) view.findViewById(R.id.gauge);
        iv_gauge = (ImageView) view.findViewById(R.id.gauge);
        chronometer = (Chronometer) view.findViewById(R.id.chronometer_session);
        chronometer.setBase(mListener.getChronometerBase());
        chronometer.start();

        //* first Spinner
        elements = new ArrayList<>();
        elements.add(0, getString(R.string.tag_add));
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
        elements.add(0, getString(R.string.tag_add));
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
        elements.add(0, getString(R.string.tag_add));
        elements.addAll( Arrays.asList(getResources().getStringArray(R.array.tag_array_3)) );
        arrayString = elements.toArray(new String[0]);
        // Populate all ArrayAdapters using string arrays and a default spinner layout
        spinner3 = (Spinner) view.findViewById(R.id.sp_tag_3);
        // ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.tag_array_1, android.R.layout.simple_spinner_item);
        adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, arrayString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter);
        spinner3.setOnItemSelectedListener(this);


        return view;
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
                try {

                    if(speed < 2.5){
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


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
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

    private void setDayOfCampaign(final int day){
        activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) getView().findViewById(R.id.tv_day);
                tv.setText(String.valueOf(day));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setDayOfCampaign(mListener.getDayOfCampaign());
    }
}
