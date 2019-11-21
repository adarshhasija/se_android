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
    public boolean tagging = false;

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
        TAGGING
        ;
    }

    public Educator() {
        // Default constructor required for calls to DataSnapshot.getValueString(Educator.class)
    }

    public Educator(String key, Map<String, Object> map) {
        this.uid = key;
        this.cc = (String) map.get("cc");
        this.status = Status.fromString((String) map.get("status"));
        this.mpn = (String) map.get("mpn");
        this.tagging = map.containsKey("tagging") && (boolean) map.get("tagging");
    }

    public Educator(String cc, String mpn, Status status) {
        this.cc = cc;
        this.mpn = mpn;
        this.status = status;
    }

    //Update permissions depending on what educator should get out of the box
    public void registrationSuccessful() {
        this.status = Status.ACTIVE;
        this.tagging = true;
    }

    protected Educator(Parcel in) {
        uid = in.readString();
        cc = in.readString();
        mpn = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(cc);
        dest.writeString(mpn);
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
