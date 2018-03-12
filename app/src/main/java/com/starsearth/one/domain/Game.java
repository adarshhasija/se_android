package com.starsearth.one.domain;

import android.os.Parcel;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by faimac on 1/30/18.
 */

public class Game extends SEBaseObject {

    public int id; //local id...text file
    public String instructions;
    public String[] content;
    public Type type;
    public boolean timed;
    public int durationMillis;

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

    public Game() {
        super();
    }

    protected Game(Parcel in) {
        super(in);
        id = in.readInt();
        instructions = in.readString();
        content = in.createStringArray();
        type = Type.fromInt(in.readInt());
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = (HashMap<String, Object>) super.toMap();
        result.put("id", id);
        result.put("instructions", instructions);
        result.put("content", content);

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
    }

}
