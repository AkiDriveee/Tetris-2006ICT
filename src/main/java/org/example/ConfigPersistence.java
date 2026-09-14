package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public final class ConfigPersistence {

    private static final String CONFIG_FILE = "config.json";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ConfigPersistence() {
        // Prevents creation of ConfigPersistence objects.
    }

    public static void save(GameConfig config) {

        try {
            OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(new File(CONFIG_FILE), config);

        } catch (IOException e) {
            System.out.println(
                    "Could not save configuration: "
                            + e.getMessage()
            );
        }
    }

    public static GameConfig load() {

        File file = new File(CONFIG_FILE);

        if (!file.exists()) {
            return new GameConfig();
        }

        try {
            return OBJECT_MAPPER.readValue(
                    file,
                    GameConfig.class
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not load configuration: "
                            + e.getMessage()
            );

            return new GameConfig();
        }
    }
}