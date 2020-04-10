package com.starsearth.two.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class AutismContent implements Parcelable {

    public String id;
    public String title;
    public String textLine1;
    public String textLine2;
    public boolean hasImage;

    public AutismContent(String title) {
        this.title = title;
    }

    public AutismContent(Map<String, Object> map) {
        this.id = map.containsKey("id") && (map.get("id") instanceof Long) ? ((Long) map.get("id")).toString() :
                  map.containsKey("id") && (map.get("id") instanceof Double) ? ((Double) map.get("id")).toString() : //If it is coming from the local json file and it is an int, gson will treat an int as a double
                map.containsKey("id") ? (String) map.get("id") :
                        null;
        this.title = map.containsKey("title") ? (String) map.get("title") : null;
        this.textLine1 = map.containsKey("textLine1") ? (String) map.get("textLine1") : null;
        this.textLine2 = map.containsKey("textLine2") ? (String) map.get("textLine2") : null;
        this.hasImage = map.containsKey("hasImage") && (boolean) map.get("hasImage");
    }

    protected AutismContent(Parcel in) {
        id = in.readString();
        title = in.readString();
        textLine1 = in.readString();
        textLine2 = in.readString();
        hasImage = in.readByte() != 0;
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
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(textLine1);
        dest.writeString(textLine2);
        dest.writeByte((byte) (hasImage ? 1 : 0));
    }
}
