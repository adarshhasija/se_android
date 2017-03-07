package com.starsearth.one.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Exercise;
import com.starsearth.one.domain.Lesson;
import com.starsearth.one.domain.Question;
import com.starsearth.one.domain.SENestedObject;
import com.starsearth.one.domain.Topic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        this.databaseReference = database.getReference(reference);
        this.storageReference = storage.getReferenceFromUrl(URL_STORAGE);
    }

    private void removeChildren(Iterator it) {
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            SENestedObject value = (SENestedObject) pair.getValue();
            //Map<String, Map<String, SENestedObject>> children = value.children;
            Map<String, SENestedObject> children = value.children;
            if (children.size() > 0) {
                removeChildren(children.entrySet().iterator());
            }
            final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(value.typePlural + "/" + value.uid);
            mRef.removeValue();

            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    //Returns key of the newly created course
    public String writeNewCourse(String type, int difficulty, String name, String description) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Course course = new Course(key, type, difficulty, name, description, user.getUid());
        Map<String, Object> courseValues = course.toMap();
        courseValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, courseValues);

        databaseReference.updateChildren(childUpdates);
        return key;
    }

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
        Map<String, SENestedObject> lessons = course.getLessons();
        Iterator it = lessons.entrySet().iterator();
        removeChildren(it);
        databaseReference.child(course.getUid()).removeValue();
    }

    public String writeNewLesson(int index, String name, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Lesson lesson = new Lesson(key, name, index, user.getUid(), parent);
        Map<String, Object> values = lesson.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return key;
    }

    public void updateExistingLesson(String key, Lesson lesson) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = lesson.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public void removeLesson(Lesson lesson) {

    }

    public String writeNewTopic(int index, String name, String description, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Topic topic = new Topic(key, name, description, index, user.getUid(), parent);
        Map<String, Object> topicValues = topic.toMap();
        topicValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, topicValues);

        databaseReference.updateChildren(childUpdates);
        return key;
    }

    public void updateExistingTopic(String key, Topic topic) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = topic.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databaseReference.child(key).setValue(values);
    }

    public void deleteTopic(Topic topic) {
        Iterator it = topic.getExercises().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Firebase firebase = new Firebase(pair.getKey().toString());
            //firebase.deleteExercise();
            it.remove(); // avoids a ConcurrentModificationException
        }
        databaseReference.removeValue();
    }

    public String writeNewExercise(int index, String title, String description, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Exercise exercise = new Exercise(key, title, description, index, user.getUid(), parent);
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

    public void deleteExercise(Exercise exercise) {
        Iterator it = exercise.getQuestions().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public String writeNewQuestion(int index, String title, String answer, String hint, float positiveWeight, float negativeWeight, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        Question question = new Question(key, title, answer, hint, index, positiveWeight, negativeWeight, user.getUid(), parent);
        Map<String, Object> values = question.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return key;
    }

    public void updateExistingQuestion(String key, Question question) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = question.toMap();
        values.put("updatedBy", user.getUid());
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
