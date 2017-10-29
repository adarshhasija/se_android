package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.activity.auth.AddEditPhoneNumberActivity;
import com.starsearth.one.activity.auth.LoginActivity;
import com.starsearth.one.activity.auth.SignupActivity;
import com.starsearth.one.activity.lists.CourseAdminUsersActivity;
import com.starsearth.one.activity.lists.CoursesListActivity;
import com.starsearth.one.activity.welcome.WelcomeOneActivity;
import com.starsearth.one.adapter.MainSEAdapter;
import com.starsearth.one.application.StarsEarthApplication;

import java.util.ArrayList;
import java.util.Arrays;

public class MainSEActivity extends AppCompatActivity {

    public String ANALYTICS_MAINSE_LOGIN = "mainse_login";
    public String ANALYTICS_MAINSE_SIGNUP = "mainse_signup";
    public String ANALYTICS_CONVERT = "mainse_convert_full_account";
    public String ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_IN = "mainse_view_courses_loggin_in";
    public String ANALYTICS_MAINSE_TYPING_TEST_LOGGED_IN = "mainse_typing_test_loggin_in";
    public String ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_OUT = "mainse_view_courses_logged_out";
    public String ANALYTICS_MAINSE_LOGOUT = "mainse_logout";
    public String ANALYTICS_MAINSE_EMAIL = "mainse_email";
    public String ANALYTICS_MAINSE_PHONE_NUMBER = "mainse_phone_number";
    public String ANALYTICS_MAINSE_CHANGE_PASSWORD = "mainse_change_password";
    public String ANALYTICS_MAINSE_ADMIN_MODE = "mainse_admin_mode";
    public String ANALYTICS_KEYBOARD_TEST = "mainse_keyboard_test";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAnalytics mFirebaseAnalytics;
    private MainSEAdapter mAdapter;

    protected LinearLayout llAction;
    protected TextView tvActionLine1;
    protected TextView tvActionLine2;
    protected TextView tvListViewHeader;
    protected ListView listView;
    protected ProgressBar progressBar;


    private void sendAnalytics(String selected) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected);
        //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_se);

        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        llAction = (LinearLayout) findViewById(R.id.ll_action);
        llAction.setVisibility(View.GONE);
        tvActionLine1 = (TextView) findViewById(R.id.tv_action_line_1);
        tvActionLine2 = (TextView) findViewById(R.id.tv_action_line_2);
        tvListViewHeader = (TextView) findViewById(R.id.tv_listview_header);
        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        ArrayList<String> mainList = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.se_main_list)));
        mainList.addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_account_phone_number_list)));
        //mainList.addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_account_email_list)));
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
                else if (selected.contains("Signup")) {
                    sendAnalytics(ANALYTICS_MAINSE_SIGNUP);
                    intent = new Intent(MainSEActivity.this, SignupActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("Logout")) {
                    sendAnalytics(ANALYTICS_MAINSE_LOGOUT);
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(MainSEActivity.this, R.string.logout_successful, Toast.LENGTH_SHORT).show();
                }
                else if (selected.contains("Admin Access")) {
                    intent = new Intent(MainSEActivity.this, CourseAdminUsersActivity.class);
                    startActivity(intent);
                }
                else if(selected.contains("Courses Admin")) {
                    sendAnalytics(ANALYTICS_MAINSE_ADMIN_MODE);
                    intent = new Intent(MainSEActivity.this, AdminModeActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("email")) {
                    sendAnalytics(ANALYTICS_MAINSE_EMAIL);
                    final AlertDialog.Builder alert = new AlertDialog.Builder(MainSEActivity.this);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    alert.setTitle(getString(R.string.email));
                    alert.setMessage(currentUser.getEmail());
                    alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
                else if (selected.contains("Phone Number")) {
                    sendAnalytics(ANALYTICS_MAINSE_PHONE_NUMBER);
                    intent = new Intent(MainSEActivity.this, AddEditPhoneNumberActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("Change Password")) {
                    sendAnalytics(ANALYTICS_MAINSE_CHANGE_PASSWORD);
                    intent = new Intent(MainSEActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);
                }
                else if (selected.contains("Keyboard Test")) {
                    sendAnalytics(ANALYTICS_KEYBOARD_TEST);
                    intent = new Intent(MainSEActivity.this, KeyboardActivity.class);
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
                else if (selected.contains("Typing Game")) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        sendAnalytics(ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_IN);
                    }
                    intent = new Intent(MainSEActivity.this, TypingTestResultActivity.class);
                    startActivity(intent);
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                String number = user.getPhoneNumber();
                String username = user.getDisplayName();
                String email = user.getEmail();
                if (user == null) {
                    ((StarsEarthApplication) getApplication()).setFirebaseUser(null);

                    //Redirecting to login scren
                    Intent newIntent = new Intent(MainSEActivity.this, WelcomeOneActivity.class);
                    startActivity(newIntent);
                    finish();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_se, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }
}
