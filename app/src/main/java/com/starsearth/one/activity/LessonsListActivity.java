package com.starsearth.one.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.R;
import com.starsearth.one.domain.Course;

public class LessonsListActivity extends AppCompatActivity {

    private String RELATIVE_LOCATION_COURSES = "/courses/";

    private Course course;
    private DatabaseReference courseDatabase;
    private DatabaseReference mDatabase; //Lessons list DB

    //UI
    private TextView tvCourseName;
    private TextView tvCourseDescription;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons_list);

        setTitle("Course Details");

        Bundle bundle = getIntent().getExtras();
        course = bundle.getParcelable("course");

        courseDatabase = FirebaseDatabase.getInstance().getReference(RELATIVE_LOCATION_COURSES + course.getUid());

        tvCourseName = (TextView) findViewById(R.id.tv_course_name);
        tvCourseName.setText(course.getName());
        tvCourseDescription = (TextView) findViewById(R.id.tv_course_description);
        tvCourseDescription.setText(course.getDescription());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            courseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    course = null;
                    Course course = dataSnapshot.getValue(Course.class);
                    String courseKey = dataSnapshot.getKey();

                    tvCourseName.setText(course.getName());
                    tvCourseDescription.setText(course.getDescription());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(),databaseError.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_lessons_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.edit_course:
                intent = new Intent(LessonsListActivity.this, AddEditCourseActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("course", course);
                intent.putExtras(bundle);
                startActivityForResult(intent, 100);
                return true;
            case R.id.delete_course:
                new AlertDialog.Builder(LessonsListActivity.this)
                        .setTitle(R.string.delete_course)
                        .setMessage(R.string.delete_course_confirm_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                courseDatabase.removeValue();
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            case R.id.add_lesson:
                intent = new Intent(this, AddEditCourseActivity.class);
                startActivityForResult(intent, -1);
                return true;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }
}
