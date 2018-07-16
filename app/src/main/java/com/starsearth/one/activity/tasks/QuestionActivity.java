package com.starsearth.one.activity.tasks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Question;
import com.starsearth.one.domain.Topic;
import com.starsearth.one.domain.User;

import java.util.Calendar;

public class QuestionActivity extends AppCompatActivity {

    public String LOG_TAG = this.getClass().getSimpleName();

    private String DATABASE_REFERENCE = "answers";
    private Topic parent;
    private Question question;
    private String expectedAnswer;
    private int expectedAnswerIndex = 0;
    private String currentAnswer = "";
    //private int answerKey;
    private long startTime;
    private long finishTime;

    private TextView tvQuestionNumber;
    private LinearLayout llInstruction;
    private TextView tvQuestion;
    private TextView etAnswer;
    private Button btnAction;

    private View.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
         /*   if (event.getAction() == KeyEvent.ACTION_DOWN) {
                char inputCharacter = (char) event.getUnicodeChar();
                if (inputCharacter == ' ') {
                    etAnswer.announceForAccessibility("Space");
                }
            }   */

            //use action up
            //this is so talkback says the key entered first, then the result(right/wrong)
            if (event.getAction() == KeyEvent.ACTION_UP) {
                //String inputCharacter = Character.toString((char) event.getUnicodeChar());
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    //if it is the down arrow ignore
                    //This might be because edittext has just been activated
                    return false;
                }
                char inputCharacter = (char) event.getUnicodeChar();
                char expectedCharacter = expectedAnswer.charAt(expectedAnswerIndex);

                if (inputCharacter == expectedCharacter) {
                    expectedAnswerIndex++;

                    //now we check the next character
                    if (expectedAnswer.indexOf('\n') == expectedAnswerIndex) {
                        finishTime = Calendar.getInstance().getTimeInMillis();
                        tvQuestion.setText(question.getFeedbackCorrectAnswer());
                        btnAction.setVisibility(View.VISIBLE);
                        Toast.makeText(QuestionActivity.this, question.getFeedbackCorrectAnswer(), Toast.LENGTH_SHORT).show();
                        questionCompleted();
                        //btnAction.setVisibility(View.VISIBLE);
                        //btnAction.requestFocus();
                    } else if (expectedAnswer.charAt(expectedAnswerIndex) == ' ') {
                        tvQuestion.setText(getResources().getString(R.string.press_spacebar));
                        tvQuestion.announceForAccessibility(getResources().getString(R.string.press_spacebar));
                    }
                    else {
                        tvQuestion.setText(getResources().getString(R.string.now_press) + " " + expectedAnswer.charAt(expectedAnswerIndex));
                        tvQuestion.announceForAccessibility(getResources().getString(R.string.space)); //Talkback might not announce space on hardware keyboard
                        tvQuestion.announceForAccessibility(getResources().getString(R.string.now_press) + " " + expectedAnswer.charAt(expectedAnswerIndex));
                    }
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //if back button is pressed, we should do nothing. allow it
                } else {
                    if (expectedCharacter == ' ') {
                        tvQuestion.setText(getResources().getString(R.string.wrong_answer) + " spacebar");
                        tvQuestion.announceForAccessibility(getResources().getString(R.string.wrong_answer) + " spacebar");
                    }
                    else {
                        tvQuestion.setText(getResources().getString(R.string.wrong_answer) + " " + expectedCharacter);
                        tvQuestion.announceForAccessibility(getResources().getString(R.string.wrong_answer) + " " + expectedCharacter);
                    }

                }
            }
            return false;
        }
    };

    private void questionCompleted() {
        checkAuthentication();

        Intent intent = new Intent();
        if (!isLastQuestion()) {
            intent.putExtra("go_to_next_question", true);
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            intent.putExtra("questions_finished", true);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void saveAnswer() {
        //Firebase firebase = new Firebase(DATABASE_REFERENCE); DATABASE_REFERENCE will not be used here as we are saving at multiple references
        Firebase firebase = new Firebase(null);
        String answer = etAnswer.getText().toString();
        long timeSpentMillis = finishTime - startTime;
        User userDetails = ((StarsEarthApplication) getApplication()).getUser();
        //firebase.writeNewUserAnswer(userDetails, question.getUid(), answer, timeSpentMillis, parent.getUid());
    }

    private void checkAuthentication() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            saveAnswer();
        }
        else {
            Toast.makeText(this, "Error: not logged in", Toast.LENGTH_SHORT).show();
        }
    }


    private void setCorrectAnswerKey(String answer) {
        switch (answer) {
            case "Escape":
                //answerKey = KeyEvent.KEYCODE_ESCAPE;
                break;
            case "F1":
                //answerKey = KeyEvent.KEYCODE_F1;
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            question = extras.getParcelable("question");
            parent = extras.getParcelable("parent");
            int totalQuestions = extras.getInt("total_questions", question.getIndex());
            tvQuestionNumber = (TextView) findViewById(R.id.tv_question_number);
            tvQuestionNumber.setText((question.getIndex() + 1) + " of " + totalQuestions);

            tvQuestion = (TextView) findViewById(R.id.tv_question);
            String questionTitle = question.getTitle();
            String questionTitle2 = question.questionType != null && question.questionType.equalsIgnoreCase("trial") ?
                                        " Repeat this " + question.repeats + " times." : "";
            tvQuestion.setText(question.getTitle() + questionTitle2);
            llInstruction = (LinearLayout) findViewById(R.id.ll_question_instruction);
            String eol = System.getProperty("line.separator");
            String[] questionInstruction = question.instruction.split(eol);
            for (String s : questionInstruction) {
                TextView tv = new TextView(this);
                tv.setText(s);
                tv.setTextColor(Color.BLACK);
                tv.setFocusable(true);
                llInstruction.addView(tv);
            }
            expectedAnswer = question.getAnswer();
            if (question.questionType != null && question.questionType.equalsIgnoreCase("trial")) {
                for (int i=0; i < question.repeats-1; i++) {
                    expectedAnswer += " " + question.getAnswer();
                }
                expectedAnswer += '\n';
            }

            //setCorrectAnswerKey(question.getAnswer());

            etAnswer = (EditText) findViewById(R.id.et_answer);
            etAnswer.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                    super.onPopulateAccessibilityEvent(host, event);
                    //event.getText().add("Apples");
                }
            });
            etAnswer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.setOnKeyListener(keyListener);
                    }
                    else {
                        v.setOnKeyListener(null);
                    }
                }
            });
            btnAction = (Button) findViewById(R.id.btn_action);
            if (!isLastQuestion()) {
                //btnAction.setText(getResources().getString(R.string.next_question));
            }
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    questionCompleted();
                }
            });
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        startTime = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private boolean isLastQuestion() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int totalQuestions = extras.getInt("total_questions", question.getIndex());
            if (question.getIndex() == totalQuestions - 1) {
                return true;
            }
        }
        return false;
    }

}
