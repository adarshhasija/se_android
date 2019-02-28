package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskContent implements Parcelable {

    public String question;
    public boolean isTapSwipe;
    public boolean isTrue;
    public String explanation;

    TaskContent(String question) {
        this.question = question;
        this.isTapSwipe = false;
        this.isTrue = false;
        this.explanation = null;
    }

    TaskContent(String question, boolean isTapSwipe, boolean isTrue, String explanation) {
        this.question = question;
        this.isTapSwipe = isTapSwipe;
        this.isTrue = isTrue;
        this.explanation = explanation;
    }


    protected TaskContent(Parcel in) {
        question = in.readString();
        isTapSwipe = in.readByte() != 0;
        isTrue = in.readByte() != 0;
        explanation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeByte((byte) (isTapSwipe ? 1 : 0));
        dest.writeByte((byte) (isTrue ? 1 : 0));
        dest.writeString(explanation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TaskContent> CREATOR = new Creator<TaskContent>() {
        @Override
        public TaskContent createFromParcel(Parcel in) {
            return new TaskContent(in);
        }

        @Override
        public TaskContent[] newArray(int size) {
            return new TaskContent[size];
        }
    };
}
