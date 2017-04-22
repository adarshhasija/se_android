package com.starsearth.one.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.starsearth.one.ExtendedEditText;
import com.starsearth.one.StateMachine;
import com.starsearth.one.ChatBot;
import com.starsearth.one.ai.ApiAi;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.listener.BotResponseListener;
import com.starsearth.one.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.storage.StorageReference;

public class ChatBotActivity extends AppCompatActivity  implements BotResponseListener, GestureDetector.OnGestureListener, View.OnTouchListener {

    public static String LOG_TAG = "ChatBotActivity";
    private FirebaseAnalytics mFirebaseAnalytics;
    private ApiAi apiAi;
    private ChatBot chatBot;
    private StateMachine stateMachine = new StateMachine();

    private RelativeLayout rlMainView;
    private LinearLayout llMainAction;
    private TextView tvMainAction;
    private Button btnMainAction;
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
        setContentView(R.layout.activity_chat_bot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rlMainView = (RelativeLayout) findViewById(R.id.rlMainView);
        llMainAction = (LinearLayout) findViewById(R.id.llMainAction);
        tvMainAction = (TextView) findViewById(R.id.tvMainAction);
        tvMainAction.setText(tvMainAction.getText().toString().replace("*","<"));
        tvUserInput = (TextView) findViewById(R.id.tvUserInput);
        llTypingView = (LinearLayout) findViewById(R.id.llTypingView);
        etUserInput = (ExtendedEditText) findViewById(R.id.etUserInput);
        etUserInput.setBotResponseListener(this);
        tvBotResponse = (TextView) findViewById(R.id.tvBotResponse);
        tvAdditionalInstructions = (TextView) findViewById(R.id.tvAdditionalInstructions);

        chatBot = new ChatBot(this, this);
        setupGestureDetector();

        btnMainAction = (Button) findViewById(R.id.btnMainAction);
        btnMainAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStateMainAction();
            }
        });
        btnQuickReplies = (Button) findViewById(R.id.btnQuickReplies);
        btnQuickReplies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStateViewingBotQuestions();
            }
        });
        btnTextSubmit = (Button) findViewById(R.id.btnTextSubmit);
        btnTextSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStateInputProcessingTyping();
            }
        });

        goToStateIdle();
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
    protected void onDestroy() {
        super.onDestroy();
        chatBot =null;
    }

    /************************
     *
     * State Machine
     *
     ************************/
    private void goToStateIdle() {
        showMainActionUI();

        String userInput = stateMachine.getCurrentUserInput();
        String botResponse = stateMachine.getCurrentBotTextResponse();
        if (userInput != null && !userInput.isEmpty()) {
            setLabel(tvUserInput, userInput);
            showUserInputUI();
        }
        else {
            setLabel(tvUserInput, getString(R.string.tap_screen_to_talk));
            showUserInputUI();
        }

        if (botResponse != null && !botResponse.isEmpty()) {
            setLabel(tvBotResponse, botResponse);
            setLabel(tvAdditionalInstructions, getString(R.string.long_press_to_repeat_response));
            showBotResponseUI();
        }
        else {
            hideBotResponseUI();
        }
        stateMachine.setState(StateMachine.State.IDLE);
    }
    private void goToStateTalking() {
        chatBot.stopTalking();
        if (((StarsEarthApplication) getApplication()).isNetworkAvailable()) {
            ((StarsEarthApplication) getApplication()).vibrate(500);
            hideMainActionUI();
            setLabel(tvUserInput, getString(R.string.talk_prompt));
            showUserInputUI();
            hideBotResponseUI();
            startListening();
        }
        else {
            ((StarsEarthApplication) getApplication()).showNoInternetDialog(this);
        }
        stateMachine.setState(StateMachine.State.TALKING);
    }
    private void goToStateTyping() {
        chatBot.stopTalking();
        hideMainActionUI();
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
        hideMainActionUI();
        setLabel(tvUserInput, text);
        showUserInputUI();
        setLabel(tvBotResponse, getString(R.string.please_wait));
        sayText(chatBot, getString(R.string.please_wait));
        setLabel(tvAdditionalInstructions, getString(R.string.long_press_to_cancel));
        showBotResponseUI();
        apiAi = new ApiAi(this, this);
        apiAi.send(text);
    }
    private void goToStateMainAction() {
        Intent intent = new Intent(this, KeyboardActivity.class);
        startActivity(intent);
    }
    private void goToStateViewingBotQuestions() {
        //Intent intent = new Intent(this, BotHelpActivity.class);
        //startActivityForResult(intent, 1);
    }



    /******************************

     All UI modifications

     *******************************/
    private LinearLayout getLlMainActionUI() { return llMainAction; }
    private TextView getUserInputUI() {
        return tvUserInput;
    }
    private TextView getBotResponseUI() {
        return tvBotResponse;
    }
    private TextView getAdditionalInstructionsUI() {
        return tvAdditionalInstructions;
    }
    private LinearLayout getTypingView() {
        return llTypingView;
    }
    private void showUI(View item) {
        if (item != null && item instanceof View) {
            View mItem = (View) item;
            mItem.setVisibility(View.VISIBLE);
        }
    }
    private void hideUI(View item) {
        if (item != null && item instanceof View) {
            View mItem = (View) item;
            mItem.setVisibility(View.GONE);
        }
    }
    private void showMainActionUI() {
        LinearLayout ll = getLlMainActionUI();
        if (ll != null) showUI(ll);
    }
    private void hideMainActionUI() {
        LinearLayout ll = getLlMainActionUI();
        if (ll != null) hideUI(ll);
    }
    private void showUserInputUI() {
        //TextView tv = getUserInputUI();
        //if (tv != null) showUI(tv);
    }
    private void hideUserInputUI() {
        TextView tv = getUserInputUI();
        if (tv != null) hideUI(tv);
    }
    private void showBotResponseUI() {
        TextView tvBot = getBotResponseUI();
        TextView tvAdInst = getAdditionalInstructionsUI();

        if (tvBot != null) {
            showUI(tvBot);
            if (!tvBot.getText().toString().isEmpty()) {
                tvAdInst.setVisibility(View.VISIBLE);
            }
        }
    }
    private void hideBotResponseUI() {
        TextView tvBot = getBotResponseUI();
        TextView tvAdInst = getAdditionalInstructionsUI();

        if (tvBot != null) tvBot.setVisibility(View.GONE);
        if (tvAdInst != null) tvAdInst.setVisibility(View.GONE);
    }
    private void showTypingUI() {
        LinearLayout view = getTypingView();
        if (view != null) view.setVisibility(View.VISIBLE);
    }
    private void hideTypingUI() {
        LinearLayout view = getTypingView();
        if (view != null) view.setVisibility(View.GONE);
    }





    /****************************


     Set user/bot input

     *****************************/
    private void setLabel(View item, String text) {
        if (text != null && !text.isEmpty()) {
            if (item instanceof TextView) {
                TextView mItem = (TextView) item;
                mItem.setText(text);
            }
        }
    }
    private void sayText(ChatBot mChatBot, String text) {
        if (text != null && !text.isEmpty() &&
                mChatBot != null && mChatBot instanceof ChatBot) {
            mChatBot.playAudio(text);
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
            sayText(chatBot, text);
            goToStateIdle();
        }
        apiAi = null;
    }
    @Override
    public void processFireBaseImage(StorageReference storageReference) {
        //Intent intent = new Intent(this, FullscreenActivity.class);
        //Bundle bundle = new Bundle();
        //bundle.putString("type", "image");
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







    /**********************
     * Listening: start + cancel
     * Typing: Cancel
     *
     *******************/
    private void startListening() {
        chatBot.startListening();
    }
    private void cancelListening() {
        chatBot.cancelListening();
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
            chatBot.stopTalking();
            sayText(chatBot, tvBotResponse.getText().toString());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CHARACTER, Integer.toString(tvBotResponse.getText().toString().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "content-length");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
        else {
            //Vibration when there is nothing to respond
         /*   ((StarsEarthApplication) getApplication()).vibrate(500);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CHARACTER, "0");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "content-length");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);    */
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
            //onBottomToTopFling();
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
        goToStateMainAction();
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
        //getMenuInflater().inflate(R.menu.main, menu);
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
