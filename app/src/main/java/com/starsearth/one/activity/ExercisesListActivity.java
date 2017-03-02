package com.starsearth.one.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.starsearth.one.R;
import com.starsearth.one.adapter.TopicsAdapter;
import com.starsearth.one.domain.Exercise;
import com.starsearth.one.domain.Lesson;
import com.starsearth.one.domain.Topic;

import java.util.ArrayList;

public class ExercisesListActivity extends ItemListAdminActivity {

    private Topic parent;
    private ArrayList<Exercise> itemList;
    private TopicsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_exercises_list);
        setTitle(R.string.topic_details);
        tvListViewHeader.setText(R.string.exercises);
        btnAddItem.setText(R.string.add_exercise);
        REFERENCE_PARENT = "/topics/";
        REFERENCE = "exercises";

        itemList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        parent = bundle.getParcelable("parent");

    }
}
