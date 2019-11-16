package com.starsearth.one.domain;

import java.util.Map;

/**
 * Created by faimac on 4/15/17.
 */

public class User {

    public String uid;
    public Educator.Status educator;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValueString(User.class)
    }

    public User(String key, Map<String, Object> map) {
        this.uid = key;
        this.educator = Educator.Status.fromString((String) map.get("educator"));
    }
}
