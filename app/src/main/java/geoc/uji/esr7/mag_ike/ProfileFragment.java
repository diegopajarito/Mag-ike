package geoc.uji.esr7.mag_ike;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;


public class ProfileFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);


        // Setting the Tab Host for selecting avatars
        TabHost host = (TabHost)rootView.findViewById(R.id.tabHost_sex);
        host.setup();

        //Tab 1 for females
        TabHost.TabSpec spec = host.newTabSpec("Tab_Female");
        spec.setContent(R.id.tab_female);
        spec.setIndicator("Female");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab_male");
        spec.setContent(R.id.tab_male);
        spec.setIndicator("Male");
        host.addTab(spec);

        return rootView;
    }
}
