package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.concurrent.Task;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        Stage splashStage = new Stage(StageStyle.UNDECORATED);
        ImageView splashImage = new ImageView(new Image(getClass().getResource("/splash-image.png").toExternalForm()));

        splashImage.setFitWidth(300);
        splashImage.setPreserveRatio(true);
        splashImage.setSmooth(true);

        StackPane splashLayout = new StackPane(splashImage);

        Scene splashScene = new Scene(splashLayout, 320, 320);

        splashStage.setScene(splashScene);
        splashStage.show();

        Task<Void> loadTask = new Task<>() {

            @Override
            protected Void call() throws Exception {
                Thread.sleep(5000);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    splashStage.close();        // close the splash window
                    showMainStage(primaryStage); // open the main window
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void showMainStage(Stage primaryStage) {
        //call main menu here
        MainMenu.show(primaryStage);
    }

    // The very first thing that runs when you click Run.
    // launch(args) hands off to JavaFX, which then calls start() for you above
    public static void main(String[] args) {
        launch(args);
    }

}


