package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TaskContent implements Parcelable {

    public int id;
    public String question;
    public boolean isTapSwipe;
    public boolean isTrue;
    public String explanation;

    public TaskContent() {
        super();
    }

    public TaskContent(int id, String question, boolean isTapSwipe, boolean isTrue, String explanation) {
        this.id = id;
        this.question = question;
        this.isTapSwipe = isTapSwipe;
        this.isTrue = isTrue;
        this.explanation = explanation;
    }

    public TaskContent(Map<String, Object> map) {
        this.id = (map.get("id") instanceof Double)? ((Double) map.get("id")).intValue() : (int) map.get("id"); //If type is not specified, gson will take int as double
        this.question = (String) map.get("question");
        this.isTapSwipe = (boolean) map.get("isTapSwipe");
        this.isTrue = (boolean) map.get("isTrue");
        this.explanation = (String) map.get("expectedAnswerExplanation");
    }


    protected TaskContent(Parcel in) {
        id = in.readInt();
        question = in.readString();
        isTapSwipe = in.readByte() != 0;
        isTrue = in.readByte() != 0;
        explanation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
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

