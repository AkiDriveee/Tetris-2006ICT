package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

            /*
             * Read the high-score file using the same structure
             * demonstrated by the tutor:
             *
             * {
             *   "scores": [...]
             * }
             */
            JsonNode root = OBJECT_MAPPER.readTree(file);
            JsonNode scoresNode = root.get("scores");

            if (scoresNode == null || !scoresNode.isArray()) {
                return new ArrayList<>();
            }

            List<ScoreEntry> scores =
                    OBJECT_MAPPER.convertValue(
                            scoresNode,
                            new TypeReference<List<ScoreEntry>>() {
                            }
                    );

            /*
             * Java Stream:
             * Remove any invalid null entries loaded from the JSON file.
             *
             * A mutable ArrayList is returned because the high-score
             * functionality later sorts and updates this list.
             */
            return scores.stream()
                    .filter(score -> score != null)
                    .collect(
                            ArrayList::new,
                            ArrayList::add,
                            ArrayList::addAll
                    );

        } catch (IOException | IllegalArgumentException e) {

            System.out.println(
                    "Could not load high scores: " +
                            e.getMessage()
            );

            return new ArrayList<>();
        }
    }

    public static void save(List<ScoreEntry> scores) {

        try {

            /*
             * Store the scores inside a JSON object so the file has
             * the following structure:
             *
             * {
             *   "scores": [...]
             * }
             */
            ObjectNode root = OBJECT_MAPPER.createObjectNode();

            root.set(
                    "scores",
                    OBJECT_MAPPER.valueToTree(scores)
            );

            OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(
                            new File(SCORES_FILE),
                            root
                    );

        } catch (IOException | IllegalArgumentException e) {

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