package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.domain.SEBaseObject;
import com.starsearth.one.domain.Task;
import com.starsearth.one.domain.ResultTyping;
import com.starsearth.one.fragments.ResultFragment;
import com.starsearth.one.fragments.ResultListFragment;

import org.jetbrains.annotations.NotNull;

public class ResultActivity extends AppCompatActivity implements ResultFragment.OnFragmentInteractionListener, ResultListFragment.OnListFragmentInteractionListener {

    Object teachingContent = null;

    private void alertScore(int words_correct, int words_total_finished, boolean highScore) {
        Toast.makeText(getApplicationContext(), getString(R.string.your_score) + " " + words_correct, Toast.LENGTH_LONG).show();
    }

    private AlertDialog.Builder createAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ResultActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ResultActivity.this);
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
            teachingContent = extras.getParcelable("teachingContent");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (teachingContent != null) {
            setTitle(Utils.formatStringFirstLetterCapital(((SEBaseObject) teachingContent).title));
            ResultFragment fragment = ResultFragment.Companion.newInstance((Parcelable) teachingContent);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_main, fragment).commit();


        }
    }

    @Override
    public void onFragmentInteraction(@NotNull Task task) {
        //business logic removed: March 26 2018
        //call to startTask
        //call to sendAnalytics
    }

    @Override
    public void onListFragmentInteraction(@NotNull ResultTyping item) {
        //list items are not clickable: March 21 2018
    }
}
