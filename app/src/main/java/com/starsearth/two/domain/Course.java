package com.starsearth.two.domain;

import android.os.Parcel;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Adarsh Hasija on 2/24/17.
 * Courses have a title and a list of organized tasks. Tasks are done in order
 * When a task is done, course list is not re-ordered
 */

@IgnoreExtraProperties
public class Course extends SETeachingContent {

    public String type;
    public String description;
    public boolean usbKeyboard = false;
    public boolean hasKeyboardTest = false;
    //public Map<String, Boolean> lessons = new HashMap<>();
    public Map<String, SENestedObject> lessons = new HashMap<>();
    public List<Task> tasks;
    public String attemptedByUserId = null; //The user who started an attempt on this Course
    public HashMap<String, Checkpoint> checkpoints; //Checkpoint after task with key == id
    public Boolean isOwnerWantingAds = false;

    public Course() {
        super();
        // Default constructor required for calls to DataSnapshot.getValueString(Course.class)
    }

    public Course(HashMap<String, Object> map) {
        super(map);
        this.uid = map.containsKey("key") ? (String) map.get("key") : null;
        this.type = map.containsKey("type") ? (String) map.get("type") : null;
        this.description = map.containsKey("description") ? (String) map.get("description") : null;
        this.usbKeyboard = map.containsKey("usbKeyboard") ? (Boolean) map.get("usbKeyboard") : false;
        this.hasKeyboardTest = map.containsKey("hasKeyboardTest") ? (Boolean) map.get("hasKeyboardTest") : false;
        //this.tasks = map.containsKey("tasks") ? (List<Task>) map.get("tasks") : null;
        ////Set tasks list
        ArrayList<HashMap<String, Object>> mpArrayListTasks = (ArrayList<HashMap<String, Object>>) map.get("tasks");
        if (mpArrayListTasks != null) {
            this.tasks = new ArrayList<>();
            for (Object mp : mpArrayListTasks) {
                if (mp instanceof Task) {
                    this.tasks.add((Task) mp);
                }
            }
        }
        ////
        ////Set checkpoints map
        HashMap<String, Checkpoint> mpCheckpoints = (HashMap<String, Checkpoint>) map.get("checkpoints");
        if (mpCheckpoints != null) {
            this.checkpoints = new HashMap<>();
            for (Map.Entry<String, Checkpoint> entry : mpCheckpoints.entrySet())
            {
                this.checkpoints.put(entry.getKey(), entry.getValue());
            }
        }
        ////
        this.attemptedByUserId = map.containsKey("attemptedByUserId") ? (String) map.get("attemptedByUserId") : null;
        this.hasKeyboardTest = map.containsKey("isOwnerWantingAds") ? (Boolean) map.get("isOwnerWantingAds") : false;
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
        description = in.readString();
        usbKeyboard = in.readByte() != 0;
        hasKeyboardTest = in.readByte() != 0;
        lessons = in.readHashMap(getClass().getClassLoader());
        tasks = in.readArrayList(Task.class.getClassLoader());
        attemptedByUserId = in.readString();
        checkpoints = in.readHashMap(getClass().getClassLoader());
        isOwnerWantingAds = in.readByte() != 0;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //public void addLesson(String lessonsId) { this.lessons.put(lessonsId, true); }
    //public void addLesson(String lessonId, SENestedObject valueString) { this.lessons.put(lessonId, valueString); }
    public void addLesson(SENestedObject value) { this.lessons.put(value.uid, value); }

    public void removeLesson(String lessonId) { this.lessons.remove(lessonId); }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("type", type);
        result.put("description", description);
        result.put("usbKeyboard", usbKeyboard);
        result.put("hasKeyboardTest", hasKeyboardTest);
        result.put("lessons", lessons);
        result.put("tasks", tasks);
        result.put("attemptedByUserId", attemptedByUserId);
        result.put("checkpoints", checkpoints);
        result.put("isOwnerWantingAds", isOwnerWantingAds);

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
        dest.writeString(description);
        dest.writeByte((byte) (usbKeyboard ? 1 : 0));
        dest.writeByte((byte) (hasKeyboardTest? 1 : 0));
        dest.writeMap(lessons);
        dest.writeList(tasks);
        dest.writeString(attemptedByUserId);
        dest.writeMap(checkpoints);
        dest.writeByte((byte) (isOwnerWantingAds? 1 : 0));
    }

    public boolean isTaskExists(String taskId) {
        boolean result = false;
        for (Task t : safe(tasks)) {
            if (t.uid.equals(taskId)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public Task getTaskById(String id) {
        Task result = null;
        for (Task t : safe(tasks)) {
            if (t.uid.equals(id)) {
                result = t;
                break;
            }
        }
        return result;
    }

    /*
        Returns index of last passed task, -1 if no tasks were passed
     */
    public int getIndexOfLastPassedTask(List<Result> results) {
        int lastPassedTaskIndex = -1;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isPassed(results)) {
                lastPassedTaskIndex = i;
            }
        }
        return lastPassedTaskIndex;
    }

    public long getIdOfLastPassedTask(ArrayList<Result> results) {
        long lastPassedTaskId = -1;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isPassed(results)) {
                lastPassedTaskId = task.id;
            }
        }
        return lastPassedTaskId;
    }

    //This checks if the last Result is that of the last Task in the Course list
    public boolean isCourseComplete(List<Result> results) {
        boolean result = false;
        int indexOfLastPassedTask = getIndexOfLastPassedTask(results);
        if (indexOfLastPassedTask == tasks.size()-1) {
            result = true;
        }
        return result;
    }

    //Returns true if results contains items relating to the first task
    public boolean isCourseStarted(ArrayList<Result> results) {
        boolean ret = false;
        if (tasks.size() > 0) {
            Task task = tasks.get(0);
            if (task.isAttempted(results)) {
                ret = true;
            }
        }
        return ret;
    }

    public boolean isFirstTaskPassed(List<Result> results) {
        boolean ret = false;
        if (tasks.size() > 0) {
            Task task = tasks.get(0);
            if (task.isPassed(results)) {
                ret = true;
            }
        }
        return ret;
    }

    /*
        Returns the next task in the course. If this is the last task, will return null
     */
    public Task getNextTask(List<Result> allResults) {
        Task task = null;
        int indexOfLastPassedTask = getIndexOfLastPassedTask(allResults);
        int indexOfNextTask = indexOfLastPassedTask + 1;
        if (indexOfNextTask < tasks.size()) {
            task = tasks.get(indexOfNextTask);
        }

        return task;
    }

    public int getNextTaskIndex(List<Result> allResults) {
        int retIndex = -1;
        int currentTaskIndex = getIndexOfLastAttemptedTask(allResults);
        if (currentTaskIndex < tasks.size()) {
            Task currentTask = tasks.get(currentTaskIndex);
            if (currentTask.isPassed(allResults) && currentTaskIndex + 1 < tasks.size()) {
                retIndex = currentTaskIndex + 1;
            }
            else {
                retIndex = currentTaskIndex;
            }
        }
        return retIndex;
    }


    /*
    Gets the index of the last attempted task, 0 if no task was attempted
     */
    public int getIndexOfLastAttemptedTask(List<Result> results) {
        int index = 0;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.isAttempted(results)) {
                index = i;
            }
        }
        return index;
    }

    public ArrayList<RecordItem> getAllPassedTasks(ArrayList<Result> results) {
        ArrayList<RecordItem> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isPassed(results)) {
                result.add(new RecordItem(task));
            }
        }
        return result;
    }

    public ArrayList<RecordItem> getAllAttemptedTasks(ArrayList<Result> results) {
        ArrayList<RecordItem> result = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isAttempted(results)) {
                result.add(new RecordItem(task));
            }
        }
        return result;
    }

    public boolean isCheckpointReached(Result result) {
        return this.checkpoints.containsKey(result.task_id) && this.getTaskById(result.task_id).isPassed(result);
    }

    //Returns yes if the next task to be attempted is a checkpoint or if its the last task in a Course
    public boolean shouldGenerateAd(List<Result> results) {
        return this.checkpoints.containsKey(getNextTask(results).id) || isLastTask(getNextTask(results).id);
    }

    public boolean isLastTask(long taskId) {
        return this.tasks.get(tasks.size() - 1).id == taskId;
    }

    public boolean shouldShowAd(Result lastAttemptResult) {
        boolean isCourseFinished = false;
        boolean isCheckpointReached = false;
        Task lastAttemptTask = getTaskById(lastAttemptResult.task_id);
        if (lastAttemptTask.isPassed(lastAttemptResult)) {
            isCourseFinished = lastAttemptTask.id == tasks.get(tasks.size() - 1).id;
            isCheckpointReached = this.checkpoints.containsKey(lastAttemptTask.id);
        }
        return isCourseFinished || isCheckpointReached;
    }

    private List<Task> safe( List<Task> other ) {
        return other == null ? Collections.EMPTY_LIST : other;
    }
}
