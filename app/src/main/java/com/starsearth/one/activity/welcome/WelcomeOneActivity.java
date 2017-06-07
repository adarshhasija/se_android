package com.starsearth.one.activity.welcome;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.R;
import com.starsearth.one.activity.KeyboardActivity;
import com.starsearth.one.activity.LoginActivity;
import com.starsearth.one.activity.MainSEActivity;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.domain.User;

public class WelcomeOneActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_one);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(WelcomeOneActivity.this, MainSEActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        Button btnKeyboardTest = (Button) findViewById(R.id.btn_keyboard_test);
        Button btnAccount = (Button) findViewById(R.id.btn_account);
        Button btnNoAccount = (Button) findViewById(R.id.btn_no_account);
        btnKeyboardTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeOneActivity.this, KeyboardActivity.class);
                startActivity(intent);
            }
        });
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeOneActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btnNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeOneActivity.this, WelcomeTwoActivity.class);
                startActivity(intent);
            }
        });
    }
}
