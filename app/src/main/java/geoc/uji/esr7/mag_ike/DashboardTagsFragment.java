package geoc.uji.esr7.mag_ike;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardTagsFragment extends Fragment {

    private View view;


    public DashboardTagsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_dashboard_tags, container, false);

        // Populate all ArrayAdapters using string arrays and a default spinner layout
        Spinner spinner = (Spinner) view.findViewById(R.id.sp_tag_1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.tag_array_1, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner = (Spinner) view.findViewById(R.id.sp_tag_2);
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.tag_array_2, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner = (Spinner) view.findViewById(R.id.sp_tag_3);
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.tag_array_3, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        return view;
    }

}
