package com.starsearth.one.domain;

import android.content.Context;

import com.google.firebase.database.Exclude;
import com.starsearth.one.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 3/8/18.
 */

public class ResultTyping extends Result {

    public int characters_correct;
    public int characters_total_attempted;
    public int words_correct;
    public int words_total_finished;

    public int getCharacters_correct() {
        return characters_correct;
    }

    public int getWords_correct() {
        return words_correct;
    }

    public void setCharacters_correct(int characters_correct) {
        this.characters_correct = characters_correct;
    }

    public void setCharacters_total_attempted(int characters_total_attempted) {
        this.characters_total_attempted = characters_total_attempted;
    }

    public void setWords_correct(int words_correct) {
        this.words_correct = words_correct;
    }

    public void setWords_total_finished(int words_total_finished) {
        this.words_total_finished = words_total_finished;
    }

    public ResultTyping() {
        super();
    }

    public ResultTyping(String uid, String userId, int characters_correct, int characters_total_attempted,
                        int words_correct, int words_total_finished, long timeTakenMillis, int gameId) {
        super(uid, userId, timeTakenMillis, gameId);
        this.characters_correct = characters_correct;
        this.characters_total_attempted = characters_total_attempted;
        this.words_correct = words_correct;
        this.words_total_finished = words_total_finished;
    }

    private int getTimeMins() {
        int timeMins = (int) timeTakenMillis/60000;
        if (timeMins < 1) {
            //If time taken was less than 1 min
            timeMins = 1;
        }

        return timeMins;
    }

    private int getSpeedWPM() {
        int x = words_correct;
        int y = getTimeMins();
        return (x*y > 0) ? (words_correct/getTimeMins()) : 0;
    }

    private int getAccuracy() {
        double accuracy = (double) words_correct/words_total_finished;
        double accuracyPercentage = Math.ceil(accuracy*100);
        int accuracyPercentageInt = (int) accuracyPercentage;
        return accuracyPercentageInt;
    }

    public String getScoreSummary(Context context) {
        String result = context.getString(R.string.accuracy) + " " + getAccuracy() + "%" +
                "\n" + context.getString(R.string.speed) + " " + getSpeedWPM() + " " + "wpm";
        return result;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = (HashMap<String, Object>) super.toMap();
        result.put("characters_correct", characters_correct);
        result.put("characters_total_attempted", characters_total_attempted);
        result.put("words_correct", words_correct);
        result.put("words_total_finished", words_total_finished);

        return result;
    }
}
