package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by faimac on 2/24/17.
 */

@IgnoreExtraProperties
public class Course extends SEBaseObject {

    public String type;
    public int difficulty;
    public String title;
    public String description;
    public boolean usbKeyboard;
    //public Map<String, Boolean> lessons = new HashMap<>();
    public Map<String, SENestedObject> lessons = new HashMap<>();

    public Course() {
        super();
        // Default constructor required for calls to DataSnapshot.getValue(Course.class)
    }

    public Course(String uid, String type, int difficulty, String title, String description, String createdBy, boolean usbKeyboard) {
        super(uid, title, createdBy);
        this.type = type;
        this.difficulty = difficulty;
        this.description = description;
        this.usbKeyboard = usbKeyboard;
    }

    protected Course(Parcel in) {
        super(in);
        type = in.readString();
        difficulty = in.readInt();
        description = in.readString();
        usbKeyboard = in.readByte() != 0;
        lessons = in.readHashMap(getClass().getClassLoader());
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //public void addLesson(String lessonsId) { this.lessons.put(lessonsId, true); }
    //public void addLesson(String lessonId, SENestedObject value) { this.lessons.put(lessonId, value); }
    public void addLesson(SENestedObject value) { this.lessons.put(value.uid, value); }

    public void removeLesson(String lessonId) { this.lessons.remove(lessonId); }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("type", type);
        result.put("difficulty", difficulty);
        result.put("description", description);
        result.put("usbKeyboard", usbKeyboard);
        result.put("lessons", lessons);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(type);
        dest.writeInt(difficulty);
        dest.writeString(description);
        dest.writeByte((byte) (usbKeyboard ? 1 : 0));
        dest.writeMap(lessons);
    }
}