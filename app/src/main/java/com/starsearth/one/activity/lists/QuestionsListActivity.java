package com.starsearth.one.activity.lists;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.starsearth.one.activity.QuestionActivity;
import com.starsearth.one.R;
import com.starsearth.one.activity.LoginActivity;
import com.starsearth.one.activity.SignupActivity;
import com.starsearth.one.activity.domaindetail.LessonDetailActivity;
import com.starsearth.one.activity.domaindetail.TopicDetailActivity;
import com.starsearth.one.activity.forms.AddEditExerciseActivity;
import com.starsearth.one.activity.forms.AddEditQuestionActivity;
import com.starsearth.one.adapter.QuestionsAdapter;
import com.starsearth.one.database.Firebase;
import com.starsearth.one.domain.Exercise;
import com.starsearth.one.domain.Question;
import com.starsearth.one.domain.SENestedObject;
import com.starsearth.one.domain.Topic;

import java.util.ArrayList;
import java.util.List;

public class QuestionsListActivity extends ItemListActivity {

    private Topic parent;
    private ArrayList<Question> itemList;
    private QuestionsAdapter adapter;
    private int currentIndex = -1;

    private ValueEventListener parentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            parent = null;
            parent = dataSnapshot.getValue(Topic.class);

            if (parent != null) {
                tvParentLine1.setText(parent.getTitle());
            }
            else {
                //This means the parent was deleted from somewhere else
                //close activity
                finish();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(getApplicationContext(),databaseError.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    };

    private ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Question newQuestion = dataSnapshot.getValue(Question.class);
            String questionKey = dataSnapshot.getKey();
            addItemReferenceToParent(questionKey);

            if (adapter != null) {
                adapter.add(newQuestion);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Question newQuestion = dataSnapshot.getValue(Question.class);
            String questionKey = dataSnapshot.getKey();

            if (adapter != null) {
                List<Question> list = adapter.getQuestionList();
                for (int i = 0; i < list.size(); i++) {
                    Question question = list.get(i);
                    if (question.getUid().equals(questionKey)) {
                        adapter.remove(question);
                        adapter.insert(newQuestion, i);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Question removedQuestion = dataSnapshot.getValue(Question.class);
            String questionKey = dataSnapshot.getKey();
            //parent.removeExercise(exerciseKey);
            //mParentDatabase.setValue(parent);
            removeItemFromParent(questionKey);

            if (adapter != null) {
                List<Question> list = adapter.getQuestionList();
                for (int i = 0; i < list.size(); i++) {
                    Question question = list.get(i);
                    if (question.getUid().equals(questionKey)) {
                        adapter.remove(question);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void addItemReferenceToParent(String questionKey) {
        SENestedObject nestedObject = new SENestedObject(questionKey, "questions");
        parent.addQuestion(nestedObject);
        mParentDatabase.setValue(parent);
    }

    private void removeItemFromParent(String questionKey) {
        parent.removeQuestion(questionKey);
        mParentDatabase.setValue(parent);
    }

    private void deleteItem(final Question deleteQuestion) {
        new AlertDialog.Builder(QuestionsListActivity.this)
                .setTitle(R.string.delete_question)
                .setMessage(R.string.delete_question_confirm_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Firebase firebase = new Firebase(REFERENCE);
                        firebase.removeQuestion(deleteQuestion);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showQuestionAdminMode(final Question question, final int position) {
        String lblAnswer = getResources().getString(R.string.question_answer);
        String lblHint = getResources().getString(R.string.question_hint);
        String lblPositiveWeight = getResources().getString(R.string.question_positive_weight);
        String lblNegativeWeight = getResources().getString(R.string.question_negative_weight);
        String lblFeedbackCorrect = getResources().getString(R.string.question_feedback_correct_answer);
        String lblFeedbackWrong = getResources().getString(R.string.question_feedback_wrong_answer);

        new AlertDialog.Builder(QuestionsListActivity.this)
                .setTitle(question.getTitle())
                .setMessage(lblAnswer + ": "+question.getAnswer()+"\n"+
                            lblHint +": "+question.getHint()+"\n"+
                        lblPositiveWeight +": "+question.getPositiveWeight()+"\n"+
                        lblNegativeWeight +": "+question.getNegativeWeight()+"\n"+
                        lblFeedbackCorrect +": "+question.getFeedbackCorrectAnswer()+"\n"+
                        lblFeedbackWrong +": "+question.getFeedbackWrongAnswer())
                .setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(QuestionsListActivity.this, AddEditQuestionActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("question", itemList.get(position));
                        intent.putExtras(bundle);
                        startActivityForResult(intent, position);
                    }
                })
                .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(question);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showLoginSignup() {
        String title = getResources().getString(R.string.no_user_found);
        String message = getResources().getString(R.string.login_signup_access_questions);
        new AlertDialog.Builder(QuestionsListActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton(R.string.signup, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(QuestionsListActivity.this, SignupActivity.class);
                        startActivity(intent);
                    }
                })
                .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(QuestionsListActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setupParentDetailView() {
        Intent intent = new Intent(QuestionsListActivity.this, TopicDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", parent);
        bundle.putBoolean("f1_help_mode", true);
        showParentDetailView(intent, bundle);
    }

    private OnCompleteListener<AuthResult> guestLoginListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {

            }
            else {
                
            }
        }
    };

    private void loginGuest() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_questions_list);
        tvListViewHeader.setText(R.string.questions);
        btnAddItem.setText(R.string.add_question);
        REFERENCE_PARENT = "/topics/";
        REFERENCE = "questions";

        itemList = new ArrayList<>();
        adapter = new QuestionsAdapter(getApplicationContext(), 0, itemList);
        listView.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();
        parent = bundle.getParcelable("parent");
        boolean parentPresent = false;
        if (parent != null) {
            parentPresent = true;
            mParentDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE_PARENT + parent.getUid());
            mParentDatabase.addValueEventListener(parentListener);

            setTitle(parent.getTitle());
            tvParentLine1.setText(parent.getTitle());
            llParent.setVisibility(View.VISIBLE);
        }

        if (admin && parentPresent) {
            llParent.setVisibility(View.VISIBLE);
        }
        else {
            llParent.setVisibility(View.GONE);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Question question = itemList.get(position);

                sendAnalytics(question.title);

                if (admin) {
                    showQuestionAdminMode(question, position);
                }
                else {
                    openQuestionView(question, position);
                }
            }
        });
        listView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_F1 && event.getAction() == KeyEvent.ACTION_UP) {
                    sendAnalyticsParentOpenedFromKeyboard(parent.title);
                    setupParentDetailView();
                }
                return false;
            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuestionsListActivity.this, AddEditQuestionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("totalItems", itemList.size());
                bundle.putString("parentId", parent.getUid());
                intent.putExtras(bundle);
                startActivityForResult(intent, -1);

            }
        });
        //listView.setEmptyView(btnAddItem);

        mDatabase = FirebaseDatabase.getInstance().getReference(REFERENCE);
        query = mDatabase.orderByChild("parentId").equalTo(parent.getUid());
        query.addChildEventListener(listener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mParentDatabase.removeEventListener(parentListener);
        mDatabase.removeEventListener(listener);
    }

    private void openQuestionView(Question question, int position) {
        currentIndex = position;
        Intent intent = new Intent(QuestionsListActivity.this, QuestionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("question", question);
        bundle.putInt("total_questions", itemList.size());
        intent.putExtras(bundle);
        startActivityForResult(intent, position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (currentIndex > -1) {
            if (resultCode == RESULT_OK && null != data) {
                if (data.getBooleanExtra("go_to_next_question", false)) {
                    currentIndex++;
                    Question question = itemList.get(currentIndex);
                    openQuestionView(question, currentIndex);
                }
            }
            else {
                currentIndex = -1;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;

        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case 0:
                intent = new Intent(this, AddEditQuestionActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("question", itemList.get(index));
                intent.putExtras(bundle);
                startActivityForResult(intent, index);
                break;
            case 1:
                deleteItem(itemList.get(index));
                break;
            default: break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.size() > 0) {
            menu.getItem(0).setTitle(R.string.edit_exercise);
            menu.getItem(1).setTitle(R.string.delete_exercise);
            menu.getItem(2).setTitle(R.string.add_question);
        }


        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Bundle bundle;

        switch (item.getItemId()) {
            case R.id.edit_parent:
                intent = new Intent(QuestionsListActivity.this, AddEditExerciseActivity.class);
                bundle = new Bundle();
                bundle.putParcelable("exercise", parent);
                intent.putExtras(bundle);
                startActivityForResult(intent, 100);
                return true;
            case R.id.delete_parent:
                new AlertDialog.Builder(QuestionsListActivity.this)
                        .setTitle(R.string.delete_exercise)
                        .setMessage(R.string.delete_exercise_confirm_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mParentDatabase.removeEventListener(parentListener);
                                mParentDatabase.removeValue();
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            case R.id.add_item:
                intent = new Intent(QuestionsListActivity.this, AddEditQuestionActivity.class);
                bundle = new Bundle();
                bundle.putInt("totalItems", itemList.size());
                bundle.putString("parentId", parent.getUid());
                intent.putExtras(bundle);
                startActivityForResult(intent, -1);
                return true;
            default: break;
        }


        return super.onOptionsItemSelected(item);
    }
}
