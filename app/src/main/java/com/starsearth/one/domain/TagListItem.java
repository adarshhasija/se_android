package com.starsearth.one.domain;

import java.util.Map;

public class TagListItem {

    public String uid;
    public String name;
    public boolean seone; //This tag is used by starsearth.one
    public String tcid; //Teaching content id if it is checked
    public String userid; //User id if it is selected

    public TagListItem() {
        //For Firebase
    }

    public TagListItem(String key, Map<String, Object> map) {
        this.uid = key;
        this.name = (String) map.get("name");
        this.seone = (boolean) map.get("seone");
    }


}
