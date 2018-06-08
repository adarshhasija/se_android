package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 10/23/17.
 */

@IgnoreExtraProperties
public class Result implements Parcelable {

    public String uid;
    public String userId;
    //public int game_id; //id for task as INT old
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

    protected Result(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        //game_id = in.readInt();
        task_id = in.readInt();
        timeTakenMillis = in.readLong();
        timestamp = in.readLong();
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

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
        //result.put("game_id", game_id);
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
        if (Math.abs(currentTime - timestamp) < 500) {
            result = true;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this.uid.equals(((Result) obj).uid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(uid);
        dest.writeString(userId);
        //dest.writeInt(game_id);
        dest.writeInt(task_id);
        dest.writeLong(timeTakenMillis);
        dest.writeLong(timestamp);
    }
}
