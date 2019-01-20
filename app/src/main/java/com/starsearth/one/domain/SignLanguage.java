package com.starsearth.one.domain;

import android.support.annotation.NonNull;
import android.util.Log;

import com.starsearth.one.managers.FirebaseManager;
import com.starsearth.one.listeners.BotResponseListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * Created by faimac on 11/24/16.
 */

public class SignLanguage {

    public static String LOG_TAG = "Sign-Language";

    public static boolean isSignLanguage(AIResponse response) {
        if (response != null) {
            Result result = response.getResult();
            String action = result.getAction();
            if (action != null && action.equals("getSignLanguage")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isActionIncomplete(AIResponse response) {
        Result result = response.getResult();
        return result.isActionIncomplete();
    }

    private String firebaseReference = "SignLanguage";
    private String firebaseIndexOn = "item";
    private String item;
    private String type;
    private BotResponseListener listener;

    public SignLanguage(String item, String type, BotResponseListener listener) {
        this.item = item;
        this.type = type;
        this.listener = listener;
    }

    public void get() {
        getDescription();
    }

    private void getDescription() {
        FirebaseManager firebaseManager = new FirebaseManager(firebaseReference);
        Query query = firebaseManager.getDatabaseQuery(firebaseIndexOn, item);
        query.addValueEventListener(descriptionReceivedListener);
    }

    private void getMedia() {
        FirebaseManager firebaseManager = new FirebaseManager(firebaseReference);
        StorageReference mediaRef = firebaseManager.getImageReference(item, type);
        mediaRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(imageReceivedListener).addOnFailureListener(failureListener);
    }

    ValueEventListener descriptionReceivedListener = new ValueEventListener() {
        boolean matchFound = false;

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
            while (children.iterator().hasNext()) {
                DataSnapshot child = children.iterator().next();
                HashMap<String, String> data = (HashMap<String, String>) child.getValue();
                String dataType = data.get("type");
                if (dataType.equals(type)) {
                    listener.processBotResponse(data.get("description"));
                    matchFound = true;
                }
            }
            if (!matchFound) {
                listener.processBotResponse("Sorry, we do not have the "+type+" for this");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException());
        }
    };

    final long ONE_MEGABYTE = 1024 * 1024;
    OnSuccessListener<byte[]> imageReceivedListener = new OnSuccessListener<byte[]>() {
        @Override
        public void onSuccess(byte[] bytes) {

        }
    };

    OnFailureListener failureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    };
}
