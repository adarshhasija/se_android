package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.activity.tasks.TaskTypingActivity;
import com.starsearth.one.adapter.ResultAdapter;
import com.starsearth.one.domain.Task;
import com.starsearth.one.domain.ResultTyping;
import com.starsearth.one.fragments.ResultFragment;
import com.starsearth.one.fragments.ResultTypingFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TaskResultActivity extends AppCompatActivity implements ResultFragment.OnFragmentInteractionListener, ResultTypingFragment.OnListFragmentInteractionListener {

    private FirebaseAnalytics mFirebaseAnalytics;

    Task task = null;

    private void alertScore(int words_correct, int words_total_finished, boolean highScore) {
        Toast.makeText(getApplicationContext(), getString(R.string.your_score) + " " + words_correct, Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), words_correct + " " + getString(R.string.words_per_minute) + " " + getString(R.string.accuracy) + " " + mAdapter.getAccuracy(words_correct, words_total_finished) + "%", Toast.LENGTH_LONG).show();
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
        //list items are not clickable: March 21 2018
    }
}
