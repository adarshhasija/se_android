package com.starsearth.one.domain;

import android.content.Context;
import android.os.Parcel;

import com.google.firebase.database.Exclude;
import com.starsearth.one.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 4/3/18.
 */

public class ResultGestures extends Result {

    public int items_attempted;
    public int items_correct;

    public ResultGestures() {
        super();
    }

    public ResultGestures(String uid, String userId, int attemped, int correct, long timeTakenMillis, int gameId, ArrayList<Response> responses) {
        super(uid, userId, timeTakenMillis, gameId, responses);
        this.items_attempted = attemped;
        this.items_correct = correct;
    }

    public ResultGestures(Map<String, Object> map) {
        this.items_correct = (Integer) map.get("items_correct");
        this.items_attempted = (Integer) map.get("items_attempted");
    }

    /*
    String should be shown as Toast when user has just finished task
     */
    public String getResultToast() {
        StringBuffer result = new StringBuffer();
        result.append(items_correct);
        return result.toString();
    }

    protected ResultGestures(Parcel in) {
        super(in);
        items_attempted = in.readInt();
        items_correct = in.readInt();
    }

    public static final Creator<ResultGestures> CREATOR = new Creator<ResultGestures>() {
        @Override
        public ResultGestures createFromParcel(Parcel in) {
            return new ResultGestures(in);
        }

        @Override
        public ResultGestures[] newArray(int size) {
            return new ResultGestures[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = (HashMap<String, Object>) super.toMap();
        result.put("items_attempted", items_attempted);
        result.put("items_correct", items_correct);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(items_attempted);
        dest.writeInt(items_correct);
    }
}
