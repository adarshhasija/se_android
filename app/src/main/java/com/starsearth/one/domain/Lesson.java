package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 2/28/17.
 */

public class Lesson extends SEBaseObject {

    public String title;
    public int index;
    //public Map<String, Boolean> topics = new HashMap<>();
    public Map<String, SENestedObject> topics = new HashMap<>();

    public Lesson() {
        super();
        // Default constructor required for calls to DataSnapshot.getValue(Lesson.class)
    }

    public Lesson(String uid, String title, String instructions, int index, String createdBy, String parentId) {
        super(uid, title, instructions, createdBy, "course", parentId);
        this.index = index;
    }

    protected Lesson(Parcel in) {
        super(in);
        index = in.readInt();
        topics = in.readHashMap(getClass().getClassLoader());
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    //public void addTopic(String topicId) { this.topics.put(topicId, true); }
    public void addTopic(SENestedObject topic) { this.topics.put(topic.uid, topic); }

    public void removeTopic(String topicId) { this.topics.remove(topicId); }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("index", index);
        result.put("topics", topics);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(index);
        dest.writeMap(topics);
    }
}
