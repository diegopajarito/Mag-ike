package geoc.uji.esr7.mag_ike;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;

import geoc.uji.esr7.mag_ike.common.status.Profile;


public class ProfileFragment extends Fragment {

    private OnProfileChangeListener mListener;
    private Profile temporalProfile;
    private Profile profile;

    private View rootView;
    private EditText et_name;
    private EditText et_email;
    private TabHost tabHostGender;
    private TabHost.TabSpec tabFemale;
    private TabHost.TabSpec tabMale;

    // Container Activity must implement this interface
    // Check Parent Activity
    public interface OnProfileChangeListener {
        boolean onProfileUpdated(Profile p);
        Profile getCurrentProfile();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        profile = mListener.getCurrentProfile();
        temporalProfile = new Profile();

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        et_name = (EditText) rootView.findViewById(R.id.et_name);
        et_email = (EditText) rootView.findViewById(R.id.et_email);

        // Setting the Tab Host for selecting avatars
        tabHostGender = (TabHost)rootView.findViewById(R.id.tabHost_gender);
        tabHostGender.setup();
        //Tab 1 for females
        tabFemale = tabHostGender.newTabSpec("Tab_Female");
        tabFemale.setContent(R.id.tab_female);
        tabFemale.setIndicator(getContext().getText(R.string.profile_female));
        tabHostGender.addTab(tabFemale);
        //Tab 2
        tabMale = tabHostGender.newTabSpec("Tab_male");
        tabMale.setContent(R.id.tab_male);
        tabMale.setIndicator(getContext().getText(R.string.profile_male));
        tabHostGender.addTab(tabMale);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProfileChangeListener) {
            mListener = (OnProfileChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        updateProfileFromScreen();

        if (mListener.onProfileUpdated(temporalProfile))
            temporalProfile = profile;

    }

    @Override
    public void onResume() {
        super.onResume();

        updateScreenFromProfile();
    }

    public void updateProfileFromScreen() {

        if ((et_name.getText() == null) || (et_name.getText().equals(profile.nameDefault) == false))
            temporalProfile.setAvatarName(et_name.getText().toString());

        RadioGroup radioGroup;
        if (tabHostGender.getCurrentTab() == 0) {
            temporalProfile.setGender(temporalProfile.gender_female);
            radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_avatar_male);
            temporalProfile.setAvatarId(getCheckedRadioButtonIndex(radioGroup));
        }
        else if (tabHostGender.getCurrentTab() == 1){
            temporalProfile.setGender(temporalProfile.gender_male);
            radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_avatar_male);
            temporalProfile.setAvatarId(getCheckedRadioButtonIndex(radioGroup));
        }
        radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_age);
        temporalProfile.setAgeRangeById(getCheckedRadioButtonIndex(radioGroup));
        radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_bike_type);
        temporalProfile.setBikeType(getCheckedRadioButtonIndex(radioGroup));
        if ((et_name.getText() == null) || et_email.getText().equals(profile.text_not_set) == false)
            temporalProfile.setEmail(et_email.getText().toString());

    }


    public void updateScreenFromProfile(){
        if (profile.getAvatarName().equals(profile.nameDefault) == false)
            et_name.setText(profile.getAvatarName());

        RadioGroup radioGroup;
        RadioButton radioButton;
        if ((profile.getAvatarId() != profile.id_not_set && profile.getGender() != profile.text_not_set) && (profile.getGender() == profile.gender_female)){
            tabHostGender.setCurrentTab(0);
            radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_avatar_female);
            radioButton = (RadioButton) radioGroup.getChildAt(profile.getAvatarId());
            radioButton.setChecked(true);
        }
        else if ((profile.getAvatarId() != profile.id_not_set && profile.getGender() != profile.text_not_set) && (profile.getGender() == profile.gender_male)){
            tabHostGender.setCurrentTab(1);
            radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_avatar_male);
            radioButton = (RadioButton) radioGroup.getChildAt(profile.getAvatarId());
            radioButton.setChecked(true);
        }
        radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_age);
        if ((radioGroup != null) && (profile.getAgeRange().equals(profile.text_not_set) != true) ) {
            radioButton = (RadioButton) radioGroup.getChildAt(profile.getAgeRangeId());
            radioButton.setChecked(true);
        }
        radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_bike_type);
        if ((radioGroup!= null) && (profile.getBikeType() != profile.id_not_set)) {
            radioButton = (RadioButton) radioGroup.getChildAt(profile.getBikeType());
            radioButton.setChecked(true);
        }
        if (profile.getEmail().equals(profile.text_not_set) == false)
            et_email.setText(profile.getEmail());
    }


    // get index of selected item on a Radio Group
    public int getCheckedRadioButtonIndex (RadioGroup radioGroup){
        int radioButtonId;
        View radioButton;
        radioButtonId = radioGroup.getCheckedRadioButtonId();
        radioButton = radioGroup.findViewById(radioButtonId);
        return radioGroup.indexOfChild(radioButton);
    }

}
