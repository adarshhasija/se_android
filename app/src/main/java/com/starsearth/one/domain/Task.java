package com.starsearth.one.domain;

import android.content.Context;
import android.os.Parcel;

import com.google.firebase.database.Exclude;
import com.starsearth.one.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by faimac on 1/30/18.
 */

public class Task extends SEBaseObject {

    public String instructions;
    public String[] content;
    public String[] tap;
    public String[] swipe;
    public Type type;
    public boolean ordered; //should the content be shown in same order to the user
    public boolean timed; //currently not used
    public int durationMillis;
    public int trials;  //number of trials, if instruction must be repeated
    public String[] tags;

    public Type getType() {
        return type;
    }

    public enum Type {
        TYPING_TIMED(1),
        TYPING_UNTIMED(2),
        KEYBOARD_TEST(3),
        TAP_SWIPE_TIMED(4)
        ;

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
        instructions = in.readString();
        content = in.createStringArray();
        tap = in.createStringArray();
        swipe = in.createStringArray();
        type = Type.fromInt(in.readInt());
        ordered = in.readByte() != 0;
        timed = in.readByte() != 0;
        durationMillis = in.readInt();
        trials = in.readInt();
        tags = in.createStringArray();
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
        result.put("instructions", instructions);
        result.put("content", content);
        result.put("tap", tap);
        result.put("swipe", swipe);
        result.put("type", type.getValue());
        result.put("ordered", ordered);
        result.put("timed", timed);
        result.put("durationMillis", durationMillis);
        result.put("trials", trials);
        result.put("tags", tags);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(instructions);
        dest.writeStringArray(content);
        dest.writeStringArray(tap);
        dest.writeStringArray(swipe);
        dest.writeInt((int) type.getValue());
        dest.writeByte((byte) (ordered ? 1 : 0));
        dest.writeByte((byte) (timed ? 1 : 0));
        dest.writeInt(durationMillis);
        dest.writeInt(trials);
        dest.writeStringArray(tags);
    }

    /*
    If content should be returned in any order
    Type: typing
     */
    public String getNextItemTyping() {
        Random random = new Random();
        int i = random.nextInt(content.length);
        return content[i];
    }


    /*
    If content should be returned in any order
    Type: gesture
     */
    public Map<String, Boolean> getNextItemGesture() {
        Map<String, Boolean> map = new HashMap<>();
        Random random = new Random();
        int i = random.nextInt(2);
        if (i % 2 == 0) {
            map.put(tap[random.nextInt(tap.length)], true);
            return map;
        }
        else {
            map.put(swipe[random.nextInt(swipe.length)], false);
            return map;
        }
    }

    /*
        If content is meant to be returned in order
        Input: Exact index OR number of words completed
        Function takes modulo and returns the exact item
        Return content at index
     */
    public String getNextItemTyping(int index) {
        return content[index % content.length];
    }


    public String getTimeLimitAsString(Context context) {
        StringBuffer buf = new StringBuffer();
        if (durationMillis >= 120000) {
            //2 mins or more
            int mins = durationMillis/60000;
            buf.append(mins + " " + context.getResources().getString(R.string.minutes) + ".");
        }
        else {
            int mins = 1;
            buf.append(mins + " " + context.getResources().getString(R.string.minute) + ".");
        }
        return buf.toString();
    }

}
