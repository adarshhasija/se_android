package com.starsearth.two.domain.datastructures;

import com.starsearth.two.domain.Result;

import java.util.Comparator;

/*
    This comparator is used to sort record items in reverse chronological order. ie: latest results come first
 */
public class ResultComparator implements Comparator<Result> {
    @Override
    public int compare(Result result1, Result result2) {
        Long timestamp1 = result1.timestamp;
        Long timestamp2 = result2.timestamp;
        return Integer.compare(timestamp1.intValue(), timestamp2.intValue());
    }
}
