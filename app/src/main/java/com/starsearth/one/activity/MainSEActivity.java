package com.starsearth.one.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.starsearth.one.R;
import com.starsearth.one.activity.profile.PhoneNumberActivity;
import com.starsearth.one.activity.welcome.WelcomeOneActivity;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.domain.Task;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.MoreOptionsMenuItem;
import com.starsearth.one.fragments.MainMenuItemFragment;
import com.starsearth.one.fragments.MoreOptionsMenuItemFragment;

import org.jetbrains.annotations.NotNull;

public class MainSEActivity extends AppCompatActivity implements MainMenuItemFragment.OnMainMenuFragmentInteractionListener, MoreOptionsMenuItemFragment.OnMoreOptionsListFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseAssistantReference;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_se);
        isPhoneNumberVerified();

        //MainMenuItemFragment mainMenuItemFragment = new MainMenuItemFragment();
        //getSupportFragmentManager().beginTransaction()
        //        .add(R.id.fragment_container_main_menu, mainMenuItemFragment).commit();

        MoreOptionsMenuItemFragment moreOptionsMenuItemFragment = new MoreOptionsMenuItemFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_main_menu, moreOptionsMenuItemFragment).commit();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    ((StarsEarthApplication) getApplication()).setUser(null);

                    //Redirecting to login screen
                    Intent newIntent = new Intent(MainSEActivity.this, WelcomeOneActivity.class);
                    startActivity(newIntent);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
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

    public void sendAnalytics(Task task) {
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, task.id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.title);
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
    public void onMainMenuListFragmentInteraction(@NotNull MainMenuItem item) {
        Task task = (Task) item.teachingContent;
        sendAnalytics(task);
        Intent intent = new Intent(this, TaskDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", task);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onMoreOptionsListFragmentInteraction(@NotNull MoreOptionsMenuItem item) {
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

    @Override
    public void setListFragmentProgressBarVisibility(int visibility, RecyclerView view) {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            findViewById(R.id.fragment_container_main_menu).setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        } else {
            findViewById(R.id.fragment_container_main_menu).setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }

        if (visibility == View.VISIBLE) {
            progressBar.announceForAccessibility(getString(R.string.loading) + " " + getString(R.string.please_wait));
        }
        else {
            progressBar.announceForAccessibility(getString(R.string.loading_complete));
        }
    }
}
