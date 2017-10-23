package com.starsearth.one.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.starsearth.one.R;
import com.starsearth.one.domain.Topic;

import java.util.ArrayList;

/**
 * Created by faimac on 3/1/17.
 */

public class TopicsAdapter extends ArrayAdapter<Topic> {

    private Context context;
    private ArrayList<Topic> topicList;


    public TopicsAdapter(Context context, int resource, ArrayList<Topic> topicList) {
        super(context, resource, topicList);
        this.context = context;
        this.topicList = topicList;
    }

    @Override
    public int getCount() {
        return topicList.size();
    }

    @Nullable
    @Override
    public Topic getItem(int position) {
        return topicList.get(position);
    }

    public ArrayList<Topic> getTopicList() {
        return topicList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_item_domain, null);

        Topic topic = getItem(position);
        String title = topic.getTitle();
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        tv1.setText(title);

        return convertView;
    }
}
