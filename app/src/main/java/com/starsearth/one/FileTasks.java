package com.starsearth.one;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.SEBaseObject;
import com.starsearth.one.domain.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.lang.reflect.Type;

/**
 * Created by faimac on 3/2/18.
 */

public class FileTasks {

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

    private static String[] getContent(String line) {
        String[] result = null;
        if (line != null) {
            line = line.replaceAll("\"","");
            result = line.split(",");
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
        if (input.get("trials") != null) task.trials = Integer.valueOf(input.get("trials"));

        return task;
    }

  /*  public static ArrayList<Task> openTextFile(Context context) {
        final AssetManager assetManager = context.getResources().getAssets();
        Vector<String> vector = new Vector<String>();
        HashMap<String, String> map = new HashMap<>();

        ArrayList<Task> tasks = null;
        BufferedReader br;
        try {
            final InputStream inputStream = assetManager.open("tasks.json");
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("{")) {
                    //vector.clear();
                    map.clear();
                }
                else if (line.contains("}")) {
                    Task task = newTask(map);
                    if (task != null) {
                        tasks.add(task);
                    }
                }
                else {
                    //vector.add(line);
                    String[] tmp = line.split(":");
                    map.put(tmp[0], tmp[1]);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tasks;
    }   */

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

    public static ArrayList<MainMenuItem> getMainMenuItems(Context context) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        try {

            JSONObject root = new JSONObject(loadJSONFromAsset(context));
            JSONArray coursesJSON = root.getJSONArray("courses");
            JSONArray tasksJSON = root.getJSONArray("tasks");
            List<Object> teachingContentList = getCourses(coursesJSON);
            teachingContentList.addAll(getTasks(tasksJSON));
            for (Object o : teachingContentList) {
                if (((SEBaseObject) o).visible) {
                    MainMenuItem mainMenuItem = new MainMenuItem();
                    mainMenuItem.teachingContent = o;
                    mainMenuItems.add(mainMenuItem);
                }
            }
          /*  Gson gson = new Gson();
            Type typeCourse = new TypeToken<List<Course>>(){}.getType();
            Type typeTask = new TypeToken<List<Task>>(){}.getType();
            List<Course> coursesList = gson.fromJson(coursesJSON.toString(), typeCourse);

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Task.Type.class, new TypeDeserializer() );
            Gson gson2 = gsonBuilder.create();
            tasksList = gson2.fromJson(tasksJSON.toString(), typeTask);
            */

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mainMenuItems;
    }

    /*
    Check if all json input have unique ids
     */
    public static boolean areIdsUnique(List<Object> teachingContentList) {
        boolean result = true;
        HashMap<Integer, Integer> map = new HashMap<>();
        for (Object o : teachingContentList) {
            if (!map.containsKey(((SEBaseObject) o).id)) {
                map.put(((SEBaseObject) o).id, 1);
            }
            else {
                map.put(((SEBaseObject) o).id, map.get(((SEBaseObject) o).id) + 1);
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

    public static int getHighestId(List<Object> teachingContentList) {
        int result = 0;
        for (Object o : teachingContentList) {
            if (((SEBaseObject) o).id > result) {
                result = ((SEBaseObject) o).id;
            }
        }
        return result;
    }

    public static ArrayList<MainMenuItem> getMainMenuItemsFromCourse(Course course) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        for (Object o : course.tasks) {
            if (((SEBaseObject) o).visible) {
                MainMenuItem mainMenuItem = new MainMenuItem();
                mainMenuItem.teachingContent = o;
                mainMenuItems.add(mainMenuItem);
            }
        }
        return mainMenuItems;
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
