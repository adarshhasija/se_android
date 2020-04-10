package com.starsearth.two.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 2/2/18.
 */

@IgnoreExtraProperties
public class Assistant implements Parcelable {

    public enum State {
        WELCOME(0), TYPING_GAMES_WELCOME(1),

        KEYBOARD_TEST_INTRO(10), KEYBOARD_TEST_START(11), KEYBOARD_TEST_IN_PROGRESS(12),
        KEYBOARD_TEST_COMPLETED_SUCCESS(13), KEYBOARD_TEST_COMPLETED_FAIL(14);

        private final long value;

        State(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public static State fromInt(long i) {
            for (State state : State.values()) {
                if (state.getValue() == i) { return state; }
            }
            return null;
        }
    }

    public String uid;
    public String userId;
    public long state;
    public long timestamp;

    public Assistant() {

    }

    public Assistant(String uid, String userId, State state) {
        this.uid = uid;
        this.userId = userId;
        this.state = state.getValue();
    }

    public State getState() {
        return State.fromInt(state);
    }


    protected Assistant(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        state = in.readLong();
        timestamp = in.readLong();
    }

    public static final Creator<Assistant> CREATOR = new Creator<Assistant>() {
        @Override
        public Assistant createFromParcel(Parcel in) {
            return new Assistant(in);
        }

        @Override
        public Assistant[] newArray(int size) {
            return new Assistant[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(userId);
        dest.writeLong(state);
        dest.writeLong(timestamp);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userId", userId);
        result.put("state", state);
        result.put("timestamp", timestamp);

        return result;
    }
}
