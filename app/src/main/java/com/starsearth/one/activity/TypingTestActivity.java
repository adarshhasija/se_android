package com.starsearth.one.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TypingTestActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private int index=0;
    //List<String> sentencesList;
    private int charactersCorrect=0;
    private int wordsCorrect=0;
    private int totalCharactersAttempted=0;
    private int totalWordsFinished=0;
    private boolean wordIncorrect = false; //This is used to show that 1 mistake has been made when typing a word
    private String expectedAnswer;
    private long timeTakenMillis;

    private RelativeLayout rl;
    private TextView tvMain;
    private TextView mTimer;
    private CountDownTimer mCountDownTimer;

    private String subject = null;
    private int level;
    private String levelString = null;
    private int gameId;
    private ArrayList<String> content = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typing_test);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            subject = extras.getString("subject");
            level = extras.getInt("level");
            levelString = extras.getString("levelString");
            gameId = extras.getInt("game_id");
            content = extras.getStringArrayList("content");
        }

        //sentencesList = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.typing_test_sentences)));
        rl = (RelativeLayout) findViewById(R.id.rl);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expectedAnswer != null) {
                    //On screen tap, announce the next expected character
                    rl.announceForAccessibility(String.valueOf(expectedAnswer.charAt(index)));
                }
            }
        });
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

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            nextSentence(); //One millis delay so user can see the result of last letter before sentence changes
                        }
                    },
                    100);


        }
        else {
            //announce next character for accessibility, index has been incremented
            char nextExpectedCharacter = expectedAnswer.charAt(index);
            if (nextExpectedCharacter == ' ') {
                tvMain.announceForAccessibility(getString(R.string.space));
            }
            else if (nextExpectedCharacter == '.') {
                tvMain.announceForAccessibility(getString(R.string.full_stop));
            }
            else {
                tvMain.announceForAccessibility(String.valueOf(nextExpectedCharacter));
            }

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
        Firebase firebase = new Firebase("results");
        firebase.writeNewResult(charactersCorrect, totalCharactersAttempted, wordsCorrect, totalWordsFinished, subject, level, levelString, gameId, timeTakenMillis);

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("words_correct", wordsCorrect);
        bundle.putInt("words_total_finished", totalWordsFinished);
        intent.putExtras(bundle);
        firebaseAnalyticsGameCompleted();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void firebaseAnalyticsGameCompleted() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "typing_game_complete");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle);
    }

    private void checkWordCorrect() {
        //You must also tap the spacebar after the word to get the word correct
        if (!wordIncorrect) {
            //if the word was not declared incorrect, increment the words correct count
            wordsCorrect++;
        }
        wordIncorrect = false; //reset the flag for the next word
    }

    private void wordComplete() {
        totalWordsFinished++;
    }

    private String generateRandomSentence() {
        int MAX_LENGTH = 3;
        int MIN_LENGTH = 2;
        StringBuilder randomStringBuilder = new StringBuilder();
        Random generator = new Random();
        int sentenceLength = generator.nextInt(MAX_LENGTH) + MIN_LENGTH;
        for (int i = 0; i < sentenceLength; i++) {
            if (i > 0) {
                //we do not want to put a space before the first word
                randomStringBuilder.append(" ");
            }
            randomStringBuilder.append(generateRandomWord());
        }

        return randomStringBuilder.toString();
    }

    public String generateRandomWord() {
        int MAX_WORD_LENGTH = 3;
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_WORD_LENGTH) + 3; //word length of 3 - 6
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            int randomInt = generator.nextInt(25) + 97; //range of lowercase letters is 25
            if (randomInt % 3 == 0) randomInt -= 32;  //Make it upper case if its modulo 3
            tempChar = (char) randomInt; //only lower case
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public String generateRandomNumber() {
        Random generator = new Random();
        return Integer.toString(generator.nextInt(999));
    }

    public String generateContent() {
        if (content.isEmpty()) {
            return null;
        }
        if (levelString.contains("1") && !content.isEmpty()) {
            return content.get(0);
        }

        Random random = new Random();
        int randomInt = random.nextInt(content.size());
        return content.get(randomInt);
    }

    /**
     * This function generates the next sentence to be displayed
     * Remove previous sentence from list so that we do not reuse it
     * If it is the last sentence in the list retain it, so that we can keep displaying it
     * Empty list not allowed
     */
    private void nextSentence() {
        index = 0; //reset the cursor to the start of the sentence
        String text = generateContent(); //generateRandomSentence();
        text = addFullStop(text);
        expectedAnswer = text;
        tvMain.setText(text);
        tvMain.announceForAccessibility(text.substring(0,1));
    }

    private String addFullStop(String text) {
        if (text.charAt(text.length() - 1) != '.') {
            //All strings must end in full stop
            text = text.concat(".");
        }
        return text;
    }

    private void testCancelled() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
