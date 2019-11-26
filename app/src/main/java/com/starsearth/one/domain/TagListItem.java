package com.starsearth.one.domain;

import java.util.Map;

public class TagListItem {

    public String name;
    public boolean seone; //This tag is used by starsearth.one
    public boolean checked; //Teaching content id if it is checked

    public TagListItem() {
        //For Firebase
    }

    public TagListItem(String key, Map<String, Object> map) {
        this.name = key;
        this.seone = map.containsKey("seone") && (boolean) map.get("seone");
    }


}
