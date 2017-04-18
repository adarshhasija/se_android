package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by faimac on 3/2/17.
 */

public class SEBaseObject implements Parcelable {

    public String uid;
    public String title;
    public String createdBy;
    public String updatedBy;
    public String parentType;
    public String parentId;
    public long timestamp;

    public SEBaseObject() {

    }

    public SEBaseObject(String uid, String title, String createdBy) {
        this.uid = uid;
        this.title = title;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }

    public SEBaseObject(String uid, String title, String createdBy, String parentType, String parentId) {
        this.uid = uid;
        this.title = title;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        this.parentType = parentType;
        this.parentId = parentId;
    }

    protected SEBaseObject(Parcel in) {
        uid = in.readString();
        title = in.readString();
        createdBy = in.readString();
        updatedBy = in.readString();
        parentType = in.readString();
        parentId = in.readString();
        timestamp = in.readLong();
    }

    public static final Creator<SEBaseObject> CREATOR = new Creator<SEBaseObject>() {
        @Override
        public SEBaseObject createFromParcel(Parcel in) {
            return new SEBaseObject(in);
        }

        @Override
        public SEBaseObject[] newArray(int size) {
            return new SEBaseObject[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("title", title);
        result.put("createdBy", createdBy);
        result.put("updatedBy", updatedBy);
        result.put("parentType", parentType);
        result.put("parentId", parentId);
        result.put("timestamp", timestamp);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(title);
        dest.writeString(createdBy);
        dest.writeString(updatedBy);
        dest.writeString(parentType);
        dest.writeString(parentId);
        dest.writeLong(timestamp);
    }
}
