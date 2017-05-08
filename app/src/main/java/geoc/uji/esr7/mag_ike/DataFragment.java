package geoc.uji.esr7.mag_ike;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import geoc.uji.esr7.mag_ike.common.status.GameStatus;


public class DataFragment extends Fragment {

    private GameStatus temporalStatus;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public GameStatus getTemporalStatus() {
        return temporalStatus;
    }

    public void setTemporalStatus(GameStatus temporalStatus) {
        this.temporalStatus = temporalStatus;
    }


}
