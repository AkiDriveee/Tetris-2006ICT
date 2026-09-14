package org.example;

public final class GameSettings {

    private static final GameConfig CONFIG =
            ConfigPersistence.load();

    private GameSettings() {
    }

    public static GameConfig getConfig() {
        return CONFIG;
    }

    public static void save() {
        ConfigPersistence.save(CONFIG);
    }
}