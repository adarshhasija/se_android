package com.starsearth.one.domain;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 10/23/17.
 */

@IgnoreExtraProperties
public class Result {

    public String uid;
    public String userId;
    public int characters_correct;
    public int characters_total_attempted;
    public int words_correct;
    public int words_total_finished;
    public String subject;
    public int level;
    public int game_type;
    public String level_string;
    public long timeTakenMillis;
    public long timestamp;

    public Result() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Result(String uid, String userId, int characters_correct, int characters_total_attempted,
                  int words_correct, int words_total_finished, String subject, int level, String levelString, int gameType, long timeTakenMillis) {
        this.uid = uid;
        this.userId = userId;
        this.characters_correct = characters_correct;
        this.characters_total_attempted = characters_total_attempted;
        this.words_correct = words_correct;
        this.words_total_finished = words_total_finished;
        this.subject = subject;
        this.level = level;
        this.game_type = gameType;
        this.level_string = levelString;
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
        result.put("subject", subject);
        result.put("level", level);
        result.put("level_string", level_string);
        result.put("game_type", game_type);
        result.put("timeTakenMillis", timeTakenMillis);
        result.put("timestamp", timestamp);

        return result;
    }

}
