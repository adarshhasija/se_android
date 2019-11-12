package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 4/15/17.
 */

public class User {

    public String uid;
    public Educator.Type educator;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValueString(User.class)
    }

    public User(String key, Map<String, Object> map) {
        this.uid = key;
        this.educator = Educator.Type.fromString((String) map.get("educator"));
    }
}
