package com.starsearth.one.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by faimac on 1/30/18.
 */

public class TypingGame extends Game {

    public enum Id {
        ONE_WORD(1), MANY_WORDS(2), ONE_SENTENCE(3), MANY_SENTENCES(4);

        private final long value;

        Id(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public static TypingGame.Id fromInt(long i) {
            for (TypingGame.Id type : TypingGame.Id.values()) {
                if (type.getValue() == i) { return type; }
            }
            return null;
        }
    };

    /*
    Assign a unique id based on type.
    If wording changes, change text here
     */
    public static TypingGame.Id assignType(String levelString) {
        if (levelString.contains("1 word")) {
            return Id.ONE_WORD;
        }
        else if (levelString.contains("many words")) {
            return Id.MANY_WORDS;
        }
        else if (levelString.contains("1 sentence")) {
            return Id.ONE_SENTENCE;
        }
        else if (levelString.contains("many sentences")) {
            return Id.MANY_SENTENCES;
        }
        return null;
    }

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
    public Id id;
}
