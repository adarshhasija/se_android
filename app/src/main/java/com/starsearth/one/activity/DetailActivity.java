package com.starsearth.one.activity;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.starsearth.one.R;
import com.starsearth.one.Utils;
import com.starsearth.one.domain.RecordItem;
import com.starsearth.one.domain.SEOneListItem;
import com.starsearth.one.domain.Response;
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.Task;
import com.starsearth.one.fragments.lists.CourseProgressListFragment;
import com.starsearth.one.fragments.LastTriedFragment;
import com.starsearth.one.fragments.lists.RecordListFragment;
import com.starsearth.one.fragments.lists.RecordListFragment;
import com.starsearth.one.fragments.lists.SeOneListFragment;
import com.starsearth.one.fragments.lists.ResponseListFragment;
import com.starsearth.one.fragments.ResultDetailFragment;
import com.starsearth.one.fragments.lists.ResultListFragment;
import com.starsearth.one.fragments.TaskDetailFragment;
import com.starsearth.one.fragments.lists.TaskDetailListFragment;
import com.starsearth.one.fragments.dummy.DummyContent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/*
This activity is responsible for all the fragment transitions
This is so that we can switch between TabbedActivity and MainActivity for the base activity
 */
public class DetailActivity extends AppCompatActivity implements
                                                                    ResultDetailFragment.OnResultDetailFragmentInteractionListener,
                                                                    ResponseListFragment.OnResponseListFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String type = extras.getString(SEOneListItem.TYPE_LABEL);
            String content = extras.getString(SEOneListItem.CONTENT);
            RecordListFragment recordsListFragment = RecordListFragment.Companion.newInstance(SEOneListItem.Type.fromString(type), content);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_main, recordsListFragment).commit();
        }
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
