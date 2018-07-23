package com.starsearth.one.activity;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.Response;
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.SEBaseObject;
import com.starsearth.one.domain.Task;
import com.starsearth.one.fragments.LastTriedFragment;
import com.starsearth.one.fragments.MainMenuItemFragment;
import com.starsearth.one.fragments.ResponseListFragment;
import com.starsearth.one.fragments.ResultDetailFragment;
import com.starsearth.one.fragments.ResultListFragment;
import com.starsearth.one.fragments.TaskDetailFragment;
import com.starsearth.one.fragments.TaskDetailListFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class TaskDetailActivity extends AppCompatActivity implements TaskDetailFragment.OnTaskDetailFragmentInteractionListener, TaskDetailListFragment.OnTaskDetailListFragmentListener, MainMenuItemFragment.OnMainMenuFragmentInteractionListener, ResultListFragment.OnResultListFragmentInteractionListener, ResultDetailFragment.OnResultDetailFragmentInteractionListener, ResponseListFragment.OnResponseListFragmentInteractionListener {

    private Object teachingContent = null;
    private ArrayList<Parcelable> results = new ArrayList<Parcelable>();
    private String action = null;
    private Long type;
    private boolean isTimed = false;
    private boolean isGame = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            teachingContent = extras.getParcelable("teachingContent");
            ArrayList<Parcelable> parcelableArrayList = extras.getParcelableArrayList("results");
            if (parcelableArrayList != null) {
                for (Parcelable o : parcelableArrayList) {
                    results.add(o);
                }
            }
            action = extras.getString("action");
            if (action != null && action.length() > 0) {
                setTitle(Utils.formatStringFirstLetterCapital(action));
            }
            else if (teachingContent != null) {
                setTitle(Utils.formatStringFirstLetterCapital(((SEBaseObject) teachingContent).title));
            }

            type = extras.getLong("type");
            isTimed = extras.getBoolean("isTimed");
            isGame = extras.getBoolean("isGame");
        }

        if (teachingContent != null) {
            if (teachingContent instanceof Task) {
                TaskDetailFragment fragment = TaskDetailFragment.Companion.newInstance((Parcelable) teachingContent, results);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container_main, fragment).commit();
            }
            else {
                //it is a course
                MainMenuItemFragment fragment = MainMenuItemFragment.Companion.newInstance((Parcelable) teachingContent, results);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container_main, fragment).commit();
            }
        }
        else {
            MainMenuItemFragment fragment;
            if (action != null || type != null || isTimed || isGame) {
                fragment = MainMenuItemFragment.Companion.newInstance(action, type, isTimed, isGame);
            }
            else {
                fragment = MainMenuItemFragment.Companion.newInstance();
            }
            //MainMenuItemFragment fragment = MainMenuItemFragment.Companion.newInstance((Parcelable) teachingContent, results);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_main, fragment).commit();
        }

     /*   if (teachingContent != null) {
            if (teachingContent instanceof Course) {
                MainMenuItemFragment fragment = MainMenuItemFragment.Companion.newInstance((Parcelable) teachingContent, results);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, fragment).commit();
            }
            else if (teachingContent instanceof Task) {
                TaskDetailFragment fragment = TaskDetailFragment.Companion.newInstance((Parcelable) teachingContent, results);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_main, fragment).commit();

            }
        }   */
    }

    @Override
    protected void onStart() {
        super.onStart();


    }


    @Override
    public void onMainMenuListFragmentInteraction(@NotNull MainMenuItem item) {

    }

    @Override
    public void setListFragmentProgressBarVisibility(int visibility, @NotNull RecyclerView view) {

    }

    @Override
    public void onTaskDetailFragmentLongPressInteraction(Object teachingContent, ArrayList<Result> results) {
        TaskDetailListFragment fragment = TaskDetailListFragment.Companion.newInstance((Parcelable) teachingContent, results);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_up, R.anim.slide_out_to_up)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onTaskDetailFragmentSwipeInteraction(@Nullable Object teachingContent) {

    }

    @Override
    public void onTaskDetailFragmentShowLastTried(Object teachingContent, @Nullable Object result, String errorTitle, String errorMessage) {
        LastTriedFragment fragment = LastTriedFragment.Companion.newInstance((Parcelable) teachingContent, (Parcelable) result, errorTitle, errorMessage);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onTaskDetailListFragmentInteraction() {

    }

    @Override
    public void onResultListFragmentInteraction(@Nullable Task task, @Nullable Result result) {
        ResultDetailFragment fragment = ResultDetailFragment.Companion.newInstance(task, result);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResultDetailFragmentInteraction(@NotNull Object result) {
        ResponseListFragment fragment = ResponseListFragment.Companion.newInstance(result);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_to_left, R.anim.slide_out_to_left)
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void onResponseListFragmentInteraction(@Nullable Response item) {

    }
}
