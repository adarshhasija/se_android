package com.starsearth.two.activity.auth;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.two.R;

public class ChangePasswordActivity extends AppCompatActivity {

    //UI
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etNewPasswordRepeat;


    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String newPasswordRepeat = etNewPasswordRepeat.getText().toString();

              /*  if (currentPassword == null || currentPassword.length() < 1) {
                    Toast.makeText(ChangePasswordActivity.this, R.string.current_password_error, Toast.LENGTH_SHORT).show();
                    return;
                }   */
        if (newPassword == null || newPassword.length() < 1) {
            Toast.makeText(ChangePasswordActivity.this, R.string.new_password_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPasswordRepeat == null || newPasswordRepeat.length() < 1) {
            Toast.makeText(ChangePasswordActivity.this, R.string.new_password_repeat_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(newPasswordRepeat)) {
            Toast.makeText(ChangePasswordActivity.this, R.string.new_password_match_error, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Toast.makeText(ChangePasswordActivity.this, R.string.updating_password_please_wait, Toast.LENGTH_SHORT).show();
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
            user.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChangePasswordActivity.this, R.string.password_updated_successfully, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else {
                                Toast.makeText(ChangePasswordActivity.this, R.string.password_update_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(ChangePasswordActivity.this, R.string.no_user_found, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etCurrentPassword = (EditText) findViewById(R.id.et_current_password);
        etCurrentPassword.setVisibility(View.GONE);
        etNewPassword = (EditText) findViewById(R.id.et_new_password);
        etNewPassword.requestFocus();
        etNewPasswordRepeat = (EditText) findViewById(R.id.et_new_password_repeat);
        Button btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    changePassword();
                }
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }
}
