package com.starsearth.one.domain;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.managers.AssetsFileManager;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 2/27/18.
 */

public class SEOneListItem implements Parcelable {

    public static String TYPE_LABEL = "TYPE";
    public static String CONTENT = "CONTENT";

    protected SEOneListItem(Parcel in) {
        text1 = in.readString();
        text2 = in.readString();
        Type.fromString(in.readString());
    }

    public static final Creator<SEOneListItem> CREATOR = new Creator<SEOneListItem>() {
        @Override
        public SEOneListItem createFromParcel(Parcel in) {
            return new SEOneListItem(in);
        }

        @Override
        public SEOneListItem[] newArray(int size) {
            return new SEOneListItem[size];
        }
    };

    public static List<SEOneListItem> populateBaseList(Context context) {
        List<SEOneListItem> list = new ArrayList<>();
        //list.add(new SEOneListItem(context.getResources().getString(R.string.timed), Type.TIMED));
        //list.add(new SEOneListItem(context.getResources().getString(R.string.games), Type.GAME));
        //list.add(new SEOneListItem(context.getResources().getString(R.string.view_all), Type.ALL));
        String sosText = ((StarsEarthApplication) context.getApplicationContext()).getFirebaseRemoteConfigWrapper().getSOSButtonText();
        if (sosText != null && sosText.equals("covid-19")) list.add(new SEOneListItem(context.getResources().getString(R.string.covid19), Type.CORONA_DASHBOARD));
        list.add(new SEOneListItem(context.getResources().getString(R.string.typing), Type.TAG));
        list.add(new SEOneListItem(context.getResources().getString(R.string.english), Type.TAG));
        list.add(new SEOneListItem(context.getResources().getString(R.string.mathematics), Type.TAG));
        list.add(new SEOneListItem(context.getResources().getString(R.string.educator_search), Type.EDUCATOR_SEARCH));
        list.add(new SEOneListItem(context.getResources().getString(R.string.search_by_class), Type.SEARCH_BY_CLASS));
        list.add(new SEOneListItem(context.getResources().getString(R.string.fun), Type.TAG));
        list.add(new SEOneListItem(context.getResources().getString(R.string.educator_profile), Type.EDUCATOR_PROFILE));
        list.add(new SEOneListItem(context.getResources().getString(R.string.keyboard_test), Type.KEYBOARD_TEST));
        list.add(new SEOneListItem(context.getResources().getString(R.string.phone_number), Type.PHONE_NUMBER));
        if (BuildConfig.DEBUG) {
            list.add(new SEOneListItem(context.getResources().getString(R.string.logout), Type.LOGOUT));
        }

        return list;
    }

    public static List<SEOneListItem> populateCoronaMenuList(Context context) {
        List<SEOneListItem> list = new ArrayList<>();
        list.add(new SEOneListItem(context.getResources().getString(R.string.corona_my_help_requests), Type.CORONA_MY_HELP_REQUESTS));
        list.add(new SEOneListItem(context.getResources().getString(R.string.corona_help_requests), Type.CORONA_HELP_REQUESTS));
        list.add(new SEOneListItem(context.getResources().getString(R.string.search_by_organization), Type.CORONA_ORGANIZATION_SEARCH));
        list.add(new SEOneListItem(context.getResources().getString(R.string.corona_make_new_help_request), Type.CORONA_NEW_HELP_REQUEST));

        return list;
    }

    public static List<SEOneListItem> populateCoronaStatesList(Context context) {
        List<SEOneListItem> list = new ArrayList<>();
        list.add(new SEOneListItem(context.getResources().getString(R.string.karnataka), Type.CORONA_HELP_REQUESTS_FOR_STATES));

        return list;
    }

    public static List<SEOneListItem> returnListForType(Context context, SEOneListItem.Type type) {
        List<String> list = new ArrayList<>();
        switch (type) {
            case TAG:
                list.addAll(AssetsFileManager.getAllTags(context));
                break;
            default:
                break;
        }

        List<SEOneListItem> returnList = new ArrayList<>();
        for (String s : list) {
            returnList.add(new SEOneListItem(s, type));
        }
        return returnList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text1);
        parcel.writeString(text2);
        parcel.writeString(type.toString());
    }

    public enum Type {
            LOGOUT("LOGOUT"),
            KEYBOARD_TEST("KEYBOARD_TEST"),
            PHONE_NUMBER("PHONE_NUMBER"),
            EDUCATOR_SEARCH("EDUCATOR_SEARCH"),
            SEARCH_BY_CLASS("SEARCH_BY_CLASS"),
            EDUCATOR_PROFILE("EDUCATOR_PROFILE"),
            GAME("GAME"),
            TIMED("TIMED"),
            ALL("ALL"),
            CORONA_DASHBOARD("CORONA_DASHBOARD"),
            CORONA_ORGANIZATION_SEARCH("CORONA_ORGANIZATION_SEARCH"),
            CORONA_HELP_REQUESTS("CORONA_HELP_REQUESTS"),
            CORONA_MY_HELP_REQUESTS("CORONA_MY_HELP_REQUESTS"),
            CORONA_HELP_REQUESTS_FOR_STATES("CORONA_HELP_REQUESTS_FOR_STATES"),
            CORONA_NEW_HELP_REQUEST("CORONA_NEW_HELP_REQUEST"),
            TAG("TAG")
            ;

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Type fromString(String i) {
            for (Type type : Type.values()) {
                if (type.getValue().equals(i)) { return type; }
            }
            return null;
        }
    }

    private String text1;
    private String text2;
    private Type type;

    public SEOneListItem(SEOneListItem.Type type) {
        this.type = type;
    }

    public SEOneListItem(String text1, SEOneListItem.Type type) {
        this.text1 = text1;
        this.type = type;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
