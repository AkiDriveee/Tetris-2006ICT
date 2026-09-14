package org.example;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public final class AudioManager {

    private static MediaPlayer musicPlayer;

    private AudioManager() {
        // Prevents creation of AudioManager objects.
    }

    // ---------------------------------------------------------
    // BACKGROUND MUSIC
    // ---------------------------------------------------------

    public static void initialiseMusic() {

        // Only create the music player once.
        if (musicPlayer != null) {
            return;
        }

        URL musicUrl = AudioManager.class.getResource(
                "/audio/background.mp3"
        );

        if (musicUrl == null) {
            System.out.println("Background music file not found.");
            return;
        }

        Media media = new Media(musicUrl.toExternalForm());
        musicPlayer = new MediaPlayer(media);

        // Keep the background music playing continuously.
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setVolume(0.4);
    }

    public static void playMusic() {

        initialiseMusic();

        if (musicPlayer != null
                && GameSettings.getConfig().isMusicEnabled()) {

            musicPlayer.play();
        }
    }

    public static void stopMusic() {

        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    /*
     * Applies the current configuration immediately.
     * This is used by both the Configuration screen
     * and the in-game M keyboard control.
     */
    public static void updateMusicState() {

        if (GameSettings.getConfig().isMusicEnabled()) {
            playMusic();
        } else {
            stopMusic();
        }
    }

    // ---------------------------------------------------------
    // SOUND EFFECTS
    // ---------------------------------------------------------

    /*
     * Plays a sound effect only when sound effects are enabled
     * in the shared game configuration.
     *
     * AudioClip is used for short effects because it is suitable
     * for sounds that may be played repeatedly during gameplay.
     */
    private static void playSoundEffect(String resourcePath) {

        if (!GameSettings.getConfig().isSoundEnabled()) {
            return;
        }

        URL soundUrl = AudioManager.class.getResource(resourcePath);

        if (soundUrl == null) {
            System.out.println(
                    "Sound effect file not found: " + resourcePath
            );
            return;
        }

        AudioClip sound = new AudioClip(
                soundUrl.toExternalForm()
        );

        sound.play();
    }

    // Plays when the player moves or rotates a tetromino.
    public static void playMoveTurnSound() {
        playSoundEffect("/audio/move-turn.wav");
    }

    // Plays whenever one or more completed rows are removed.
    public static void playEraseLineSound() {
        playSoundEffect("/audio/erase-line.wav");
    }

    // Reserved for the scoring/level system.
    public static void playLevelUpSound() {
        playSoundEffect("/audio/level-up.wav");
    }

    // Plays when the current game finishes.
    public static void playGameFinishSound() {
        playSoundEffect("/audio/game-finish.wav");
    }
}