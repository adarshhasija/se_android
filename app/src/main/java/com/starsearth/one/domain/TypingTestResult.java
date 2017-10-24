package com.starsearth.one.domain;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 10/23/17.
 */

@IgnoreExtraProperties
public class TypingTestResult {

    public String uid;
    public String userId;
    public int score;
    public int total;
    public long timeTakenMillis;

    public TypingTestResult() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public TypingTestResult(String uid, String userId, int score, int total, long timeTakenMillis) {
        this.uid = uid;
        this.userId = userId;
        this.score = score;
        this.total = total;
        this.timeTakenMillis = timeTakenMillis;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userId", userId);
        result.put("score", score);
        result.put("total", total);
        result.put("timeTakenMillis", timeTakenMillis);

        return result;
    }

}
