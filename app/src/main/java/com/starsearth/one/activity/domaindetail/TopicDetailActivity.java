package com.starsearth.one.activity.domaindetail;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.R;
import com.starsearth.one.activity.LoginActivity;
import com.starsearth.one.activity.QuestionActivity;
import com.starsearth.one.activity.SignupActivity;
import com.starsearth.one.activity.lists.ExercisesListActivity;
import com.starsearth.one.activity.lists.QuestionsListActivity;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.domain.Question;
import com.starsearth.one.domain.SENestedObject;
import com.starsearth.one.domain.Topic;
import com.starsearth.one.domain.User;
import com.starsearth.one.domain.UserAnswer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TopicDetailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference userRef;
    private Query queryQuestions;
    private Query queryAnswers;
    private Topic topic;
    private boolean admin = false;
    private List<Question> questionList = new ArrayList<>();
    private List<UserAnswer> answerList = new ArrayList<>();
    private int questionIndex = -1;

    private TextView tvTopicTitle;
    private ScrollView sv;
    private LinearLayout llTopicDescription;
    private Button btnAction;
    private LinearLayout llLoginSignupText;
    private Button btnLogin;
    private Button btnSignup;

    private ValueEventListener userValuesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            ((StarsEarthApplication) getApplication()).setFirebaseUser(user);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener questionsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Question question = dataSnapshot.getValue(Question.class);
            questionList.add(question);
            if (questionIndex < 0) questionIndex=0;
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener userAnswersListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            UserAnswer answer = dataSnapshot.getValue(UserAnswer.class);
            if (!answer.topicId.equals(topic.getUid())) {
                return; //Filtering out. If it does not belong to this topic then exit
            }
            answerList.add(answer);
            if (answerList.size() >= questionList.size()) {
                questionIndex = 0; //give user the ability to start from the top
                btnAction.setText(getResources().getString(R.string.go_to_questions));
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setOnClickListener(null);
                btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToNextQuestion();
                    }
                });

             /*   //all questions completed
                btnAction.setText(getResources().getString(R.string.go_back_to_lessons));
                btnAction.setOnClickListener(null);
                btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                btnAction.requestFocus();   */
            }
            else if (answerList.size() < questionList.size()) {
                //next question
                questionIndex++;
                //if user has answered some questions, setup UI for the next question
                btnAction.setText(getResources().getString(R.string.go_to_questions));
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setOnClickListener(null);
                btnAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToNextQuestion();
                    }
                });
            }


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void goToNextQuestion() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("admin", admin);
        bundle.putParcelable("parent", topic);
        bundle.putInt("total_questions", questionList.size());
        bundle.putParcelable("question", questionList.get(questionIndex));
        Intent intent = new Intent(TopicDetailActivity.this, QuestionActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
    }

    private void showLoginSignup() {
        if (llLoginSignupText != null) llLoginSignupText.setVisibility(View.VISIBLE);
        if (btnLogin != null) btnLogin.setVisibility(View.VISIBLE);
        if (btnSignup != null) btnSignup.setVisibility(View.VISIBLE);
    }

    private void hideLoginSignup() {
        if (llLoginSignupText != null) llLoginSignupText.setVisibility(View.GONE);
        if (btnLogin != null) btnLogin.setVisibility(View.GONE);
        if (btnSignup != null) btnSignup.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    if (queryAnswers != null) {
                        queryAnswers.removeEventListener(userAnswersListener);
                        queryAnswers = null;
                    }

                    userRef = FirebaseDatabase.getInstance().getReference("users/"+currentUser.getUid());
                    userRef.addValueEventListener(userValuesListener);

                    DatabaseReference mDatabaseAnswers = FirebaseDatabase.getInstance().getReference("answers");
                    queryAnswers = mDatabaseAnswers.orderByChild("userId").equalTo(currentUser.getUid());
                    queryAnswers.addChildEventListener(userAnswersListener);

                    hideLoginSignup();
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.announceForAccessibility(getResources().getString(R.string.login_successful));
                    btnAction.requestFocus();
                }
                else {
                    if (queryAnswers != null) queryAnswers.removeEventListener(userAnswersListener);
                    if (userRef != null) userRef.removeEventListener(userValuesListener);
                    btnAction.setVisibility(View.GONE);
                    showLoginSignup();
                }
            }
        };

        tvTopicTitle = (TextView) findViewById(R.id.tv_topic_title);
        sv = (ScrollView) findViewById(R.id.sv);
        llTopicDescription = (LinearLayout) findViewById(R.id.ll_topic_description);
        btnAction = (Button) findViewById(R.id.btn_action);
        llLoginSignupText = (LinearLayout) findViewById(R.id.ll_login_signup_text);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopicDetailActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopicDetailActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        Bundle bundle = getIntent().getExtras();
        topic = bundle.getParcelable("topic");
        admin = bundle.getBoolean("admin");
        boolean f1HelpMode = bundle.getBoolean("f1_help_mode", false); //If this was accessed by pressing F1 on questions list
                                                                        //If so, do not show button. User should just go back
        boolean isLastTopic = bundle.getBoolean("is_last_topic", false); //Need to modify UI accordingly

        setTitle(topic.getTitle());
        String eol = System.getProperty("line.separator");
        String[] topicDescription = topic.getDescription().split(eol);
        for (int i = 0; i < topicDescription.length; i++) {
            String s = topicDescription[i];
            TextView tv = new TextView(this);
            tv.setText(s);
            tv.setTextColor(Color.BLACK);
            tv.setFocusable(true);
            if (i == topicDescription.length - 1) {
                //incase the scrollview is not at the bottom, move it to the bottom when focus
                //is on the last element. Then the next item to get focus will be the button
                tv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            sv.fullScroll(View.FOCUS_DOWN);
                        }
                    }
                });
            }
            llTopicDescription.addView(tv);
        }
        View.OnClickListener goToNextTopicListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("go_to_next_topic", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        };

        if (f1HelpMode) {
            btnAction.setVisibility(View.GONE);
        }
        else if (topic.questions.size() > 0) {
            //If there are questions attached to the topic, setup the mAuth listener in onStart
            btnAction.setText(getResources().getString(R.string.go_to_questions));
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToNextQuestion();
                }
            });
        }
        else if (!isLastTopic) {
            //No questions in this topic, go to the next topic
            btnAction.setText(getResources().getString(R.string.go_to_next_topic));
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setOnClickListener(goToNextTopicListener);
        }
        else {
            //No questions in this topic
            //No next topic after this
            btnAction.setVisibility(View.GONE);
            TextView tv = new TextView(this);
            tv.setText(getResources().getString(R.string.lesson_last_topic_no_questions));
            tv.setAllCaps(true);
            tv.setTextColor(Color.BLACK);
            tv.setFocusable(true);
            llTopicDescription.addView(tv);
        }

        DatabaseReference mDatabaseQuestions = FirebaseDatabase.getInstance().getReference("questions");
        queryQuestions = mDatabaseQuestions.orderByChild("parentId").equalTo(topic.getUid());
        queryQuestions.addChildEventListener(questionsListener);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference mDatabaseAnswers = FirebaseDatabase.getInstance().getReference("answers");
            queryAnswers = mDatabaseAnswers.orderByChild("userId").equalTo(currentUser.getUid());
            queryAnswers.addChildEventListener(userAnswersListener);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (topic.questions.size() > 0) {
            //Auth listener is only required if there are questions to move forward to.
            //Otherwise it is not required
            //mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            //mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
     /*   if (userRef != null) {
            userRef.removeEventListener(userValuesListener);
        }
        if (queryQuestions != null) { queryQuestions.removeEventListener(questionsListener); }
        if (queryAnswers != null) {
            queryAnswers.removeEventListener(userAnswersListener);
        }   */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (questionIndex > -1) {
            if (resultCode == RESULT_OK && null != data) {
                if (data.getBooleanExtra("go_to_next_question", false)) {
                    //questionIndex++; //already done in listener
                    goToNextQuestion();
                }
            }
        }
    }
}
