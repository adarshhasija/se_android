package com.starsearth.one.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 2/5/18.
 */

public class MainMenuItem {

    public String subject;
    public String levelString;
    public TypingGame.Id gameId;
    public long lastTriedMillis;
    public String other; //Phone number/email etc

    public Game game;
    public List<Result> results = new ArrayList<>(); //This should only contain the most recent result

    public MainMenuItem() {

    }
}
