package com.starsearth.two.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 3/2/17.
 */


//This class is used to keep track of nested ids in objects
public class SENestedObject implements Parcelable {

    public String uid;
    public String typePlural;
    public long timestamp;
    //public Map<String, Map<String, SENestedObject>> children = new HashMap<>();
    public Map<String, SENestedObject> children = new HashMap<>();

    public SENestedObject() {

    }

    public SENestedObject(String uid, String typePlural) {
        this.uid = uid;
        this.typePlural = typePlural;
    }

    public SENestedObject(String uid, String typePlural, long timestamp) {
        this.uid = uid;
        this.typePlural = typePlural;
        this.timestamp = timestamp;
    }

    protected SENestedObject(Parcel in) {
        uid = in.readString();
        children = in.readHashMap(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeMap(children);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SENestedObject> CREATOR = new Creator<SENestedObject>() {
        @Override
        public SENestedObject createFromParcel(Parcel in) {
            return new SENestedObject(in);
        }

        @Override
        public SENestedObject[] newArray(int size) {
            return new SENestedObject[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("children", children);

        return result;
    }
}
