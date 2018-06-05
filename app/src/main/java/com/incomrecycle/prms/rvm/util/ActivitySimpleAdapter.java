package com.incomrecycle.prms.rvm.util;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import java.util.List;
import java.util.Map;

public class ActivitySimpleAdapter extends SimpleAdapter {
    public ActivitySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = super.getView(position, convertView, parent);
        }
        view.setBackgroundColor(new int[]{-1, Color.rgb(219, 238, 244)}[position % 2]);
        return super.getView(position, view, parent);
    }
}
