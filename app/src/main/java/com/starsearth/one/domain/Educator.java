package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class Educator implements Parcelable {

    //WARNING: Do not use camel case or underscore for variable names. Leads to bugs on server side. Use abbreviations and explain the meaning in comments

    public String uid;
    public String cc; //Country code
    public String mpn; //Mobile phone number
    public Status status;
    public PERMISSIONS tagging;

    public enum Status {
        AUTHORIZED, //Authorized to be an educator on the platform, but not registered
        ACTIVE, //Is currently an educator on the platform
        SUSPENDED, //Suspended for some wrong action
        DEACTIVATED
        ;

        public static Status fromString(String value) {
            Status result = null;
            switch (value.toLowerCase()) {
                case "authorized":
                    result = AUTHORIZED;
                    break;
                case "active":
                    result = ACTIVE;
                    break;
                case "suspended":
                    result = SUSPENDED;
                    break;
                case "deactivated":
                    result = DEACTIVATED;
                    break;

                default: break;

            }

            return result;
        }
    };

    public enum PERMISSIONS {
        TAGGING_ALL, //Allowed to add/edit tags for all teaching content
        TAGGING_OWN, //Allowed to tag only content you have created
        TAGGING_NONE //Not allowed to tag any content
        ;

        public static PERMISSIONS fromString(String value) {
            PERMISSIONS result = null;
            switch (value.toLowerCase()) {
                case "tagging_all":
                    result = TAGGING_ALL;
                    break;
                case "tagging_own":
                    result = TAGGING_OWN;
                    break;
                case "tagging_none":
                    result = TAGGING_NONE;
                    break;

                default: break;

            }

            return result;
        }
    }

    public Educator() {
        // Default constructor required for calls to DataSnapshot.getValueString(Educator.class)
    }

    public Educator(String key, Map<String, Object> map) {
        this.uid = key;
        this.cc = map.containsKey("cc") ? (String) map.get("cc") : null;
        this.status = map.containsKey("status") ? Status.fromString((String) map.get("status")) : null;
        this.mpn = map.containsKey("mpn") ? (String) map.get("mpn") : null;
        this.tagging = map.containsKey("tagging") ? PERMISSIONS.fromString((String) map.get("tagging")) : PERMISSIONS.TAGGING_NONE;
    }

    public Educator(String cc, String mpn, Status status) {
        this.cc = cc;
        this.mpn = mpn;
        this.status = status;
    }

    //Update permissions depending on what educator should get out of the box
    public void registrationSuccessful() {
        this.status = Status.ACTIVE;
    }

    protected Educator(Parcel in) {
        uid = in.readString();
        cc = in.readString();
        mpn = in.readString();
        status = Status.fromString(in.readString());
        tagging = PERMISSIONS.fromString(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(cc);
        dest.writeString(mpn);
        dest.writeString(status.toString());
        dest.writeString(tagging.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Educator> CREATOR = new Creator<Educator>() {
        @Override
        public Educator createFromParcel(Parcel in) {
            return new Educator(in);
        }

        @Override
        public Educator[] newArray(int size) {
            return new Educator[size];
        }
    };
}
