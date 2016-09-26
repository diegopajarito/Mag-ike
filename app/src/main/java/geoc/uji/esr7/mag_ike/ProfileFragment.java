package geoc.uji.esr7.mag_ike;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TabHost;

import geoc.uji.esr7.mag_ike.common.status.Profile;


public class ProfileFragment extends Fragment {

    private OnProfileChangeListener mCallback;
    private Profile profile = new Profile();

    private EditText et_name;
    private EditText et_email;

    // Container Activity must implement this interface
    // Check Parent Activity
    public interface OnProfileChangeListener {
        void onProfileUpdated(Profile p);
        void updateProfileScreen();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        et_name = (EditText) getView().findViewById(R.id.et_name);
        et_email = (EditText) getView().findViewById(R.id.et_email);

        // Setting the Tab Host for selecting avatars
        TabHost host = (TabHost)rootView.findViewById(R.id.tabHost_sex);
        host.setup();

        //Tab 1 for females
        TabHost.TabSpec spec = host.newTabSpec("Tab_Female");
        spec.setContent(R.id.tab_female);
        spec.setIndicator(getContext().getText(R.string.profile_female));
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab_male");
        spec.setContent(R.id.tab_male);
        spec.setIndicator(getContext().getText(R.string.profile_male));
        host.addTab(spec);

        // Set profileValues on Screen
        updateScreenFromProfile();

        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();

        updateProfileFromScreen();

        mCallback.onProfileUpdated(profile);

    }

    @Override
    public void onResume() {
        super.onResume();

        updateScreenFromProfile();
    }

    public void updateProfileFromScreen() {

        if (et_name.getText().equals(profile.nameDefault) == false)
            profile.setName(et_name.getText().toString());
        if (et_email.getText().equals("") == false)
            profile.setEmail(et_email.getText().toString());

    }


    public void updateScreenFromProfile(){



        if (et_name.getText().equals(profile.nameDefault) == false)
            et_name.setText(profile.getName());

        if (et_email.getText().equals("") == false)
            et_email.setText(profile.getEmail());
    }


}
