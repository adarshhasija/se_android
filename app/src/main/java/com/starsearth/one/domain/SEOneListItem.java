package com.starsearth.one.domain;

import android.content.Context;

import com.starsearth.one.managers.AssetsFileManager;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 2/27/18.
 */

public class SEOneListItem {

    public static String TYPE_LABEL = "TYPE";
    public static String CONTENT = "CONTENT";

    public static List<SEOneListItem> populateBaseList(Context context) {
        List<SEOneListItem> list = new ArrayList<>();
        list.add(new SEOneListItem(context.getResources().getString(R.string.timed), Type.TIMED));
        list.add(new SEOneListItem(context.getResources().getString(R.string.games), Type.GAME));
        list.add(new SEOneListItem(context.getResources().getString(R.string.view_all), Type.ALL));
        list.add(new SEOneListItem(context.getResources().getString(R.string.keyboard_test), Type.KEYBOARD_TEST));
        list.add(new SEOneListItem(context.getResources().getString(R.string.phone_number), Type.PHONE_NUMBER));
        if (BuildConfig.DEBUG) {
            list.add(new SEOneListItem(context.getResources().getString(R.string.logout), Type.LOGOUT));
        }

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

    public enum Type {
            LOGOUT("LOGOUT"),
            KEYBOARD_TEST("KEYBOARD_TEST"),
            PHONE_NUMBER("PHONE_NUMBER"),
            GAME("GAME"),
            TIMED("TIMED"),
            ALL("ALL"),
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
