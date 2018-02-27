package com.starsearth.one.domain;

/**
 * Created by faimac on 2/27/18.
 */

public class TopMenuItem {

    private String text1;
    private String text2;

    public TopMenuItem(String text1) {
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
}
