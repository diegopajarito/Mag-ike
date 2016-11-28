package geoc.uji.esr7.mag_ike;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
    private RadioGroup rg_avatar;

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
        temporalProfile = profile;

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        //Setting all interface elements and listeners to control interface interaction
        et_name = (EditText) rootView.findViewById(R.id.et_name);
        rg_avatar = (RadioGroup) rootView.findViewById(R.id.rg_avatar);

        rg_avatar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (getCheckedRadioButtonIndex(group) % 2 == 0)
                    temporalProfile.setGender(profile.gender_female);
                else
                    temporalProfile.setGender(profile.gender_male);
                temporalProfile.setAvatarId(getCheckedRadioButtonIndex(group));
            }
        });

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

        // Name
        if ((et_name.getText() == null) || (et_name.getText().equals(profile.nameDefault) == false))
            temporalProfile.setAvatarName(et_name.getText().toString());

        RadioGroup radioGroup;
        RadioButton rb;

        // Age
        radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_age);
        temporalProfile.setAgeRangeById(getCheckedRadioButtonIndex(radioGroup));
        radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_bike_type);
        temporalProfile.setBikeType(getCheckedRadioButtonIndex(radioGroup));
    }


    public void updateScreenFromProfile(){
        RadioGroup radioGroup;
        RadioButton radioButton;
        // Name
        if (profile.getAvatarName().equals(profile.nameDefault) == false)
            et_name.setText(profile.getAvatarName());
        //Avatar
        if (profile.getAvatarId() != profile.id_not_set ){
            radioButton = (RadioButton) rg_avatar.getChildAt(profile.getAvatarId());
            radioButton.setChecked(true);
        }
        //Age
        radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_age);
        if ((radioGroup != null) && (profile.getAgeRange().equals(profile.text_not_set) != true) ) {
            radioButton = (RadioButton) radioGroup.getChildAt(profile.getAgeRangeId());
            radioButton.setChecked(true);
        }
        // Bike
        // public
        radioGroup = (RadioGroup) getActivity().findViewById(R.id.rg_bike_type);
        if ((radioGroup!= null) && (profile.getBikeType() != profile.id_not_set)) {
            radioButton = (RadioButton) radioGroup.getChildAt(profile.getBikeType());
            radioButton.setChecked(true);
        }
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
