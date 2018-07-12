package com.starsearth.one.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.starsearth.one.domain.Assistant;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Exercise;
import com.starsearth.one.domain.Lesson;
import com.starsearth.one.domain.Question;
import com.starsearth.one.domain.ResultGestures;
import com.starsearth.one.domain.ResultTyping;
import com.starsearth.one.domain.SENestedObject;
import com.starsearth.one.domain.Topic;
import com.starsearth.one.domain.User;
import com.starsearth.one.domain.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by faimac on 11/24/16.
 */

public class Firebase {

    public String URL_STORAGE = "gs://starsearth-59af6.appspot.com";

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public Firebase(String reference) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if (reference != null) {
            this.databaseReference = database.getReference(reference);
        }
        else {
            this.databaseReference = database.getReference();
        }
        this.storageReference = storage.getReferenceFromUrl(URL_STORAGE);
    }

    private void removeChildren(Iterator it) {
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            SENestedObject value = (SENestedObject) pair.getValue();
            Map<String, SENestedObject> children = value.children;
            if (children.size() > 0) {
                removeChildren(children.entrySet().iterator());
            }
            final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(value.typePlural + "/" + value.uid);
            mRef.removeValue();

            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public void writeNewUser(String uid, boolean courseAdmin, String email) {
        User user = new User(uid, courseAdmin, email);
        databaseReference.child(uid).setValue(user);
    }

    public void writeNewGuestUser(String uid) {
        User user = new User(uid, true);
        databaseReference.child(uid).setValue(user);
    }

    public void convertGuestUserToFullUser(String key, User user) {
        Map<String, Object> values = user.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    //Returns key of the newly created course
 /*   public String writeNewCourse(String type, int difficulty, String name, String description, boolean usbKeyboard) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Course course = new Course(key, type, difficulty, name, description, user.getUid(), usbKeyboard);
        Map<String, Object> courseValues = course.toMap();
        courseValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, courseValues);

        databaseReference.updateChildren(childUpdates);
        return key;
    }   */

    public void updateExistingCourse(String key, Course course) {
        //databasereference.child(key).setValue(course);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = course.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public void removeCourse(Course course) {
        if (course == null) return;
        Map<String, SENestedObject> lessons = course.lessons;
        Iterator it = lessons.entrySet().iterator();
        removeChildren(it);
        databaseReference.child(course.getUid()).removeValue();
    }

 /*   public String writeNewLesson(int index, String name, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Lesson lesson = new Lesson(key, name, index, user.getUid(), parent);
        Map<String, Object> values = lesson.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return key;
    }   */

    public void updateExistingLesson(String key, Lesson lesson) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = lesson.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public void removeLesson(Lesson lesson) {
        if (lesson == null) return;
        Map<String, SENestedObject> topics = lesson.topics;
        Iterator it = topics.entrySet().iterator();
        removeChildren(it);
        databaseReference.child(lesson.getUid()).removeValue();
    }

 /*   public String writeNewTopic(int index, String name, String description, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Topic topic = new Topic(key, name, description, index, user.getUid(), parent);
        Map<String, Object> topicValues = topic.toMap();
        topicValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, topicValues);

        databaseReference.updateChildren(childUpdates);
        return key;
    }   */

    public void updateExistingTopic(String key, Topic topic) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = topic.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public void removeTopic(Topic topic) {
        if (topic == null) return;
        Map<String, SENestedObject> questions = topic.questions;
        Iterator it = questions.entrySet().iterator();
        removeChildren(it);
        databaseReference.child(topic.getUid()).removeValue();
    }

    public String writeNewExercise(int index, String title, String instructions, String description, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Exercise exercise = new Exercise(key, title, instructions, description, index, user.getUid(), parent);
        Map<String, Object> topicValues = exercise.toMap();
        topicValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, topicValues);

        databaseReference.updateChildren(childUpdates);
        return key;
    }

    public void updateExistingExercise(String key, Exercise exercise) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = exercise.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public void removeExercise(Exercise exercise) {
        if (exercise == null) return;
        Map<String, SENestedObject> exercises = exercise.questions;
        Iterator it = exercises.entrySet().iterator();
        removeChildren(it);
        databaseReference.child(exercise.getUid()).removeValue();
    }

 /*   public String writeNewQuestion(int index, String title, String answer, String hint, float positiveWeight, float negativeWeight,
                                   String feedbackCorrectAnswer, String feedbackWrongAnswer, String parent, String instruction, int repeats,
                                   String questionType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Question question = new Question(key, title, answer, hint, index, positiveWeight, negativeWeight,
                                            feedbackCorrectAnswer, feedbackWrongAnswer, user.getUid(), parent, instruction, repeats, questionType);
        Map<String, Object> values = question.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return key;
    }   */

    public void updateExistingQuestion(String key, Question question) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = question.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public void removeQuestion(Question question) {
        if (question == null) return;
        databaseReference.child(question.getUid()).removeValue();
    }


    //Returns key of the newly created course
 /*   public String writeNewUserAnswer(User firebaseUserValues, String questionId, String userAnswerString, long timeSpent, String topicId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Response userResponse = new Response(key, questionId, user.getUid(), userAnswerString, timeSpent, topicId);
        Map<String, Object> userAnswerValues = userResponse.toMap();
        userAnswerValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/answers/"+key, userAnswerValues);

        firebaseUserValues.addAnswer(new SENestedObject(key, "answers"));
        childUpdates.put("/users/" + firebaseUserValues.uid, firebaseUserValues);

        databaseReference.updateChildren(childUpdates);
        return key;
    }   */

    public String writeNewAssistant(Assistant.State state) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Assistant assistant = new Assistant(key, user.getUid(), state);
        Map<String, Object> values = assistant.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return key;
    }

    public void updateExistingAssistant(String key, Assistant assistant) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = assistant.toMap();
        values.put("state", assistant.state);
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public ResultTyping writeNewResultTyping(int characters_correct, int characters_total_attempted, int words_correct, int words_total_finished, long timeTakenMillis, int gameId, ArrayList<Response> responses) {  //String subject, int level, String levelString
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        ResultTyping testResult = new ResultTyping(key, user.getUid(), characters_correct, characters_total_attempted, words_correct, words_total_finished, timeTakenMillis, gameId, responses);
        Map<String, Object> values = testResult.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return testResult;
    }

    public ResultGestures writeNewResultGestures(int attempted, int correct, long timeTakenMillis, int taskId, ArrayList<Response> responses) {  //String subject, int level, String levelString
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        ResultGestures testResult = new ResultGestures(key, user.getUid(), attempted, correct, timeTakenMillis, taskId, responses);
        Map<String, Object> values = testResult.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return testResult;
    }

    public void updateExistingTypingTestResult(String key, ResultTyping result) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = result.toMap();
        values.put("characters_correct", result.getCharacters_correct());
        values.put("words_correct", result.getWords_correct());
        values.put("timeTakenMillis", result.timeTakenMillis);
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public void updateExistingResult(String key, ResultTyping result) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = result.toMap();
        values.put("characters_correct", result.getCharacters_correct());
        values.put("words_correct", result.getWords_correct());
        values.put("timeTakenMillis", result.timeTakenMillis);
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }


    public Query getDatabaseQuery(String indexOn, String item) {
        //Query query = reference.orderByChild("item").equalTo(item);
        Query query = databaseReference.orderByChild(indexOn).equalTo(item);
        return query;
    }

    public StorageReference getImageReference(String item, String type) {
        String fullPath = getImagePath(item, type);
        if (fullPath != null && !fullPath.isEmpty()) {
            StorageReference pathReference = storageReference.child(fullPath);
            return pathReference;
        }
        return null;
    }

    private String getImagePath(String item, String type) {
        String path = "images/";
        if (item != null && !item.isEmpty()) {
            String fullPath;
            item = formatItemImageName(item);
            if (type != null && !type.isEmpty()) {
                fullPath = path + item + "_" + type + ".png";
            }
            else {
                fullPath = path + item + "_ISL" + ".png";
            }
            return fullPath;
        }
        return null;
    }


    //This formats the item as an image name for firebase
    //All characters are made upper case
    //If item is more than one word, _ is used to separate words
    //If item is null or empty, null is returned
    private String formatItemImageName(String item) {
        return item != null && !item.isEmpty() ?
                        item.replaceAll(" ", "_").toUpperCase() : null;
    }

}
