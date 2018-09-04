package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 3/1/17.
 */

public class Topic extends SEBaseObject implements Parcelable {

    public String description;
    public int index;
    public Map<String, SENestedObject> questions = new HashMap<>();

    public Topic() {
        super();
    }

    public Topic(HashMap<String, Object> map) {
        super(map);
        this.description = map.containsKey("description") ? (String) map.get("description") : null;
        this.index = map.containsKey("index") ? ((Long) map.get("index")).intValue() : -1;
    }

    protected Topic(Parcel in) {
        super(in);
        description = in.readString();
        index = in.readInt();
        in.readMap(questions, getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(description);
        dest.writeInt(index);
        dest.writeMap(questions);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Topic> CREATOR = new Creator<Topic>() {
        @Override
        public Topic createFromParcel(Parcel in) {
            return new Topic(in);
        }

        @Override
        public Topic[] newArray(int size) {
            return new Topic[size];
        }
    };

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addQuestion(SENestedObject question) { this.questions.put(question.uid, question); }

    public void removeQuestion(String questionId) { this.questions.remove(questionId); }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("description", description);
        result.put("index", index);
        result.put("questions", questions);

        return result;
    }


}
