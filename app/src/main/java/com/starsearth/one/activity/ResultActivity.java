package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.SEBaseObject;
import com.starsearth.one.domain.Task;
import com.starsearth.one.domain.ResultTyping;
import com.starsearth.one.fragments.MainMenuItemFragment;
import com.starsearth.one.fragments.ResultFragment;
import com.starsearth.one.fragments.ResultListFragment;

import org.jetbrains.annotations.NotNull;

public class ResultActivity extends AppCompatActivity implements ResultFragment.OnFragmentInteractionListener, ResultListFragment.OnListFragmentInteractionListener, MainMenuItemFragment.OnListFragmentInteractionListener {

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
        setContentView(R.layout.activity_results);

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            teachingContent = extras.getParcelable("teachingContent");
        }

        if (teachingContent != null) {
            setTitle(Utils.formatStringFirstLetterCapital(((SEBaseObject) teachingContent).title));
            if (teachingContent instanceof Course) {
                MainMenuItemFragment fragment = MainMenuItemFragment.Companion.newInstance((Parcelable) teachingContent);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container_main, fragment).commit();
            }
            else if (teachingContent instanceof Task) {
                ResultFragment fragment = ResultFragment.Companion.newInstance((Parcelable) teachingContent);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container_main, fragment).commit();

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


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

    @Override
    public void onListFragmentInteraction(@NotNull MainMenuItem item) {

    }

    @Override
    public void setListFragmentProgressBarVisibility(int visibility, RecyclerView view) {

    }
}
