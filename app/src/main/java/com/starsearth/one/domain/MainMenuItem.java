package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by faimac on 2/5/18.
 */

public class MainMenuItem implements Parcelable {

    //Either a course or a task
    public Object teachingContent;
    public Stack<Result> results;

    public MainMenuItem() {
        results = new Stack<>();
    }

    public MainMenuItem(Object teachingContent) {
        this.teachingContent = teachingContent;
        results = new Stack<>();
    }

    public boolean isTaskIdExists(long taskId) {
        boolean result = false;
        if (teachingContent instanceof Course) {
            result = ((Course) teachingContent).isTaskExists(taskId);
        }
        else if (teachingContent instanceof Task) {
            if (taskId == ((Task) teachingContent).id) {
                result = true;
            }
        }
        return result;
    }

    protected MainMenuItem(Parcel in) {
        teachingContent = in.readParcelable(ClassLoader.getSystemClassLoader());
        results.addAll(in.readArrayList(Result.class.getClassLoader()));
    }

    public static final Creator<MainMenuItem> CREATOR = new Creator<MainMenuItem>() {
        @Override
        public MainMenuItem createFromParcel(Parcel in) {
            return new MainMenuItem(in);
        }

        @Override
        public MainMenuItem[] newArray(int size) {
            return new MainMenuItem[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeParcelable((Parcelable) teachingContent, 0);
        dest.writeList(results);
    }
}
