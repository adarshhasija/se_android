package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Adarsh Hasija on 2/24/17.
 * Courses have a title and a list of organized tasks. Tasks are done in order
 * When a task is done, course list is not re-ordered
 */

@IgnoreExtraProperties
public class Course extends SEBaseObject {

    public String type;
    public int difficulty;
    public String description;
    public boolean usbKeyboard = false;
    public boolean hasKeyboardTest = false;
    //public Map<String, Boolean> lessons = new HashMap<>();
    public Map<String, SENestedObject> lessons = new HashMap<>();
    public List<Task> tasks;

    public Course() {
        super();
        // Default constructor required for calls to DataSnapshot.getValue(Course.class)
    }

    public Course(HashMap<String, Object> map) {
        super(map);
        this.type = type;
        this.difficulty = difficulty;
        this.description = description;
        this.usbKeyboard = usbKeyboard;

        this.type = map.containsKey("type") ? (String) map.get("type") : null;
        this.difficulty = map.containsKey("difficulty") ? ((Long) map.get("difficulty")).intValue() : -1;
        this.description = map.containsKey("description") ? (String) map.get("description") : null;
        this.usbKeyboard = map.containsKey("usbKeyboard") ? (Boolean) map.get("usbKeyboard") : false;
        this.hasKeyboardTest = map.containsKey("hasKeyboardTest") ? (Boolean) map.get("hasKeyboardTest") : false;
        this.tasks = map.containsKey("tasks") ? (List<Task>) map.get("tasks") : null;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    protected Course(Parcel in) {
        super(in);
        type = in.readString();
        difficulty = in.readInt();
        description = in.readString();
        usbKeyboard = in.readByte() != 0;
        hasKeyboardTest = in.readByte() != 0;
        lessons = in.readHashMap(getClass().getClassLoader());
        tasks = in.readArrayList(Task.class.getClassLoader());
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
        result.put("hasKeyboardTest", hasKeyboardTest);
        result.put("lessons", lessons);
        result.put("tasks", tasks);

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
        dest.writeByte((byte) (hasKeyboardTest? 1 : 0));
        dest.writeMap(lessons);
        dest.writeList(tasks);
    }

    public boolean isTaskExists(long taskId) {
        boolean result = false;
        for (Task t : safe(tasks)) {
            if (t.id == taskId) {
                result = true;
                break;
            }
        }
        return result;
    }

    public Task getTaskById(long id) {
        Task result = null;
        for (Task t : safe(tasks)) {
            if (t.id == id) {
                result = t;
                break;
            }
        }
        return result;
    }

    public int getIndexOfLastPassedTask(ArrayList<Result> results) {
        int lastPassedTaskIndex = -1;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isPassed(results)) {
                lastPassedTaskIndex = i;
            }
        }
        return lastPassedTaskIndex;
    }

    //This checks if the last Result is that of the last Task in the Course list
    public boolean isCourseComplete(ArrayList<Result> results) {
        boolean result = false;
        int indexOfLastPassedTask = getIndexOfLastPassedTask(results);
        if (indexOfLastPassedTask == tasks.size()-1) {
            result = true;
        }
        return result;
    }

    public boolean isFirstTaskAttempted(ArrayList<Result> results) {
        boolean ret = false;
        if (tasks.size() > 0) {
            Task task = tasks.get(0);
            if (task.isAttempted(results)) {
                ret = true;
            }
        }
        return ret;
    }

    public boolean isFirstTaskPassed(ArrayList<Result> results) {
        boolean ret = false;
        if (tasks.size() > 0) {
            Task task = tasks.get(0);
            if (task.isPassed(results)) {
                ret = true;
            }
        }
        return ret;
    }

    public Task getNextTask(ArrayList<Result> allResults) {
        Task ret = null;
        int currentTaskIndex = getCurrentTaskIndex(allResults);
        if (currentTaskIndex < tasks.size()) {
            Task currentTask = tasks.get(currentTaskIndex);
            if (currentTask.isPassed(allResults) && currentTaskIndex + 1 < tasks.size()) {
                ret = tasks.get(currentTaskIndex + 1);
            }
            else {
                ret = currentTask;
            }
        }
        return ret;
    }

    public int getCurrentTaskIndex(ArrayList<Result> results) {
        int index = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isAttempted(results)) {
                index = i;
            }
        }
        return index;
    }

    public ArrayList<MainMenuItem> getAllPassedTasks(ArrayList<Result> results) {
        ArrayList<MainMenuItem> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isPassed(results)) {
                result.add(new MainMenuItem(task));
            }
        }
        return result;
    }

    private List<Task> safe( List<Task> other ) {
        return other == null ? Collections.EMPTY_LIST : other;
    }
}
