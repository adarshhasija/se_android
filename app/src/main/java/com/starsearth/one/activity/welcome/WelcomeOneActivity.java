package com.starsearth.one.activity.welcome;

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
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(WelcomeOneActivity.this, MainSEActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        etUsername = (EditText) findViewById(R.id.et_username);
        etUsername.requestFocus();
        etPassword = (EditText) findViewById(R.id.et_password);
        Button btnLogin = (Button) findViewById(R.id.btn_login);
        Button btnNoAccount = (Button) findViewById(R.id.btn_no_account);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
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
                }
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
