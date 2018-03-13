package com.starsearth.one;

import android.content.Context;
import android.content.res.AssetManager;

import com.starsearth.one.domain.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by faimac on 3/2/18.
 */

public class FileGames {

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

    private static String[] getDataArray(String line) {
        String[] result = null;
        if (line != null) {
            result = line.split(",");
        }
        return result;
    }

    private static Game newGame(HashMap<String, String> input) {
        if (input.size() == 0) {
            return null;
        }
        Game game = new Game();
        game.id = Integer.valueOf(input.get("id"));
        game.title = input.get("title");
        game.instructions = input.get("instructions");
        game.content = getDataArray(input.get("content"));
        if (input.get("type") != null) game.type = Game.Type.fromInt(Integer.valueOf(input.get("type")));
        game.ordered = Boolean.parseBoolean(input.get("ordered"));
        game.timed = Boolean.parseBoolean(input.get("timed"));
        if (input.get("durationMillis") != null) game.durationMillis = Integer.valueOf(input.get("durationMillis"));

        return game;
    }

    public static ArrayList<Game> openFile(Context context) {
        final AssetManager assetManager = context.getResources().getAssets();
        Vector<String> vector = new Vector<String>();
        HashMap<String, String> map = new HashMap<>();

        ArrayList<Game> games = new ArrayList<>();
        BufferedReader br;
        try {
            final InputStream inputStream = assetManager.open("games.txt");
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("{")) {
                    //vector.clear();
                    map.clear();
                }
                else if (line.contains("}")) {
                    Game game = newGame(map);
                    if (game != null) {
                        games.add(game);
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

        return games;
    }
}
