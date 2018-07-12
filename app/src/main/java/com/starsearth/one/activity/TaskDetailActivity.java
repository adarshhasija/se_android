package com.starsearth.one.activity;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.SEBaseObject;
import com.starsearth.one.domain.Task;
import com.starsearth.one.fragments.LastTriedFragment;
import com.starsearth.one.fragments.MainMenuItemFragment;
import com.starsearth.one.fragments.ResultDetailFragment;
import com.starsearth.one.fragments.ResultListFragment;
import com.starsearth.one.fragments.TaskDetailFragment;
import com.starsearth.one.fragments.TaskDetailListFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaskDetailActivity extends AppCompatActivity implements TaskDetailFragment.OnTaskDetailFragmentInteractionListener, TaskDetailListFragment.OnTaskDetailListFragmentListener, MainMenuItemFragment.OnListFragmentInteractionListener, ResultListFragment.OnResultListFragmentInteractionListener, ResultDetailFragment.OnResultDetailFragmentInteractionListener {

    MainMenuItem mainMenuItem = null;
    Object teachingContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            mainMenuItem = extras.getParcelable("MAIN_MENU_ITEM");
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
                TaskDetailFragment fragment = TaskDetailFragment.Companion.newInstance((Parcelable) teachingContent);
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
    public void onTaskDetailFragmentLongPressInteraction(Object teachingContent) {
        TaskDetailListFragment fragment = TaskDetailListFragment.Companion.newInstance((Parcelable) teachingContent);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                //.setCustomAnimations(android.R.anim.slide_in_up, R.anim.slide_out_up)
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
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResultDetailFragmentInteraction(@NotNull Task task, @NotNull Result result) {

    }


}
