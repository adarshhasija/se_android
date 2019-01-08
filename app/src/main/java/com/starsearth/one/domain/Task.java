package com.starsearth.one.domain;

import android.content.Context;
import android.os.Parcel;

import com.google.firebase.database.Exclude;
import com.starsearth.one.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by faimac on 1/30/18.
 */

public class Task extends SETeachingContent {

    public static String FAIL_REASON = "fail_reason";
    public static String NO_ATTEMPT = "no_attempt";
    public static String GESTURE_SPAM = "gesture_spam";
    public static String BACK_PRESSED = "back_button_pressed";
    public static String HOME_BUTTON_TAPPED = "home_button_tapped";
    public static String NO_MORE_CONTENT = "no_more_content";

    public List<String> content = new ArrayList<>(); //Has to be List<String> to save to Firebase
    public List<String> tap = new ArrayList<>();
    public List<String> swipe = new ArrayList<>();
    public Type type;
    public boolean ordered; //should the content be shown in same order to the user
    public boolean timed = false;
    public int durationMillis;
    public boolean isTextVisibleOnStart         = true;
    public boolean submitOnReturnTapped         = false; //submit the activity when user has tapped return
    public boolean isPassFail                   = false;
    public int passPercentage                   = 0; //Relevant only if task is type isPassFail = true
    public boolean showUserAnswerWithBackground = false;
    public boolean isBackspaceAllowed           = true;
    public boolean isKeyboardRequired           = false;
    public boolean isExitOnInterruption         = false;
    public boolean isGame                       = false;    //As of July 2018, all timed tasks are considered games
    public boolean isOwnerWantingAds            = false;    //Owner of the task might want to earn money from task through ads

    public Type getType() {
        return type;
    }

    public enum Type {
        TYPING(1),
        KEYBOARD_TEST(3),
        TAP_SWIPE(4),
        SPELLING(5)
        ;

        private final long value;

        Type(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public static Type fromInt(long i) {
            for (Type type : Type.values()) {
                if (type.getValue() == i) { return type; }
            }
            return null;
        }
    };

    public Task() {
        super();
    }

    protected Task(Parcel in) {
        super(in);
        content = in.readArrayList(String.class.getClassLoader());
        tap = in.readArrayList(String.class.getClassLoader());
        swipe = in.readArrayList(String.class.getClassLoader());
        type = Type.fromInt(in.readInt());
        ordered = in.readByte() != 0;
        timed = in.readByte() != 0;
        durationMillis = in.readInt();
        isTextVisibleOnStart = in.readByte() != 0;
        submitOnReturnTapped = in.readByte() != 0;
        isPassFail = in.readByte() != 0;
        passPercentage = in.readInt();
        showUserAnswerWithBackground = in.readByte() != 0;
        isBackspaceAllowed = in.readByte() != 0;
        isKeyboardRequired = in.readByte() != 0;
        isExitOnInterruption = in.readByte() != 0;
        isGame = in.readByte() != 0;
        isOwnerWantingAds = in.readByte() != 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = (HashMap<String, Object>) super.toMap();
        result.put("content", content);
        result.put("tap", tap);
        result.put("swipe", swipe);
        result.put("type", type.getValue());
        result.put("ordered", ordered);
        result.put("timed", timed);
        result.put("durationMillis", durationMillis);
        result.put("isTextVisibleOnStart", isTextVisibleOnStart);
        result.put("submitOnReturnTapped", submitOnReturnTapped);
        result.put("isPassFail", isPassFail);
        result.put("passPercentage", passPercentage);
        result.put("showUserAnswerWithBackground", showUserAnswerWithBackground);
        result.put("isBackspaceAllowed", isBackspaceAllowed);
        result.put("isKeyboardRequired", isKeyboardRequired);
        result.put("isExitOnInterruption", isExitOnInterruption);
        result.put("isGame", isGame);
        result.put("isOwnerWantingAds", isOwnerWantingAds);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(content);
        dest.writeList(tap);
        dest.writeList(swipe);
        dest.writeInt((int) type.getValue());
        dest.writeByte((byte) (ordered ? 1 : 0));
        dest.writeByte((byte) (timed ? 1 : 0));
        dest.writeInt(durationMillis);
        dest.writeByte((byte) (isTextVisibleOnStart ? 1 : 0));
        dest.writeByte((byte) (submitOnReturnTapped ? 1 : 0));
        dest.writeByte((byte) (isPassFail ? 1 : 0));
        dest.writeInt(passPercentage);
        dest.writeByte((byte) (showUserAnswerWithBackground ? 1 : 0));
        dest.writeByte((byte) (isBackspaceAllowed ? 1 : 0));
        dest.writeByte((byte) (isKeyboardRequired ? 1 : 0));
        dest.writeByte((byte) (isExitOnInterruption ? 1 : 0));
        dest.writeByte((byte) (isGame ? 1 : 0));
        dest.writeByte((byte) (isOwnerWantingAds ? 1 : 0));
    }

    /*
    If content should be returned in any order
    Type: typing
     */
    public String getNextItemTyping() {
        Random random = new Random();
        int i = random.nextInt(content.size());
        return content.get(i);
    }


    /*
    If content should be returned in any order
    Type: gesture
     */
    public Map<String, Boolean> getNextItemGesture() {
        Map<String, Boolean> map = new HashMap<>();
        Random random = new Random();
        int i = random.nextInt(2);
        if (i % 2 == 0 && tap.size() > 0) {
            map.put(tap.get(random.nextInt(tap.size())), true);
        }
        else if (swipe.size() > 0) {
            map.put(swipe.get(random.nextInt(swipe.size())), false);
        }
        return map;
    }

    /*
        If content is meant to be returned in order
        Input: Exact index OR number of words completed
        Function takes modulo and returns the exact item
        Return content at index
     */
    public String getNextItemTyping(int index) {
        return content.get(index % content.size());
    }


    public String getTimeLimitAsString(Context context) {
        StringBuffer buf = new StringBuffer();
        if (durationMillis >= 120000) {
            //2 mins or more
            int mins = durationMillis/60000;
            buf.append(mins + " " + context.getResources().getString(R.string.minutes) + ".");
        }
        else {
            int mins = 1;
            buf.append(mins + " " + context.getResources().getString(R.string.minute) + ".");
        }
        return buf.toString();
    }

    //Swiping tasks will return false
    public boolean isTaskItemsCompleted(long itemsAttempted) {
        boolean result = false;
        if (type != Type.TAP_SWIPE && itemsAttempted >= content.size()) {
            result = true;
        }
        return result;
    }

    public boolean isAttempted(List<Result> results) {
        boolean ret = false;
        for (Object result : results) {
            if (result instanceof ResultTyping) {
                if (((ResultTyping) result).task_id == id) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public boolean isPassed(List<Result> results) {
        boolean ret = false;
        for (Object result : results) {
            if (result instanceof ResultTyping) {
                if (((ResultTyping) result).task_id == id) {
                    int accuracy = ((ResultTyping) result).getAccuracy();
                    if (accuracy >= passPercentage) {
                        ret = true;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    public boolean isPassed(Result result) {
        boolean ret = false;
        if (result instanceof ResultTyping && ((ResultTyping) result).task_id == id) {
            int accuracy = ((ResultTyping) result).getAccuracy();
            if (accuracy >= passPercentage) {
                ret = true;
            }
        }
        return ret;
    }

    public Result getHighScoreResult(ArrayList<Result> results) {
        Result ret = null;
        for (Result result : results) {
            if (ret == null) {
                ret = result;
            }
            else if (result.items_correct > ret.items_correct) {
                ret = result;
            }
        }
        return ret;
    }

}
