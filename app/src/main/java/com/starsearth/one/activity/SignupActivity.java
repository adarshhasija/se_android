package com.starsearth.one.activity;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;

public class SignupActivity extends AppCompatActivity {

    private String USERS_REFERENCE = "users";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //UI
    private ProgressBar mProgressBar;
    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setTitle(R.string.signup);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
                    Firebase firebase = new Firebase(USERS_REFERENCE);
                    firebase.writeNewUser(user.getUid(), false, user.getEmail());
                    //Intent intent = new Intent(SignupActivity.this, AdminModeActivity.class);
                    //startActivity(intent);
                    finish();
                }
                else {
                    //user is signed out
                }
            }
        };

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        Button btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if (username != null && password != null) {
                    if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(SignupActivity.this, "Starting signup please wait", Toast.LENGTH_SHORT).show();
                    mAuth.createUserWithEmailAndPassword(username, password)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignupActivity.this, "Signup failed: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                                    if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
