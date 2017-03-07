package com.starsearth.one.activity.forms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Lesson;

import java.util.ArrayList;

public class AddEditLessonActivity extends AppCompatActivity {

    private String DATABASE_REFERENCE = "lessons";
    private String UID;
    private String parentId;
    private Lesson lesson;

    //UI
    private Spinner spinnerLessonNumber;
    private EditText etLessonTitle;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_lesson);
        setTitle(R.string.enter_edit_lesson_details);

        Bundle extras = getIntent().getExtras();
        int totalLessons = 0;
        if (extras != null) {
            totalLessons = extras.getInt("totalItems");
            parentId = extras.getString("parentId");
            lesson = extras.getParcelable("lesson");
            if (lesson != null )UID = lesson.getUid();
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < (totalLessons + 1); i++) {
            list.add(i+1);
        }
        spinnerLessonNumber = (Spinner) findViewById(R.id.spinner_lesson_number);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLessonNumber.setAdapter(adapter);
        if (lesson != null) {

        }
        else {
            spinnerLessonNumber.setSelection(spinnerLessonNumber.getCount() - 1);
        }

        etLessonTitle = (EditText) findViewById(R.id.et_lesson_title);
        if (lesson != null) { etLessonTitle.setText(lesson.getTitle()); }
        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedIndex = spinnerLessonNumber.getSelectedItemPosition();
                String lessonTitle = etLessonTitle.getText().toString();
                if (lessonTitle == null || lessonTitle.length() < 1) {
                    Toast.makeText(AddEditLessonActivity.this, R.string.lesson_name_blank, Toast.LENGTH_SHORT).show();
                    return;
                }

                Firebase firebase = new Firebase(DATABASE_REFERENCE);
                if (UID != null) {
                    lesson.setTitle(lessonTitle);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    lesson.setUpdatedBy(currentUser.getUid());
                    firebase.updateExistingLesson(UID, lesson);
                }
                else {
                    UID = firebase.writeNewLesson(selectedIndex, lessonTitle, parentId);
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("uid", UID);
                bundle.putString("title", lessonTitle);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
