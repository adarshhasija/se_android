package com.starsearth.one.activity.tasks;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.starsearth.one.R;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.domain.Response;
import com.starsearth.one.domain.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class TaskActivity extends AppCompatActivity {

    public final String GESTURE_SWIPE                   = "GESTURE_SWIPE";
    public final String GESTURE_TAP                     = "GESTURE_TAP";
    public String QUESTION_SPELL_IGNORE_CASE            = "QUESTION_SPELL_IGNORE_CASE";
    public String QUESTION_TYPE_CHARACTER               = "QUESTION_TYPE_CHARACTER";

    //List<String> sentencesList;
    private long startTimeMillis;
    private long timeTakenMillis;
    private ArrayList<Response> responses = new ArrayList<>();

    //typing activity
    private int index=0;
    private long charactersCorrect=0;
    private long charactersTotalAttempted =0;
    private long wordsCorrect=0;
    private long wordsTotalFinished =0;
    private boolean wordIncorrect = false; //This is used to show that 1 mistake has been made when typing a word
    private String expectedAnswer; //for typing tasks

    //gesture activity
    private boolean expectedAnswerGesture;
    private long itemsAttempted =0;              //In TYPING, only used to see how many have been completed
    private long itemsCorrect =0;
    private boolean itemIncorrect = false;  //This is used to show that 1 mistake has been made when typing an item(character/word/sentence)
    private int gestureSpamItemCounter=0;

    private RelativeLayout rl;
    private TextView tvMain;
    private TextView mTimer;
    private TextView tvCompletedTotal;
    private TextView tvTapScreenToHearContent;

    private CountDownTimer mCountDownTimer;
    TextToSpeech tts;

    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_task);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            task = extras.getParcelable("task");
        }

        //sentencesList = new LinkedList<>(Arrays.asList(getResources().getStringArray(R.array.typing_test_sentences)));
        mTimer = (TextView) findViewById(R.id.tv_timer);
        rl = (RelativeLayout) findViewById(R.id.rl);
        tvCompletedTotal = (TextView) findViewById(R.id.tv_completed_total);
        tvTapScreenToHearContent = (TextView) findViewById(R.id.tv_tap_screen_to_hear_content);
      /*  rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expectedAnswer != null) {
                    //On screen tap, announce the next expected character
                    rl.announceForAccessibility(String.valueOf(expectedAnswer.charAt(index)));
                }
            }
        }); */

        tvMain = (TextView) findViewById(R.id.tv_main);
        nextItem();

        startTimeMillis = System.currentTimeMillis();
        if (task.timed) {
            mTimer.setVisibility(View.VISIBLE);
            setupTimer();
        }
        else {
            mTimer.setVisibility(View.GONE);
            if (task.content != null && task.content.length > 1) {
                tvCompletedTotal.setVisibility(View.VISIBLE);
                tvCompletedTotal.setText("1" + "/" + task.content.length);
                if (((StarsEarthApplication) getApplication()).getAccessibility().isTalkbackOn()) {
                    tvTapScreenToHearContent.setText(getResources().getString(R.string.double_tap_screen_to_hear_text_again));
                }
            }
        }

        if (!task.isTextVisibleOnStart) {
            tvTapScreenToHearContent.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        tts = new TextToSpeech(this, null);
        tts.setLanguage(Locale.US);

        //Show keyboard only if it is a typing task
        rl.requestFocus();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(rl, 0);
            }
        };
        if (task.isKeyboardRequired) {
            rl.postDelayed(runnable,200); //use 300 to make it run when coming back from lock screen
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }

    private void setupTimer() {
        mCountDownTimer = new CountDownTimer(task.durationMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                if (mTimer != null) {
                    timeTakenMillis = 61000 - millisUntilFinished;
                    if ((millisUntilFinished/1000) % 10 == 0) {
                        if (gestureSpamItemCounter > 30) {
                            taskCancelled("gesture spam");
                        }
                        gestureSpamItemCounter=0;
                    }

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
                if ((task.getType() == Task.Type.TYPING && charactersTotalAttempted == 0) ||
                        (task.getType() == Task.Type.TAP_SWIPE && itemsAttempted == 0)) {
                    taskCancelled("no attempt");
                }
                else {
                    taskCompleted();
                }
            }
        };
        mCountDownTimer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StarsEarthApplication application = (StarsEarthApplication) getApplication();
        application.logFragmentViewEvent(this.getClass().getSimpleName(), this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        taskCancelled("back pressed");
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
            if (task.isBackspaceAllowed) {
                CharSequence text = tvMain.getText().subSequence(0, tvMain.getText().length() - 1);
                tvMain.setText(text);
            }
            else {
                //Backspace not allowed, signal error.
                beep();
                vibrate();
            }

            return super.onKeyDown(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER && task.submitOnReturnTapped) {
            itemsAttempted++;
            tvCompletedTotal.setText((itemsAttempted + 1) + "/" + task.content.length);
            String userAnswer = tvMain.getText().toString();
            if (userAnswer.equalsIgnoreCase(expectedAnswer)) {
                flashRightAnswer();
                itemsCorrect++;
                responses.add(new Response(
                        QUESTION_SPELL_IGNORE_CASE,
                        expectedAnswer,
                        userAnswer,
                        true
                ));

                if (!task.isTextVisibleOnStart) {
                    //announce correct/not correct this as, in this mode, app does not say the next word
                    //user has to tap for the next wod
                    tvMain.announceForAccessibility(getString(R.string.correct));
                }

            }
            else {
                flashWrongAnswer();
                vibrate();
                responses.add(new Response(
                        QUESTION_SPELL_IGNORE_CASE,
                        expectedAnswer,
                        userAnswer,
                        false
                ));

                if (!task.isTextVisibleOnStart) {
                    tvMain.announceForAccessibility(getString(R.string.not_correct));
                }
            }

            if (!task.isTaskItemsCompleted(itemsAttempted)) {
                nextItem();
            }
            else {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                //One millis delay so user can see the result of last letter before finishing
                                taskCompleted();
                            }
                        },
                        100);
            }
            return super.onKeyDown(keyCode, event);
        }
        if (expectedAnswer == null) {
            //If there is no expected answer, we cannot proceed.
            //Likely a non-typing activity
            return super.onKeyDown(keyCode, event);
        }
        final TextView tvMain = (TextView) findViewById(R.id.tv_main);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString str2= new SpannableString(tvMain.getText().toString());

        char inputCharacter = (char) event.getUnicodeChar();

        charactersTotalAttempted++;
        if (!task.showUserAnswerWithBackground) {
            tvMain.setText(
                    tvMain.getText().toString() + inputCharacter
            );
        }
        else if (index < expectedAnswer.length()){
            char expectedCharacter = expectedAnswer.charAt(index);
            if (inputCharacter == expectedCharacter) {
                charactersCorrect++;
                str2.setSpan(new BackgroundColorSpan(Color.GREEN), index, index+1, 0);
                responses.add(new Response(
                        QUESTION_TYPE_CHARACTER,
                        Character.toString(expectedCharacter),
                        Character.toString(inputCharacter),
                        true
                ));
            }
            else {
                wordIncorrect = true;
                itemIncorrect = true;
                str2.setSpan(new BackgroundColorSpan(Color.RED), index, index+1, 0);
                responses.add(new Response(
                        QUESTION_TYPE_CHARACTER,
                        Character.toString(expectedCharacter),
                        Character.toString(inputCharacter),
                        false
                ));
            }
            builder.append(str2);
            tvMain.setText( builder, TextView.BufferType.SPANNABLE);

            if ((expectedCharacter == ' ' || index == (expectedAnswer.length() - 1))
                    && !task.submitOnReturnTapped) {
                //only consider this when submit on enter is not selected
                checkWordCorrect();
                wordsTotalFinished++; //on spacebar, or on end of string, we have completed a word
            }
        }



        index++;
        if (index == expectedAnswer.length() && !task.submitOnReturnTapped) {
            itemsAttempted++;
            checkItemCorrect();
            if (tvCompletedTotal.getVisibility() == View.VISIBLE) {
                tvCompletedTotal.setText((itemsAttempted + 1) + "/" + task.content.length);
            }
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            //One millis delay so user can see the result of last letter before sentence changes
                            if (task.timed || !task.isTaskItemsCompleted(wordsTotalFinished)) {
                                nextItem();
                            }
                            else {
                                taskCompleted();
                            }

                        }
                    },
                    100);


        }
        else if (task.isTextVisibleOnStart && index < expectedAnswer.length()){
            //announce next character for accessibility, index has been incremented
            //do it only if text is visible on start
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

    private float x1,x2,y1,y2;
    static final int MIN_DISTANCE = 150;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                float deltaX = x2 - x1;
                float deltaY = y2 - y1;
                if (Math.abs(deltaX) > MIN_DISTANCE || Math.abs(deltaY) > MIN_DISTANCE)
                {
                    if (task.type == Task.Type.TAP_SWIPE) {
                        //left -> right or top ->bottom
                        //swipe means false
                        itemsAttempted++;
                        gestureSpamItemCounter++;
                        if (!expectedAnswerGesture) {
                            flashRightAnswer();
                            itemsCorrect++;
                            responses.add(new Response(
                                    tvMain.getText().toString(),
                                    GESTURE_SWIPE,
                                    GESTURE_SWIPE,
                                    true
                            ));
                        }
                        else {
                            flashWrongAnswer();
                            vibrate();
                            responses.add(new Response(
                                    tvMain.getText().toString(),
                                    GESTURE_TAP,
                                    GESTURE_SWIPE,
                                    false
                            ));
                        }
                        nextItemGesture();
                    }
                }
                else
                {
                    // consider as something else - a screen tap for example
                    if (task.type == Task.Type.TYPING || task.type == Task.Type.SPELLING) {
                        if (expectedAnswer != null) {
                            //On screen tap, announce the next expected character
                            //If text is not visible to user, use normal TTS
                            //If text is visible, use only talkback
                            if (!task.isTextVisibleOnStart) {
                                AudioManager mgr = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                int volume = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
                                if (volume > 0) {
                                    tts.speak(expectedAnswer, TextToSpeech.QUEUE_ADD, null);
                                }
                                else {
                                    AlertDialog.Builder builder = ((StarsEarthApplication) getApplication()).createAlertDialog(this);
                                    builder.setTitle(getResources().getString(R.string.alert));
                                    builder.setMessage(getResources().getString(R.string.volume_is_mute));
                                    builder.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });
                                    builder.show();
                                }
                            }
                            else {
                                rl.announceForAccessibility(String.valueOf(expectedAnswer.charAt(index)));
                            }
                        }
                    }
                    else if (task.type == Task.Type.TAP_SWIPE) {
                        //tap
                        //tap means true
                        itemsAttempted++;
                        gestureSpamItemCounter++;
                        if (expectedAnswerGesture) {
                            flashRightAnswer();
                            itemsCorrect++;
                            responses.add(new Response(
                                    tvMain.getText().toString(),
                                    "GESTURE_TAP",
                                    "GESTURE_TAP",
                                    true
                            ));
                        }
                        else {
                            flashWrongAnswer();
                            vibrate();
                            responses.add(new Response(
                                    tvMain.getText().toString(),
                                    "GESTURE_TAP",
                                    "GESTURE_SWIPE",
                                    false
                            ));
                        }
                        nextItemGesture();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
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

    //If timed activity, return timeTakenMillis
    //If untimed activity, calculate the time taken
    private long calculateTimeTaken() {
        if (!task.timed) {
            timeTakenMillis = System.currentTimeMillis() - startTimeMillis;
        }
        return timeTakenMillis;
    }

    //Integers must be saved as Long
    //Results constructor takes values as Long
    //Results from Firebase have Integer as Long
    private void taskCompleted() {
        if (mCountDownTimer != null) mCountDownTimer.cancel();
        HashMap<String, Object> map = new HashMap<>();
        map.put("task_id", task.id);
        map.put("taskTypeLong", task.type.getValue());
        map.put("startTimeMillis", startTimeMillis);
        map.put("timeTakenMillis", calculateTimeTaken());
        map.put("items_correct", itemsCorrect);
        map.put("items_attempted", itemsAttempted);
        map.put("characters_correct", charactersCorrect);
        map.put("characters_total_attempted", charactersTotalAttempted);
        map.put("words_correct", wordsCorrect);
        map.put("words_total_finished", wordsTotalFinished);
        map.put("responses", responses);

        Bundle bundle = new Bundle();
        bundle.putSerializable("result_map", map);
        setResult(RESULT_OK, new Intent().putExtras(bundle));
        finish();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        //Home button or square button tapped
        if (task.isExitOnInterruption) taskCancelled();
    }

    private void analyticsTaskCancelled() {
        logAnalytics(new Bundle());
    }

    private void analyticsTaskCancelled(String reason) {
        Bundle bundle = new Bundle();
        bundle.putString("reason", reason);
        logAnalytics(bundle);
    }

    private void logAnalytics(Bundle bundle) {
        bundle.putLong(FirebaseAnalytics.Param.ITEM_ID, task.id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.title);
        bundle.putInt("item_timed", task.timed? 1 : 0);
        StarsEarthApplication application = (StarsEarthApplication) getApplication();
        application.logActionEvent("task_cancelled", bundle);
    }

    private void checkWordCorrect() {
        //You must also tap the spacebar after the word to get the word correct
        if (!wordIncorrect) {
            //if the word was not declared incorrect, increment the words correct count
            wordsCorrect++;
        }
        wordIncorrect = false; //reset the flag for the next word
    }

    //Only used in type = TYPING
    private void checkItemCorrect() {
        if (!itemIncorrect) {
            //if NO characters in item were declared incorrect, increment the items correct count
            itemsCorrect++;
        }
        itemIncorrect = false; //reset the flag for the next word
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

    public enum LetterCase {
        LOWER, UPPER
    }

    /*
        Returns a random letter as a string
     */
    public String getRandomLetterString(LetterCase letterCase) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(generateRandomLetterChar(letterCase));
        return stringBuilder.toString();
    }

    /*
        Returns a random letter as a char
     */
    public char generateRandomLetterChar(LetterCase letterCase) {
        Random generator = new Random();
        int randomInt = letterCase == LetterCase.UPPER?
                generator.nextInt(25) + 65 : //range of uppercase letters is 25
                generator.nextInt(25) + 97; //range of lowercase letters is 25
        return (char) randomInt;
    }

    /**
     * This function generates the next sentence to be displayed
     * Remove previous sentence from list so that we do not reuse it
     * If it is the last sentence in the list retain it, so that we can keep displaying it
     * Empty list not allowed
     */
    private void nextItem() {
        if (task.type == Task.Type.TYPING || task.type == Task.Type.SPELLING) {
            nextItemTyping();
        }
        else {
            nextItemGesture();
        }
    }

    private void nextItemTyping() {
        index = 0; //reset the cursor to the start of the sentence
        String text = task.ordered ? task.getNextItemTyping((int) itemsAttempted) : task.getNextItemTyping();
        if (task.isTextVisibleOnStart) { tvMain.setText(text); }
        else { tvMain.setText(""); }
        expectedAnswer = formatSpaceCharacter(text);
        tvMain.announceForAccessibility(text.substring(0,1));
    }

    private void nextItemGesture() {
        Map<String, Boolean> map = task.getNextItemGesture();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            tvMain.setText(pair.getKey().toString());
            expectedAnswerGesture = (boolean) pair.getValue();
            //it.remove(); // avoids a ConcurrentModificationException
        }
        tvMain.announceForAccessibility(tvMain.getText());
    }

    /*
    Some string input might use special characters to represent spacebar
    In this case, return a normal space so that expectedAnswer can be compared to keyboard input
     */
    private String formatSpaceCharacter(String s) {
        return s.replaceAll("␣", " ");
    }

    private String addFullStop(String text) {
        if (text.charAt(text.length() - 1) != '.') {
            //All strings must end in full stop
            text = text.concat(".");
        }
        return text;
    }

    private void taskCancelled(String reason) {
        analyticsTaskCancelled(reason);
        endTask(reason);
    }

    private void taskCancelled() {
        analyticsTaskCancelled();
        endTask();
    }

    private void stopTimer() {
        if (mCountDownTimer != null) mCountDownTimer.cancel();
    }

    private void endTask() {
        stopTimer();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void endTask(String reason) {
        stopTimer();
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        bundle.putString("reason", reason);
        intent.putExtras(bundle);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void flashWrongAnswer() {
        final ImageView mContentView = (ImageView) findViewById(R.id.img_red);
        mContentView.setAlpha(0f);
        mContentView.setVisibility(View.VISIBLE);

        mContentView.animate()
                .alpha(1f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mContentView.setVisibility(View.GONE);
                    }
                });
    }

    private void flashRightAnswer() {
        final ImageView mContentView = (ImageView) findViewById(R.id.img_green);
        mContentView.setAlpha(0f);
        mContentView.setVisibility(View.VISIBLE);

        mContentView.animate()
                .alpha(1f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mContentView.setVisibility(View.GONE);
                    }
                });
    }

}
