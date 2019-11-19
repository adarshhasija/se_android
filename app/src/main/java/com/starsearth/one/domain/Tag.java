package com.starsearth.one.domain;

import java.util.Map;

public class Tag {

    public String uid;
    public String name;
    public boolean seone; //This tag is used by starsearth.one

    public Tag() {
        //For Firebase
    }

    public Tag(String name, boolean seone) {
        this.name = name;
        this.seone = seone;
    }

    public Tag(String key, Map<String, Object> map) {
        this.uid = key;
        this.name = (String) map.get("name");
        this.seone = (boolean) map.get("seone");
    }
}
