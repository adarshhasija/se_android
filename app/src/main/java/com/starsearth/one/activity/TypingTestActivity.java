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
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TypingTestActivity extends AppCompatActivity {

    private int index=0;
    private int randomNumber;
    List<String> sentencesList;
    private int charactersCorrect=0;
    private int wordsCorrect=0;
    private int totalCharactersAttempted=0;
    private int totalWordsAttempted=0;
    private boolean wordIncorrect = false; //This is used to show that 1 mistake has been made when typing a word
    private String expectedAnswer;
    private long timeTakenMillis;

    private TextView tvMain;
    private TextView mTimer;
    private CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typing_test);


        sentencesList = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.typing_test_sentences)));
        tvMain = (TextView) findViewById(R.id.tv_main);
        nextSentence();


        mTimer = (TextView) findViewById(R.id.tv_timer);
        mCountDownTimer = new CountDownTimer(61000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (mTimer != null) {
                    timeTakenMillis = 61000 - millisUntilFinished;
                    if (millisUntilFinished/1000 < 11) {
                        mTimer.setTextColor(Color.RED);
                    }
                    if (millisUntilFinished/1000 < 10) {
                        mTimer.setText((millisUntilFinished/1000)/60 + ":0" + millisUntilFinished / 1000);
                    }
                    else {
                        int mins = (int) (millisUntilFinished/1000)/60;
                        int seconds = (int) (millisUntilFinished/1000) % 60;
                        mTimer.setText(mins + ":" + ((seconds == 0)? "00" : seconds)); //If seconds are 0, print double 0, else print seconds
                    }
                }

            }

            public void onFinish() {
                timeTakenMillis = timeTakenMillis + 1000; //take the last second into consideration
                testCompleted();
            }
        };
        mCountDownTimer.start();
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

        totalCharactersAttempted++;
        if (inputCharacter == expectedCharacter) {
            charactersCorrect++;
            str2.setSpan(new BackgroundColorSpan(Color.GREEN), index, index+1, 0);
        }
        else {
            wordIncorrect = true;
            str2.setSpan(new BackgroundColorSpan(Color.RED), index, index+1, 0);
        }
        builder.append(str2);
        tvMain.setText( builder, TextView.BufferType.SPANNABLE);

        if (expectedCharacter == ' ') {
            checkWordCorrect();
            wordComplete(); //on spacebar, we have completed a word
        }
        index++;
        if (index == expectedAnswer.length()) {
            checkWordCorrect();
            wordComplete(); //on end of sentence we have also completed a word
            removeCompletedSentence();
            nextSentence();
        }

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
        mCountDownTimer.cancel();
        Firebase firebase = new Firebase("typing_game_results");
        firebase.writeNewTypingTestResult(charactersCorrect, totalCharactersAttempted, wordsCorrect, totalWordsAttempted, timeTakenMillis);

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(TypingTestActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(TypingTestActivity.this);
        }

        builder
                .setMessage(String.format(getString(R.string.your_score), wordsCorrect, totalWordsAttempted))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                //builder.show();

        finish();
    }

    private void checkWordCorrect() {
        if (!wordIncorrect) {
            //if the word was not declared incorrect, increment the words correct count
            wordsCorrect++;
        }
        wordIncorrect = false; //reset the flag for the next word
    }

    private void wordComplete() {
        totalWordsAttempted++;
    }

    /**
     * This function generates the next sentence to be displayed
     * Remove previous sentence from list so that we do not reuse it
     * If it is the last sentence in the list retain it, so that we can keep displaying it
     * Empty list not allowed
     */
    private void nextSentence() {
        index = 0; //reset the cursor to the start of the sentence
        randomNumber = (new Random()).nextInt(sentencesList.size());
        String text = sentencesList.get(randomNumber);
        expectedAnswer = text;
        tvMain.setText(text);
    }

    private void removeCompletedSentence() {
        if (sentencesList.size() > 1) {
            sentencesList.remove(sentencesList.get(randomNumber));
        }
    }

    private void testCancelled() {
        finish();
    }
}
