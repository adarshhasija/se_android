package com.starsearth.one.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.starsearth.one.R;
import com.starsearth.one.activity.welcome.WelcomeOneActivity;
import com.starsearth.one.adapter.MainSEAdapter;
import com.starsearth.one.adapter.TopMenuAdapter;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.domain.Assistant;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.TypingGame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private DatabaseReference mDatabaseUserReference;
    private DatabaseReference mDatabaseAssistantReference;
    private DatabaseReference mDatabaseResultsReference;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAnalytics mFirebaseAnalytics;
    private MainSEAdapter mAdapter;
    private TopMenuAdapter mAdapterTopMenu;

    private List<Assistant> assistants = new ArrayList<>();

    protected LinearLayout llAction;
    protected TextView tvActionLine1;
    protected TextView tvActionLine2;
    protected TextView tvListViewHeader;
    protected RecyclerView mRecyclerViewTopMenu;
    protected RecyclerView mRecyclerView;
    protected ProgressBar progressBar;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.LayoutManager mLayoutManagerHorizontal;


    public void sendAnalytics(String selected) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void assistantStateChangeded(Assistant mAssistant) {
        if (mAssistant == null) {
            return;
        }

        String assistantStatus = null;
        if (mAssistant.state > 9 && mAssistant.state < 13) {
            assistantStatus = getString(R.string.se_assistant_tap_here_to_continue);
        }
        else if (mAssistant.state == Assistant.State.KEYBOARD_TEST_COMPLETED_SUCCESS.getValue() ||
                    mAssistant.state == Assistant.State.KEYBOARD_TEST_COMPLETED_FAIL.getValue()) {
            assistantStatus = getString(R.string.se_assistant_keyboard_test_completed);
        }
        else {
            assistantStatus = getString(R.string.se_assistant_no_update);
        }

        if (mAdapterTopMenu != null) mAdapterTopMenu.setSEAssistantStatus(assistantStatus);
        //tvActionLine2.setText(assistantStatus);

        if (llAction != null) {
            llAction.setContentDescription(tvActionLine1.getText() + " " + tvActionLine2.getText());
        }
    }

    private ChildEventListener mAssistantChildListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Assistant assistant = dataSnapshot.getValue(Assistant.class);
            assistants.add(assistant); //add at end
            if (assistants.size() > 1) {
                Assistant firstItem = assistants.get(0);
                mDatabaseAssistantReference.child(firstItem.uid).removeValue(); //delete from the database
                assistants.remove(firstItem);
            }
            assistantStateChangeded(assistant);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Assistant assistant = dataSnapshot.getValue(Assistant.class);
            if (assistant != null) {
                mDatabaseAssistantReference.child(assistant.uid).removeValue(); //delete from the database
                assistants.remove(assistant);
            }

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mResultsChildListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Result result = dataSnapshot.getValue(Result.class);
            if (result != null) {
                MainMenuItem mainMenuItem = new MainMenuItem();
                mainMenuItem.subject = result.subject;
                mainMenuItem.levelString = result.level_string;
                mainMenuItem.gameId = TypingGame.Id.fromInt(result.game_id);
                mainMenuItem.lastTriedMillis = result.timestamp;

                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    MainMenuItem menuItem = mAdapter.getObjectList().get(i);
                    /*if (data.subject != null &&
                            data.subject.equalsIgnoreCase(result.subject) &&
                            data.levelString != null &&
                            data.levelString.equalsIgnoreCase(result.level_string)) {   */
                    if (menuItem.gameId != null && menuItem.gameId.getValue() == result.game_id) {
                        mAdapter.removeAt(i);
                        mAdapter.addItem(mainMenuItem);
                        mRecyclerView.getLayoutManager().scrollToPosition(0);
                    }
                }
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

    private void setupTopMenu() {
        mRecyclerViewTopMenu = (RecyclerView) findViewById(R.id.rv_top_menu);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerViewTopMenu.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManagerHorizontal = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewTopMenu.setLayoutManager(mLayoutManagerHorizontal);
    }

    private void setupMainList() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_se);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        isPhoneNumberVerified();

        llAction = (LinearLayout) findViewById(R.id.ll_action);
        llAction.setVisibility(View.GONE);
        tvActionLine1 = (TextView) findViewById(R.id.tv_action_line_1);
        tvActionLine2 = (TextView) findViewById(R.id.tv_action_line_2);
        tvListViewHeader = (TextView) findViewById(R.id.tv_listview_header);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        setupTopMenu();
        setupMainList();
        mAdapterTopMenu = new TopMenuAdapter(MainSEActivity.this);
        mRecyclerViewTopMenu.setAdapter(mAdapterTopMenu);


        llAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAnalytics("assistant");
                Intent intent = new Intent(MainSEActivity.this, AssistantActivity.class);
                Bundle bundle = new Bundle();
                if (!assistants.isEmpty()) {
                    bundle.putParcelable("assistant", assistants.get(0));
                }
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        ArrayList<String> mainList = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.se_keyboard_test_list)));
        mainList.addAll(Arrays.asList(getResources().getStringArray(R.array.se_main_list_practice)));
        mainList.addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_account_list)));
        mainList.addAll(Arrays.asList(getResources().getStringArray(R.array.se_user_account_email_list)));
        for (String s : mainList) {
            String[] tmp = s.split("-");
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = tmp[i].trim();
            }
            MainMenuItem mainMenuItem = new MainMenuItem();
            if (tmp.length == 1) {
                //other item
                mainMenuItem.other = tmp[0];
            }
            else {
                mainMenuItem.subject = tmp[0];
                mainMenuItem.levelString = tmp[1];
                if (mainMenuItem.subject.equalsIgnoreCase("typing")) {
                    mainMenuItem.gameId = TypingGame.assignType(mainMenuItem.levelString);
                }
            }
            mainMenuItems.add(mainMenuItem);
        }
        mAdapter = new MainSEAdapter(MainSEActivity.this, 0, mainMenuItems);
        mRecyclerView.setAdapter(mAdapter);
      /*  recycleView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                    intent = new Intent(MainSEActivity.this, PhoneNumberActivity.class);
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
                else if (selected.contains("1 Word")) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        //sendAnalytics(ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_IN);
                    }
                    intent = new Intent(MainSEActivity.this, GameResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("subject", "typing");
                    //bundle.putInt("level", 1);
                    bundle.putString("levelString", "1 word");
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else if (selected.contains("Many Words")) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        //sendAnalytics(ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_IN);
                    }
                    intent = new Intent(MainSEActivity.this, GameResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("subject", "typing");
                    //bundle.putInt("level", 2);
                    bundle.putString("levelString", "many words");
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else if (selected.contains("1 Sentence")) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        //sendAnalytics(ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_IN);
                    }
                    intent = new Intent(MainSEActivity.this, GameResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("subject", "typing");
                    //bundle.putInt("level", 3);
                    bundle.putString("levelString", "1 sentence");
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else if (selected.contains("Many Sentences")) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        //sendAnalytics(ANALYTICS_MAINSE_VIEW_COURSES_LOGGED_IN);
                    }
                    intent = new Intent(MainSEActivity.this, GameResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("subject", "typing");
                    //bundle.putInt("level", 4);
                    bundle.putString("levelString", "many sentences");
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        }); */

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            setupAssistantListener(currentUser);
            setupResultsListener(currentUser);

        }
    }

    private void setupAssistantListener(FirebaseUser currentUser) {
        mDatabaseAssistantReference = FirebaseDatabase.getInstance().getReference("assistants");
        //mDatabaseAssistantReference.keepSynced(true);
        Query query = mDatabaseAssistantReference.orderByChild("userId").equalTo(currentUser.getUid());
        query.addChildEventListener(mAssistantChildListener);
    }

    private void setupResultsListener(FirebaseUser currentUser) {
        mDatabaseResultsReference = FirebaseDatabase.getInstance().getReference("results");
        mDatabaseResultsReference.keepSynced(true);
        Query query = mDatabaseResultsReference.orderByChild("userId").equalTo(currentUser.getUid());
        query.addChildEventListener(mResultsChildListener);
    }

    /**
     * Notify the user that their login via phone number verification was successful
     */
    private void isPhoneNumberVerified() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("verifiedPhoneNumber")) {
                Toast.makeText(getApplicationContext(), R.string.phone_number_verified, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (mAssistantChildListener != null) {
            mDatabaseAssistantReference.removeEventListener(mAssistantChildListener);
        }
        if (mResultsChildListener != null) {
            mDatabaseResultsReference.removeEventListener(mResultsChildListener);
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
