package com.starsearth.one.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 2/5/18.
 */

public class MainMenuItem {

    //Either a course or a task
    public Object teachingContent;
    public List<Result> results = new ArrayList<>(); //This should only contain the most recent result

    public MainMenuItem() {

    }

    public boolean isTaskIdExists(int taskId) {
        boolean result = false;
        if (teachingContent instanceof Course) {
            result = ((Course) teachingContent).isTaskExists(taskId);
        }
        else if (teachingContent instanceof Task) {
            if (taskId == ((Task) teachingContent).id) {
                result = true;
            }
        }
        return result;
    }
}
