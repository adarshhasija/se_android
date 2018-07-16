package com.starsearth.one.activity.auth;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.User;

public class SignupActivity extends AppCompatActivity {

    private String USERS_REFERENCE = "users";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //UI
    private ProgressBar mProgressBar;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etPasswordRepeat;

    private OnFailureListener guestToFullUserFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            etUsername.announceForAccessibility(getResources().getString(R.string.signup_failed));
            Toast.makeText(SignupActivity.this, getResources().getString(R.string.signup_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
        }
    };

    private OnCompleteListener authCompleteListener = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            if (task.isSuccessful()) {
                AuthResult result = (AuthResult) task.getResult();
                FirebaseUser user = result.getUser();
                if (user != null) {
                    if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
                    Firebase firebase = new Firebase(USERS_REFERENCE);
                    firebase.writeNewUser(user.getUid(), false, user.getEmail());
                    finish();
                }
            }
            else {
                etUsername.announceForAccessibility(getResources().getString(R.string.signup_failed));
                Toast.makeText(SignupActivity.this, getResources().getString(R.string.signup_failed), Toast.LENGTH_SHORT).show();
                if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
            }
        }
    };

    private void signUp() {
        final String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String passwordRepeat = etPasswordRepeat.getText().toString();
        if (username == null || username.length() < 1) {
            Toast.makeText(SignupActivity.this, R.string.email_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password == null || password.length() < 1) {
            Toast.makeText(SignupActivity.this, R.string.new_password_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordRepeat == null || passwordRepeat.length() < 1) {
            Toast.makeText(SignupActivity.this, R.string.new_password_repeat_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(passwordRepeat)) {
            Toast.makeText(SignupActivity.this, R.string.new_password_match_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (username != null && password != null) {
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            Toast.makeText(SignupActivity.this, R.string.starting_signup_please_wait, Toast.LENGTH_SHORT).show();
            final User currentUser = ((StarsEarthApplication) getApplication()).getUser();
            if (currentUser != null && currentUser.isGuest) {
                AuthCredential credential = EmailAuthProvider.getCredential(username, password);
                mAuth.getCurrentUser().linkWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    currentUser.isGuest = false;
                                    currentUser.email = username;
                                    Firebase firebase = new Firebase("users");
                                    firebase.convertGuestUserToFullUser(currentUser.uid, currentUser);
                                    //ref.child("users").child(currentUser.uid).child("isGuest").setValue(null); //set in cloud
                                    ((StarsEarthApplication) getApplication()).setUser(currentUser); //set locally
                                }
                            }
                        })
                        .addOnFailureListener(guestToFullUserFailureListener);
            }
            else {
                mAuth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(authCompleteListener);
            }

        }
    }

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

            }
        };

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        etPasswordRepeat = (EditText) findViewById(R.id.et_password_repeat);
        Button btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    signUp();
                }
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth != null) {
            //mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            //mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
