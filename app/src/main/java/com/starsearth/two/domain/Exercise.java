package com.starsearth.two.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 3/1/17.
 */

public class Exercise extends SETeachingContent implements Parcelable{

    public String description;
    public int index;
    public Map<String, SENestedObject> questions = new HashMap<>();

    public Exercise() {

    }

    public Exercise(HashMap<String, Object> map) {
        super(map);
        this.description = map.containsKey("description") ? (String) map.get("description") : null;
        this.index = map.containsKey("index") ? ((Long) map.get("index")).intValue() : -1;
    }

    protected Exercise(Parcel in) {
        super(in);
        description = in.readString();
        index = in.readInt();
        questions = in.readHashMap(getClass().getClassLoader());
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addQuestion(SENestedObject question) { questions.put(question.uid, question); }

    public void removeQuestion(String questionId) { questions.remove(questionId); }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("description", description);
        result.put("index", index);
        result.put("questions", questions);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(description);
        dest.writeInt(index);
        dest.writeMap(questions);
    }
}
