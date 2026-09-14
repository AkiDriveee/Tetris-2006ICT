package org.example;

public class GameConfig {

    private int fieldWidth;
    private int fieldHeight;
    private int gameLevel;
    private boolean musicEnabled;
    private boolean soundEnabled;
    private boolean aiEnabled;
    private boolean extendedMode;

    public GameConfig() {
        this.fieldWidth = 10;
        this.fieldHeight = 20;
        this.gameLevel = 1;
        this.musicEnabled = true;
        this.soundEnabled = true;
        this.aiEnabled = false;
        this.extendedMode = false;
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public void setFieldWidth(int fieldWidth) {
        this.fieldWidth = fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public void setFieldHeight(int fieldHeight) {
        this.fieldHeight = fieldHeight;
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel(int gameLevel) {
        this.gameLevel = gameLevel;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public boolean isExtendedMode() {
        return extendedMode;
    }

    public void setExtendedMode(boolean extendedMode) {
        this.extendedMode = extendedMode;
    }
}