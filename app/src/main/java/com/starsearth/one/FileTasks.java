package com.starsearth.one;

import android.content.Context;
import android.content.res.AssetManager;

import com.starsearth.one.domain.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

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
        if (input.get("type") != null) task.type = Task.Type.fromInt(Integer.valueOf(input.get("type")));
        task.ordered = Boolean.parseBoolean(input.get("ordered"));
        task.timed = Boolean.parseBoolean(input.get("timed"));
        if (input.get("durationMillis") != null) task.durationMillis = Integer.valueOf(input.get("durationMillis"));
        if (input.get("trials") != null) task.trials = Integer.valueOf(input.get("trials"));

        return task;
    }

    public static ArrayList<Task> openFile(Context context) {
        final AssetManager assetManager = context.getResources().getAssets();
        Vector<String> vector = new Vector<String>();
        HashMap<String, String> map = new HashMap<>();

        ArrayList<Task> tasks = new ArrayList<>();
        BufferedReader br;
        try {
            final InputStream inputStream = assetManager.open("tasks.txt");
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
            throw new RuntimeException("error reading labels file!", e);
        }

        return tasks;
    }
}
