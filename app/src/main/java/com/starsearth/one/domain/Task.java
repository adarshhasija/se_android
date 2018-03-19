package com.starsearth.one.domain;

import android.os.Parcel;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by faimac on 1/30/18.
 */

public class Task extends SEBaseObject {

    public int id; //local id...text file
    public String instructions;
    public String[] content;
    public Type type;
    public boolean ordered; //should the content be shown in same order to the user
    public boolean timed;
    public int durationMillis;
    public int trials;  //number of trials, if instruction must be repeated

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        TYPING_TIMED(1);

        private final long value;

        Type(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public static Type fromInt(long i) {
            for (Type type : Type.values()) {
                if (type.getValue() == i) { return type; }
            }
            return null;
        }
    };

    public Task() {
        super();
    }

    protected Task(Parcel in) {
        super(in);
        id = in.readInt();
        instructions = in.readString();
        content = in.createStringArray();
        type = Type.fromInt(in.readInt());
        ordered = in.readByte() != 0;
        timed = in.readByte() != 0;
        durationMillis = in.readInt();
        trials = in.readInt();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = (HashMap<String, Object>) super.toMap();
        result.put("id", id);
        result.put("instructions", instructions);
        result.put("content", content);
        result.put("type", type.getValue());
        result.put("ordered", ordered);
        result.put("timed", timed);
        result.put("durationMillis", durationMillis);
        result.put("trials", trials);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeString(instructions);
        dest.writeStringArray(content);
        dest.writeInt((int) type.getValue());
        dest.writeByte((byte) (ordered ? 1 : 0));
        dest.writeByte((byte) (timed ? 1 : 0));
        dest.writeInt(durationMillis);
        dest.writeInt(trials);
    }

    /*
    If content should be returned in any order
     */
    public String getNextItem() {
        Random random = new Random();
        int i = random.nextInt(content.length);
        return content[i];
    }

    /*
        If content is meant to be returned in order
        Input: Exact index OR number of words completed
        Function takes modulo and returns the exact item
        Return content at index
     */
    public String getNextItem(int index) {
        return content[index % content.length];
    }



}
