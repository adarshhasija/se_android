package com.starsearth.one.activity.welcome;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.R;
import com.starsearth.one.activity.KeyboardActivity;
import com.starsearth.one.activity.MainSEActivity;
import com.starsearth.one.activity.TabbedActivity;
import com.starsearth.one.activity.auth.AddEditPhoneNumberActivity;
import com.starsearth.one.activity.auth.LoginActivity;
import com.starsearth.one.application.StarsEarthApplication;

public class WelcomeOneActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
                redirectToMainMenu(null);
            }
        }
    };

    private EditText etUsername;
    private EditText etPassword;
    private ProgressBar mProgressBar;

    OnFailureListener authFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(WelcomeOneActivity.this, R.string.login_failed +e.getMessage(), Toast.LENGTH_SHORT).show();
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_one);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        //mAuth.addAuthStateListener(mAuthListener);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        etUsername = (EditText) findViewById(R.id.et_username);
        if (etUsername != null) etUsername.requestFocus();
        etPassword = (EditText) findViewById(R.id.et_password);
        Button btnLoginOne = (Button) findViewById(R.id.btn_login_one);
        Button btnLoginTwo = (Button) findViewById(R.id.btn_login_two);
        Button btnKeyboard = (Button) findViewById(R.id.btn_keyboard);
        btnLoginOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if (username == null || username.length() < 1) {
                    Toast.makeText(WelcomeOneActivity.this, R.string.email_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password == null || password.length() < 1) {
                    Toast.makeText(WelcomeOneActivity.this, R.string.password_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (username != null && password != null) {
                    if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(WelcomeOneActivity.this, R.string.login_started, Toast.LENGTH_SHORT).show();
                    mAuth.signInWithEmailAndPassword(username, password)
                            .addOnFailureListener(authFailureListener);
                }*/
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, (((Button) v).getText()).toString());
                StarsEarthApplication application = (StarsEarthApplication) getApplication();
                application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                
              Intent intent = new Intent(WelcomeOneActivity.this, AddEditPhoneNumberActivity.class);
              startActivityForResult(intent, 0);
            }
        });
        btnLoginTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, (((Button) v).getText()).toString());
                StarsEarthApplication application = (StarsEarthApplication) getApplication();
                application.logActionEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Intent intent = new Intent(WelcomeOneActivity.this, LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        if (BuildConfig.DEBUG) {
            btnLoginTwo.setVisibility(View.VISIBLE);
        }
        else {
            btnLoginTwo.setVisibility(View.GONE);
        }
        if (btnKeyboard != null) {
            btnKeyboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(WelcomeOneActivity.this, KeyboardActivity.class);
                    startActivity(intent);
                }
            });
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            redirectToMainMenu(null);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuth != null) {
            //mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("verifiedPhoneNumber", true);
            redirectToMainMenu(bundle);
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("verifiedEmailAddress", true);
            redirectToMainMenu(bundle);
        }
    }

    private void updateUserProperties() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            StarsEarthApplication application = (StarsEarthApplication) getApplication();
            application.updateUserAnalyticsInfo(currentUser.getUid());
        }

    }

    private void redirectToMainMenu(Bundle bundle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateUserProperties();
            }
        }).start();
        Intent intent = new Intent(WelcomeOneActivity.this, TabbedActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
        finish();
    }
}
