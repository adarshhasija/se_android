package com.starsearth.two.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class TagListItem implements Parcelable {

    public String name;
    public boolean seone; //This tag is used by starsearth.one
    public boolean checked; //Teaching content id if it is checked

    public TagListItem() {
        //For Firebase
    }

    public TagListItem(String key, Map<String, Object> map) {
        this.name = key;
        this.seone = map.containsKey("seone") && (boolean) map.get("seone");
    }

    public TagListItem(String name) {
        this.name = name;
    }


    protected TagListItem(Parcel in) {
        name = in.readString();
        seone = in.readByte() != 0;
        checked = in.readByte() != 0;
    }

    public static final Creator<TagListItem> CREATOR = new Creator<TagListItem>() {
        @Override
        public TagListItem createFromParcel(Parcel in) {
            return new TagListItem(in);
        }

        @Override
        public TagListItem[] newArray(int size) {
            return new TagListItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeByte((byte) (seone ? 1 : 0));
        dest.writeByte((byte) (checked ? 1 : 0));
    }
}
