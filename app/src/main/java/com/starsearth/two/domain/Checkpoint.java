package com.starsearth.two.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class Checkpoint implements Parcelable {

    public long id;
    public String title;

    protected Checkpoint(Parcel in) {
        id = in.readLong();
        title = in.readString();
    }

    public static final Creator<Checkpoint> CREATOR = new Creator<Checkpoint>() {
        @Override
        public Checkpoint createFromParcel(Parcel in) {
            return new Checkpoint(in);
        }

        @Override
        public Checkpoint[] newArray(int size) {
            return new Checkpoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
    }
}
