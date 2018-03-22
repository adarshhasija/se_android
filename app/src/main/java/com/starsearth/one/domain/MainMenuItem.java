package com.starsearth.one.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 2/5/18.
 */

public class MainMenuItem {

    public Task task;
    public List<Result> results = new ArrayList<>(); //This should only contain the most recent result

    public MainMenuItem() {

    }
}
