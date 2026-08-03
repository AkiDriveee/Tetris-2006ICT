package com.example;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        showSplashScreen(stage);
    }

    private void showSplashScreen(Stage stage) {

        SplashScreen splashScreen =
                new SplashScreen();

        Scene splashScene =
                new Scene(
                        splashScreen,
                        1000,
                        700
                );

        stage.setTitle("Tetris - Group 39");

        stage.setScene(splashScene);

        // Prevent the splash screen becoming too small
        stage.setMinWidth(800);
        stage.setMinHeight(560);

        stage.centerOnScreen();

        stage.show();

        // Keep splash visible for 4 seconds
        PauseTransition delay =
                new PauseTransition(
                        Duration.seconds(4)
                );

        delay.setOnFinished(event ->
                showMainMenu(stage)
        );

        delay.play();
    }

    private void showMainMenu(Stage stage) {

        Label title =
                new Label("TETRIS MAIN MENU");

        title.setStyle("""
                -fx-font-size: 34px;
                -fx-font-weight: bold;
                """);

        StackPane root =
                new StackPane(title);

        Scene mainScene =
                new Scene(
                        root,
                        1000,
                        700
                );

        stage.setScene(mainScene);

        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}