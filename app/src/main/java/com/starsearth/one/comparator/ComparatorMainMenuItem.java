package com.starsearth.one.comparator;

import com.starsearth.one.domain.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by faimac on 4/4/18.
 */

public class ComparatorMainMenuItem implements Comparator<Result> {
    @Override
    public int compare(Result o1, Result o2) {
        return o1.timestamp > o2.timestamp? 1 : -1;
    }
}
