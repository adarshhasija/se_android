package com.starsearth.one.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.starsearth.one.R;
import com.starsearth.one.domain.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 4/6/17.
 */

public class MainSEAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> objectList;

    public MainSEAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objectList = objects;
    }

    @Override
    public int getCount() {
        return objectList.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return objectList.get(position);
    }

    public ArrayList<String> getObjectList() {
        return objectList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //convertView = inflater.inflate(R.layout.courses_list_item, null);
        convertView = inflater.inflate(R.layout.list_item_main, null);

        String object = getItem(position);
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        tv1.setText(object);

        return convertView;
    }


}
