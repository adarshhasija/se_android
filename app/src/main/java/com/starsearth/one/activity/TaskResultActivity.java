package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.activity.tasks.TaskTypingActivity;
import com.starsearth.one.adapter.ResultAdapter;
import com.starsearth.one.adapter.ResultTypingAdapter;
import com.starsearth.one.domain.Task;
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.ResultTyping;
import com.starsearth.one.fragments.ResultFragment;
import com.starsearth.one.fragments.ResultTypingFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TaskResultActivity extends AppCompatActivity implements ResultFragment.OnFragmentInteractionListener, ResultTypingFragment.OnListFragmentInteractionListener {

    public static int MAX_NUMBER_IN_LIST = 1;

    private FirebaseAnalytics mFirebaseAnalytics;

    private ArrayList<Result> list = new ArrayList<>();
    private DatabaseReference mDatabase;

    Task task = null;

    private Button btnStart;
    private TextView tvInstruction;
    private RecyclerView mRecyclerView;
    private ResultAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Result result = null; dataSnapshot.getValue(Result.class);

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                //if it is not the same task type, return
                if (task != null) {
                    if (task.id != result.getTask_id()) {
                        return;
                    }
                }

            }

          /*  if (mAdapter != null && list != null) {
                if (!list.isEmpty()) {
                    Result highScore = list.get(0);
                    if (isTopResult(result.words_correct, result.words_total_finished)) {
                        highScore = list.get(0);
                        mDatabase.child(highScore.uid).removeValue();
                        list.remove(highScore);
                        list.add(0, result); //this is the new highscore
                    }
                    if (list.size() > 1) {
                        //last tried row exists
                        //remove last tried
                        //replace with new value
                        Result lastItem = list.get(list.size()-1);
                        list.remove(lastItem);
                        if (!highScore.uid.equals(lastItem.uid)) {
                            //delete it from the cloud ONLY if it is not the highscore
                            mDatabase.child(lastItem.uid).removeValue();
                        }

                    }
                    list.add(result);

                } else {
                    //if list is empty, add it twice
                    //once as highscore
                    //once as last attempt
                    list.add(result);
                    list.add(result);
                }
                mAdapter.notifyDataSetChanged();
            }   */

            int index = 0; //indexToInsert(result);
            if (mAdapter != null && list != null) {
                switch (task.type) {
                    case TYPING_TIMED:
                        ResultTyping resultTyping = null; dataSnapshot.getValue(ResultTyping.class);
                        list.add(index,resultTyping);
                        break;
                    default:
                        break;
                }
              /*  if (index == -1)  {
                    //if -1, insert at the end of the list
                    list.add(result);
                }
                else {
                    list.add(index,result);
                }   */

                if (list.size() > MAX_NUMBER_IN_LIST) {
                    //If the list is now more than MAX_NUMBER_IN_LIST items, remove the lowest item
                    Result lastItem = list.get(list.size()-1);
                    mDatabase.child(lastItem.uid).removeValue(); //delete from the database
                    list.remove(lastItem);
                }
                //mAdapter.notifyItemChanged(index);
                mAdapter.notifyDataSetChanged();
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

    private boolean isTopResult(int correct, int totalFinished) {
        //if (list.size() < MAX_NUMBER_IN_LIST) {
        //    return true;
        //}
        boolean result = false;
        if (!list.isEmpty()) {
            Result highScore = list.get(0);
            if (true
                    //correct > highScore.words_correct
                    //&& mAdapter.getAccuracy(correct, totalFinished) > mAdapter.getAccuracy(highScore.words_correct, highScore.words_total_finished)
                    ) {
                result = true;
            }
        }
        else {
            result = true;
        }

        return result;
    }

    /**
     *
     * @param result
     * @return index of list. -1 if need to insert at the end of the list
     */
    private int indexToInsert(Result result) {
        if (list.isEmpty()) {
            return 0;
        }

       /* for (int i = 0; i < list.size(); i++) {
            Result listItem = list.get(i);
            if (result.words_correct > listItem.words_correct) {
                return i;
            }
        }   */
        //If the score is smaller than all current scores, add it in the end
        return -1;
    }

    private void alertScore(int words_correct, int words_total_finished, boolean highScore) {
        Toast.makeText(getApplicationContext(), getString(R.string.your_score) + " " + words_correct, Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), words_correct + " " + getString(R.string.words_per_minute) + " " + getString(R.string.accuracy) + " " + mAdapter.getAccuracy(words_correct, words_total_finished) + "%", Toast.LENGTH_LONG).show();
    }

    private void setInstructionTextAndContent() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (task != null) {
                String instructions = task.instructions;
                tvInstruction.setText(instructions);
            }

        }
        else {
            return;
        }
    }

    private AlertDialog.Builder createAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(TaskResultActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(TaskResultActivity.this);
        }

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typing_test_result);

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            task = extras.getParcelable("task");
        }


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        if (task != null) {
            switch (task.type) {
                case TYPING_TIMED:
                    ArrayList<ResultTyping> list = new ArrayList<>();
                    mAdapter = new ResultTypingAdapter(getApplicationContext(), list);
                    break;
                default:break;
            }
        }

        mRecyclerView.setAdapter(mAdapter);

        tvInstruction = (TextView) findViewById(R.id.tv_instruction);
        setInstructionTextAndContent();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference("results");
        mDatabase.keepSynced(true);
        Query query = mDatabase.orderByChild("userId").equalTo(currentUser.getUid());
        //query.addChildEventListener(childEventListener);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (task != null) {
                    Bundle analyticsBundle = new Bundle();
                    analyticsBundle.putInt(FirebaseAnalytics.Param.ITEM_ID, task.id);
                    analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.instructions);
                    analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button start task");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, analyticsBundle);

                    Intent intent = new Intent(TaskResultActivity.this, TaskTypingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("task", task);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, 0);
                }

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (task != null) {
            setTitle(Utils.formatStringFirstLetterCapital(task.title));
            ResultFragment fragment = ResultFragment.Companion.newInstance(task);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_main, fragment).commit();


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), R.string.typing_game_cancelled, Toast.LENGTH_LONG).show();
        }
        else if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                int wordsCorrect = bundle.getInt("words_correct");
                int wordsTotalFinished = bundle.getInt("words_total_finished");
                //This should not be in onChildAdded as it should only be shown once we return from completing a task
              /*  if (isTopResult(wordsCorrect, wordsTotalFinished)) {
                    alertScore(wordsCorrect, wordsTotalFinished, true);
                }
                else {
                    alertScore(wordsCorrect, wordsTotalFinished, false);
                }   */
              alertScore(wordsCorrect, wordsTotalFinished, true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.removeEventListener(childEventListener);
    }

    private void sendAnalytics(Task task) {
        Bundle analyticsBundle = new Bundle();
        analyticsBundle.putInt(FirebaseAnalytics.Param.ITEM_ID, task.id);
        analyticsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, task.instructions);
        analyticsBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button start task");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, analyticsBundle);
    }

    private void startTask(Task task) {
        Intent intent = new Intent(TaskResultActivity.this, TaskTypingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", task);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onFragmentInteraction(@NotNull Task task) {
        sendAnalytics(task);
        startTask(task);
    }

    @Override
    public void onListFragmentInteraction(@NotNull ResultTyping item) {

    }
}
