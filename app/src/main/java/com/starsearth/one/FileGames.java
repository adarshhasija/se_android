package com.starsearth.one;

import android.content.Context;
import android.content.res.AssetManager;

import com.starsearth.one.domain.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
        String[] tmp = line.split(":");
        if (tmp.length > 1) {
            result = tmp[1].trim();
        }
        return result;
    }

    private static Game newGame(Vector<String> input) {
        if (input.size() == 0) {
            return null;
        }
        Game game = new Game();
        game.id = getDataInt(input.get(0));
        game.title = getDataString(input.get(1));
        game.instructions = getDataString(input.get(2));
        return game;
    }

    public static ArrayList<Game> openFile(Context context) {
        final AssetManager assetManager = context.getResources().getAssets();
        Vector<String> vector = new Vector<String>();

        ArrayList<Game> games = new ArrayList<>();
        BufferedReader br;
        try {
            final InputStream inputStream = assetManager.open("games.txt");
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("{")) {
                    vector.clear();
                }
                else if (line.contains("}")) {
                    Game game = newGame(vector);
                    if (game != null) {
                        games.add(newGame(vector));
                    }
                }
                else {
                    vector.add(line);
                }
            }
            br.close();


        } catch (IOException e) {
            throw new RuntimeException("error reading labels file!", e);
        }

        return games;
    }
}
