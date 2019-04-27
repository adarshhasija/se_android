package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by faimac on 10/23/17.
 */

@IgnoreExtraProperties
public class Result implements Parcelable {

    public String uid;
    public String userId;
    //public int game_id; //id for task as INT old
    public long task_id;
    public long startTimeMillis;
    public long timeTakenMillis;
    public long timestamp;
    public int items_attempted = -1; //Item = One list item in array(content/tap/swipe). -1 = error, no valueString
    public int items_correct = -1;
    public ArrayList<Response> responses;

    public long getTimestamp() {
        return timestamp;
    }

    public Result() {
        // Default constructor required for calls to DataSnapshot.getValueString(Post.class)
    }

    public Result(Map<String, Object> map) {
        this.uid = (String) map.get("uid");
        this.userId = (String) map.get("userId");
        this.task_id = map.containsKey("game_id") ? (Long) map.get("game_id") : //If game_id exists, take game_id
                        map.containsKey("task_id") ? (Long) map.get("task_id") : 0;  //Else take task_id
        this.startTimeMillis = map.containsKey("startTimeMillis") ? (Long) map.get("startTimeMillis") : -1 ;
        this.timeTakenMillis = (Long) map.get("timeTakenMillis");
        this.timestamp = map.containsKey ("timestamp") ? (Long) map.get("timestamp") : Calendar.getInstance().getTimeInMillis();
        ////Set responses
        ArrayList<HashMap<String, Object>> mpArrayList = (ArrayList<HashMap<String, Object>>) map.get("responses");
        if (mpArrayList != null) {
            this.responses = new ArrayList<>();
            for (Object mp : mpArrayList) {
                if (mp instanceof Response) {
                    this.responses.add((Response) mp);
                }
                else {
                    this.responses.add(new Response((HashMap<String, Object>) mp));
                }
            }
        }
        ////
        this.items_attempted = map.containsKey("items_attempted") ? ((Long) map.get("items_attempted")).intValue() :
                                    map.containsKey("words_total_finished") ? ((Long) map.get("words_total_finished")).intValue() : -1; //Backward compatibility. Some use words_attempted
        this.items_correct = map.containsKey("items_correct") ? ((Long) map.get("items_correct")).intValue() :
                                    map.containsKey("words_correct") ? ((Long) map.get("words_correct")).intValue() : -1; //Backward compatibility. Some use words_correct
    }

    protected Result(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        task_id = in.readLong();
        startTimeMillis = in.readLong();
        timeTakenMillis = in.readLong();
        timestamp = in.readLong();
        items_attempted = in.readInt();
        items_correct = in.readInt();
        responses = in.readArrayList(Response.class.getClassLoader());
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

    public long getTask_id() {
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
        result.put("startTimeMillis", startTimeMillis);
        result.put("timeTakenMillis", timeTakenMillis);
        result.put("timestamp", timestamp);
        result.put("items_attempted", items_attempted);
        result.put("items_correct", items_correct);
        result.put("responses", responses);

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
        dest.writeLong(task_id);
        dest.writeLong(startTimeMillis);
        dest.writeLong(timeTakenMillis);
        dest.writeLong(timestamp);
        dest.writeInt(items_attempted);
        dest.writeInt(items_correct);
        dest.writeList(responses);
    }
}
