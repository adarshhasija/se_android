package com.starsearth.one.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by faimac on 1/30/18.
 */

public class TypingTask extends Task {

    public enum Id {
        ONE_WORD(1), MANY_WORDS(2), ONE_SENTENCE(3), MANY_SENTENCES(4), LETTERS_LOWER_CASE(5), LETTERS_UPPER_CASE(6);

        private final long value;

        Id(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public static TypingTask.Id fromInt(long i) {
            for (TypingTask.Id type : TypingTask.Id.values()) {
                if (type.getValue() == i) { return type; }
            }
            return null;
        }
    };

    /*
    Assign a unique id based on type.
    If wording changes, change text here
     */
    public static TypingTask.Id assignType(String levelString) {
        TypingTask.Id id = null;
        if (levelString.contains("1 word")) {
            id =  Id.ONE_WORD;
        }
        else if (levelString.contains("many words")) {
            id =  Id.MANY_WORDS;
        }
        else if (levelString.contains("1 sentence")) {
            id =  Id.ONE_SENTENCE;
        }
        else if (levelString.contains("many sentences")) {
            id =  Id.MANY_SENTENCES;
        }
        else if (levelString.contains("small letters")) {
            id =  Id.LETTERS_LOWER_CASE;
        }
        else if (levelString.contains("capital letters")) {
            id = Id.LETTERS_UPPER_CASE;
        }
        return id;
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
