package com.starsearth.two.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class TaskContent implements Parcelable {

    public int id;
    public String question;
    public String hintAudio; //Audio details to help the user answer the question
    public boolean isTapSwipe;
    public boolean isTrue;
    public String explanation;

    public TaskContent() {
        super();
    }

    public TaskContent(String question) {
        this.question = question;
        this.isTapSwipe = false;
    }

    public TaskContent(String question, boolean isTrue) {
        this.question = question;
        this.isTapSwipe = true;
        this.isTrue = isTrue;
    }

    public TaskContent(Map<String, Object> map) {
        this.id = (map.get("id") instanceof Double)? ((Double) map.get("id")).intValue() : //If we are getting it from the local file, gson will take int as double
                (map.get("id") instanceof Long) ? ((Long) map.get("id")).intValue() : //If we are getting it from Firebase its a Long
                (int) map.get("id");
        this.question = map.containsKey("question") ? (String) map.get("question") : null;
        this.hintAudio = map.containsKey("hintAudio") ? (String) map.get("hintAudio") : null;
        this.isTapSwipe = map.containsKey("isTapSwipe") ? (boolean) map.get("isTapSwipe") : false;
        this.isTrue = map.containsKey("isTrue") ? (boolean) map.get("isTrue") : false;
        this.explanation = map.containsKey("expectedAnswerExplanation") ? (String) map.get("expectedAnswerExplanation") : null;
    }


    protected TaskContent(Parcel in) {
        id = in.readInt();
        question = in.readString();
        hintAudio = in.readString();
        isTapSwipe = in.readByte() != 0;
        isTrue = in.readByte() != 0;
        explanation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(question);
        dest.writeString(hintAudio);
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

