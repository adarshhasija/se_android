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
import com.starsearth.one.domain.Course;

import java.util.ArrayList;

/**
 * Created by faimac on 2/27/17.
 */

public class CoursesAdapter extends ArrayAdapter<Course> {

    private Context context;
    private ArrayList<Course> courseList;

    public CoursesAdapter(Context context, int resource, ArrayList<Course> courseList) {
        super(context, resource, courseList);
        this.context = context;
        this.courseList = courseList;
    }

    @Override
    public int getCount() {
        return courseList.size();
    }

    @Nullable
    @Override
    public Course getItem(int position) {
        return courseList.get(position);
    }

    public ArrayList<Course> getCourseList() {
        return courseList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //convertView = inflater.inflate(R.layout.courses_list_item, null);
        convertView = inflater.inflate(R.layout.list_item_domain, null);

        Course course = getItem(position);
        String title = course.getTitle();
        String description = course.getDescription();
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        tv1.setText(title);

        convertView.setTag(course);

        return convertView;
    }
}
