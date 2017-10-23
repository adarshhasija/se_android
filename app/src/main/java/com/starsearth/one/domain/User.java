package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 4/15/17.
 */

public class User  implements Parcelable {

    public String uid;
    public boolean course_admin;
    public String email;
    public String firstName;
    public String lastName;
    public boolean isGuest;
    public Map<String, SENestedObject> answers = new HashMap<>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, boolean course_admin, String email) {
        this.uid = uid;
        this.course_admin = course_admin;
        this.email = email;
    }

    public User(String uid, boolean isGuest) {
        this.uid = uid;
        this.course_admin = false;
        this.isGuest = isGuest;
    }

    protected User(Parcel in) {
        uid = in.readString();
        //courseAdmin = in.readByte() != 0;
        email = in.readString();
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
        dest.writeByte((byte) (course_admin ? 1 : 0));
        dest.writeString(email);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("course_admin", course_admin);
        result.put("email", email);
        result.put("isGuest", isGuest);

        return result;
    }

    public void addAnswer(SENestedObject value) { this.answers.put(value.uid, value); }
    public void removeAnswer(String answerId) { this.answers.remove(answerId); }
}
