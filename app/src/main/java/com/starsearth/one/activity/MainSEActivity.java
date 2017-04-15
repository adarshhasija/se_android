package com.starsearth.one.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.R;
import com.starsearth.one.activity.lists.AdminUsersActivity;
import com.starsearth.one.activity.lists.CoursesListActivity;
import com.starsearth.one.adapter.MainSEAdapter;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.User;

import java.util.ArrayList;
import java.util.Arrays;

public class MainSEActivity extends AppCompatActivity {

    private enum State {
        LOGGED_IN, LOGGED_OUT;
    }

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private MainSEAdapter mAdapter;
    private State mCurrentState = State.LOGGED_OUT;

    protected LinearLayout llAction;
    protected TextView tvActionLine1;
    protected TextView tvActionLine2;
    protected TextView tvListViewHeader;
    protected ListView listView;

    private void changeListOnLoginStatus(User user) {
        mAdapter.getObjectList().clear();
        if (user != null) {
            mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_main_list)));
            if (user.email.contains("hasijaadarsh")) {
                //mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_god_mode)));
            }
            if (user.admin) {
                mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_admin_list)));
            }
            mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_account_list)));
            mAdapter.notifyDataSetChanged();
            changeState(State.LOGGED_IN);
        }
        else {
            //user is signed out
            changeListOnUserSignOut();
        }
    }

    private void changeListOnUserSignOut() {
        mAdapter.getObjectList().clear();
        mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_main_list)));
        mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_login_list)));
        mAdapter.notifyDataSetChanged();
        changeState(State.LOGGED_OUT);
    }

    private void changeState(State state) {
        mCurrentState = state;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_se);

        llAction = (LinearLayout) findViewById(R.id.ll_action);
        llAction.setVisibility(View.GONE);
        tvActionLine1 = (TextView) findViewById(R.id.tv_action_line_1);
        tvActionLine2 = (TextView) findViewById(R.id.tv_action_line_2);
        tvListViewHeader = (TextView) findViewById(R.id.tv_listview_header);
        listView = (ListView) findViewById(R.id.listView);

        ArrayList<String> mainList = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.se_main_list)));
        mainList.addAll(Arrays.asList(getResources().getStringArray(R.array.se_login_list)));
        mAdapter = new MainSEAdapter(MainSEActivity.this, 0, mainList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = mAdapter.getItem(position);
                Intent intent;
                if (selected.contains("Login")) {
                    intent = new Intent(MainSEActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("Logout")) {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(MainSEActivity.this, R.string.logout_successful, Toast.LENGTH_SHORT).show();
                }

                else if (selected.contains("Admin Access")) {
                    intent = new Intent(MainSEActivity.this, AdminUsersActivity.class);
                    startActivity(intent);
                }
                else if(selected.contains("Admin Mode")) {
                    intent = new Intent(MainSEActivity.this, AdminModeActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("Change Password")) {
                    intent = new Intent(MainSEActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("View Courses")) {
                    intent = new Intent(MainSEActivity.this, CoursesListActivity.class);
                    startActivity(intent);
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //checkUser(firebaseAuth);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/"+user.getUid());
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User userDetails = dataSnapshot.getValue(User.class);
                            changeListOnLoginStatus(userDetails);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            changeListOnUserSignOut();
                        }
                    });
                }
                else {
                    changeListOnUserSignOut();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
