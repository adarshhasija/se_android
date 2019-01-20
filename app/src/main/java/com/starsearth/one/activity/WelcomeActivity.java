package com.starsearth.one.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.R;
import com.starsearth.one.activity.auth.AddEditPhoneNumberActivity;
import com.starsearth.one.activity.auth.LoginActivity;
import com.starsearth.one.application.StarsEarthApplication;

public class WelcomeActivity extends AppCompatActivity {

    public static int LOGIN_WITH_PHONE_NUMBER_REQUEST = 0;
    public static int LOGIN_WITH_EMAIL_ADDRESS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_one);
        getSupportActionBar().hide();

        Button btnLoginPhone = (Button) findViewById(R.id.btn_login_phone);
        Button btnLoginEmail = (Button) findViewById(R.id.btn_login_email);
        Button btnKeyboard = (Button) findViewById(R.id.btn_keyboard);
        btnLoginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(WelcomeActivity.this, AddEditPhoneNumberActivity.class);
              startActivityForResult(intent, LOGIN_WITH_PHONE_NUMBER_REQUEST);
            }
        });
        btnLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_WITH_EMAIL_ADDRESS_REQUEST);
            }
        });
        if (BuildConfig.DEBUG) {
            btnLoginEmail.setVisibility(View.VISIBLE);
        }
        else {
            btnLoginEmail.setVisibility(View.GONE);
        }
        if (btnKeyboard != null) {
            btnKeyboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(WelcomeActivity.this, KeyboardActivity.class);
                    startActivity(intent);
                }
            });
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            redirectToMainMenu();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == LOGIN_WITH_PHONE_NUMBER_REQUEST) {
                Toast.makeText(getApplicationContext(), R.string.phone_number_verified, Toast.LENGTH_LONG).show();
            }
            else if (requestCode == LOGIN_WITH_EMAIL_ADDRESS_REQUEST) {
                Toast.makeText(getApplicationContext(), R.string.email_address_verified, Toast.LENGTH_LONG).show();
            }
            redirectToMainMenu();
        }

    }

    private void updateUserProperties() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            ((StarsEarthApplication) getApplication()).getAnalyticsManager().updateUserAnalyticsInfo(currentUser.getUid());
        }

    }

    private void redirectToMainMenu() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateUserProperties();
            }
        }).start();
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
