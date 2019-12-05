package com.starsearth.one.managers;

import android.util.Log;

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
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.ResultTyping;
import com.starsearth.one.domain.SENestedObject;
import com.starsearth.one.domain.Topic;
import com.starsearth.one.domain.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.google.common.base.Predicates.equalTo;

/**
 * Created by faimac on 11/24/16.
 */

public class FirebaseManager {

    public String URL_STORAGE = "gs://starsearth-59af6.appspot.com";

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public FirebaseManager(String reference) {
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

    //Returns key of the newly created course
    public String writeNewCourse(HashMap<String, Object> map) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String key = databaseReference.push().getKey();
        map.put("key", key);
        Course course = new Course(map);
        Map<String, Object> courseValues = course.toMap();
        courseValues.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, courseValues);

        databaseReference.updateChildren(childUpdates);
        return key;
    }

    //When a user starts an attempt on a new course, save a copy of it
    //We will always refer to this copy of the course in future and ignore any updates made
    //Cannot pass Course object directly to writeNewCourse as it saves lots of metadata, which we dont want
    public String writeNewCourseAttempt(Course course) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        course.attemptedByUserId = user.getUid();
        return writeNewCourse((HashMap<String, Object>) course.toMap());
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

    public ResultTyping writeNewResultTyping(HashMap<String, Object> map) {
        String key = databaseReference.push().getKey();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        map.put("uid", key);
        map.put("userId", user.getUid());
        ResultTyping testResult = new ResultTyping(map);
        Map<String, Object> values = testResult.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return testResult;
    }

    public Result writeNewResult(HashMap<String, Object> map) {
        String key = databaseReference.push().getKey();
        FirebaseUser userId = FirebaseAuth.getInstance().getCurrentUser();
        map.put("uid", key);
        map.put("userId", userId.getUid());
        Result testResult = new Result(map);
        Map<String, Object> values = testResult.toMap();
        values.put("timestamp", ServerValue.TIMESTAMP); //testResult has local timestamp, values has sever timestamp
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, values);

        databaseReference.updateChildren(childUpdates);
        return testResult;
    }


    public Query getDatabaseQuery(String indexOn, String item) {
        //Query query = reference.orderByChild("item").equalTo(item);
        Query query = databaseReference.orderByChild(indexOn).equalTo(item);
        return query;
    }

    public Query getQueryForUserObject(String uid) {
        Query query = databaseReference.child(uid);
        return query;
    }

    public Query getQueryForEducatorsByUserid(String userid) {
        return databaseReference.orderByChild("userid").equalTo(userid); //if indexOn has not been set
    }

    public Query getQueryForEducatorsByPhoneNumber(String phoneNumber) {
        return databaseReference.orderByChild("mpn").equalTo(phoneNumber); //if indexOn has not been set
        //return databaseReference.equalTo("mpn", phoneNumber); //if indexOn has been set. Not working
    }

    //Returns all tags selected by a particular user for a certain teaching content id
    public Query getQueryForTagsByUserId(String teachingContentId, String userId) {
        return databaseReference.child(teachingContentId).child("tags").orderByChild(userId).equalTo(true);
    }

    //Returns all teaching content associated with a particular tag
    public Query getQueryForTeachingContentByTag(String tag) {
        return databaseReference.child(tag).child("teachingcontent");
    }

    //Returns Teaching content under a particular tag that has been tagged by a particular person
    public Query getQueryForTeachingContentTaggedByUserId(String tag, String userId) {
        return databaseReference.child(tag).child("teachingcontent").orderByChild(userId).equalTo(true);
    }

    public Query getQueryForTeachingContentCreatedByUserId(String userId) {
        return databaseReference.orderByChild("creator").equalTo(userId);
    }


    public Query getQueryForTags() {
        return databaseReference.orderByChild("seone").equalTo(true);
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
