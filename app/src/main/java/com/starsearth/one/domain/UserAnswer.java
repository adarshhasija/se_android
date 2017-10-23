package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 5/29/17.
 */

public class UserAnswer  implements Parcelable{

    public String uid;
    public String userId;
    public String questionId;
    public String topicId;
    public String userAnswer;
    public long marks;
    public String remarks;
    public long timeSpentMillis;
    public long timestamp;

    public UserAnswer() {
        super();
    }

    public UserAnswer(String uid, String questionId, String userId, String userAnswer, long timeSpentMillis, String topicId) {
        this.uid = uid;
        this.questionId = questionId;
        this.userId = userId;
        this.userAnswer = userAnswer;
        this.timeSpentMillis = timeSpentMillis;
        this.topicId = topicId;
    }

    protected UserAnswer(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        questionId = in.readString();
        topicId = in.readString();
        userAnswer = in.readString();
        marks = in.readLong();
        remarks = in.readString();
        timeSpentMillis = in.readLong();
        timestamp = in.readLong();
    }

    public static final Creator<UserAnswer> CREATOR = new Creator<UserAnswer>() {
        @Override
        public UserAnswer createFromParcel(Parcel in) {
            return new UserAnswer(in);
        }

        @Override
        public UserAnswer[] newArray(int size) {
            return new UserAnswer[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userId", userId);
        result.put("questionId", questionId);
        result.put("topicId", topicId);
        result.put("userAnswer", userAnswer);
        result.put("marks", marks);
        result.put("remarks", remarks);
        result.put("timeSpentMillis", timeSpentMillis);
        result.put("timestamp", timestamp);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(userId);
        dest.writeString(questionId);
        dest.writeString(topicId);
        dest.writeString(userAnswer);
        dest.writeLong(timeSpentMillis);
        dest.writeLong(timestamp);
    }


}
