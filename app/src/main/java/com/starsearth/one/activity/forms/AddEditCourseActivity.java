package com.starsearth.one.activity.forms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Course;

public class AddEditCourseActivity extends AppCompatActivity {

    private String DATABASE_REFERENCE = "courses";
    private String UID = null;

    private Course course;

    //UI
    private Spinner spinnerCourseType;
    private Spinner spinnerCourseDifficulty;
    private EditText etCourseDescription;
    private Switch switchUSBKeyboard;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_course);

        setTitle(R.string.enter_edit_course_details);

        spinnerCourseType = (Spinner) findViewById(R.id.spinner_course_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.course_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseType.setAdapter(adapter);

        spinnerCourseDifficulty = (Spinner) findViewById(R.id.spinner_course_difficulty);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.course_difficulty, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseDifficulty.setAdapter(adapter);

        etCourseDescription = (EditText) findViewById(R.id.et_course_description);
        switchUSBKeyboard = (Switch) findViewById(R.id.switch_usb_keyboard);

        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseType = (String) spinnerCourseType.getSelectedItem();
                int courseDifficulty = spinnerCourseDifficulty.getSelectedItemPosition();
                String courseDescription = etCourseDescription.getText().toString();
                if (courseDescription == null || courseDescription.length() < 1) {
                    Toast.makeText(AddEditCourseActivity.this, R.string.course_description_blank, Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean usbKeyboard = switchUSBKeyboard.isChecked();

                Firebase firebase = new Firebase(DATABASE_REFERENCE);
                String courseTitle;
                String courseDifficultyString = "Basic";
                if (courseDifficulty == 1) { courseDifficultyString = "Advanced"; }
                courseTitle = courseDifficultyString + " " + courseType;
                if (UID != null) {
                    course.setType(courseType);
                    course.setDifficulty(courseDifficulty);
                    course.setTitle(courseTitle);
                    course.setDescription(courseDescription);
                    course.usbKeyboard = usbKeyboard;
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    course.setUpdatedBy(currentUser.getUid());
                    firebase.updateExistingCourse(UID, course);
                }
                else {
                    UID = firebase.writeNewCourse(courseType, courseDifficulty, courseTitle, courseDescription, usbKeyboard);
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("uid", UID);
                bundle.putString("name", courseTitle);
                bundle.putString("description", courseDescription);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            course = extras.getParcelable("course");
            spinnerCourseDifficulty.setSelection(course.difficulty);
            if (course.type.equalsIgnoreCase("typing")) {
                spinnerCourseType.setSelection(0);
            }
            etCourseDescription.setText(course.getDescription());
            switchUSBKeyboard.setChecked(course.usbKeyboard);
            UID = course.getUid();
        }
    }
}
