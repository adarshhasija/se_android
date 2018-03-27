package com.starsearth.one.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faimac on 2/5/18.
 */

public class MainMenuItem {

    //Either a course or a task
    public Course course;
    public Task task;

    public List<Result> results = new ArrayList<>(); //This should only contain the most recent result

    public MainMenuItem() {

    }

    public boolean isTaskIdExists(int taskId) {
        boolean result = false;
        if (course != null) {
            result = course.isTaskExists(taskId);
        }
        else if (taskId == task.id) {
            result = true;
        }
        return result;
    }
}
