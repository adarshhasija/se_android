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

public class AssistantActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{

    private RelativeLayout rl;
    private TextView tvLine1;

    private Assistant.State currentState = Assistant.State.BEGIN;

    private void onStateChanged() {
        Firebase firebase = new Firebase("assistants");
        firebase.writeNewAssistant(currentState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        Assistant mAssistant = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAssistant = extras.getParcelable("assistant");
            if (mAssistant != null) {
                currentState = mAssistant.getState();
            }
        }

        rl = (RelativeLayout) findViewById(R.id.rl);
        rl.setOnClickListener(this);
        rl.setOnLongClickListener(this);
        tvLine1 = (TextView) findViewById(R.id.tv_line_1);
        if (mAssistant != null) {
            changeState();
        }

    }

    private void changeState() {
        switch (currentState) {
            case BEGIN:
                tvLine1.setText(getString(R.string.se_assistant_welcome_message));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_welcome_message));
                currentState = Assistant.State.WELCOME;
                break;
            case WELCOME:
                tvLine1.setText(getString(R.string.se_assistant_keyboard_test_introduction));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_keyboard_test_introduction));
                currentState = Assistant.State.KEYBOARD_TEST_INTRO;
                break;
            case KEYBOARD_TEST_INTRO:
                tvLine1.setText(getString(R.string.se_assistant_keyboard_test_start));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_keyboard_test_start));
                currentState = Assistant.State.KEYBOARD_TEST_START;
                break;
            case KEYBOARD_TEST_START:
                Intent intent = new Intent(AssistantActivity.this, KeyboardActivity.class);
                startActivityForResult(intent, 0);
                break;
            default:
                break;
        }
        onStateChanged();
    }

    @Override
    public void onClick(View v) {
        changeState();
    }


    @Override
    public boolean onLongClick(View v) {
        tvLine1.announceForAccessibility(tvLine1.getText());
        return true; //false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && currentState == Assistant.State.KEYBOARD_TEST_START) {
            if (resultCode == RESULT_OK) {
                String formattedString = String.format(getString(R.string.se_assistant_keyboard_test_end), "good");
                tvLine1.setText(formattedString);
                tvLine1.announceForAccessibility(formattedString);
                currentState = Assistant.State.KEYBOARD_TEST_END;
            }
            else if (resultCode == RESULT_CANCELED) {
                String formattedString = String.format(getString(R.string.se_assistant_keyboard_test_end), "not good");
                tvLine1.setText(formattedString);
                tvLine1.announceForAccessibility(formattedString);
                currentState = Assistant.State.KEYBOARD_TEST_END;
            }
            onStateChanged();
        }
    }
}
