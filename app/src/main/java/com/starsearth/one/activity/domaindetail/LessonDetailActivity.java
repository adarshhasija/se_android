package com.starsearth.one.activity.domaindetail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.starsearth.one.R;
import com.starsearth.one.domain.Lesson;

public class LessonDetailActivity extends ItemDetailActivity {

    private Lesson lesson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lesson = extras.getParcelable("item");
            tvTitle.setText(lesson.getTitle());
            tvDescription.setVisibility(View.GONE);
        }
    }
}
