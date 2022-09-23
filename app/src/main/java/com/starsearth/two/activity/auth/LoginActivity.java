package com.starsearth.two.activity.auth;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.two.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //UI
    private ProgressBar mProgressBar;
    private EditText etUsername;
    private EditText etPassword;

    private void login() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if (username == null || username.length() < 1) {
            Toast.makeText(LoginActivity.this, R.string.email_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (password == null || password.length() < 1) {
            Toast.makeText(LoginActivity.this, R.string.password_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (username != null && password != null) {
            if (mProgressBar != null) mProgressBar.setVisibility(View.VISIBLE);
            Toast.makeText(LoginActivity.this, R.string.login_started, Toast.LENGTH_SHORT).show();
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if (authResult != null) {
                                Toast.makeText(LoginActivity.this, R.string.login_successful, Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, R.string.login_failed +e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
                    loginSuccessful();
                }
                else {
                    //user is signed out
                }
            }
        };

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        Button btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
                int available = availability.isGooglePlayServicesAvailable(getApplicationContext());
                if (available == ConnectionResult.SUCCESS) {
                    if (hasFocus) {
                        login();
                    }
                }
                else {
                    availability.showErrorDialogFragment(LoginActivity.this, available, 1);
                }
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
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

    private void loginSuccessful() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (etPassword != null) etPassword.setText("");
        }
    }
}
