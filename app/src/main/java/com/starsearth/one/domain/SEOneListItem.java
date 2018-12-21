package com.starsearth.one.domain;

import java.util.Set;

/**
 * Created by faimac on 2/27/18.
 */

public class SEOneListItem {

    public enum Type {
            LOGOUT("LOGOUT"),
            KEYBOARD_TEST("KEYBOARD_TEST"),
            PHONE_NUMBER("PHONE_NUMBER"),
            GAME("GAME"),
            TIMED("TIMED"),
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
                if (type.getValue() == i) { return type; }
            }
            return null;
        }
    }

    private String text1;
    private String text2;
    private Type type;

    public SEOneListItem(String text1) {
        this.text1 = text1;
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
