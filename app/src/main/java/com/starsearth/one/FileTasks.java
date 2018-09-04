package com.starsearth.one;

import android.content.Context;

import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.MainMenuItem;
import com.starsearth.one.domain.SEBaseObject;
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

    public static ArrayList<MainMenuItem> getMainMenuItemsByTag(Context context, String tag) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SEBaseObject) o).visible) {
                boolean isTagPresent = false;
                if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        String[] tags = task.tags;
                        if (tags != null) {
                            List<String> tagsList = Arrays.asList(tags);
                            if (tagsList.contains(tag)) {
                                mainMenuItems.add(new MainMenuItem(task));
                            }
                        }
                    }
                }
                else if (o instanceof Task) {
                    String[] tags = ((Task) o).tags;
                    if (tags != null) {
                        List<String> tagsList = Arrays.asList(tags);
                        if (tagsList.contains(tag)) {
                            mainMenuItems.add(new MainMenuItem(o));
                        }
                    }
                }
            }
        }
        return mainMenuItems;
    }

    public static ArrayList<MainMenuItem> getMainMenuItemsByType(Context context, Task.Type type) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SEBaseObject) o).visible) {
                if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        if (task.type == type) {
                            mainMenuItems.add(new MainMenuItem(task));
                        }
                    }
                }
                else if (o instanceof Task) {
                    if (((Task) o).type == type) {
                        MainMenuItem mainMenuItem = new MainMenuItem();
                        mainMenuItem.teachingContent = o;
                        mainMenuItems.add(mainMenuItem);
                    }
                }
            }
        }
        return mainMenuItems;
    }

    public static ArrayList<MainMenuItem> getMainMenuItemsTimed(Context context) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SEBaseObject) o).visible) {
                if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        if (task.timed) {
                            mainMenuItems.add(new MainMenuItem(task));
                        }
                    }
                }
                else if (o instanceof Task) {
                    if (((Task) o).timed) {
                        mainMenuItems.add(new MainMenuItem(o));
                    }
                }
            }
        }
        return mainMenuItems;
    }

    public static ArrayList<MainMenuItem> getMainMenuItemsGames(Context context) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SEBaseObject) o).visible) {
                if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        if (task.isGame) {
                            mainMenuItems.add(new MainMenuItem(task));
                        }
                    }
                }
                else if (o instanceof Task) {
                    if (((Task) o).isGame) {
                        mainMenuItems.add(new MainMenuItem(o));
                    }
                }
            }

        }
        return mainMenuItems;
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

    //Gets all items from JSON and returns them as array of MainMenuItem
    public static ArrayList<MainMenuItem> getAllMainMenuItems(Context context) {
        ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>();
        List<Object> teachingContentList = getAllItemsFromJSON(context);
        for (Object o : teachingContentList) {
            if (((SEBaseObject) o).visible) {
              /*  if (o instanceof Course) {
                    Course course = (Course) o;
                    List<Task> tasks = course.getTasks();
                    for (Task task : tasks) {
                        mainMenuItems.add(new MainMenuItem(task));
                    }
                }
                else if (o instanceof Task) {
                    mainMenuItems.add(new MainMenuItem(o));
                }   */
              mainMenuItems.add(new MainMenuItem(o));
            }
        }
        return mainMenuItems;
    }


    private static List<Object> getAllItemsFromJSON(Context context) {
        List<Object> teachingContentList = new ArrayList<>();
        try {

            JSONObject root = new JSONObject(loadJSONFromAsset(context));
            JSONArray coursesJSON = root.getJSONArray("courses");
            JSONArray tasksJSON = root.getJSONArray("tasks");
            teachingContentList.addAll(getCourses(coursesJSON));
            teachingContentList.addAll(getTasks(tasksJSON));
            long highestId = getHighestId(teachingContentList);
            //boolean b = areIdsUnique(teachingContentList);
            int i = 0;
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
                if (!map.containsKey(((SEBaseObject) o).id)) {
                    map.put(((SEBaseObject) o).id, 1);
                }
                else {
                    map.put(((SEBaseObject) o).id, map.get(((SEBaseObject) o).id) + 1);
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
