package com.starsearth.one.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.TypingTestResult;

public class TypingTestActivity extends AppCompatActivity {

    private String UID=null;
    private TypingTestResult testResult=null;

    private int index=0;
    private int correct=0;
    private String expectedAnswer;
    private long timeTakenMillis;

    private TextView mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typing_test);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("typing_test_results");
        Query query = mDatabase.orderByChild("userId").equalTo(currentUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                testResult = dataSnapshot.getValue(TypingTestResult.class);
                if (testResult != null) UID = testResult.uid;
                int i =0;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                int i = 0;
            }
        });

        String text = getResources().getString(R.string.first_prime_minister_of_india);
        final TextView tvMain = (TextView) findViewById(R.id.tv_main);
        tvMain.setText(text);
        expectedAnswer = text;


        mTimer = (TextView) findViewById(R.id.tv_timer);
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (mTimer != null) {
                    timeTakenMillis = 60000 - millisUntilFinished;
                    if (millisUntilFinished/1000 < 10) {
                        mTimer.setTextColor(Color.RED);
                        mTimer.setText((millisUntilFinished/1000)/60 + ":0" + millisUntilFinished / 1000);
                    }
                    else {
                        mTimer.setText((millisUntilFinished/1000)/60 + ":" + millisUntilFinished / 1000);
                    }
                }

            }

            public void onFinish() {
                testCompleted();
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        testCancelled();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT ||
                keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT ||
                    keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
            //allow Caps Lock, ignore
            return super.onKeyDown(keyCode, event);
        }
        if(keyCode == KeyEvent.KEYCODE_DEL) {
            //If backspace is pressed, signal error. This is not allowed
            beep();
            vibrate();
            return super.onKeyDown(keyCode, event);
        }
        final TextView tvMain = (TextView) findViewById(R.id.tv_main);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString str2= new SpannableString(tvMain.getText().toString());

        char inputCharacter = (char) event.getUnicodeChar();
        char expectedCharacter = expectedAnswer.charAt(index);

        if (inputCharacter == expectedCharacter) {
            correct++;
            str2.setSpan(new BackgroundColorSpan(Color.GREEN), index, index+1, 0);
        }
        else {
            str2.setSpan(new BackgroundColorSpan(Color.RED), index, index+1, 0);
        }
        builder.append(str2);
        tvMain.setText( builder, TextView.BufferType.SPANNABLE);
        if (index == expectedAnswer.length() -1 ) {
            testCompleted();
        }
        index++;

        return super.onKeyDown(keyCode, event);
    }

    private void beep() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 100 milliseconds
        v.vibrate(100);
    }

    private void testCompleted() {
        Firebase firebase = new Firebase("typing_test_results");
        firebase.writeNewTypingTestResult(correct, expectedAnswer.length(), timeTakenMillis);

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(TypingTestActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(TypingTestActivity.this);
        }

        builder
                .setMessage("Score: " + correct + "/" + expectedAnswer.length())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void testCancelled() {
        finish();
    }
}
