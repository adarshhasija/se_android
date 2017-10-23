package com.starsearth.one.activity.lists;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.starsearth.one.R;
import com.starsearth.one.adapter.AdminUsersAdapter;
import com.starsearth.one.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseAdminUsersActivity extends AppCompatActivity {

    private String REFERENCE = "users";

    private DatabaseReference mDatabase;
    private ArrayList<User> itemList;
    private AdminUsersAdapter adapter;


    private ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            User newUser = dataSnapshot.getValue(User.class);

            if (adapter != null) {
                adapter.add(newUser);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

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
        setContentView(R.layout.activity_course_admin_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.done, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        itemList = new ArrayList<>();
        adapter = new AdminUsersAdapter(getApplicationContext(), 0, itemList);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemList.get(position).course_admin = !itemList.get(position).course_admin;
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE);
        mDatabase.addChildEventListener(listener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_admin_users, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.done:
                mDatabase = FirebaseDatabase.getInstance().getReference();
                Map<String, Object> childUpdates = new HashMap<>();
                for (User user : itemList) {

                    childUpdates.put("/users/" + user.uid + "/course_admin", user.course_admin);
                }
                mDatabase.updateChildren(childUpdates);
                finish();
                break;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

}
