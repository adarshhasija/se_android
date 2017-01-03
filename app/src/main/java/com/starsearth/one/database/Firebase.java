package com.starsearth.one.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
