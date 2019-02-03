package com.starsearth.one.domain;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;

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

    public List<String> content = new ArrayList<>(); //Has to be List<String> to save to FirebaseManager
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

    public static enum ResponseViewType {
        CHARACTER("CHARACTER"), //View Responses at a character level
        WORD("WORD")    //View Responses at a word level
        ;
        private final String value;

        ResponseViewType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ResponseViewType fromString(String i) {
            for (ResponseViewType responseViewType : ResponseViewType.values()) {
                if (responseViewType.getValue().equals(i)) { return responseViewType; }
            }
            return null;
        }
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

    public Object getNextItem() {
        Object ret = null;
        Random random = new Random();
        int i;
        switch (this.type) {
            case TYPING:
            case SPELLING:
                i = random.nextInt(content.size());
                ret = content.get(i);
                break;
            case TAP_SWIPE:
                ret = new HashMap<>();
                i = random.nextInt(2);
                if (i % 2 == 0 && tap.size() > 0) {
                    ((HashMap) ret).put(tap.get(random.nextInt(tap.size())), true);
                }
                else if (swipe.size() > 0) {
                    ((HashMap) ret).put(swipe.get(random.nextInt(swipe.size())), false);
                }
                break;
            default:
                break;
        }
        return ret;
    }

    public Object getNextItem(int index) {
        Object ret = null;
        switch (this.type) {
            case TYPING:
            case SPELLING:
                ret = content.get(index % content.size());
                break;
            default:
                break;
        }
        return ret;
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
        if (itemsAttempted >= content.size()) {
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
        for (Result result : results) {
            if (result.task_id == id) {
                if (result instanceof ResultTyping) {
                    int accuracy = ((ResultTyping) result).getAccuracy();
                    if (accuracy >= passPercentage) {
                        ret = true;
                        break;
                    }
                }
            }
            if (result instanceof ResultTyping) {

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

    /*
        Response view type levels can be word level->character level
        This function will decide which is the highest level
        This will only return a type if task is typing. Else it will return null
     */
    private ResponseViewType getHighestResponseViewType() {
        ResponseViewType responseViewType = null;
        if (type == Type.TYPING) {
            responseViewType = ResponseViewType.CHARACTER;
            for (String item : content) {
                if (item.length() > 1) {
                    responseViewType = ResponseViewType.WORD; //If even one item in the contents array has a length > 1, it means we have words in the array, not only characters
                    break;
                }
            }
        }

        return responseViewType;
    }

    /*
        Returns an tree of response nodes with each word broken up into character nodes
        Input: responses: List of responses at the character level, which is collected when the task is done
     */
    public ResponseTreeNode getResponsesForType(List<Response> responses, long startTimeMillis) {
        ResponseTreeNode rootResponseTreeNode = new ResponseTreeNode();
        ResponseViewType highestResponseViewType = getHighestResponseViewType();
        if (highestResponseViewType == ResponseViewType.WORD) {

            int startIndex = 0;
            for (String question : content) {
                ResponseTreeNode responseTreeNode = getTreeForResponses(responses, startIndex, question);
                if (startIndex == 0) {
                    //Getting the tree for the first question. Will add timestamp here instead of passing it into getTreeForResponses()
                    responseTreeNode.setStartTimeMillis(startTimeMillis);
                }
                rootResponseTreeNode.addChild(responseTreeNode);
                startIndex = startIndex + question.length();
            }
        }
        else if (responses != null) {
            for (Response r : responses) {
                rootResponseTreeNode.addChild(new ResponseTreeNode(r)); //If no responseViewType provided, simply return the original
            }
        }
        return rootResponseTreeNode;
    }

    /*
        This should only be called for tasks where a response tree applies
        eg: TYPING
     */
    public ResponseTreeNode getTreeForResponses(List<Response> responses, int startIndex, String question) {
        ResponseTreeNode responseTreeNode;
        if (question.length() == 0) {
            return null;
        }
        if (question.length() == 1) {
            responseTreeNode = new ResponseTreeNode(responses.get(startIndex));
            return responseTreeNode;
        }

        int originalStartIndex = startIndex; //As startIndex will be updated in loop
        ArrayList<ResponseTreeNode> children = new ArrayList<>();
        boolean isCorrect = true;
        StringBuilder sb = new StringBuilder();
        int endIndex = startIndex + question.length();
        int a = question.length();
        while (startIndex < endIndex) {
            sb.append(responses.get(startIndex).answer);
            if (!responses.get(startIndex).isCorrect) isCorrect = false;
            int indexInString = startIndex - originalStartIndex;
            if (indexInString < question.length()) {
                children.add(getTreeForResponses(responses, startIndex, String.valueOf(question.charAt(indexInString))));
            }
            startIndex++;

        }
        Response r = new Response(question, question, sb.toString(), isCorrect);
        startIndex = startIndex - 1; //startIndex was incremented before this. We need to go one back for the last element
        r.timestamp = responses.get(startIndex).timestamp;
        responseTreeNode = new ResponseTreeNode(r);
        if (originalStartIndex > 0) {
            responseTreeNode.setStartTimeMillis(responses.get(originalStartIndex - 1).timestamp);
        }
        responseTreeNode.addChildren(children);

        return responseTreeNode;
    }

}
