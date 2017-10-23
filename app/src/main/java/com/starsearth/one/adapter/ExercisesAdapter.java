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
import com.starsearth.one.domain.Exercise;

import java.util.ArrayList;

/**
 * Created by faimac on 3/2/17.
 */

public class ExercisesAdapter extends ArrayAdapter<Exercise> {

    private Context context;
    private ArrayList<Exercise> exerciseList;

    public ExercisesAdapter(Context context, int resource, ArrayList<Exercise> exerciseList) {
        super(context, resource, exerciseList);
        this.context = context;
        this.exerciseList = exerciseList;
    }

    @Override
    public int getCount() {
        return exerciseList.size();
    }

    @Nullable
    @Override
    public Exercise getItem(int position) {
        return exerciseList.get(position);
    }

    public ArrayList<Exercise> getExerciseList() {
        return exerciseList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_item_domain, null);

        Exercise exercise = getItem(position);
        String title = exercise.getTitle();
        TextView tv1 = (TextView) convertView.findViewById(R.id.text1);
        tv1.setText(title);

        return convertView;
    }
}
