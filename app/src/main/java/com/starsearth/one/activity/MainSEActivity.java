package com.starsearth.one.activity;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.AssetManager;
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
import com.starsearth.one.activity.profile.PhoneNumberActivity;
import com.starsearth.one.activity.welcome.WelcomeOneActivity;
import com.starsearth.one.adapter.MainSEAdapter;
import com.starsearth.one.adapter.TopMenuAdapter;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.domain.Assistant;
import com.starsearth.one.domain.Game;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.MoreOptionsMenuItem;
import com.starsearth.one.fragments.MainMenuItemFragment;
import com.starsearth.one.fragments.MoreOptionsMenuItemFragment;
import com.starsearth.one.fragments.dummy.DummyContent;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MainSEActivity extends AppCompatActivity implements MainMenuItemFragment.OnListFragmentInteractionListener, MoreOptionsMenuItemFragment.OnListFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseAssistantReference;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAnalytics mFirebaseAnalytics;

    private List<Assistant> assistants = new ArrayList<>();

    protected LinearLayout llAction;
    protected TextView tvActionLine1;
    protected TextView tvActionLine2;

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

        //if (mAdapterTopMenu != null) mAdapterTopMenu.setSEAssistantStatus(assistantStatus);
        tvActionLine2.setText(assistantStatus);

        if (llAction != null) {
            llAction.setContentDescription(tvActionLine1.getText() + " " + tvActionLine2.getText());
        }
    }

    private ChildEventListener mAssistantChildListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Assistant assistant = dataSnapshot.getValue(Assistant.class);
            assistants.add(assistant);
            //mAdapterTopMenu.addAssistant(assistant);
            //mAdapterTopMenu.assistantStateChanged(assistant);
            // mAdapterTopMenu.removeOldAssistantRecord(mDatabaseAssistantReference);
            if (assistants.size() > 1) {
                //delete old entry from the db
                Assistant firstItem = assistants.get(0);
                mDatabaseAssistantReference.child(firstItem.uid).removeValue();
                assistants.remove(firstItem);
            }
            assistantStateChangeded(assistant);
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
        setContentView(R.layout.activity_main_se);
        isPhoneNumberVerified();

        MainMenuItemFragment mainMenuItemFragment = new MainMenuItemFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_main_menu, mainMenuItemFragment).commit();

        MoreOptionsMenuItemFragment moreOptionsMenuItemFragment = new MoreOptionsMenuItemFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_top_menu, moreOptionsMenuItemFragment).commit();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    ((StarsEarthApplication) getApplication()).setFirebaseUser(null);

                    //Redirecting to login screen
                    Intent newIntent = new Intent(MainSEActivity.this, WelcomeOneActivity.class);
                    startActivity(newIntent);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void setupAssistantListener(FirebaseUser currentUser) {
        //mDatabaseAssistantReference = FirebaseDatabase.getInstance().getReference("assistants");
        //mDatabaseAssistantReference.keepSynced(true);
        //Query query = mDatabaseAssistantReference.orderByChild("userId").equalTo(currentUser.getUid());
        //query.addChildEventListener(mAssistantChildListener);
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

    public void sendAnalytics(Game game) {
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, game.id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, game.title);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "list_item");
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    public void sendAnalytics(String selected) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selected);
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    @Override
    public void onListFragmentInteraction(@NotNull MainMenuItem item) {
        Game game = item.game;
        sendAnalytics(game);
        Intent intent = new Intent(this, GameResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("game", game);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteraction(@NotNull MoreOptionsMenuItem item) {
        sendAnalytics(item.getText1());
        Intent intent;
        String title = item.getText1();
        if (title != null && title.contains("Keyboard")) {
            intent = new Intent(this, KeyboardActivity.class);
            startActivity(intent);
        }
        else if (title != null && title.contains("Phone")) {
            intent = new Intent(this, PhoneNumberActivity.class);
            startActivity(intent);
        }
    }
}
