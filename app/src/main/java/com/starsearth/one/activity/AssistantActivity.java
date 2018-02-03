package com.starsearth.one.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Assistant;

public class AssistantActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout rl;
    private TextView tvLine1;

    private Assistant.State currentState = Assistant.State.WELCOME;

    private void onStateChanged() {
        Firebase firebase = new Firebase("assistants");
        firebase.writeNewAssistant(currentState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        rl = (RelativeLayout) findViewById(R.id.rl);
        rl.setOnClickListener(this);
        tvLine1 = (TextView) findViewById(R.id.tv_line_1);

        Assistant mAssistant = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAssistant = extras.getParcelable("assistant");
            if (mAssistant != null) {
                currentState = mAssistant.getState();
            }
        }

        showText(currentState);

    }

    private void changeState(Assistant.State mState) {
        switch (mState) {
            case WELCOME:
                currentState = Assistant.State.KEYBOARD_TEST_INTRO;
                showText(currentState);
                break;
            case KEYBOARD_TEST_INTRO:
                currentState = Assistant.State.KEYBOARD_TEST_START;
                showText(currentState);
                break;
            case KEYBOARD_TEST_START:
                currentState = Assistant.State.KEYBOARD_TEST_IN_PROGRESS;
                showText(currentState);
                break;
            case KEYBOARD_TEST_IN_PROGRESS:
                openNewIntent(currentState);
                break;
            case KEYBOARD_TEST_COMPLETED_SUCCESS:
            case KEYBOARD_TEST_COMPLETED_FAIL:
                currentState = Assistant.State.TYPING_GAMES_WELCOME;
                showText(currentState);
                break;
            default:
                break;
        }
        onStateChanged();
    }

    private void showText(Assistant.State mState) {
        String formattedString;
        switch (mState) {
            case WELCOME:
                tvLine1.setText(getString(R.string.se_assistant_welcome_message));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_welcome_message));
                break;
            case KEYBOARD_TEST_INTRO:
                tvLine1.setText(getString(R.string.se_assistant_keyboard_test_introduction));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_keyboard_test_introduction));
                break;
            case KEYBOARD_TEST_START:
            case KEYBOARD_TEST_IN_PROGRESS:
                tvLine1.setText(getString(R.string.se_assistant_keyboard_test_start));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_keyboard_test_start));
                break;
            case KEYBOARD_TEST_COMPLETED_SUCCESS:
                formattedString = String.format(getString(R.string.se_assistant_keyboard_test_end), getString(R.string.good));
                tvLine1.setText(formattedString);
                tvLine1.announceForAccessibility(formattedString);
                break;
            case KEYBOARD_TEST_COMPLETED_FAIL:
                formattedString = String.format(getString(R.string.se_assistant_keyboard_test_end), getString(R.string.not_good));
                tvLine1.setText(formattedString);
                tvLine1.announceForAccessibility(formattedString);
                break;
            case TYPING_GAMES_WELCOME:
                tvLine1.setText(getString(R.string.se_assistant_typing_games_welcome));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_typing_games_welcome));
                break;
            default:
                break;
        }
    }

    private void openNewIntent(Assistant.State mState) {
        switch (mState) {
            case KEYBOARD_TEST_IN_PROGRESS:
                Intent intent = new Intent(AssistantActivity.this, KeyboardActivity.class);
                startActivityForResult(intent, 0);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        changeState(currentState);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && currentState == Assistant.State.KEYBOARD_TEST_IN_PROGRESS) {
            if (resultCode == RESULT_OK) {
                currentState = Assistant.State.KEYBOARD_TEST_COMPLETED_SUCCESS;
            }
            else if (resultCode == RESULT_CANCELED) {
                currentState = Assistant.State.KEYBOARD_TEST_COMPLETED_FAIL;
            }
            showText(currentState);
            onStateChanged();
        }
    }
}
