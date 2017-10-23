package com.starsearth.one.activity.domaindetail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;

import com.starsearth.one.R;
import com.starsearth.one.domain.Course;

public class CourseDetailActivity extends ItemDetailActivity {

    private Course course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            course = extras.getParcelable("item");
            tvTitle.setText(course.getTitle());
            tvDescription.setText(course.getDescription());
        }

    }
}
