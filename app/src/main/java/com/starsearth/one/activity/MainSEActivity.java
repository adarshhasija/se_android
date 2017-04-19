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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
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

    public String ANALYTICS_MAINSE_LOGIN = "mainse_login";
    public String ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_IN = "mainse_view_courses_loggin_in";
    public String ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_OUT = "mainse_view_courses_logged_out";
    public String ANALYTICS_MAINSE_LOGOUT = "mainse_logout";
    public String ANALYTICS_MAINSE_CHANGE_PASSWORD = "mainse_change_password";
    public String ANALYTICS_MAINSE_ADMIN_MODE = "mainse_admin_mode";

    private enum State {
        LOGGED_IN, LOGGED_OUT;
    }

    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private MainSEAdapter mAdapter;
    private State mCurrentState = State.LOGGED_OUT;

    protected LinearLayout llAction;
    protected TextView tvActionLine1;
    protected TextView tvActionLine2;
    protected TextView tvListViewHeader;
    protected ListView listView;
    protected ProgressBar progressBar;

    private void addToListOnUserSignIn(User user) {
        mAdapter.getObjectList().clear();
        mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_main_list)));
        if (user != null) {
            if (user.email.contains("hasijaadarsh")) {
                //mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_god_mode)));
            }
            if (user.admin) {
                mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_admin_list)));
            }
            mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_account_list)));
            mAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            listView.announceForAccessibility(getResources().getString(R.string.view_courses_selected));
            changeState(State.LOGGED_IN);
        }
        progressBar.setVisibility(View.GONE);
    }


    private void addToListOnUserSignOut() {
        mAdapter.getObjectList().clear();
        mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_main_list)));
        mAdapter.getObjectList().addAll(Arrays.asList(getResources().getStringArray(R.array.se_login_list)));
        mAdapter.notifyDataSetChanged();
        listView.setSelection(0);
        listView.announceForAccessibility(getResources().getString(R.string.view_courses_selected));
        changeState(State.LOGGED_OUT);
        progressBar.setVisibility(View.GONE);
    }

    private void changeState(State state) {
        mCurrentState = state;
    }

    private void sendAnalytics(String selected) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_se);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        llAction = (LinearLayout) findViewById(R.id.ll_action);
        llAction.setVisibility(View.GONE);
        tvActionLine1 = (TextView) findViewById(R.id.tv_action_line_1);
        tvActionLine2 = (TextView) findViewById(R.id.tv_action_line_2);
        tvListViewHeader = (TextView) findViewById(R.id.tv_listview_header);
        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        ArrayList<String> mainList = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.se_main_list)));
        mAdapter = new MainSEAdapter(MainSEActivity.this, 0, mainList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = mAdapter.getItem(position);
                Intent intent;
                if (selected.contains("Login")) {
                    sendAnalytics(ANALYTICS_MAINSE_LOGIN);
                    intent = new Intent(MainSEActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("Logout")) {
                    sendAnalytics(ANALYTICS_MAINSE_LOGOUT);
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(MainSEActivity.this, R.string.logout_successful, Toast.LENGTH_SHORT).show();
                }

                else if (selected.contains("Admin Access")) {
                    intent = new Intent(MainSEActivity.this, AdminUsersActivity.class);
                    startActivity(intent);
                }
                else if(selected.contains("Admin Mode")) {
                    sendAnalytics(ANALYTICS_MAINSE_ADMIN_MODE);
                    intent = new Intent(MainSEActivity.this, AdminModeActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("Change Password")) {
                    sendAnalytics(ANALYTICS_MAINSE_CHANGE_PASSWORD);
                    intent = new Intent(MainSEActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("View Courses")) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        sendAnalytics(ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_IN);
                    }
                    else {
                        sendAnalytics(ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_OUT);
                    }
                    intent = new Intent(MainSEActivity.this, CoursesListActivity.class);
                    startActivity(intent);
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                progressBar.setVisibility(View.VISIBLE);
                listView.announceForAccessibility(getResources().getString(R.string.please_wait));

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/"+user.getUid());
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User userDetails = dataSnapshot.getValue(User.class);
                            addToListOnUserSignIn(userDetails);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            addToListOnUserSignOut();
                        }
                    });
                }
                else {
                    addToListOnUserSignOut();
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
