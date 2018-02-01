package com.starsearth.one.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by faimac on 1/30/18.
 */

public class TypingGame extends Game {

    public enum Type {
        ONE_WORD, MULTIPLE_WORDS, ONE_SENTENCE, MULTIPLE_SENTENCES
    };

    public List<String> words = new ArrayList<>(Arrays.asList("World", "Car", "Train", "Water", "Rain"));
    public List<String> sentences = new ArrayList<>(Arrays.asList(
            "Roses are red",
                "Mary had a little lamb",
                "The sun is bright",
                "The Earth is round",
                "The cat looked out of the window",
                "Tom and Jerry",
                "The horse ran fast",
                "The door was open",
                "Dinosaurs were very big",
                "The grass is green",
                "The sky is blue",
                "The spider spun a web",
                "Birds fly in the air"
            ));
    public Type type;
}
