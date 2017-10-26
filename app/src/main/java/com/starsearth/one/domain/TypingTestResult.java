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
    public int characters_correct;
    public int characters_total_attempted;
    public int words_correct;
    public int words_total_finished;
    public long timeTakenMillis;

    public TypingTestResult() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public TypingTestResult(String uid, String userId, int characters_correct, int characters_total_attempted,
                                    int words_correct, int words_total_finished, long timeTakenMillis) {
        this.uid = uid;
        this.userId = userId;
        this.characters_correct = characters_correct;
        this.characters_total_attempted = characters_total_attempted;
        this.words_correct = words_correct;
        this.words_total_finished = words_total_finished;
        this.timeTakenMillis = timeTakenMillis;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userId", userId);
        result.put("characters_correct", characters_correct);
        result.put("characters_total_attempted", characters_total_attempted);
        result.put("words_correct", words_correct);
        result.put("words_total_finished", words_total_finished);
        result.put("timeTakenMillis", timeTakenMillis);

        return result;
    }

}
