package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.collect.MinMaxPriorityQueue;
import com.starsearth.one.domain.datastructures.ResultComparator;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by faimac on 2/5/18.
 */

public class RecordItem implements Parcelable {

    //Either a course or a task
    public Object teachingContent;
    public MinMaxPriorityQueue<Result> results;
    public Object type; //DetailListFragment.ListItem. Needed if we have to show results screen directly instead of DetailFragment

    public RecordItem(Object teachingContent) {
        this.teachingContent = teachingContent;
        results = MinMaxPriorityQueue
                //.orderedBy(Comparator.comparing(Result::getTimestamp))
                .orderedBy(new ResultComparator())
                .maximumSize(1) //change this based on requirement
                .create();
    }

    public boolean isResultLatest(Result result) {
        if (results != null && results.size() > 0) {
            return result.timestamp > results.peek().timestamp;
        }
        return true;
    }

    public boolean isTaskIdExists(String taskId) {
        boolean result = false;
        if (teachingContent instanceof Course) {
            result = ((Course) teachingContent).isTaskExists(taskId);
        }
        else if (teachingContent instanceof Task) {
            if (taskId.equals(((Task) teachingContent).uid)) {
                result = true;
            }
        }
        return result;
    }

    protected RecordItem(Parcel in) {
        teachingContent = in.readParcelable(ClassLoader.getSystemClassLoader());
        results.addAll(in.readArrayList(Result.class.getClassLoader()));
        type = in.readParcelable(ClassLoader.getSystemClassLoader());
    }

    public static final Creator<RecordItem> CREATOR = new Creator<RecordItem>() {
        @Override
        public RecordItem createFromParcel(Parcel in) {
            return new RecordItem(in);
        }

        @Override
        public RecordItem[] newArray(int size) {
            return new RecordItem[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeParcelable((Parcelable) teachingContent, 0);
        dest.writeList(Collections.singletonList(results));
        dest.writeParcelable((Parcelable) type, 0);
    }
}
