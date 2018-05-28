package com.starsearth.one.domain;

import android.os.Parcel;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 3/2/17.
 */

public class Question extends SEBaseObject {

    public String answer;
    public int index;
    public String instruction;
    public String questionType;
    public int repeats;
    public String hint;
    public float positiveWeight;
    public float negativeWeight;
    public String feedbackCorrectAnswer;
    public String feedbackWrongAnswer;

    public Question() {
        super();
    }

    public Question(String uid, String title, String instructions, String answer, String hint, int index, float positiveWeight, float negativeWeight,
                        String feedbackCorrectAnswer, String feedbackWrongAnswer, String createdBy, String parentId,
                            String instruction, int repeats, String questionType) {
        super(uid, title, instructions, createdBy, "exercise", parentId);
        this.answer = answer;
        this.hint = hint;
        this.index = index;
        this.instruction = instruction;
        this.questionType = questionType;
        this.repeats = repeats;
        this.positiveWeight = positiveWeight;
        this.negativeWeight = negativeWeight;
        this.feedbackCorrectAnswer = feedbackCorrectAnswer;
        this.feedbackWrongAnswer = feedbackWrongAnswer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public float getPositiveWeight() {
        return positiveWeight;
    }

    public void setPositiveWeight(float positiveWeight) {
        this.positiveWeight = positiveWeight;
    }

    public float getNegativeWeight() {
        return negativeWeight;
    }

    public void setNegativeWeight(float negativeWeight) {
        this.negativeWeight = negativeWeight;
    }

    public String getFeedbackCorrectAnswer() {
        return feedbackCorrectAnswer;
    }

    public String getFeedbackWrongAnswer() {
        return feedbackWrongAnswer;
    }

    public void setFeedbackCorrectAnswer(String feedbackCorrectAnswer) {
        this.feedbackCorrectAnswer = feedbackCorrectAnswer;
    }

    public void setFeedbackWrongAnswer(String feedbackWrongAnswer) {
        this.feedbackWrongAnswer = feedbackWrongAnswer;
    }

    protected Question(Parcel in) {
        super(in);
        answer = in.readString();
        index = in.readInt();
        instruction = in.readString();
        questionType = in.readString();
        repeats = in.readInt();
        hint = in.readString();
        positiveWeight = in.readFloat();
        negativeWeight = in.readFloat();
        feedbackCorrectAnswer = in.readString();
        feedbackWrongAnswer = in.readString();
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("answer", answer);
        result.put("index", index);
        result.put("instruction", instruction);
        result.put("questionType", questionType);
        result.put("repeats", repeats);
        result.put("hint", hint);
        result.put("positiveWeight", positiveWeight);
        result.put("negativeWeight", negativeWeight);
        result.put("feedbackCorrectAnswer", feedbackCorrectAnswer);
        result.put("feedbackWrongAnswer", feedbackWrongAnswer);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(answer);
        dest.writeInt(index);
        dest.writeString(instruction);
        dest.writeString(questionType);
        dest.writeInt(repeats);
        dest.writeString(hint);
        dest.writeFloat(positiveWeight);
        dest.writeFloat(negativeWeight);
        dest.writeString(feedbackCorrectAnswer);
        dest.writeString(feedbackWrongAnswer);
    }


}
