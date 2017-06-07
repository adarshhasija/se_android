package com.starsearth.one.activity.welcome;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.activity.MainSEActivity;
import com.starsearth.one.activity.SignupActivity;
import com.starsearth.one.database.Firebase;

public class WelcomeTwoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mGuestAuthListener;
    private FirebaseAuth.AuthStateListener mSignupAuthListener;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_two);

        mAuth = FirebaseAuth.getInstance();
        mSignupAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If we return from the signup screen and we have a user
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    finish();
                }
            }
        };
        mGuestAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    progressBar.setVisibility(View.GONE);
                    Firebase firebase = new Firebase("users");
                    firebase.writeNewGuestUser(user.getUid());
                    finish();
                }
            }
        };

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        Button btnSignup = (Button) findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.removeAuthStateListener(mGuestAuthListener);
                mAuth.addAuthStateListener(mSignupAuthListener);
                Intent intent = new Intent(WelcomeTwoActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
        Button btnGuest = (Button) findViewById(R.id.btn_continue_as_guest);
        btnGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.announceForAccessibility(getResources().getString(R.string.please_wait));
                    mAuth.removeAuthStateListener(mSignupAuthListener);
                    mAuth.addAuthStateListener(mGuestAuthListener);
                    mAuth.signInAnonymously();
                }
            }
        });
    }
}
