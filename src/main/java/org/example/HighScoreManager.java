package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class HighScoreManager {

    private static final String SCORES_FILE = "highscores.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HighScoreManager() {
    }

    public static List<ScoreEntry> load() {

        File file = new File(SCORES_FILE);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try {

            List<ScoreEntry> scores =
                    OBJECT_MAPPER.readValue(
                            file,
                            new TypeReference<List<ScoreEntry>>() {}
                    );

            /*
             * Java Stream:
             * Remove any invalid null entries loaded from the JSON file.
             * The result is returned as a new mutable ArrayList because
             * the high-score screen later sorts and updates this list.
             */
            return scores.stream()
                    .filter(score -> score != null)
                    .collect(
                            ArrayList::new,
                            ArrayList::add,
                            ArrayList::addAll
                    );

        } catch (IOException e) {

            System.out.println(
                    "Could not load high scores: " +
                            e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    public static void save(List<ScoreEntry> scores) {

        try {

            OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(
                            new File(SCORES_FILE),
                            scores
                    );

        } catch (IOException e) {

            System.out.println(
                    "Could not save high scores: " +
                            e.getMessage()
            );
        }
    }

    public static void clear() {
        save(new ArrayList<>());
    }
}