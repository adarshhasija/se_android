package com.starsearth.one.activity;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.SEBaseObject;
import com.starsearth.one.domain.Task;
import com.starsearth.one.fragments.LastTriedFragment;
import com.starsearth.one.fragments.MainMenuItemFragment;
import com.starsearth.one.fragments.ResultFragment;
import com.starsearth.one.fragments.ResultListFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResultActivity extends AppCompatActivity implements ResultFragment.OnFragmentInteractionListener, ResultListFragment.OnListFragmentInteractionListener, MainMenuItemFragment.OnListFragmentInteractionListener {

    MainMenuItem mainMenuItem = null;
    Object teachingContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

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
    public void onResultFragmentSwipeInteraction(Object teachingContent) {
        ResultListFragment fragment = ResultListFragment.Companion.newInstance((Parcelable) teachingContent);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                //.setCustomAnimations(android.R.anim.slide_in_up, R.anim.slide_out_up)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResultFragmentShowLastTried(Object teachingContent, @Nullable Object result) {
        LastTriedFragment fragment = LastTriedFragment.Companion.newInstance((Parcelable) teachingContent, (Parcelable) result);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFragmentInteraction() {

    }
}
