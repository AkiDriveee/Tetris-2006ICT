package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HighScore {

    public static void show(Stage stage) {

        BorderPane root = new BorderPane();

        // Title
        Label title = new Label("HIGH SCORES");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        VBox top = new VBox(title);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(20));
        root.setTop(top);

        // High score list
        ListView<String> scoreList = new ListView<>();

        scoreList.setStyle("-fx-font-family: Consolas; -fx-font-size: 18px;");

        scoreList.getItems().addAll(
                String.format("%-15s %5d", "Aksa", 9800),
                String.format("%-15s %5d", "Sukhdeep", 9200),
                String.format("%-15s %5d", "Taj", 8700),
                String.format("%-15s %5d", "Havana", 8300),
                String.format("%-15s %5d", "Emma", 7900),
                String.format("%-15s %5d", "Lima", 7500),
                String.format("%-15s %5d", "John", 7100),
                String.format("%-15s %5d", "Peria", 6800),
                String.format("%-15s %5d", "Olivia", 6400),
                String.format("%-15s %5d", "Max", 6000)
        );

        scoreList.setMaxWidth(350);
        scoreList.setMaxHeight(350);

        VBox center = new VBox(scoreList);
        center.setAlignment(Pos.CENTER);
        root.setCenter(center);

        // Back button
        Button backButton = new Button("Back");

        backButton.setOnAction(e -> {
            MainMenu.show(stage);
        });



        VBox bottom = new VBox(backButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(20));
        root.setBottom(bottom);

        Scene scene = new Scene(root, 1000, 700);

        stage.setTitle("High Scores");
        stage.setScene(scene);
        stage.show();
    }
}