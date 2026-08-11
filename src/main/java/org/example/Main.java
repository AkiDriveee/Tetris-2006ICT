package org.example;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.MainMenu;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        // Display the final PG-39 splash screen
        SplashScreen splashScreen = new SplashScreen();

        Scene splashScene = new Scene(
                splashScreen,
                1000,
                700
        );

        primaryStage.setTitle("Tetris - Group 39");
        primaryStage.setScene(splashScene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // After four seconds, open the team's existing main menu
        PauseTransition delay =
                new PauseTransition(Duration.seconds(4));

        delay.setOnFinished(event ->
                MainMenu.show(primaryStage)
        );

        delay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}