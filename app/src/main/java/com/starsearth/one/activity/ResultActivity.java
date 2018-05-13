package com.starsearth.one.activity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
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

import java.util.List;

public class ResultActivity extends AppCompatActivity implements ResultFragment.OnFragmentInteractionListener, ResultListFragment.OnListFragmentInteractionListener, MainMenuItemFragment.OnListFragmentInteractionListener {

    Object teachingContent = null;

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
    public void onListFragmentInteraction(@NotNull MainMenuItem item) {

    }

    @Override
    public void setListFragmentProgressBarVisibility(int visibility, @NotNull RecyclerView view) {

    }

    @Override
    public void onFragmentInteraction() {

    }
}
