package com.starsearth.two.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

/**
 * Created by faimac on 4/15/17.
 */

public class User implements Parcelable {

    public String uid;
    public Educator.Status educator;
    public String name;
    public String pic;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValueString(User.class)
    }

    public User(String key) {
        this.uid = key;
    }

    public User(String key, Map<String, Object> map) {
        this.uid = key;
        this.educator = map.containsKey("educator") ? Educator.Status.fromString((String) map.get("educator")) : null;
        this.name = map.containsKey("name") ? (String) map.get("name") : null;
        this.pic = map.containsKey("pic") ? (String) map.get("pic") : null;
    }


    protected User(Parcel in) {
        uid = in.readString();
        educator = Educator.Status.fromString(in.readString());
        name = in.readString();
        pic = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(educator.toString());
        dest.writeString(name);
        dest.writeString(pic);
    }
}
