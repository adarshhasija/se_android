package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

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
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Course;

import java.util.ArrayList;
import java.util.Map;

public class CoursesListActivity extends AppCompatActivity {

    private String REFERENCE = "courses";

    private ListView listView;

    private CoursesAdapter adapter;
    private DatabaseReference mDatabase;
    private ArrayList<Course> courseList;

    private ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Course newCourse = dataSnapshot.getValue(Course.class);
            String courseKey = dataSnapshot.getKey();

            if (adapter != null) {
                adapter.add(newCourse);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Course newCourse = dataSnapshot.getValue(Course.class);
            String courseKey = dataSnapshot.getKey();

            if (adapter != null) {
                ArrayList<Course> list = adapter.getCourseList();
                for (int i = 0; i < list.size(); i++) {
                    Course course = list.get(0);
                    if (course.getUid().equals(courseKey)) {
                        adapter.remove(course);
                        adapter.insert(newCourse, i);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Course removedCourse = dataSnapshot.getValue(Course.class);
            String courseKey = dataSnapshot.getKey();

            if (adapter != null) {
                ArrayList<Course> list = adapter.getCourseList();
                for (int i = 0; i < list.size(); i++) {
                    Course course = list.get(0);
                    if (course.getUid().equals(courseKey)) {
                        adapter.remove(course);
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

    private void deleteItem(final Course deleteCourse) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_course)
                .setMessage(R.string.delete_course_confirm_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase.child(deleteCourse.getUid()).removeValue();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_list);

        courseList = new ArrayList<>();

        setTitle(R.string.courses_list);
        listView = (ListView) findViewById(R.id.listView);
        registerForContextMenu(listView);
        Button button = (Button) findViewById(R.id.btn_add_course);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CoursesListActivity.this, AddEditCourseActivity.class);
                startActivityForResult(intent, -1);
            }
        });
        listView.setEmptyView(button);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course course = adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putParcelable("parent", course);
                Intent intent = new Intent(CoursesListActivity.this, LessonsListActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, position);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE);
        Query query = mDatabase.child(REFERENCE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    courseList.add(course);
                }
                adapter = new CoursesAdapter(getApplicationContext(), 0, courseList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.addChildEventListener(listener);

    }

    @Override
    protected void onDestroy() {
        super.onStop();
        mDatabase.removeEventListener(listener);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 0, 0, R.string.edit);
        menu.add(0, 1, 1, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case 0:
                intent = new Intent(this, AddEditCourseActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("course", courseList.get(index));
                intent.putExtras(bundle);
                startActivityForResult(intent, index);
                break;
            case 1:
                deleteItem(courseList.get(index));
                break;
            default: break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_courses_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_course:
                Intent intent = new Intent(this, AddEditCourseActivity.class);
                startActivityForResult(intent, -1);
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }
}
