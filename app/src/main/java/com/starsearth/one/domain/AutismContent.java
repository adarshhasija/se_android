package com.starsearth.one.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class AutismContent implements Parcelable {

    public int id;
    public String title;
    public String textLine1;
    public String textLine2;

    public AutismContent(String title) {
        this.title = title;
    }

    public AutismContent(Map<String, Object> map) {
        this.id = (map.get("id") instanceof Double)? ((Double) map.get("id")).intValue() : (int) map.get("id"); //If type is not specified, gson will take int as double
        this.title = map.containsKey("title") ? (String) map.get("title") : null;
        this.textLine1 = map.containsKey("textLine1") ? (String) map.get("textLine1") : null;
        this.textLine2 = map.containsKey("textLine2") ? (String) map.get("textLine2") : null;
    }

    protected AutismContent(Parcel in) {
        id = in.readInt();
        title = in.readString();
        textLine1 = in.readString();
        textLine2 = in.readString();
    }

    public static final Creator<AutismContent> CREATOR = new Creator<AutismContent>() {
        @Override
        public AutismContent createFromParcel(Parcel in) {
            return new AutismContent(in);
        }

        @Override
        public AutismContent[] newArray(int size) {
            return new AutismContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(textLine1);
        dest.writeString(textLine2);
    }
}
