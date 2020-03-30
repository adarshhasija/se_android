package com.starsearth.one.domain;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class HelpRequest implements Parcelable {

    public String uid;
    public String phone;
    public String name;
    public String landmark;
    public String request;
    public SEAddress address;
    public String status; //ACTIVE / COMPLETE / CANCELLED
    public long timestamp;

    public HelpRequest() {

    }

    public HelpRequest(String key, HashMap<String, Object> map) {
        this.uid =  key != null ? key : null;
        this.phone = map.containsKey("phone") ? (String) map.get("phone") : null;
        this.name = map.containsKey("name") ? (String) map.get("name") : null;
        this.landmark = map.containsKey("landmark") ? (String) map.get("landmark") : null;
        this.request = map.containsKey("request") ? (String) map.get("request") : null;
        this.address = map.containsKey("address") ? new SEAddress((HashMap<String, Object>) map.get("address")) : null;
        this.status = map.containsKey("status") ? (String) map.get("status") : null;
        this.timestamp = map.containsKey("timestamp") ? (long) map.get("timestamp") : 0;
    }

    protected HelpRequest(Parcel in) {
        uid = in.readString();
        phone = in.readString();
        name = in.readString();
        landmark = in.readString();
        request = in.readString();
        address = in.readParcelable(ClassLoader.getSystemClassLoader());
        status = in.readString();
        timestamp = in.readLong();
    }

    public static final Creator<HelpRequest> CREATOR = new Creator<HelpRequest>() {
        @Override
        public HelpRequest createFromParcel(Parcel in) {
            return new HelpRequest(in);
        }

        @Override
        public HelpRequest[] newArray(int size) {
            return new HelpRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(phone);
        parcel.writeString(name);
        parcel.writeString(landmark);
        parcel.writeString(request);
        parcel.writeParcelable(address, 0);
        parcel.writeString(status);
        parcel.writeLong(timestamp);
    }
}
