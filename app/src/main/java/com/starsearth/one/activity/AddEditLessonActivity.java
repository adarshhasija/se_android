package com.starsearth.one.activity;

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
    private EditText etLessonName;
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

        etLessonName = (EditText) findViewById(R.id.et_lesson_name);
        if (lesson != null) { etLessonName.setText(lesson.getName()); }
        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedIndex = spinnerLessonNumber.getSelectedItemPosition();
                String lessonName = etLessonName.getText().toString();
                if (lessonName == null || lessonName.length() < 1) {
                    Toast.makeText(AddEditLessonActivity.this, R.string.lesson_name_blank, Toast.LENGTH_SHORT).show();
                    return;
                }

                Firebase firebase = new Firebase(DATABASE_REFERENCE);
                if (UID != null) {
                    lesson.setName(lessonName);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    lesson.setUpdatedBy(currentUser.getUid());
                    firebase.updateExistingLesson(UID, lesson);
                }
                else {
                    UID = firebase.writeNewLesson(selectedIndex, lessonName, parentId);
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("uid", UID);
                bundle.putString("name", lessonName);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
