package com.starsearth.one.domain;

import android.content.Context;

import com.google.firebase.database.Exclude;
import com.starsearth.one.R;

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

    public ResultGestures(String uid, String userId, int attemped, int correct, long timeTakenMillis, int gameId) {
        super(uid, userId, timeTakenMillis, gameId);
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

    public String getScoreSummary(Context context, Task.Type taskType) {
        StringBuffer result = new StringBuffer();
        switch (taskType) {
            case TAP_SWIPE_TIMED:
                result.append(Integer.valueOf(items_correct));
                break;
            default: break;
        }
        return result.toString();
    }

    public String getExplanationSummary(Context context, Task.Type taskType) {
        StringBuffer result = new StringBuffer();
        result.append(context.getResources().getString(R.string.high_score));
        return result.toString();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = (HashMap<String, Object>) super.toMap();
        result.put("items_correct", items_correct);
        result.put("items_attempted", items_attempted);

        return result;
    }
}
