package geoc.uji.esr7.mag_ike.common.adapter;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import geoc.uji.esr7.mag_ike.R;

/**
 * Created by pajarito on 05/08/2017.
 */

public class TopAdapter extends BaseExpandableListAdapter{

    private List<String> header_list;
    private HashMap<String, List<String>> avatar_list;
    private HashMap<String, List<Integer>> trips_list;
    private int[] images = { R.drawable.ic_leaderboard_position_1, R.drawable.ic_leaderboard_position_2, R.drawable.ic_leaderboard_position_3 };
    private Context context;


    public TopAdapter(List<String> header, HashMap<String, List<String>> avatar, HashMap<String, List<Integer>> trips,  Context context) {
        this.header_list = header;
        this.avatar_list = avatar;
        this.trips_list = trips;
        this.context = context;
    }



    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.avatar_list.get(header_list.get(groupPosition)).get(childPosition);
    }

    public Object getChildValue(int groupPosition, int childPosition) {
        return this.trips_list.get(header_list.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.layout_top_item, null);
        }
        if (groupPosition<=2 && childPosition<=3) {
            final String childText = (String) getChild(groupPosition, childPosition);
            final long childValue = (long) getChildValue(groupPosition, childPosition);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.itemImg);
            imageView.setImageResource(images[childPosition]);
            TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);
            txtListChild.setText(childText);
            TextView txtListValue = (TextView) convertView.findViewById(R.id.lblListValue);
            txtListValue.setText(String.valueOf(childValue));
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return avatar_list.get(header_list.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return header_list.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return header_list.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null){
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.layout_top_header, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setText(headerTitle);

        return convertView;
    }



    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
