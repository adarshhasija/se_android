package com.starsearth.one.activity.forms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starsearth.one.R;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Exercise;
import com.starsearth.one.domain.Question;

import java.util.ArrayList;

public class AddEditQuestionActivity extends AppCompatActivity {

    private String DATABASE_REFERENCE = "questions";
    private String UID;
    private String parentId;
    private Question question;
    private int selectedIndex = 0;

    //UI
    private EditText etTitle;
    private EditText etAnswer;
    private EditText etHint;
    private EditText etInstruction;
    private EditText etRepeats;
    private EditText etPositiveWeight;
    private EditText etNegativeWeight;
    private EditText etFeedbackCorrectAnswer;
    private EditText etFeedbackWrongAnswer;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_question);
        setTitle(R.string.enter_edit_question_details);

        Bundle extras = getIntent().getExtras();
        int totalQuestions = 0;
        if (extras != null) {
            totalQuestions = extras.getInt("totalItems");
            parentId = extras.getString("parentId");
            question = extras.getParcelable("question");
            if (question != null )UID = question.getUid();
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < (totalQuestions + 1); i++) {
            list.add(i+1);
        }
        selectedIndex = list.get(list.size()-1) - 1;

        etTitle = (EditText) findViewById(R.id.et_title);
        etAnswer = (EditText) findViewById(R.id.et_answer);
        etHint = (EditText) findViewById(R.id.et_hint);
        etInstruction = (EditText) findViewById(R.id.et_instruction);
        etRepeats = (EditText) findViewById(R.id.et_repeats);
        etPositiveWeight = (EditText) findViewById(R.id.et_positive_weight);
        etNegativeWeight = (EditText) findViewById(R.id.et_negative_weight);
        etFeedbackCorrectAnswer = (EditText) findViewById(R.id.et_feedback_correct_answer);
        etFeedbackWrongAnswer = (EditText) findViewById(R.id.et_feedback_wrong_answer);
        if (question != null) {
            etTitle.setText(question.getTitle());
            etAnswer.setText(question.getAnswer());
            etHint.setText(question.getHint());
            etInstruction.setText(question.instruction);
            etRepeats.setText(Integer.toString(question.repeats));
            etPositiveWeight.setText(Float.toString(question.getPositiveWeight()));
            etNegativeWeight.setText(Float.toString(question.getNegativeWeight()));
            etFeedbackCorrectAnswer.setText(question.getFeedbackCorrectAnswer());
            etFeedbackWrongAnswer.setText(question.getFeedbackWrongAnswer());
        }

        btnDone = (Button) findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString();
                String answer = etAnswer.getText().toString();
                String hint = etHint.getText().toString();
                String instruction = etInstruction.getText().toString();
                int repeats = 0;
                float positiveWeight = 0;
                float negativeWeight = 0;
                String feedbackCorrectAnswer = etFeedbackCorrectAnswer.getText().toString();
                String feedbackWrongAnswer = etFeedbackWrongAnswer.getText().toString();

                try {
                    repeats = Integer.parseInt(etRepeats.getText().toString());
                } catch (NumberFormatException e) {

                }

                try {
                    positiveWeight = Float.parseFloat(etPositiveWeight.getText().toString());
                } catch (NumberFormatException e) {

                }

                try {
                    negativeWeight = Float.parseFloat(etNegativeWeight.getText().toString());
                } catch (NumberFormatException e) {

                }

                if (title == null || title.length() < 1) {
                    Toast.makeText(AddEditQuestionActivity.this, R.string.question_title_blank, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (answer == null || answer.length() < 1) {
                    Toast.makeText(AddEditQuestionActivity.this, R.string.question_answer_blank, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (negativeWeight > 0) {
                    negativeWeight = 0 - negativeWeight;
                }
                if (feedbackCorrectAnswer == null || feedbackCorrectAnswer.length() < 1) {
                    Toast.makeText(AddEditQuestionActivity.this, R.string.question_feedback_correct_answer_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
             /*   if (feedbackWrongAnswer == null || feedbackWrongAnswer.length() < 1) {
                    Toast.makeText(AddEditQuestionActivity.this, R.string.question_feedback_wrong_answer_hint, Toast.LENGTH_SHORT).show();
                    return;
                }   */

                Firebase firebase = new Firebase(DATABASE_REFERENCE);
                if (UID != null) {
                    question.setTitle(title);
                    question.setAnswer(answer);
                    question.setHint(hint);
                    question.instruction = instruction;
                    question.repeats = repeats;
                    question.setPositiveWeight(positiveWeight);
                    question.setNegativeWeight(negativeWeight);
                    question.setFeedbackCorrectAnswer(feedbackCorrectAnswer);
                    question.setFeedbackWrongAnswer(feedbackWrongAnswer);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    question.setUpdatedBy(currentUser.getUid());
                    firebase.updateExistingQuestion(UID, question);
                }
                else {
                    UID = firebase.writeNewQuestion(selectedIndex, title, answer, hint, positiveWeight, negativeWeight,
                            feedbackCorrectAnswer, feedbackWrongAnswer, parentId, instruction, repeats);
                }

                finish();
            }
        });
    }
}
