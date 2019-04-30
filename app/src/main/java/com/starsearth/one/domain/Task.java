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

    public List<Object> content = new ArrayList<>(); //Has to be List<String> to save to FirebaseManager
    public List<Object> tap = new ArrayList<>();
    public List<Object> swipe = new ArrayList<>();
    public Type type;
    public String subType;
    public boolean ordered = false; //should the content be shown in same order to the user
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
        WORD("WORD"),    //Can View Responses at a word level
        SENTENCE("SENTENCE") //Can View Responses at a sentence level
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
        TYPING(1), //Typing out the characters on the screen
        KEYBOARD_TEST(3),
        TAP_SWIPE(4),
        DICTATION(5), //Blank screen. Type characters based on audio input
        CAROUSEL(6) //Horizontal list of cards
        ;

        private final long value;

        Type(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        @Override
        public String toString() {
            String result = null;
            switch ((int) value) {
                case 1:
                    result = "Typing";
                    break;
                case 3:
                    result = "Keyboard Test";
                    break;
                case 4:
                    result = "Tap and Swipe";
                    break;
                case 5:
                    result = "Dictation";
                    break;
                case 6:
                    result = "Carousel";
                    break;

                    default: break;

            }

            return result;
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
        content = in.readArrayList(Object.class.getClassLoader());
        tap = in.readArrayList(String.class.getClassLoader());
        swipe = in.readArrayList(String.class.getClassLoader());
        type = Type.fromInt(in.readInt());
        subType = in.readString();
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
        result.put("subType", subType);
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
        dest.writeString(subType);
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
        Object taskContent = null;
        Random random = new Random();
        int i;
        switch (this.type) {
            case TYPING:
            case DICTATION:
                i = random.nextInt(content.size());
                taskContent = content.get(i);
                if (taskContent instanceof  Map) {
                    ret =  new TaskContent((HashMap<String, Object>) taskContent);
                }
                else if (taskContent instanceof String) {
                    ret = (String) taskContent;
                }
                break;
            case TAP_SWIPE:
                i = random.nextInt(2);
                taskContent = null;
                boolean isTrue = false;
                if (i % 2 == 0 && tap.size() > 0) {
                    taskContent = tap.get(random.nextInt(tap.size()));
                    isTrue = true;
                }
                else if (swipe.size() > 0) {
                    taskContent = swipe.get(random.nextInt(swipe.size()));
                    isTrue = false;
                }

                if (taskContent instanceof String) {
                    ret = new TaskContent((String) taskContent, isTrue);
                }
                else if (taskContent instanceof Map) {
                    ret =  new TaskContent((HashMap<String, Object>) taskContent);
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
            case DICTATION:
                Object taskContent = content.get(index % content.size());
                if (taskContent instanceof Map) {
                    ret = new TaskContent((HashMap<String, Object>) taskContent);
                }
                else if (taskContent instanceof String) {
                    ret = (String) taskContent;
                }
                break;
            case TAP_SWIPE:
                ret = new TaskContent((HashMap<String, Object>) content.get(index % content.size()));
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
        return (String) content.get(i);
    }


    /*
    If content should be returned in any order
    Type: gesture
     */
    public Map<Object, Boolean> getNextItemGesture() {
        Map<Object, Boolean> map = new HashMap<>();
        Random random = new Random();
        int i = random.nextInt(2);
        if (i % 2 == 0 && tap.size() > 0) {
            Object tapObj = tap.get(random.nextInt(tap.size()));
            if (tapObj instanceof Map) {
                ((HashMap) map).put(new TaskContent((HashMap<String, Object>) tapObj), true);
            }
            else {
                ((HashMap) map).put((String) tapObj, true);
            }

        }
        else if (swipe.size() > 0) {
            Object swipeObj = swipe.get(random.nextInt(swipe.size()));
            if (swipeObj instanceof Map) {
                ((HashMap) map).put(new TaskContent((HashMap<String, Object>) swipeObj), false);
            }
            else {
                ((HashMap) map).put((String) swipeObj, false);
            }
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
        return (String) content.get(index % content.size());
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
    public ResponseViewType getHighestResponseViewType() {
        ResponseViewType responseViewType = null;
        if (type == Type.TYPING) {
            responseViewType = ResponseViewType.CHARACTER;
            for (Object item : content) {
                String question = "";
                if (item instanceof TaskContent) {
                    question = ((TaskContent) item).question;
                }
                else if (item instanceof String) {
                    question = (String) item;
                }
                if (question.contains(" ")) {
                    responseViewType = ResponseViewType.SENTENCE;
                    break;
                }
                else if (question.length() > 1) {
                    responseViewType = ResponseViewType.WORD; //If even one item in the contents array has a length > 1, it means we have words in the array, not only characters
                    break;
                }
            }
        }

        return responseViewType;
    }

    /*
        Different arrays are used for different content types
        This function will return all the content in one array
        If its tap and swipe arrays, it will add both in one array
     */
    private List<Object> getContentList() {
        List<Object> returnList = new ArrayList<>();
        if (this.content != null && this.content.size() > 0) {
            //ordered content
            returnList.addAll(this.content);
        }
        else {
            //unordered content
            if (this.tap != null && this.tap.size() > 0) {
                returnList.addAll(this.tap);
            }
            if (this.swipe != null && this.swipe.size() > 0) {
                returnList.addAll(this.swipe);
            }
        }

        return returnList;
    }

    /*
        Returns an tree of response nodes with each word broken up into character nodes
        Input: responses: List of responses at the character level, which is collected when the task is done
     */
    public ResponseTreeNode getResponsesForTask(List<Response> responses, long startTimeMillis) {
        ResponseTreeNode rootResponseTreeNode = new ResponseTreeNode();
        ResponseViewType highestResponseViewType = getHighestResponseViewType();
        if (highestResponseViewType == ResponseViewType.WORD || highestResponseViewType == ResponseViewType.SENTENCE) {

            int startIndex = 0;
            for (Object item : content) {
                String question = (String) item;
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
            for (int i = 0; i < responses.size(); i++) {
                Response r = responses.get(i);
                ResponseTreeNode responseTreeNode = new ResponseTreeNode(r);
                List<Object> contentList = getContentList();
                if (contentList.size() > 0) {
                    for (Object contentObject : contentList) {
                        if (contentObject instanceof Map) {
                            TaskContent taskContent = new TaskContent((Map) contentObject);
                            if (r.taskContentId == taskContent.id) {
                                //The response is for this task content
                                String expectedAnswerExplanation = taskContent.explanation;
                                if (expectedAnswerExplanation != null && expectedAnswerExplanation.length() > 0) {
                                    Response data = responseTreeNode.getData();
                                    data.expectedAnswerExplanation = expectedAnswerExplanation;
                                    responseTreeNode.setData(data);
                                    break;
                                }
                            }
                        }
                    }

                }
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

        List<Integer> indexesOfSpaces = new ArrayList<>();
        int originalStartIndex = startIndex; //Retain the startIndex as startIndex will be updated in loop
        ArrayList<ResponseTreeNode> children = new ArrayList<>();
        boolean isCorrect = true;
        StringBuilder sb = new StringBuilder();
        int endIndex = startIndex + question.length();
        while (startIndex < endIndex) {
            sb.append(responses.get(startIndex).answer);
            if (!responses.get(startIndex).isCorrect) isCorrect = false;
            int indexInString = startIndex - originalStartIndex;
            if (!question.contains(" ") && indexInString < question.length()) {
                //It does not contain spaces = not a sentence
                //For every character in a word, create a child node
                children.add(getTreeForResponses(responses, startIndex, String.valueOf(question.charAt(indexInString))));
            }
            else if (question.contains(" ")) {
                //It is a sentence. We need to save the index of the space. If the character at this index in the string is a space
                if (question.charAt(indexInString) == ' ') {
                    indexesOfSpaces.add(startIndex); //Save the index of the overall responses array. Will use it later
                }
            }
            startIndex++;

        }

        if (question.contains(" ")) {
            //If it is a sentence
            //Start of string to first space
            children.add(getTreeForResponses(responses,
                                            originalStartIndex, //Pointer to start of sentence in responses list
                                            question.substring(0,
                                                                    indexesOfSpaces.get(0) - originalStartIndex //index of spaces gives index in response array. Minus starting index of this question gives index of this particular word
                                            )
                        ));
            children.add(getTreeForResponses(responses,
                                                indexesOfSpaces.get(0), //The answer for the space is at this index
                                        " ") //Theres a space after the word
                            );

            for (int i = 0; i < indexesOfSpaces.size(); i++) {
                if (i == indexesOfSpaces.size() - 1) {
                    //If it the final space, take the word from the last space to the end of the string
                    children.add(getTreeForResponses(responses,
                            indexesOfSpaces.get(i) + 1, //That index is index of space. To get the first character of the word, take the next index
                            question.substring(indexesOfSpaces.get(i) + 1 - originalStartIndex) //Starting index of the word. Index in the overall responses array - index of start of this sentence in responses array
                    ));
                    //No space needed after last word
                }
                else {
                    //Get word between current space and next space
                    children.add(getTreeForResponses(responses,
                                                indexesOfSpaces.get(i) + 1, //Get index after space to get a word
                                                            question.substring(indexesOfSpaces.get(i) + 1 - originalStartIndex, //Get index after the space to get a word. As this is index in whole responses array, subtract original start index to get index of this particular character. Else IndexOutOfBoundsException
                                                                                indexesOfSpaces.get(i + 1) - originalStartIndex) //Get index of next space in order to get substring = exact word
                                                    ));
                    children.add(getTreeForResponses(responses, indexesOfSpaces.get(i), " ")); //Space after the word
                }
            }
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
