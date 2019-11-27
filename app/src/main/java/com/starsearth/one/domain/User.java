package com.starsearth.one.domain;

import java.util.Map;

/**
 * Created by faimac on 4/15/17.
 */

public class User {

    public String uid;
    public Educator.Status educator;
    public String name;
    public String pic;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValueString(User.class)
    }

    public User(String key, Map<String, Object> map) {
        this.uid = key;
        this.educator = map.containsKey("educator") ? Educator.Status.fromString((String) map.get("educator")) : null;
        this.name = map.containsKey("name") ? (String) map.get("name") : null;
        this.pic = map.containsKey("pic") ? (String) map.get("pic") : null;
    }
}
