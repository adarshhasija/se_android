package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by faimac on 3/2/17.
 */

public class SETeachingContent implements Parcelable {

    public long id; //local id
    public String uid;
    public String creator;
    public String title;
    public String instructions;
    public boolean visible = true; //visible to the user
    public String updatedBy;
    public String parentType;
    public String parentId;
    public String views; //The number of times this piece of content was viewed. Applies only to stories as of March 2020
    public long timestamp;
    public List<String> tags = new ArrayList<>();

    public enum TAGS {
        ENGLISH("english"),
        MATHEMATICS("mathematics"),
        TYPING("typing"),
        SPELLING("spelling")
        ;

        private String value;

        TAGS(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toLowerCase();
        }
    }

    public SETeachingContent() {

    }

    public SETeachingContent(String key, HashMap<String, Object> map) {
        this(map);
        this.uid = key;
    }

    public SETeachingContent(HashMap<String, Object> map) {
        this.id =  map.containsKey("id") ? (Long) map.get("id") : -1;
        this.uid = (map.containsKey("uid") && map.get("uid") instanceof Long) ? Long.toString((Long) map.get("uid")) :
                (map.containsKey("uid") && map.get("uid") instanceof String) ? (String) map.get("uid") :
                    "";
        this.creator = map.containsKey("creator") ? (String) map.get("creator") : null;
        this.title = map.containsKey("title") ? (String) map.get("title") : null;
        this.instructions = map.containsKey("instructions") ? (String) map.get("instructions") : null;
        this.visible = map.containsKey ("visible") ? (Boolean) map.get("visible") : false;
        this.updatedBy = map.containsKey("updatedBy") ? (String) map.get("updatedBy") : null;
        this.parentType = map.containsKey("parentType") ? (String) map.get("parentType") : null;
        this.parentId = map.containsKey("parentId") ? (String) map.get("parentId") : null;
        this.views = map.containsKey("views") ? (String) map.get("views") : null;
        this.timestamp = map.containsKey("timestamp") ? (Long) map.get("timestamp") : -1;
     /*   ////Set tags list
        ArrayList<String> mpArrayListTags = (ArrayList<String>) map.get("tags");
        if (mpArrayListTags != null) {
            this.tags = new ArrayList<>();
            for (Object tag : mpArrayListTags) {
                if (tag instanceof String) {
                    this.tags.add((String) tag);
                }
            }
        }
        ////    */
        HashMap<String, Object> mpArrayListTags = (HashMap<String, Object>) map.get("tags");
        if (mpArrayListTags != null) {
            for (Map.Entry<String,Object> entry : mpArrayListTags.entrySet()) {
                this.tags.add(entry.getKey());
            }
        }

    }

    protected SETeachingContent(Parcel in) {
        id = in.readLong();
        uid = in.readString();
        creator = in.readString();
        title = in.readString();
        instructions = in.readString();
        visible = in.readByte() != 0;
        updatedBy = in.readString();
        parentType = in.readString();
        parentId = in.readString();
        views = in.readString();
        timestamp = in.readLong();
        tags = in.readArrayList(String.class.getClassLoader());
    }

    public static final Creator<SETeachingContent> CREATOR = new Creator<SETeachingContent>() {
        @Override
        public SETeachingContent createFromParcel(Parcel in) {
            return new SETeachingContent(in);
        }

        @Override
        public SETeachingContent[] newArray(int size) {
            return new SETeachingContent[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public String getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        //result.put("id", id);
        //result.put("uid", uid);
        result.put("creator", creator);
        result.put("title", title);
        result.put("instructions", instructions);
        result.put("visible", visible);
        result.put("updatedBy", updatedBy);
        result.put("parentType", parentType);
        result.put("parentId", parentId);
        result.put("views", views);
        //result.put("timestamp", timestamp);
        //result.put("tags", tags);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(uid);
        dest.writeString(creator);
        dest.writeString(title);
        dest.writeString(instructions);
        dest.writeByte((byte) (visible ? 1 : 0));
        dest.writeString(updatedBy);
        dest.writeString(parentType);
        dest.writeString(parentId);
        dest.writeString(views);
        dest.writeLong(timestamp);
        dest.writeList(tags);
    }
}
