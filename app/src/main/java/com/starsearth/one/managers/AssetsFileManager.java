package com.starsearth.one.managers;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.RecordItem;
import com.starsearth.one.domain.SETeachingContent;
import com.starsearth.one.domain.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;

/**
 * Created by faimac on 3/2/18.
 * Get Course/Task data from JSON file in assets
 */

public class AssetsFileManager {

    private static int getDataInt(String line) {
        String result = null;
        String[] tmp = line.split(":");
        if (tmp.length > 1) {
            result = tmp[1].trim();
        }
        return Integer.valueOf(result);
    }

    private static String getDataString(String line) {
        String result = null;
        if (line != null) {
            String[] tmp = line.split(":");
            if (tmp.length > 1) {
                result = tmp[1].trim();
            }
        }
        return result;
    }

    private static List<String> getContent(String line) {
        ArrayList<String> result = new ArrayList<>();
        if (line != null) {
            line = line.replaceAll("\"","");
            result.addAll(Arrays.asList(line.split(",")));
        }
        return result;
    }

    private static Task newTask(HashMap<String, String> input) {
        if (input.size() == 0) {
            return null;
        }
        Task task = new Task();
        task.id = Integer.valueOf(input.get("id"));
        task.title = input.get("title");
        task.instructions = input.get("instructions");
        task.content = getContent(input.get("content"));
        //if (input.get("type") != null) task.type = Task.Type.fromInt(Integer.valueOf(input.get("type")));
        task.ordered = Boolean.parseBoolean(input.get("ordered"));
        task.timed = Boolean.parseBoolean(input.get("timed"));
        if (input.get("durationMillis") != null) task.durationMillis = Integer.valueOf(input.get("durationMillis"));

        return task;
    }

    private static String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getResources().getAssets().open("tasks.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    


    private static List<Object> getCourses(JSONArray json) {
        //Gson gson = new Gson();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Task.Type.class, new TypeDeserializer() );
        Gson gson = gsonBuilder.create();
        Type type = new TypeToken<List<Course>>(){}.getType();
        List<Object> list = gson.fromJson(json.toString(), type);
        return list;
    }

    private static List<Object> getTasks(JSONArray json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Task.Type.class, new TypeDeserializer() );
        Gson gson = gsonBuilder.create();
        Type type = new TypeToken<List<Task>>(){}.getType();
        List<Object> list = gson.fromJson(json.toString(), type);
        return list;
    }

    public static ArrayList<String> getAllTags(Context context) {
        ArrayList<String> tagList = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SETeachingContent) o).visible) {
                if (o instanceof Course) {
                    List<String> tags = ((Course) o).tags;
                    if (tags != null) {
                        for (String tag : tags) {
                            if (!tagList.contains(tag)) {
                                tagList.add(tag);
                            }
                        }
                    }
                }
                else if (o instanceof Task) {
                    List<String> tags = ((Task) o).tags;
                    if (tags != null) {
                        for (String tag : tags) {
                            if (!tagList.contains(tag)) {
                                tagList.add(tag);
                            }
                        }
                    }
                }
            }
        }
        return tagList;
    }

    public static ArrayList<RecordItem> getItemsByTag(Context context, String tag) {
        ArrayList<RecordItem> recordItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SETeachingContent) o).visible) {
                if (o instanceof Course) {
                    List<String> tags = ((Course) o).tags;
                    if (tags != null) {
                        //List<String> tagsList = Arrays.asList(tags);
                        if (tags.contains(tag)) {
                            recordItems.add(new RecordItem(o));
                        }
                    }
                }
                else if (o instanceof Task) {
                    List<String> tags = ((Task) o).tags;
                    if (tags != null) {
                        //List<String> tagsList = Arrays.asList(tags);
                        if (tags.contains(tag)) {
                            recordItems.add(new RecordItem(o));
                        }
                    }
                }
            }
        }
        return recordItems;
    }

    public static RecordItem getItemById(Context context, long id) {
        RecordItem recordItem = null;
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SETeachingContent) o).visible && ((SETeachingContent) o).id == id) {
                recordItem = new RecordItem(o);
            }
        }
        return recordItem;
    }

    public static ArrayList<RecordItem> getItemsByType(Context context, Task.Type type) {
        ArrayList<RecordItem> recordItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SETeachingContent) o).visible) {
                if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        if (task.type == type) {
                            recordItems.add(new RecordItem(task));
                        }
                    }
                }
                else if (o instanceof Task) {
                    if (((Task) o).type == type) {
                        RecordItem recordItem = new RecordItem(o);
                        recordItems.add(recordItem);
                    }
                }
            }
        }
        return recordItems;
    }

    public static ArrayList<RecordItem> getAllTimedItems(Context context) {
        ArrayList<RecordItem> recordItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SETeachingContent) o).visible) {
                if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        if (task.timed) {
                            recordItems.add(new RecordItem(task));
                        }
                    }
                }
                else if (o instanceof Task) {
                    if (((Task) o).timed) {
                        recordItems.add(new RecordItem(o));
                    }
                }
            }
        }
        return recordItems;
    }

    public static ArrayList<RecordItem> getAllGames(Context context) {
        ArrayList<RecordItem> recordItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SETeachingContent) o).visible) {
                if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        if (task.isGame) {
                            recordItems.add(new RecordItem(task));
                        }
                    }
                }
                else if (o instanceof Task) {
                    if (((Task) o).isGame) {
                        recordItems.add(new RecordItem(o));
                    }
                }
            }

        }
        return recordItems;
    }

    public static Course getCourseById(Context context, int courseId) {
        Course result = null;
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (o instanceof Course) {
                Course course = (Course) o;
                if (course.id == courseId && course.tasks.size() > 0) {
                    result = course;
                    break;
                }
            }
        }
        return result;
    }

    //Gets all items from JSON and returns them as array of RecordItem
    public static ArrayList<RecordItem> getAllItems(Context context) {
        ArrayList<RecordItem> recordItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SETeachingContent) o).visible) {
              /*  if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        recordItems.add(new RecordItem(task));
                    }
                }
                else if (o instanceof Task) {
                    recordItems.add(new RecordItem(o));
                }   */
              recordItems.add(new RecordItem(o));
            }
        }
        return recordItems;
    }


    private static List<Object> getAllItemsFromJSON(Context context) {
        List<Object> teachingContentList = new ArrayList<>();
        try {

            JSONObject root = new JSONObject(loadJSONFromAsset(context));
            JSONArray coursesJSON = root.getJSONArray("courses");
            JSONArray tasksJSON = root.getJSONArray("tasks");
            teachingContentList.addAll(getCourses(coursesJSON));
            teachingContentList.addAll(getTasks(tasksJSON));
            //long highestId = getHighestId(teachingContentList);
            //boolean b = areIdsUnique(teachingContentList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return teachingContentList;
    }

    /*
    Check if all json input have unique ids
     */
    public static boolean areIdsUnique(List<Object> teachingContentList) {
        boolean result = true;
        HashMap<Long, Integer> map = new HashMap<>();
        for (Object o : teachingContentList) {
            if (o instanceof Course) {
                //Course
                ArrayList<Task> tasks = (ArrayList<Task>) ((Course) o).tasks;
                for (Task task : tasks) {
                    if (!map.containsKey(task.id)) {
                        map.put(task.id, 1);
                    }
                    else {
                        map.put(task.id, map.get(task.id) + 1);
                    }
                }
            }
            else {
                //Its a task
                if (!map.containsKey(((SETeachingContent) o).id)) {
                    map.put(((SETeachingContent) o).id, 1);
                }
                else {
                    map.put(((SETeachingContent) o).id, map.get(((SETeachingContent) o).id) + 1);
                }
            }

        }
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            int x = (int) pair.getValue();
            if (x > 1) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static long getHighestId(List<Object> teachingContentList) {
        long result = 0;
        for (Object o : teachingContentList) {
            if (o instanceof Course) {
                //Course
                ArrayList<Task> tasks = (ArrayList<Task>) ((Course) o).tasks;
                for (Task task : tasks) {
                    if (task.id > result) {
                        result = task.id;
                    }
                }
            }
            else if (((Task) o).id > result) {
                //Task
                result = ((Task) o).id;
            }
        }
        return result;
    }

    private static class TypeDeserializer implements
            JsonDeserializer<Task.Type>
    {

        @Override
        public Task.Type deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            int typeInt = jsonElement.getAsInt();
            return Task.Type.fromInt(typeInt);
        }
    }
}
