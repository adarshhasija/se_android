package com.starsearth.one.activity.forms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Exercise;

import java.util.ArrayList;

public class AddEditExerciseActivity extends AppCompatActivity {

    private String DATABASE_REFERENCE = "exercises";
    private String UID;
    private String parentId;
    private Exercise exercise;
    private int selectedIndex = 0;

    //UI
    private EditText etTitle;
    private EditText etDescription;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_exercise);
        setTitle(R.string.enter_edit_topic_details);

        Bundle extras = getIntent().getExtras();
        int totalExercises = 0;
        if (extras != null) {
            totalExercises = extras.getInt("totalItems");
            parentId = extras.getString("parentId");
            exercise = extras.getParcelable("exercise");
            if (exercise != null )UID = exercise.getUid();
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < (totalExercises + 1); i++) {
            list.add(i+1);
        }
        selectedIndex = list.get(list.size()-1) - 1;

        etTitle = (EditText) findViewById(R.id.et_title);
        etDescription = (EditText) findViewById(R.id.et_description);
        if (exercise != null) {
            etTitle.setText(exercise.getTitle());
            etDescription.setText(exercise.getDescription());
        }

        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicTitle = etTitle.getText().toString();
                String topicDescription = etDescription.getText().toString();

                if (topicTitle == null || topicTitle.length() < 1) {
                    Toast.makeText(AddEditExerciseActivity.this, R.string.exercise_title_blank, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (topicDescription == null || topicDescription.length() < 1) {
                    Toast.makeText(AddEditExerciseActivity.this, R.string.exercise_description_blank, Toast.LENGTH_SHORT).show();
                    return;
                }

                Firebase firebase = new Firebase(DATABASE_REFERENCE);
                if (UID != null) {
                    exercise.setTitle(topicTitle);
                    exercise.setDescription(topicDescription);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    exercise.setUpdatedBy(currentUser.getUid());
                    firebase.updateExistingExercise(UID, exercise);
                }
                else {
                    UID = firebase.writeNewExercise(selectedIndex, topicTitle, topicDescription, parentId);
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("uid", UID);
                bundle.putString("title", topicTitle);
                bundle.putString("description", topicDescription);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();

            }
        });
    }
}
