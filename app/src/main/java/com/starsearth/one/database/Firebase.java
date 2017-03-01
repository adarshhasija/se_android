package com.starsearth.one.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Lesson;
import com.starsearth.one.domain.Topic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 11/24/16.
 */

public class Firebase {

    public String URL_STORAGE = "gs://starsearth-59af6.appspot.com";

    private DatabaseReference databasereference;
    private StorageReference storageReference;

    public Firebase(String reference) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        this.databasereference = database.getReference(reference);
        this.storageReference = storage.getReferenceFromUrl(URL_STORAGE);
    }

    //Returns key of the newly created course
    public String writeNewCourse(String type, int difficulty, String name, String description) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databasereference.push().getKey();
        Course course = new Course(key, type, difficulty, name, description, user.getUid());
        Map<String, Object> courseValues = course.toMap();
        courseValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, courseValues);

        databasereference.updateChildren(childUpdates);
        return key;
    }

    public void updateExistingCourse(String key, Course course) {
        //databasereference.child(key).setValue(course);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = course.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databasereference.child(key).setValue(values);
    }

    public String writeNewLesson(int index, String name, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databasereference.push().getKey();
        Lesson lesson = new Lesson(key, name, index, user.getUid(), parent);
        Map<String, Object> lessonValues = lesson.toMap();
        lessonValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, lessonValues);

        databasereference.updateChildren(childUpdates);
        return key;
    }

    public void updateExistingLesson(String key, Lesson lesson) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = lesson.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databasereference.child(key).setValue(values);
    }

    public String writeNewTopic(int index, String name, String content, String parent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databasereference.push().getKey();
        Topic topic = new Topic(key, name, content, index, user.getUid(), parent);
        Map<String, Object> topicValues = topic.toMap();
        topicValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, topicValues);

        databasereference.updateChildren(childUpdates);
        return key;
    }

    public void updateExistingTopic(String key, Topic topic) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> values = topic.toMap();
        values.put("updatedBy", user.getUid());
        values.put("timestamp", ServerValue.TIMESTAMP);
        databasereference.child(key).setValue(values);
    }

    public Query getDatabaseQuery(String indexOn, String item) {
        //Query query = reference.orderByChild("item").equalTo(item);
        Query query = databasereference.orderByChild(indexOn).equalTo(item);
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
