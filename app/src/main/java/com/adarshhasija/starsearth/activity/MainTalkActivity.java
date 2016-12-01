package com.adarshhasija.starsearth.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adarshhasija.starsearth.ExtendedEditText;
import com.adarshhasija.starsearth.StateMachine;
import com.adarshhasija.starsearth.Talk;
import com.adarshhasija.starsearth.ai.ApiAi;
import com.adarshhasija.starsearth.application.StarsEarthApplication;
import com.adarshhasija.starsearth.listener.BotResponseListener;
import com.adarshhasija.starsearth.R;
import com.adarshhasija.starsearth.listener.VoiceListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

public class MainTalkActivity extends AppCompatActivity  implements BotResponseListener, GestureDetector.OnGestureListener, View.OnTouchListener {

    public static String LOG_TAG = "MainTalkActivity";
    private FirebaseAnalytics mFirebaseAnalytics;
    private ApiAi apiAi;
    private Talk talk;
    private StateMachine stateMachine = new StateMachine();

    private RelativeLayout rlMainView;
    private TextView tvActionSwipeRight;
    private TextView tvUserInput;
    private LinearLayout llTypingView;
    private Button btnQuickReplies;
    private ExtendedEditText etUserInput;
    private Button btnTextSubmit;
    private TextView tvBotResponse;
    private TextView tvAdditionalInstructions;

    private GestureDetector gestureDetector=null;
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_talk);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rlMainView = (RelativeLayout) findViewById(R.id.rlMainView);
        tvActionSwipeRight = (TextView) findViewById(R.id.tvActionSwipeRight);
        tvActionSwipeRight.setText(tvActionSwipeRight.getText().toString().replace("*","<"));
        tvUserInput = (TextView) findViewById(R.id.tvUserInput);
        llTypingView = (LinearLayout) findViewById(R.id.llTypingView);
        etUserInput = (ExtendedEditText) findViewById(R.id.etUserInput);
        etUserInput.setBotResponseListener(this);
        tvBotResponse = (TextView) findViewById(R.id.tvBotResponse);
        tvAdditionalInstructions = (TextView) findViewById(R.id.tvAdditionalInstructions);

        talk = new Talk(this, this);
        setupGestureDetector();

        btnQuickReplies = (Button) findViewById(R.id.btnQuickReplies);
        btnQuickReplies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStateViewingQuickReplies();
            }
        });
        btnTextSubmit = (Button) findViewById(R.id.btnTextSubmit);
        btnTextSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStateInputProcessingTyping();
            }
        });


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        rlMainView.setOnTouchListener(null);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!((StarsEarthApplication) getApplication()).isNetworkAvailable()) {
            ((StarsEarthApplication) getApplication()).showNoInternetDialog(this);
        }
        rlMainView.setOnTouchListener(this);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
        }
    }








    /********************************
        Bot response listener

     *********************************/
    @Override
    public void processUserVoiceInput(String text) {
        goToStateInputProcessingTalking(text);
    }
    @Override
    public void processBotResponse(String text) {
        //If the state is not processing(user is typing/talking), do not process input
        if (stateMachine.getState() == StateMachine.State.INPUT_PROCESSING_TALKING
                || stateMachine.getState() == StateMachine.State.INPUT_PROCESSING_TYPING) {
            //Set the state machine
            stateMachine.setCurrentUserInput(tvUserInput.getText().toString());
            stateMachine.setCurrentBotTextResponse(text);
            sayBotResponse(text);
            goToStateIdle();
        }
        apiAi = null;
    }
    @Override
    public void processFireBaseImage(StorageReference storageReference) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type", "image");
    }
    @Override
    public void onKeyboardClosed() {
        goToStateIdle();
    }

    @Override
    public void onEnterPressed() {
        goToStateInputProcessingTyping();
    }

    @Override
    public void onAICancelled() {
        apiAi = null;
    }


    /************************
     *
     * State Machine
     *
     ************************/
    private void goToStateIdle() {
        String userInput = stateMachine.getCurrentUserInput();
        String botResponse = stateMachine.getCurrentBotTextResponse();
        if (userInput != null && !userInput.isEmpty()) {
            tvUserInput.setText(userInput);
            showUserInputUI();
        }
        else {
            tvUserInput.setText(getString(R.string.tap_screen_to_talk));
            showUserInputUI();
        }

        if (botResponse != null && !botResponse.isEmpty()) {
            tvBotResponse.setText(botResponse);
            tvAdditionalInstructions.setText(R.string.long_press_to_repeat_response);
            showBotResponseUI();
        }
        else {
            hideBotResponseUI();
        }
        stateMachine.setState(StateMachine.State.IDLE);
    }
    private void goToStateTalking() {
        talk.stopTalking();
        if (((StarsEarthApplication) getApplication()).isNetworkAvailable()) {
            ((StarsEarthApplication) getApplication()).vibrate(500);
            startListening();
        }
        else {
            ((StarsEarthApplication) getApplication()).showNoInternetDialog(this);
        }
        stateMachine.setState(StateMachine.State.TALKING);
    }
    private void goToStateTyping() {
        talk.stopTalking();
        hideUserInputUI();
        hideBotResponseUI();
        showTypingUI();
        stateMachine.setState(StateMachine.State.TYPING);
    }
    private void goToStateInputProcessingTalking(String text) {
        stateTypingTalking(text);
        stateMachine.setState(StateMachine.State.INPUT_PROCESSING_TALKING);
    }
    private void goToStateInputProcessingTyping() {
        String text = etUserInput.getText().toString();
        hideTypingUI();
        stateTypingTalking(text);
        stateMachine.setState(StateMachine.State.INPUT_PROCESSING_TYPING);
    }
    private void stateTypingTalking(String text) {
        setUserInput(text);
        showUserInputUI();
        setBotResponse(getString(R.string.please_wait));
        sayBotResponse(getString(R.string.please_wait));
        setAdditionalInstructions(getString(R.string.long_press_to_cancel));
        showBotResponseUI();
        apiAi = new ApiAi(this, this);
        apiAi.send(text);
    }
    private void goToStateViewingQuickReplies() {

    }






    /****************************


            Set user/bot input

     *****************************/
    private void setUserInput(String text) {
        if (text != null && !text.isEmpty()) {
            tvUserInput.setText(text);
        }
    }
    private void setBotResponse(String text) {
        if (text != null && !text.isEmpty()) {
            tvBotResponse.setText(text);
        }
    }
    private void setAdditionalInstructions(String text) {
        if (text != null & !text.isEmpty()) {
            tvAdditionalInstructions.setText(text);
        }
    }
    private void sayBotResponse(String text) {
        if (text != null && !text.isEmpty()) {
            talk.playAudio(text);
        }
    }


    /*

           All UI modifications

     */

    private void showUserInputUI() {
        tvUserInput.setVisibility(View.VISIBLE);
    }
    private void hideUserInputUI() {
        tvUserInput.setVisibility(View.GONE);
    }
    private void showBotResponseUI() {
        tvBotResponse.setVisibility(View.VISIBLE);
        if (!tvBotResponse.getText().toString().isEmpty()) {
            tvAdditionalInstructions.setVisibility(View.VISIBLE);
        }
    }
    private void hideBotResponseUI() {
        tvBotResponse.setVisibility(View.GONE);
        tvAdditionalInstructions.setVisibility(View.GONE);
    }
    private void showTypingUI() {
        llTypingView.setVisibility(View.VISIBLE);
    }
    private void hideTypingUI() {
        llTypingView.setVisibility(View.GONE);
    }


    /**********************
     * Start listening user actions
     *
     *******************/
    private void startListening() {
        tvUserInput.setText(R.string.talk_prompt);
        showUserInputUI();
        hideBotResponseUI();
        talk.startListening();
    }





    /***********************
     * Stop listening and stop typing user actions
     *
     */
    private void cancelListening() {
        talk.cancelListening();
    }
    private void cancelTyping() {
        etUserInput.clearFocus();
        etUserInput.setText("");
        hideTypingUI();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rlMainView.getWindowToken(), 0);
    }


    /****************************
     * Gesture detector
     * @param e
     * @return
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (stateMachine.getState() == StateMachine.State.TYPING) {
            cancelTyping();
            goToStateIdle();
        }
        else if (stateMachine.getState() == StateMachine.State.TALKING) {
            cancelListening();
            goToStateIdle();
        }
        else if (stateMachine.getState() == StateMachine.State.INPUT_PROCESSING_TALKING) {
            if (apiAi != null) {
                apiAi.cancel();
            }
            goToStateIdle();
        }
        else if (stateMachine.getState() == StateMachine.State.INPUT_PROCESSING_TYPING) {
            //If we are in the middle of processing, cancel the process
            if (apiAi != null) {
                apiAi.cancel();
            }
            goToStateIdle();
        }
        else {
            //Assuming this is idle state
            //goToStateTalking();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        //If the user is giving an input, do not say response
        if (stateMachine.getState() == StateMachine.State.TALKING ||
                stateMachine.getState() == StateMachine.State.TYPING) {
            return;
        }
        if (tvBotResponse.getText().toString() != null && !tvBotResponse.getText().toString().isEmpty()) {
            talk.stopTalking();
            sayBotResponse(tvBotResponse.getText().toString());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CHARACTER, Integer.toString(tvBotResponse.getText().toString().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "content-length");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
        else {
            //Vibration when there is nothing to respond
            ((StarsEarthApplication) getApplication()).vibrate(500);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CHARACTER, "0");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "content-length");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }

    private final int SWIPE_MIN_DISTANCE = 150;
    private final int SWIPE_THRESHOLD_VELOCITY = 100;
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            onRightToLeft();
            return true;
        }
        else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            onLeftToRight();
            return true;
        }

        if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            onBottomToTopFling();
            return true;
        }
        else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            onTopToBottomFling();
            return true;
        }
        return false;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }


    /**********************
     * On Swipe functions
     *
     */
    private void onRightToLeft() {

    }
    private void onLeftToRight() {

    }
    private void onBottomToTopFling() {
        if (stateMachine.getState() != StateMachine.State.TALKING &&
                stateMachine.getState() != StateMachine.State.TYPING) {
            //If user is not currently talking, allow this
            if (apiAi != null) {
                apiAi.cancel();
            }
            goToStateTyping();
        }

    }
    private void onTopToBottomFling() {
        //If user is currently typing, close typing
        if (stateMachine.getState() == StateMachine.State.TYPING) {
            goToStateIdle();
        }
    }


    /**********************
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_questions_feedback) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.action_questions_feedback);
            alertDialog.setMessage(R.string.message_questions_feedback);
            alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Intent viewIntent =
                                new Intent("android.intent.action.VIEW",
                                        Uri.parse("https://play.google.com/store/apps/details?id=com.adarshhasija.starsearth"));
                        startActivity(viewIntent);
                    }catch(Exception e) {
                        Toast.makeText(getApplicationContext(),"Unable to Connect Try Again...",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
            alertDialog.setNegativeButton(android.R.string.no, null);
            alertDialog.show();
            return true;
        }
        if (id == R.id.action_talk_by_typing) {
            onBottomToTopFling();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
