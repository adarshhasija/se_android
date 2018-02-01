package com.starsearth.one.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.starsearth.one.R;

public class AssistantActivity extends AppCompatActivity implements View.OnClickListener{

    public enum State {
        BEGIN, WELCOME, KEYBOARD_TEST_INTRO, KEYBOARD_TEST_START, KEYBOARD_TEST_END
    }

    private RelativeLayout rl;
    private TextView tvLine1;

    private State currentState = State.BEGIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        rl = (RelativeLayout) findViewById(R.id.rl);
        rl.setOnClickListener(this);
        tvLine1 = (TextView) findViewById(R.id.tv_line_1);
        tvLine1.setText(getString(R.string.se_assistant_tap_here_to_begin));

    }

    @Override
    public void onClick(View v) {
        switch (currentState) {
            case BEGIN:
                tvLine1.setText(getString(R.string.se_assistant_welcome_message));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_welcome_message));
                currentState = State.WELCOME;
                break;
            case WELCOME:
                tvLine1.setText(getString(R.string.se_assistant_keyboard_test_introduction));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_keyboard_test_introduction));
                currentState = State.KEYBOARD_TEST_INTRO;
                break;
            case KEYBOARD_TEST_INTRO:
                tvLine1.setText(getString(R.string.se_assistant_keyboard_test_start));
                tvLine1.announceForAccessibility(getString(R.string.se_assistant_keyboard_test_start));
                currentState = State.KEYBOARD_TEST_START;
                break;
            case KEYBOARD_TEST_START:
                Intent intent = new Intent(AssistantActivity.this, KeyboardActivity.class);
                startActivityForResult(intent, 0);
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK && currentState == State.KEYBOARD_TEST_START) {
            String formattedString = String.format(getString(R.string.se_assistant_keyboard_test_end), "good");
            tvLine1.setText(formattedString);
            tvLine1.announceForAccessibility(formattedString);
            currentState = State.KEYBOARD_TEST_END;
        }
        else if (requestCode == 0 && resultCode == RESULT_CANCELED && currentState == State.KEYBOARD_TEST_START) {
            String formattedString = String.format(getString(R.string.se_assistant_keyboard_test_end), "not good");
            tvLine1.setText(formattedString);
            tvLine1.announceForAccessibility(formattedString);
            currentState = State.KEYBOARD_TEST_END;
        }
    }
}
