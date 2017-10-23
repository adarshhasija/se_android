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
import com.starsearth.one.domain.Lesson;

import java.util.ArrayList;

/**
 * Created by faimac on 3/1/17.
 */

public class LessonsAdapter extends ArrayAdapter<Lesson> {

    private Context context;
    private ArrayList<Lesson> lessonList;

    public LessonsAdapter(Context context, int resource, ArrayList<Lesson> lessonList) {
        super(context, resource, lessonList);
        this.context = context;
        this.lessonList = lessonList;
    }

    @Override
    public int getCount() {
        return lessonList.size();
    }

    @Nullable
    @Override
    public Lesson getItem(int position) {
        return lessonList.get(position);
    }

    public ArrayList<Lesson> getLessonList() {
        return lessonList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_item_domain, null);

        Lesson lesson = getItem(position);
        String title = lesson.getTitle();
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        tv1.setText(title);

        return convertView;
    }
}
