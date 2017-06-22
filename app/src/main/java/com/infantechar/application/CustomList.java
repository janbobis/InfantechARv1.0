package com.infantechar.application;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Eldon on 5/23/2017.
 */

public class CustomList extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] resource;


    public CustomList(@NonNull Activity context, String[] resource) {
        super(context, R.layout.layout, resource);
        this.context = context;
        this.resource = resource;
    }

    public View getView(int pos, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout, null, true);

        ImageView img = (ImageView) rowView.findViewById(R.id.coupon_icon);
        TextView title = (TextView) rowView.findViewById(R.id.coupon_title);

        title.setText(resource[pos]);
        String content[] = resource[pos].split(":");
        img.setImageResource(BitmapEnum.getEnum(content[0]).getBitmap());
        return rowView;

    }
}
