package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 5/29/17.
 */

public class Response implements Parcelable{

    public String uid;
    public String userId;
    public String questionId;
    public String topicId;
    //public long marks;
    public String remarks;
    public long timestamp; //timestamp according to on-device clock. Only used to calculate how long user took to answer

    public String question;
    public String expectedAnswer;
    public String answer;
    public boolean isCorrect;


    public Response() {
        //super();
    }

    public Response(String question, String expectedAnswer, String answer, boolean isCorrect) {
        this.question = question;
        this.expectedAnswer = expectedAnswer;
        this.answer = answer;
        this.isCorrect = isCorrect;
        this.timestamp = System.currentTimeMillis();
    }

    public Response(Map<String, Object> map) {
        this.question = (String) map.get("question");
        this.expectedAnswer = (String) map.get("expectedAnswer");
        this.answer = (String) map.get("answer");
        this.isCorrect = (Boolean) map.get("isCorrect");
        this.timestamp = (Long) map.get("timestamp");
    }

    protected Response(Parcel in) {
        uid = in.readString();
        userId = in.readString();
        questionId = in.readString();
        topicId = in.readString();
        //marks = in.readLong();
        remarks = in.readString();
        timestamp = in.readLong();

        question = in.readString();
        expectedAnswer = in.readString();
        answer = in.readString();
        isCorrect = in.readByte() != 0;
    }

    public static final Creator<Response> CREATOR = new Creator<Response>() {
        @Override
        public Response createFromParcel(Parcel in) {
            return new Response(in);
        }

        @Override
        public Response[] newArray(int size) {
            return new Response[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userId", userId);
        result.put("questionId", questionId);
        result.put("topicId", topicId);
        //result.put("marks", marks);
        result.put("remarks", remarks);
        result.put("timestamp", timestamp);

        result.put("question", question);
        result.put("expectedAnswer", expectedAnswer);
        result.put("answer", answer);
        result.put("isCorrect", isCorrect);

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
        //dest.writeLong(marks);
        dest.writeString(remarks);
        dest.writeLong(timestamp);

        dest.writeString(question);
        dest.writeString(expectedAnswer);
        dest.writeString(answer);
        dest.writeByte((byte) (isCorrect ? 1 : 0));
    }


}
