package com.starsearth.one.domain;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 10/23/17.
 */

@IgnoreExtraProperties
public class Result {

    public String uid;
    public String userId;
    public int game_id; //id for task as INT old
    public int task_id;
    public long timeTakenMillis;
    public long timestamp;

    public Result() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Result(String uid, String userId, long timeTakenMillis, int taskId) {
        this.uid = uid;
        this.userId = userId;
        this.timeTakenMillis = timeTakenMillis;
        this.task_id = taskId;

    }

    public Result(Map<String, Object> map) {
        this.uid = (String) map.get("uid");
        this.userId = (String) map.get("userId");
        this.task_id = map.containsKey("game_id") ? ((Long) map.get("game_id")).intValue() :
                        map.containsKey("task_id") ? ((Long) map.get("task_id")).intValue() : 0;
        this.timeTakenMillis = (Long) map.get("timeTakenMillis");
        this.timestamp = (Long) map.get("timestamp");
    }

    public int getTask_id() {
        return task_id;
    }

    public void setGame_id(int game_id) {
        this.task_id = game_id;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userId", userId);
        result.put("task_id", task_id);
        result.put("timeTakenMillis", timeTakenMillis);
        result.put("timestamp", timestamp);

        return result;
    }

    /**
     *
     * @return Returns true if the result was created within 5 seconds of current time
     */
    public boolean isJustCompleted() {
        boolean result = false;
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();
        if (Math.abs(currentTime - timestamp) < 5000) {
            result = true;
        }
        return result;
    }

}
