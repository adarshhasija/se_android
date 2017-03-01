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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.R;
import com.starsearth.one.adapter.CoursesAdapter;
import com.starsearth.one.adapter.LessonsAdapter;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Lesson;

import java.util.ArrayList;

public class LessonsListActivity extends AppCompatActivity {

    private String REFERENCE_PARENT = "/courses/";
    private String REFERENCE = "lessons";

    private Course course;
    private ArrayList<Lesson> lessonList;
    private DatabaseReference mParentDatabase;
    private DatabaseReference mDatabase; //Lessons list DB
    private LessonsAdapter adapter;

    //UI
    private TextView tvParentName;
    private TextView tvParentDescription;
    private ListView listView;
    private Button btnAddLesson;

    private ValueEventListener parentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            course = null;
            course = dataSnapshot.getValue(Course.class);
            String courseKey = dataSnapshot.getKey();

            if (course != null) {
                tvParentName.setText(course.getName());
                tvParentDescription.setText(course.getDescription());
            }
            else {
                //this means the parent was deleted from somewhere else
                //close the activity
                finish();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(),databaseError.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    };

    private ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Lesson newLesson = dataSnapshot.getValue(Lesson.class);
            String lessonKey = dataSnapshot.getKey();
            course.addLesson(lessonKey);
            mParentDatabase.setValue(course);

            if (adapter != null) {
                adapter.add(newLesson);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Lesson newLesson = dataSnapshot.getValue(Lesson.class);
            String lessonKey = dataSnapshot.getKey();

            if (adapter != null) {
                ArrayList<Lesson> list = adapter.getLessonList();
                for (int i = 0; i < list.size(); i++) {
                    Lesson lesson = list.get(0);
                    if (lesson.getUid().equals(lessonKey)) {
                        adapter.remove(lesson);
                        adapter.insert(newLesson, i);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Lesson removedLesson = dataSnapshot.getValue(Lesson.class);
            String lessonKey = dataSnapshot.getKey();
            course.removeLesson(lessonKey);
            mParentDatabase.setValue(course);

            if (adapter != null) {
                ArrayList<Lesson> list = adapter.getLessonList();
                for (int i = 0; i < list.size(); i++) {
                    Lesson lesson = list.get(0);
                    if (lesson.getUid().equals(lessonKey)) {
                        adapter.remove(lesson);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons_list);

        lessonList = new ArrayList<>();

        setTitle(R.string.course_details);
        Bundle bundle = getIntent().getExtras();
        course = bundle.getParcelable("course");

        tvParentName = (TextView) findViewById(R.id.tv_parent_name);
        tvParentName.setText(course.getName());
        tvParentDescription = (TextView) findViewById(R.id.tv_course_description);
        tvParentDescription.setText(course.getDescription());
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Lesson lesson = adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putParcelable("parent", lesson);
                Intent intent = new Intent(LessonsListActivity.this, TopicsListActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, position);
            }
        });
        btnAddLesson = (Button) findViewById(R.id.btn_add_lesson);
        btnAddLesson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LessonsListActivity.this, AddEditLessonActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("totalItems", lessonList.size());
                bundle.putString("parentId", course.getUid());
                intent.putExtras(bundle);
                startActivityForResult(intent, -1);
            }
        });
        listView.setEmptyView(btnAddLesson);

        mParentDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE_PARENT + course.getUid());

        mDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE);
        Query query = mDatabase.child(REFERENCE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Lesson lesson = snapshot.getValue(Lesson.class);
                    lessonList.add(lesson);
                }
                adapter = new LessonsAdapter(getApplicationContext(), 0, lessonList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mParentDatabase.addValueEventListener(parentListener);
        mDatabase.addChildEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mParentDatabase.removeEventListener(parentListener);
        mDatabase.removeEventListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_lessons_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case R.id.edit_course:
                intent = new Intent(LessonsListActivity.this, AddEditCourseActivity.class);
                bundle = new Bundle();
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
                                mParentDatabase.removeEventListener(parentListener);
                                mParentDatabase.removeValue();
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
                intent = new Intent(this, AddEditLessonActivity.class);
                bundle = new Bundle();
                bundle.putInt("totalItems", lessonList.size());
                bundle.putString("parentId", course.getUid());
                intent.putExtras(bundle);
                startActivityForResult(intent, -1);
                return true;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToAddEditLessonActivity() {

    }
}
