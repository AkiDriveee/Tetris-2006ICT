package org.example;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public final class AudioManager {

    private static MediaPlayer musicPlayer;

    private AudioManager() {
        // Prevents creation of AudioManager objects.
    }

    public static void initialiseMusic() {

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

    public static void updateMusicState() {

        if (GameSettings.getConfig().isMusicEnabled()) {
            playMusic();
        } else {
            stopMusic();
        }
    }
}